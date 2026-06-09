
CREATE SCHEMA IF NOT EXISTS eventsync_app;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE eventsync_app.user_role AS ENUM ('ADMIN', 'SPEAKER', 'PARTICIPANT');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS eventsync_app."user" (
    id              UUID                             NOT NULL    PRIMARY KEY,
    first_name      VARCHAR(50)                      NOT NULL,
    last_name       VARCHAR(50)                      NOT NULL,
    email           VARCHAR(50)                      NOT NULL    UNIQUE,
    password        VARCHAR(100),
    bio             TEXT,
    profile_picture VARCHAR(255),
    role            eventsync_app.user_role,
    created_at      TIMESTAMP                        NOT NULL    DEFAULT NOW()
);

COMMENT ON TABLE     eventsync_app."user"                IS 'Utilisateurs de la plateforme';
COMMENT ON COLUMN    eventsync_app."user".id             IS 'Identifiant unique UUID';
COMMENT ON COLUMN    eventsync_app."user".first_name     IS 'Prénom';
COMMENT ON COLUMN    eventsync_app."user".last_name      IS 'Nom de famille';
COMMENT ON COLUMN    eventsync_app."user".email          IS 'Adresse email (unique)';
COMMENT ON COLUMN    eventsync_app."user".password       IS 'Mot de passe hashé';
COMMENT ON COLUMN    eventsync_app."user".bio            IS 'Biographie / présentation';
COMMENT ON COLUMN    eventsync_app."user".profile_picture IS 'URL ou chemin de la photo de profil';
COMMENT ON COLUMN    eventsync_app."user".role           IS 'Rôle: ADMIN, SPEAKER ou PARTICIPANT';
COMMENT ON COLUMN    eventsync_app."user".created_at     IS 'Date de création du compte';
