-- =====================================================
-- KAROOTH TASK MANAGEMENT SYSTEM - DATA INITIALIZATION
-- =====================================================
-- Sample users for testing authentication and authorization

-- Insert sample users (password: password123)
INSERT INTO users (name, email, password, role) VALUES
('Test Manager', 'test@karooth.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MANAGER'),
('Test User', 'user@karooth.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'USER'),
('Admin User', 'admin@karooth.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN');

-- Sample projects for testing task creation functionality
INSERT INTO projects (name, description, priority, due_date, status) VALUES
('Website Redesign', 'Complete redesign of the company website with modern UI/UX', 'HIGH', DATEADD('MONTH', 2, CURRENT_DATE()), 'IN_PROGRESS'),
('Mobile App Development', 'Develop a new mobile application for iOS and Android', 'CRITICAL', DATEADD('MONTH', 3, CURRENT_DATE()), 'IN_PROGRESS'),
('Database Migration', 'Migrate legacy database to new cloud infrastructure', 'MEDIUM', DATEADD('MONTH', 1, CURRENT_DATE()), 'TODO'),
('Security Audit', 'Conduct comprehensive security audit of all systems', 'HIGH', DATEADD('MONTH', 1, CURRENT_DATE()), 'TODO'),
('Marketing Campaign', 'Launch new marketing campaign for Q4 product release', 'MEDIUM', DATEADD('MONTH', 2, CURRENT_DATE()), 'IN_PROGRESS');
