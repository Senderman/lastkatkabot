ALTER TABLE USER_STATS ALTER COLUMN duels_total INT DEFAULT 0 NOT NULL;
ALTER TABLE USER_STATS ALTER COLUMN duel_wins INT DEFAULT 0 NOT NULL;
ALTER TABLE USER_STATS ALTER COLUMN bnc_score INT DEFAULT 0 NOT NULL;
ALTER TABLE USER_STATS ALTER COLUMN locale VARCHAR(5) DEFAULT 'ru' NOT NULL;
ALTER TABLE USER_STATS ALTER COLUMN name VARCHAR(255) NOT NULL;

ALTER TABLE BNC_RECORD ALTER COLUMN time_spent BIGINT NOT NULL;
