CREATE TABLE MARRIAGE_REQUEST (
    id IDENTITY PRIMARY KEY,
    proposer_id BIGINT NOT NULL,
    proposer_name VARCHAR(100) NOT NULL,
    proposee_id BIGINT NOT NULL,
    proposee_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE USER_STATS (
    user_id BIGINT PRIMARY KEY,
    duels_total INT DEFAULT 0,
    duel_wins INT DEFAULT 0,
    bnc_score INT DEFAULT 0,
    locale varchar(5) default 'ru',
    location varchar(100),
    lover_id BIGINT
);