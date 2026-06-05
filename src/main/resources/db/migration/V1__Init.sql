CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(32) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR NOT NULL,
    registration_date DATE NOT NULL,
    profile_picture_url TEXT,
    bio TEXT,
    admin BOOL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS mixes (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    slug VARCHAR NOT NULL UNIQUE,
    title VARCHAR NOT NULL,
    description TEXT,
    genre VARCHAR(50),
    duration_seconds INTEGER,
    audio_url VARCHAR,
    cover_url VARCHAR,
    uploaded_at TIMESTAMP,
    total_plays BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS mix_tracks (
    id BIGSERIAL PRIMARY KEY,
    mix_id BIGINT NOT NULL REFERENCES mixes(id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    start_time_seconds INTEGER NOT NULL,
    artist VARCHAR NOT NULL,
    title VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    mix_id BIGINT NOT NULL REFERENCES mixes(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP,
    content TEXT NOT NULL,
    track_time_seconds INTEGER
);

CREATE TABLE IF NOT EXISTS likes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mix_id BIGINT NOT NULL REFERENCES mixes(id) ON DELETE CASCADE,
    UNIQUE(user_id, mix_id)
);

CREATE TABLE IF NOT EXISTS follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    followed_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(follower_id, followed_id)
);

CREATE TABLE IF NOT EXISTS playlists (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    slug VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS playlist_mix (
    playlist_id BIGINT NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    mix_id BIGINT NOT NULL REFERENCES mixes(id) ON DELETE CASCADE,
    PRIMARY KEY(playlist_id, mix_id)
);
