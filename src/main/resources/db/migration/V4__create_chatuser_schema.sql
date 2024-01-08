CREATE TABLE CHAT_USER (
    user_id BIGINT,
    chat_id BIGINT,
    name VARCHAR(100) NOT NULL,
    last_message_date INT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, chat_id)
);