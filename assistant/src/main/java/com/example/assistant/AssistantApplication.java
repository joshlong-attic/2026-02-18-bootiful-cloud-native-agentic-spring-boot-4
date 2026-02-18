package com.example.assistant;

import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.security.client.sync.AuthenticationMcpTransportContextProvider;
import org.springaicommunity.mcp.security.client.sync.oauth2.http.client.OAuth2AuthorizationCodeSyncHttpRequestCustomizer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
public class AssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssistantApplication.class, args);
    }

    @Bean
    PromptChatMemoryAdvisor promptChatMemoryAdvisor(DataSource dataSource) {
        var jdbc = JdbcChatMemoryRepository
                .builder()
                .dataSource(dataSource)
                .build();
        var mwa = MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(jdbc)
                .build();
        return PromptChatMemoryAdvisor
                .builder(mwa)
                .build();
    }

    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore).build();
    }

    @Bean
    ApplicationRunner runner(JdbcClient db) {
        record Table(String tableName, String type, String totalSize, String description) {
        }
        return a -> db.sql("""
                        SELECT
                            c.relname AS table_name,
                            CASE c.relkind
                                WHEN 'r' THEN 'table'
                                WHEN 'v' THEN 'view'
                                WHEN 'm' THEN 'materialized view'
                                WHEN 'S' THEN 'sequence'
                            END AS type,
                            pg_size_pretty(pg_total_relation_size(c.oid)) AS total_size,
                            obj_description(c.oid) AS description
                        FROM pg_class c
                        JOIN pg_namespace n ON n.oid = c.relnamespace
                        WHERE n.nspname = current_schema()
                          AND c.relkind IN ('r', 'v', 'm', 'S')
                        ORDER BY c.relname;
                        """)
                .query((rs, rowNum) -> new Table(rs.getString("table_name"),
                        rs.getString("type"), rs.getString("total_size"), rs.getString("description")))
                .list()
                .forEach(System.out::println);
    }
}

@Configuration
class SecurityConfiguration {


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity security) {
        return security
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().permitAll())
                .oauth2Client(Customizer.withDefaults())
                .build();
    }

    @Bean
    OAuth2AuthorizationCodeSyncHttpRequestCustomizer auth2AuthorizationCodeSyncHttpRequestCustomizer(OAuth2AuthorizedClientManager authorizedClientManager) {
        return new OAuth2AuthorizationCodeSyncHttpRequestCustomizer(authorizedClientManager,
                "authserver");
    }

    @Bean
    McpSyncClientCustomizer mcpSyncClientCustomizer() {
        return (_, spec) -> spec
                .transportContextProvider(new AuthenticationMcpTransportContextProvider());
    }

}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

// look mom, no Lombok!!
record Dog(@Id int id, String name, String description) {
}

@Component
class Reset implements ApplicationRunner {

    private final JdbcClient db;

    Reset(JdbcClient db) {
        this.db = db;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (var sql : " event_publication; spring_ai_chat_memory ".split(";"))
            this.db
                    .sql(" delete from " + sql)
                    .update();
    }
}

@Controller
@ResponseBody
class AssistantController {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ChatClient ai;
    private final JdbcClient db;

    AssistantController(
            JdbcClient db,
            ToolCallbackProvider scheduler,
            DogRepository repository,
            VectorStore vectorStore, QuestionAnswerAdvisor qa,
            PromptChatMemoryAdvisor promptChatMemoryAdvisor,
            ChatClient.Builder ai) {
        this.db = db;

        var vectorCount = this.fetchAllVectors();
        this.log.info("vector count: {}", vectorCount);
        for (var vector : vectorCount) {
            this.log.info("vector row: {}", vector);
        }
        if (vectorCount.isEmpty()) {
            repository.findAll().forEach(dog -> {
                var dogument = new Document(
                        "id: %s, name: %s, description: %s".formatted(dog.id(), dog.name(), dog.description()));
                vectorStore.add(List.of(dogument));
            });
        }

        var system = """
                
                You are an AI powered assistant to help people adopt a dog from the adoptions agency named Pooch Palace with locations in Antwerp, Seoul, Tokyo, Singapore, Paris, Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs availables will be presented below. If there is no information, then return a polite response suggesting wes don't have any dogs available.
                
                If somebody asks for a time to pick up the dog, don't ask other questions: simply provide a time by consulting the tools you have available.
                
                """;
        this.ai = ai
                .defaultToolCallbacks(scheduler)
                .defaultAdvisors(qa, promptChatMemoryAdvisor)
                .defaultSystem(system)
                .build();
    }

    @GetMapping("/vectors")
    List<Map<String, Object>> fetchAllVectors() {
        return db
                .sql("select * from vector_store")
                .query((RowMapper<Map<String, Object>>) (rs, _) ->
                        Map.of("id", rs.getString("id"), "content", rs.getString("content"),
                                "embedding", ((PGobject) rs.getObject("embedding")),
                                "metadata", rs.getString("metadata")))
                .list();
    }


    @GetMapping("/ask")
    String ask(@RequestParam String question) {
        var user = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();
        return this.ai
                .prompt()
                .user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, user))
                .call()
                .content();
    }
}

record DogAdoptionSuggestion(int dogId, String name) {
}