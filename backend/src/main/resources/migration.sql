-- =====================================================
-- KAROOTH TASK MANAGEMENT SYSTEM - DATABASE MIGRATION
-- =====================================================
-- This script adds the profile_photo_url column to existing users table
-- Run this script if you have an existing database without the profile_photo_url column

-- Add profile_photo_url column to users table if it doesn't exist
-- This is safe to run multiple times as it checks if the column exists first

-- For H2 Database
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_photo_url VARCHAR(500);

-- For MySQL (uncomment if using MySQL)
-- ALTER TABLE users ADD COLUMN profile_photo_url VARCHAR(500);

-- For PostgreSQL (uncomment if using PostgreSQL)
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_photo_url VARCHAR(500);

-- For SQL Server (uncomment if using SQL Server)
-- IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'profile_photo_url')
-- BEGIN
--     ALTER TABLE users ADD profile_photo_url VARCHAR(500);
-- END
