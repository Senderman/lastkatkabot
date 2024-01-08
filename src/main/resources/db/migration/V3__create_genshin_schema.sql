CREATE TABLE GENSHIN_CHAT_USER (
    chat_id BIGINT,
    user_id BIGINT,
    last_roll_date INT NOT NULL DEFAULT 0,
    four_pity INT NOT NULL DEFAULT 0,
    five_pity INT NOT NULL DEFAULT 0,
    PRIMARY KEY (chat_id, user_id)
);

CREATE TABLE GENSHIN_USER_INVENTORY_ITEM (
     chat_id BIGINT,
     user_id BIGINT,
     item_id VARCHAR(50) NOT NULL,
     amount INT NOT NULL DEFAULT 0,
     PRIMARY KEY (chat_id, user_id, item_id)
);
