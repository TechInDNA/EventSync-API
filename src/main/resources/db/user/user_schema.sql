-- 1. On crée d'abord la table "users"
CREATE TABLE IF NOT EXISTS eventsync_app.users (
    id              uuid            DEFAULT gen_random_uuid() PRIMARY KEY,
    first_name      varchar(50)     NOT NULL,
    last_name       varchar(50)     NOT NULL,
    email           varchar(50)     UNIQUE NOT NULL,
    password        varchar(100),
    bio             text,
    created_at      timestamp       DEFAULT now(),
    profile_picture varchar(255),
    "role"          varchar(50)     NOT NULL
);


CREATE INDEX idx_users_role ON eventsync_app.users("role");
CREATE INDEX idx_users_role_name ON eventsync_app.users("role", last_name, first_name);