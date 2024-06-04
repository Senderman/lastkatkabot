CREATE TABLE admin_user (
    user_id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE blacklisted_user (
    user_id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE blacklisted_chat (
    chat_id BIGINT PRIMARY KEY
);

CREATE TABLE settings (
    id VARCHAR(255) PRIMARY KEY,
    data VARCHAR(255) NOT NULL
);

CREATE TABLE bnc_game_message (
    game_id BIGINT,
    message_id INT,
    PRIMARY KEY (game_id, message_id)
);

CREATE TABLE bnc_game_save (
    id BIGINT PRIMARY KEY,
    game VARCHAR(5000) NOT NULL,
    edit_date TIMESTAMP NOT NULL
);

CREATE TABLE cake (
    id INT PRIMARY KEY,
    filling VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE feedback (
    id SERIAL PRIMARY KEY,
    message VARCHAR(1000) NOT NULL,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    chat_id BIGINT NOT NULL,
    chat_title VARCHAR(500),
    message_id INT NOT NULL,
    replied BOOLEAN NOT NULL DEFAULT FALSE,
    user_locale VARCHAR(5) NOT NULL
);

CREATE TABLE marriage_request (
    id INT PRIMARY KEY,
    proposer_id BIGINT NOT NULL,
    proposer_name VARCHAR(255) NOT NULL,
    proposee_id BIGINT NOT NULL,
    proposee_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE user_stats (
    user_id BIGINT PRIMARY KEY,
    duels_total INT DEFAULT 0 NOT NULL,
    duel_wins INT DEFAULT 0 NOT NULL,
    bnc_score INT DEFAULT 0 NOT NULL,
    locale VARCHAR(5),
    location varchar(255),
    lover_id BIGINT,
    name VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE genshin_chat_user (
    chat_id BIGINT,
    user_id BIGINT,
    last_roll_date INT NOT NULL DEFAULT 0,
    four_pity INT NOT NULL DEFAULT 0,
    five_pity INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (chat_id, user_id)
);

CREATE TABLE genshin_user_inventory_item (
     chat_id BIGINT,
     user_id BIGINT,
     item_id VARCHAR(50),
     amount INT NOT NULL DEFAULT 0,
     PRIMARY KEY (chat_id, user_id, item_id)
);

CREATE TABLE chat_user (
    user_id BIGINT,
    chat_id BIGINT,
    last_message_date INT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, chat_id)
);

CREATE TABLE chat_info (
    chat_id BIGINT PRIMARY KEY,
    last_pairs VARCHAR(5500),
    last_pair_date INT,
    forbidden_commands VARCHAR(3000),
    greeting_sticker_id VARCHAR(255)
);

CREATE TABLE bnc_record (
    `length` INT CHECK (`LENGTH` BETWEEN 4 AND 16),
    hexadecimal BOOLEAN,
    user_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    time_spent BIGINT NOT NULL,

    PRIMARY KEY (`LENGTH`, HEXADECIMAL)
);
