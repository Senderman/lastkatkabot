CREATE TABLE CHAT_INFO (
    chat_id BIGINT PRIMARY KEY,
    last_pairs VARCHAR(5500),
    last_pair_date INT,
    forbidden_commands VARCHAR(3000),
    greeting_sticker_id VARCHAR(255)
);
