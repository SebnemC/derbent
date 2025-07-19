-- Delete existing data to ensure clean state
DELETE FROM crisk;
DELETE FROM cactivity;
DELETE FROM cuser;
DELETE FROM cproject;

-- Check application.java to see WHEN this file is run
-- Insert sample login users into cuser table
INSERT INTO cuser (
    created_date,        -- Timestamp when the user was created
    email,               -- User's email address
    enabled,             -- Account enabled status (boolean)
    lastname,            -- User's last name
    login,               -- Username for login
    name,                -- User's first name
    password,            -- BCrypt encoded password hash ($2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu = password: derbent)
    phone,               -- User's phone number
    roles,               -- User roles (e.g., 'USER')
    updated_date         -- Timestamp when the user was last updated
) VALUES 
(
    '2025-07-19 15:58:12.244818',
    'user@derbent.tech',
    TRUE,
    'User',
    'user',
    'Demo',
    '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu',
    '+1-555-0001',
    'USER',
    '2025-07-19 15:58:12.244818'
),
(
    '2025-07-19 15:58:12.244818',
    'admin@derbent.tech',
    TRUE,
    'Administrator',
    'admin',
    'System',
    '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu',
    '+1-555-0002',
    'ADMIN',
    '2025-07-19 15:58:12.244818'
),
(
    '2025-07-19 15:58:12.244818',
    'manager@derbent.tech',
    TRUE,
    'Manager',
    'manager',
    'Project',
    '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu',
    '+1-555-0003',
    'MANAGER',
    '2025-07-19 15:58:12.244818'
);

-- Insert sample projects for comprehensive demo
INSERT INTO cproject (name, created_date, updated_date) VALUES 
('Derbent Project', '2025-07-19 10:00:00.000000', '2025-07-19 10:00:00.000000'),
('Website Redesign', '2025-07-19 10:15:00.000000', '2025-07-19 10:15:00.000000'),
('Mobile App Development', '2025-07-19 10:30:00.000000', '2025-07-19 10:30:00.000000'),
('Data Migration Project', '2025-07-19 10:45:00.000000', '2025-07-19 10:45:00.000000'),
('Security Audit Initiative', '2025-07-19 11:00:00.000000', '2025-07-19 11:00:00.000000');

-- Insert comprehensive sample activities for each project
INSERT INTO cactivity (name, project_id, created_date, updated_date) VALUES
-- Derbent Project Activities
('Requirements Analysis', 1, '2025-07-19 11:00:00.000000', '2025-07-19 11:00:00.000000'),
('System Architecture Design', 1, '2025-07-19 11:15:00.000000', '2025-07-19 11:15:00.000000'),
('Database Schema Creation', 1, '2025-07-19 11:30:00.000000', '2025-07-19 11:30:00.000000'),
('Core Module Development', 1, '2025-07-19 11:45:00.000000', '2025-07-19 11:45:00.000000'),
('Integration Testing', 1, '2025-07-19 12:00:00.000000', '2025-07-19 12:00:00.000000'),

-- Website Redesign Activities
('User Experience Research', 2, '2025-07-19 12:15:00.000000', '2025-07-19 12:15:00.000000'),
('Wireframe Creation', 2, '2025-07-19 12:30:00.000000', '2025-07-19 12:30:00.000000'),
('Visual Design Implementation', 2, '2025-07-19 12:45:00.000000', '2025-07-19 12:45:00.000000'),
('Frontend Development', 2, '2025-07-19 13:00:00.000000', '2025-07-19 13:00:00.000000'),
('Content Management Setup', 2, '2025-07-19 13:15:00.000000', '2025-07-19 13:15:00.000000'),
('SEO Optimization', 2, '2025-07-19 13:30:00.000000', '2025-07-19 13:30:00.000000'),

-- Mobile App Development Activities
('Platform Assessment', 3, '2025-07-19 13:45:00.000000', '2025-07-19 13:45:00.000000'),
('Native iOS Development', 3, '2025-07-19 14:00:00.000000', '2025-07-19 14:00:00.000000'),
('Native Android Development', 3, '2025-07-19 14:15:00.000000', '2025-07-19 14:15:00.000000'),
('API Integration', 3, '2025-07-19 14:30:00.000000', '2025-07-19 14:30:00.000000'),
('App Store Deployment', 3, '2025-07-19 14:45:00.000000', '2025-07-19 14:45:00.000000'),

-- Data Migration Project Activities
('Legacy System Analysis', 4, '2025-07-19 15:00:00.000000', '2025-07-19 15:00:00.000000'),
('Data Mapping Strategy', 4, '2025-07-19 15:15:00.000000', '2025-07-19 15:15:00.000000'),
('ETL Process Development', 4, '2025-07-19 15:30:00.000000', '2025-07-19 15:30:00.000000'),
('Data Validation Testing', 4, '2025-07-19 15:45:00.000000', '2025-07-19 15:45:00.000000'),

-- Security Audit Initiative Activities
('Vulnerability Assessment', 5, '2025-07-19 16:00:00.000000', '2025-07-19 16:00:00.000000'),
('Penetration Testing', 5, '2025-07-19 16:15:00.000000', '2025-07-19 16:15:00.000000'),
('Security Policy Review', 5, '2025-07-19 16:30:00.000000', '2025-07-19 16:30:00.000000');

-- Insert comprehensive sample risks for each project
INSERT INTO crisk (name, risk_severity, project_id, created_date, updated_date) VALUES
-- Derbent Project Risks
('Technical Complexity Risk', 'HIGH', 1, '2025-07-19 11:00:00.000000', '2025-07-19 11:00:00.000000'),
('Resource Availability Risk', 'MEDIUM', 1, '2025-07-19 11:15:00.000000', '2025-07-19 11:15:00.000000'),
('Integration Complexity Risk', 'MEDIUM', 1, '2025-07-19 11:30:00.000000', '2025-07-19 11:30:00.000000'),
('Performance Degradation Risk', 'LOW', 1, '2025-07-19 11:45:00.000000', '2025-07-19 11:45:00.000000'),

-- Website Redesign Risks
('Browser Compatibility Risk', 'MEDIUM', 2, '2025-07-19 12:15:00.000000', '2025-07-19 12:15:00.000000'),
('User Experience Degradation', 'HIGH', 2, '2025-07-19 12:30:00.000000', '2025-07-19 12:30:00.000000'),
('Content Migration Risk', 'MEDIUM', 2, '2025-07-19 12:45:00.000000', '2025-07-19 12:45:00.000000'),
('SEO Ranking Impact', 'HIGH', 2, '2025-07-19 13:00:00.000000', '2025-07-19 13:00:00.000000'),
('Third-party Integration Risk', 'LOW', 2, '2025-07-19 13:15:00.000000', '2025-07-19 13:15:00.000000'),

-- Mobile App Development Risks
('Platform Fragmentation Risk', 'HIGH', 3, '2025-07-19 13:45:00.000000', '2025-07-19 13:45:00.000000'),
('App Store Approval Risk', 'MEDIUM', 3, '2025-07-19 14:00:00.000000', '2025-07-19 14:00:00.000000'),
('Device Compatibility Risk', 'MEDIUM', 3, '2025-07-19 14:15:00.000000', '2025-07-19 14:15:00.000000'),
('API Rate Limiting Risk', 'LOW', 3, '2025-07-19 14:30:00.000000', '2025-07-19 14:30:00.000000'),
('User Adoption Risk', 'CRITICAL', 3, '2025-07-19 14:45:00.000000', '2025-07-19 14:45:00.000000'),

-- Data Migration Project Risks
('Data Loss Risk', 'CRITICAL', 4, '2025-07-19 15:00:00.000000', '2025-07-19 15:00:00.000000'),
('Data Corruption Risk', 'HIGH', 4, '2025-07-19 15:15:00.000000', '2025-07-19 15:15:00.000000'),
('Migration Downtime Risk', 'MEDIUM', 4, '2025-07-19 15:30:00.000000', '2025-07-19 15:30:00.000000'),
('Legacy System Dependency', 'MEDIUM', 4, '2025-07-19 15:45:00.000000', '2025-07-19 15:45:00.000000'),

-- Security Audit Initiative Risks
('Security Breach Discovery', 'CRITICAL', 5, '2025-07-19 16:00:00.000000', '2025-07-19 16:00:00.000000'),
('Compliance Violation Risk', 'HIGH', 5, '2025-07-19 16:15:00.000000', '2025-07-19 16:15:00.000000'),
('Audit Timeline Delay', 'MEDIUM', 5, '2025-07-19 16:30:00.000000', '2025-07-19 16:30:00.000000'),
('False Positive Risk', 'LOW', 5, '2025-07-19 16:45:00.000000', '2025-07-19 16:45:00.000000');