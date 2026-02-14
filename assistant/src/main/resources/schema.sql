CREATE TABLE users
(
    username text primary key NOT NULL,
    password text             NOT NULL,
    enabled  boolean          NOT NULL
);
CREATE TABLE authorities
(
    username  text NOT NULL,
    authority text NOT NULL,
    FOREIGN KEY (username) REFERENCES users (username)
);
CREATE TABLE dog
(
    id          serial primary key               NOT NULL,
    name        text                             NOT NULL,
    description text                             NOT NULL,
    dob         date                             NOT NULL,
    owner       text,
    gender      character(1) DEFAULT 'f'::bpchar NOT NULL,
    image       text                             NOT NULL
);
CREATE TABLE spring_ai_chat_memory
(
    conversation_id character varying(36)       NOT NULL,
    content         text                        NOT NULL,
    type            character varying(10)       NOT NULL,
    "timestamp"     timestamp without time zone NOT NULL,
    CONSTRAINT spring_ai_chat_memory_type_check CHECK (((type)::text = ANY
                                                        ((ARRAY ['USER'::character varying, 'ASSISTANT'::character varying, 'SYSTEM'::character varying, 'TOOL'::character varying])::text[])))
);

CREATE TABLE vector_store
(
    id        uuid DEFAULT uuid_generate_v4() primary key NOT NULL,
    content   text,
    metadata  json,
    embedding vector(1536)
);
CREATE UNIQUE INDEX ix_auth_username ON authorities USING btree (username, authority);
CREATE INDEX spring_ai_chat_memory_conversation_id_timestamp_idx ON spring_ai_chat_memory USING btree (conversation_id, "timestamp");
CREATE INDEX spring_ai_vector_index ON vector_store USING hnsw (embedding vector_cosine_ops);
