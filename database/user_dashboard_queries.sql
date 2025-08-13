-- =====================================================
-- KAROOTH TASK MANAGER - USER DASHBOARD QUERIES
-- Real-time data fetching for user dashboards
-- =====================================================

-- =====================================================
-- 1. USER PERSONAL STATISTICS
-- =====================================================

-- Get user's task statistics
SELECT 
    COUNT(*) as total_tasks,
    COUNT(CASE WHEN status = 'DONE' THEN 1 END) as completed_tasks,
    COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) as in_progress_tasks,
    COUNT(CASE WHEN status = 'TODO' THEN 1 END) as todo_tasks,
    COUNT(CASE WHEN DATE(due_date) = CURDATE() THEN 1 END) as due_today
FROM tasks 
WHERE assigned_user_id = ?;

-- =====================================================
-- 2. USER'S ASSIGNED TASKS WITH DETAILS
-- =====================================================

-- Get all tasks assigned to user with project and progress info
SELECT 
    t.id,
    t.title,
    t.description,
    t.status,
    t.due_date,
    t.created_at,
    t.updated_at,
    p.id as project_id,
    p.name as project_name,
    p.description as project_description,
    -- Calculate task progress (you can modify this based on your progress tracking)
    CASE 
        WHEN t.status = 'TODO' THEN 0
        WHEN t.status = 'IN_PROGRESS' THEN 50
        WHEN t.status = 'DONE' THEN 100
        ELSE 0
    END as progress_percentage,
    -- Priority calculation (you can add a priority field to tasks table)
    CASE 
        WHEN t.due_date <= DATE_ADD(CURDATE(), INTERVAL 1 DAY) THEN 'HIGH'
        WHEN t.due_date <= DATE_ADD(CURDATE(), INTERVAL 3 DAY) THEN 'MEDIUM'
        ELSE 'LOW'
    END as priority
FROM tasks t
JOIN projects p ON t.project_id = p.id
WHERE t.assigned_user_id = ?
ORDER BY 
    CASE 
        WHEN t.due_date <= CURDATE() THEN 1
        WHEN t.due_date <= DATE_ADD(CURDATE(), INTERVAL 1 DAY) THEN 2
        ELSE 3
    END,
    t.due_date ASC;

-- =====================================================
-- 3. USER'S PROJECT INVOLVEMENT
-- =====================================================

-- Get projects where user has tasks assigned
SELECT 
    p.id,
    p.name,
    p.description,
    p.created_at,
    -- Calculate project progress based on user's tasks
    ROUND(
        (COUNT(CASE WHEN t.status = 'DONE' THEN 1 END) * 100.0) / 
        COUNT(*), 2
    ) as user_progress_percentage,
    -- Count user's tasks in this project
    COUNT(*) as user_tasks_count,
    COUNT(CASE WHEN t.status = 'DONE' THEN 1 END) as completed_tasks,
    COUNT(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 END) as in_progress_tasks,
    COUNT(CASE WHEN t.status = 'TODO' THEN 1 END) as todo_tasks
FROM projects p
JOIN tasks t ON p.id = t.project_id
WHERE t.assigned_user_id = ?
GROUP BY p.id, p.name, p.description, p.created_at
ORDER BY p.created_at DESC;

-- =====================================================
-- 4. USER ROLE IN PROJECTS (if you have user_project_roles table)
-- =====================================================

-- Create user_project_roles table if it doesn't exist
CREATE TABLE IF NOT EXISTS user_project_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'DEVELOPER',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_project (user_id, project_id)
);

-- Get user's role in each project
SELECT 
    p.id as project_id,
    p.name as project_name,
    COALESCE(upr.role, 'DEVELOPER') as user_role
FROM projects p
JOIN tasks t ON p.id = t.project_id
LEFT JOIN user_project_roles upr ON p.id = upr.project_id AND upr.user_id = ?
WHERE t.assigned_user_id = ?
GROUP BY p.id, p.name, upr.role;

-- =====================================================
-- 5. RECENT ACTIVITY FOR USER
-- =====================================================

-- Get recent task updates for the user
SELECT 
    t.id,
    t.title,
    t.status,
    t.updated_at,
    p.name as project_name,
    'TASK_UPDATE' as activity_type
FROM tasks t
JOIN projects p ON t.project_id = p.id
WHERE t.assigned_user_id = ?
ORDER BY t.updated_at DESC
LIMIT 10;

-- =====================================================
-- 6. UPCOMING DEADLINES
-- =====================================================

-- Get tasks due in the next 7 days
SELECT 
    t.id,
    t.title,
    t.due_date,
    p.name as project_name,
    DATEDIFF(t.due_date, CURDATE()) as days_until_due
FROM tasks t
JOIN projects p ON t.project_id = p.id
WHERE t.assigned_user_id = ?
    AND t.status != 'DONE'
    AND t.due_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
ORDER BY t.due_date ASC;

-- =====================================================
-- 7. TASK PROGRESS TRACKING (if you have subtasks)
-- =====================================================

-- Create subtasks table if you want to track detailed progress
CREATE TABLE IF NOT EXISTS subtasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('TODO', 'IN_PROGRESS', 'DONE') DEFAULT 'TODO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Calculate detailed task progress based on subtasks
SELECT 
    t.id,
    t.title,
    COUNT(st.id) as total_subtasks,
    COUNT(CASE WHEN st.status = 'DONE' THEN 1 END) as completed_subtasks,
    ROUND(
        (COUNT(CASE WHEN st.status = 'DONE' THEN 1 END) * 100.0) / 
        COUNT(st.id), 2
    ) as detailed_progress
FROM tasks t
LEFT JOIN subtasks st ON t.id = st.task_id
WHERE t.assigned_user_id = ?
GROUP BY t.id, t.title;

-- =====================================================
-- 8. PERFORMANCE METRICS
-- =====================================================

-- Get user's performance metrics
SELECT 
    -- Tasks completed this week
    COUNT(CASE 
        WHEN t.status = 'DONE' 
        AND t.updated_at >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)
        THEN 1 
    END) as completed_this_week,
    
    -- Tasks completed this month
    COUNT(CASE 
        WHEN t.status = 'DONE' 
        AND t.updated_at >= DATE_FORMAT(CURDATE(), '%Y-%m-01')
        THEN 1 
    END) as completed_this_month,
    
    -- Average completion time (in days)
    AVG(DATEDIFF(
        CASE WHEN t.status = 'DONE' THEN t.updated_at ELSE NULL END,
        t.created_at
    )) as avg_completion_days,
    
    -- On-time completion rate
    ROUND(
        (COUNT(CASE 
            WHEN t.status = 'DONE' 
            AND t.updated_at <= t.due_date 
            THEN 1 
        END) * 100.0) / 
        COUNT(CASE WHEN t.status = 'DONE' THEN 1 END), 2
    ) as on_time_rate
FROM tasks t
WHERE t.assigned_user_id = ?;

-- =====================================================
-- 9. NOTIFICATIONS AND ALERTS
-- =====================================================

-- Get overdue tasks
SELECT 
    t.id,
    t.title,
    t.due_date,
    p.name as project_name,
    DATEDIFF(CURDATE(), t.due_date) as days_overdue
FROM tasks t
JOIN projects p ON t.project_id = p.id
WHERE t.assigned_user_id = ?
    AND t.status != 'DONE'
    AND t.due_date < CURDATE()
ORDER BY t.due_date ASC;

-- Get tasks due today
SELECT 
    t.id,
    t.title,
    p.name as project_name
FROM tasks t
JOIN projects p ON t.project_id = p.id
WHERE t.assigned_user_id = ?
    AND t.status != 'DONE'
    AND DATE(t.due_date) = CURDATE();

-- =====================================================
-- 10. DASHBOARD SUMMARY VIEW
-- =====================================================

-- Create a comprehensive dashboard view
CREATE OR REPLACE VIEW user_dashboard_summary AS
SELECT 
    u.id as user_id,
    u.name as user_name,
    u.email as user_email,
    
    -- Task statistics
    COUNT(t.id) as total_tasks,
    COUNT(CASE WHEN t.status = 'DONE' THEN 1 END) as completed_tasks,
    COUNT(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 END) as in_progress_tasks,
    COUNT(CASE WHEN t.status = 'TODO' THEN 1 END) as todo_tasks,
    COUNT(CASE WHEN DATE(t.due_date) = CURDATE() THEN 1 END) as due_today,
    COUNT(CASE WHEN t.due_date < CURDATE() AND t.status != 'DONE' THEN 1 END) as overdue_tasks,
    
    -- Project involvement
    COUNT(DISTINCT t.project_id) as projects_involved,
    
    -- Performance metrics
    ROUND(
        (COUNT(CASE WHEN t.status = 'DONE' THEN 1 END) * 100.0) / 
        COUNT(t.id), 2
    ) as completion_rate,
    
    -- Recent activity
    MAX(t.updated_at) as last_activity
    
FROM users u
LEFT JOIN tasks t ON u.id = t.assigned_user_id
WHERE u.id = ?
GROUP BY u.id, u.name, u.email;

-- Usage: SELECT * FROM user_dashboard_summary WHERE user_id = ?;

-- =====================================================
-- 11. STORED PROCEDURES FOR COMMON OPERATIONS
-- =====================================================

-- Procedure to get user dashboard data
DELIMITER //
CREATE PROCEDURE GetUserDashboardData(IN user_id_param BIGINT)
BEGIN
    -- Get user info
    SELECT id, name, email, role FROM users WHERE id = user_id_param;
    
    -- Get task statistics
    SELECT 
        COUNT(*) as total_tasks,
        COUNT(CASE WHEN status = 'DONE' THEN 1 END) as completed_tasks,
        COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) as in_progress_tasks,
        COUNT(CASE WHEN status = 'TODO' THEN 1 END) as todo_tasks,
        COUNT(CASE WHEN DATE(due_date) = CURDATE() THEN 1 END) as due_today
    FROM tasks 
    WHERE assigned_user_id = user_id_param;
    
    -- Get user's tasks
    SELECT 
        t.*,
        p.name as project_name,
        CASE 
            WHEN t.status = 'TODO' THEN 0
            WHEN t.status = 'IN_PROGRESS' THEN 50
            WHEN t.status = 'DONE' THEN 100
            ELSE 0
        END as progress_percentage
    FROM tasks t
    JOIN projects p ON t.project_id = p.id
    WHERE t.assigned_user_id = user_id_param
    ORDER BY t.due_date ASC;
    
    -- Get user's projects
    SELECT DISTINCT
        p.*,
        COUNT(t.id) as user_tasks_in_project,
        COUNT(CASE WHEN t.status = 'DONE' THEN 1 END) as completed_tasks_in_project
    FROM projects p
    JOIN tasks t ON p.id = t.project_id
    WHERE t.assigned_user_id = user_id_param
    GROUP BY p.id;
END //
DELIMITER ;

-- Usage: CALL GetUserDashboardData(?);

-- =====================================================
-- 12. INDEXES FOR PERFORMANCE
-- =====================================================

-- Create indexes for better performance
CREATE INDEX idx_tasks_assigned_user ON tasks(assigned_user_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_user_project_roles_user ON user_project_roles(user_id);
CREATE INDEX idx_user_project_roles_project ON user_project_roles(project_id);

-- =====================================================
-- 13. TRIGGERS FOR REAL-TIME UPDATES
-- =====================================================

-- Trigger to update task progress when status changes
DELIMITER //
CREATE TRIGGER update_task_progress
AFTER UPDATE ON tasks
FOR EACH ROW
BEGIN
    -- You can add logic here to update progress tracking
    -- or send notifications when task status changes
    IF OLD.status != NEW.status THEN
        -- Log the status change
        INSERT INTO task_activity_log (task_id, user_id, action, old_value, new_value, created_at)
        VALUES (NEW.id, NEW.assigned_user_id, 'STATUS_CHANGE', OLD.status, NEW.status, NOW());
    END IF;
END //
DELIMITER ;

-- Create task activity log table
CREATE TABLE IF NOT EXISTS task_activity_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value VARCHAR(255),
    new_value VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================================================
-- END OF USER DASHBOARD QUERIES
-- =====================================================

-- Usage Examples:
-- 1. Replace ? with actual user_id when executing queries
-- 2. Use these queries in your backend services
-- 3. Call the stored procedure for comprehensive dashboard data
-- 4. Set up scheduled jobs to refresh dashboard data
-- 5. Use the activity log for real-time notifications
