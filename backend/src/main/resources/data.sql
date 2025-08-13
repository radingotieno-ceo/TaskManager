-- Sample data initialization for Task Management System
-- This script runs automatically when Spring Boot starts up

-- Debug: Check if script is running
INSERT INTO users (name, email, password, role, created_at) VALUES
('Debug Test', 'debug@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'USER', NOW());

-- Insert sample users with BCrypt encoded passwords
-- Password for all users is 'password123' (encoded with BCrypt)
-- Updated with correct hash: $2a$10$0ZytMgDUKb1GL00g8YEe/OW4FRhwQugv9PmxP0KZJYFFwdZR9FLu6
INSERT INTO users (name, email, password, role, created_at) VALUES
('John Admin', 'admin@karooth.com', '$2a$10$0ZytMgDUKb1GL00g8YEe/OW4FRhwQugv9PmxP0KZJYFFwdZR9FLu6', 'ADMIN', NOW()),
('Sarah Manager', 'manager@karooth.com', '$2a$10$0ZytMgDUKb1GL00g8YEe/OW4FRhwQugv9PmxP0KZJYFFwdZR9FLu6', 'MANAGER', NOW()),
('Test User', 'test@karooth.com', '$2a$10$0ZytMgDUKb1GL00g8YEe/OW4FRhwQugv9PmxP0KZJYFFwdZR9FLu6', 'USER', NOW()),
('Mike Developer', 'mike@karooth.com', '$2a$10$0ZytMgDUKb1GL00g8YEe/OW4FRhwQugv9PmxP0KZJYFFwdZR9FLu6', 'USER', NOW()),
('Lisa Designer', 'lisa@karooth.com', '$2a$10$0ZytMgDUKb1GL00g8YEe/OW4FRhwQugv9PmxP0KZJYFFwdZR9FLu6', 'USER', NOW()),
('David Tester', 'david@karooth.com', '$2a$10$0ZytMgDUKb1GL00g8YEe/OW4FRhwQugv9PmxP0KZJYFFwdZR9FLu6', 'USER', NOW()),
('Emma Analyst', 'emma@karooth.com', '$2a$10$0ZytMgDUKb1GL00g8YEe/OW4FRhwQugv9PmxP0KZJYFFwdZR9FLu6', 'USER', NOW());

-- Insert sample projects
INSERT INTO projects (name, description, created_at) VALUES
('E-Commerce Platform', 'Modern e-commerce solution with payment integration', NOW()),
('Mobile App Development', 'Cross-platform mobile application for task management', NOW()),
('Website Redesign', 'Complete redesign of corporate website', NOW()),
('API Development', 'RESTful API for third-party integrations', NOW()),
('Database Migration', 'Legacy system migration to cloud infrastructure', NOW());

-- No sample tasks - we'll create them through the application to test task assignment functionality
