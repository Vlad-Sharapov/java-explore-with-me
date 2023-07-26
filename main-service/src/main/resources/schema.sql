CREATE TABLE IF NOT EXISTS compilation
(
    id     INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    pinned BOOLEAN NOT NULL,
    title  VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS location
(
    id  BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    lat REAL NOT NULL,
    lon REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS category
(
    id   INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email VARCHAR(254) UNIQUE NOT NULL,
    name  VARCHAR(250) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    initiator_id          BIGINT                      NOT NULL,
    annotation         VARCHAR(2000)               NOT NULL,
    title              VARCHAR(120)                NOT NULL,
    category_id           INT                         NOT NULL,
    published_on         TIMESTAMP WITHOUT TIME ZONE,
    create_on         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    description        VARCHAR(7000)                     NOT NULL,
    event_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    location_id           BIGINT                      NOT NULL,
    paid               BOOLEAN                     NOT NULL,
    participant_limit  INT                         NOT NULL,
    request_moderation BOOLEAN                     NOT NULL DEFAULT true,
    state             VARCHAR(20)                 NOT NULL,
    FOREIGN KEY (initiator_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category (id),
    FOREIGN KEY (location_id) REFERENCES location (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS compilation_events
(
    compilation_id BIGINT NOT NULL,
    event_id       BIGINT NOT NULL,
    FOREIGN KEY (compilation_id) REFERENCES compilation (id),
    FOREIGN KEY (event_id) REFERENCES events (id)
);

CREATE TABLE IF NOT EXISTS requests
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    created   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    event     BIGINT                      NOT NULL,
    requester BIGINT                      NOT NULL,
    status    VARCHAR(10)                     NOT NULL,
    FOREIGN KEY (event) REFERENCES events (id),
    FOREIGN KEY (requester) REFERENCES users (id)
);

