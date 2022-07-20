CREATE TABLE [IF NOT EXISTS] user (
    id serial PRIMARY KEY,
    username VARCHAR (150) UNIQUE NOT NULL,
    weather_action_id INT,
    lat DECIMAL,
    lng DECIMAL,
    notify_at TIME,
    last_notified TIMESTAMP,
    created_on TIMESTAMP NOT NULL
);