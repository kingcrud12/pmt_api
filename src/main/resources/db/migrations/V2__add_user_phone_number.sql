-- Add phone_number column to user table

ALTER TABLE user
    ADD COLUMN phone_number VARCHAR(20);
