-- spring ai specific tables
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY
(
    conversation_id VARCHAR(36) NOT NULL,
    content         TEXT        NOT NULL,
    type            VARCHAR(10) NOT NULL CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')),
    "timestamp"     TIMESTAMP   NOT NULL
);


CREATE TABLE IF NOT EXISTS vector_store
(
    id        uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content   text,
    metadata  json,
    embedding vector(1536)
);

-- spring security specific tables
CREATE TABLE if not exists users
(
    username text primary key NOT NULL,
    password text             NOT NULL,
    enabled  boolean          NOT NULL
);

CREATE TABLE if not exists authorities
(
    username  text NOT NULL,
    authority text NOT NULL,
    FOREIGN KEY (username) REFERENCES users (username),
    UNIQUE (username, authority)
);

-- spring modulith specific tables
CREATE TABLE IF NOT EXISTS event_publication
(
    id                     UUID NOT NULL,
    listener_id            TEXT NOT NULL,
    event_type             TEXT NOT NULL,
    serialized_event       TEXT NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP WITH TIME ZONE,
    status                 TEXT,
    completion_attempts    INT,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);
--

CREATE TABLE if not exists dog
(
    id          serial primary key NOT NULL,
    name        text               NOT NULL,
    description text               NOT NULL,
    owner       text
);