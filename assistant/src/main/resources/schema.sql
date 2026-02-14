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

drop table if exists dog;
CREATE TABLE if not exists dog
(
    id          serial primary key NOT NULL,
    name        text               NOT NULL,
    description text               NOT NULL,
    owner       text
);