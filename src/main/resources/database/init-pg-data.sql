CREATE TABLE IF NOT EXISTS public.user (
    id serial PRIMARY KEY,
    userName VARCHAR (150) UNIQUE NOT NULL,
    weather_action_id INT,
    lat DECIMAL,
    lng DECIMAL,
    notify_at_hour INT,
    last_notified TIMESTAMP,
    created_on TIMESTAMP NOT NULL
);