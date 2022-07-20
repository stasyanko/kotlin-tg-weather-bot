CREATE TABLE [IF NOT EXISTS] user (
    id serial PRIMARY KEY,
    userId VARCHAR (150) UNIQUE NOT NULL,
    weather_action_id INT,
    lat DECIMAL,
    lng DECIMAL,
    notify_at_hour INT,
    last_notified TIMESTAMP,
    created_on TIMESTAMP NOT NULL
);