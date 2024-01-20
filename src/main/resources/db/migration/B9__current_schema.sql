CREATE TABLE ADMIN_USER (
    user_id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE BLACKLISTED_USER (
    user_id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE BLACKLISTED_CHAT (
    chat_id BIGINT PRIMARY KEY
);

CREATE TABLE SETTINGS (
    id VARCHAR(255) PRIMARY KEY,
    data VARCHAR(255) NOT NULL
);

CREATE TABLE BNC_GAME_MESSAGE (
    game_id BIGINT,
    message_id INT,
    PRIMARY KEY (game_id, message_id)
);

CREATE TABLE BNC_GAME_SAVE (
    id BIGINT PRIMARY KEY,
    game VARCHAR(5000) NOT NULL,
    edit_date TIMESTAMP NOT NULL
);

CREATE TABLE CAKE (
    id IDENTITY PRIMARY KEY,
    filling VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE FEEDBACK (
    id IDENTITY PRIMARY KEY,
    message VARCHAR(1000) NOT NULL,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    chat_id BIGINT NOT NULL,
    chat_title VARCHAR(500),
    message_id INT NOT NULL,
    replied BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE MARRIAGE_REQUEST (
    id IDENTITY PRIMARY KEY,
    proposer_id BIGINT NOT NULL,
    proposer_name VARCHAR(255) NOT NULL,
    proposee_id BIGINT NOT NULL,
    proposee_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE USER_STATS (
    user_id BIGINT PRIMARY KEY,
    duels_total INT DEFAULT 0,
    duel_wins INT DEFAULT 0,
    bnc_score INT DEFAULT 0,
    locale varchar(5) default 'ru',
    location varchar(255),
    lover_id BIGINT,
    name VARCHAR(130),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE GENSHIN_CHAT_USER (
    chat_id BIGINT,
    user_id BIGINT,
    last_roll_date INT NOT NULL DEFAULT 0,
    four_pity INT NOT NULL DEFAULT 0,
    five_pity INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (chat_id, user_id)
);

CREATE TABLE GENSHIN_USER_INVENTORY_ITEM (
     chat_id BIGINT,
     user_id BIGINT,
     item_id VARCHAR(50),
     amount INT NOT NULL DEFAULT 0,
     PRIMARY KEY (chat_id, user_id, item_id)
);

CREATE TABLE CHAT_USER (
    user_id BIGINT,
    chat_id BIGINT,
    last_message_date INT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, chat_id)
);

CREATE TABLE CHAT_INFO (
    chat_id BIGINT PRIMARY KEY,
    last_pairs VARCHAR(5500),
    last_pair_date INT,
    forbidden_commands VARCHAR(3000),
    greeting_sticker_id VARCHAR(255)
);

CREATE TABLE BNC_RECORD(
    `LENGTH` INT CHECK (`LENGTH` BETWEEN 4 AND 16),
    HEXADECIMAL BOOLEAN,
    USER_ID BIGINT NOT NULL,
    NAME VARCHAR(64) NOT NULL,
    TIME_SPENT BIGINT,

    PRIMARY KEY (`LENGTH`, HEXADECIMAL)
);
