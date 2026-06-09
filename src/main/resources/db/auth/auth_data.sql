-- Seed: admin user (password: admin123)
INSERT INTO eventsync_app."user" (id, first_name, last_name, email, password, role, created_at)
VALUES (
           '3f553f56-792c-4c80-9ea9-b259ef1247a9',
           'Admin',
           'User',
           'admin@eventsync.com',
           '$argon2id$v=19$m=16384,t=2,p=1$dizuhrMYICNif5ZLchCBrw$FfApSuvcBfHNWUbyVG/HXItFbpVS0EpErUxQdNIrldg',
           'ADMIN',
           NOW()
       ) ON CONFLICT (email) DO NOTHING;