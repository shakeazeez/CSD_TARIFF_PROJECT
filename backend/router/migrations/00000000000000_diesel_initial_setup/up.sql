-- This file was automatically created by Diesel to setup helper functions
-- and other internal bookkeeping. This file is safe to edit, any future
-- changes will be added to existing projects as new migrations.

CREATE TYPE industry_enum AS ENUM (
    'AGRICULTURE',
    'MANUFACTURING',
    'RETAIL',
    'HEALTHCARE',
    'TECHNOLOGY',
    'FINANCE',
    'EDUCATION',
    'CONSTRUCTION',
    'TRANSPORTATION',
    'ENERGY',
    'REAL_ESTATE',
    'HOSPITALITY',
    'ENTERTAINMENT',
    'FOOD_AND_BEVERAGE',
    'AUTOMOTIVE',
    'PHARMACEUTICALS',
    'TELECOMMUNICATIONS',
    'AEROSPACE',
    'CHEMICALS',
    'MINING',
    'OTHER'
);

CREATE TYPE role_enum AS ENUM ('ADMIN', 'BANK', 'BUSINESS', 'MEMBER');

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    user_type VARCHAR(50) NOT NULL CHECK (user_type IN ('bank_user', 'business')),
    industry industry_enum,
    origin_country VARCHAR(100)
);

CREATE TABLE user_history (
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    history_key INT NOT NULL,
    history_value INT,
    PRIMARY KEY (user_id, history_key)
);

CREATE TABLE user_role (
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_role role_enum NOT NULL,
    PRIMARY KEY (user_id, user_role)
);

CREATE TABLE items_sold (
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_sold VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, item_sold)
);

CREATE TABLE destination_country (
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    destination_country VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, destination_country)
);

-- Sets up a trigger for the given table to automatically set a column called
-- `updated_at` whenever the row is modified (unless `updated_at` was included
-- in the modified columns)
--
-- # Example
--
-- ```sql
-- CREATE TABLE users (id SERIAL PRIMARY KEY, updated_at TIMESTAMP NOT NULL DEFAULT NOW());
--
-- SELECT diesel_manage_updated_at('users');
-- ```
CREATE OR REPLACE FUNCTION diesel_manage_updated_at(_tbl regclass) RETURNS VOID AS $$
BEGIN
    EXECUTE format('CREATE TRIGGER set_updated_at BEFORE UPDATE ON %s
                    FOR EACH ROW EXECUTE PROCEDURE diesel_set_updated_at()', _tbl);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION diesel_set_updated_at() RETURNS trigger AS $$
BEGIN
    IF (
        NEW IS DISTINCT FROM OLD AND
        NEW.updated_at IS NOT DISTINCT FROM OLD.updated_at
    ) THEN
        NEW.updated_at := current_timestamp;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
