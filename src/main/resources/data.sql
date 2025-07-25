-- Sample data initialization for Derbent application
-- This script assumes tables are created by Hibernate with the classname_id convention:
-- cusertype table with cusertype_id as primary key
-- cactivitytype table with cactivitytype_id as primary key  
-- cuser table with user_id as primary key and cusertype_id as foreign key
-- cproject table with project_id as primary key

-- Insert sample user types
INSERT INTO cusertype (name, description) VALUES 
('Developer', 'Software developers and engineers'),
('Manager', 'Project and team managers'),
('Designer', 'UI/UX and graphic designers'),
('Tester', 'Quality assurance and testing personnel'),
('Analyst', 'Business and system analysts');

-- Insert sample activity types
INSERT INTO cactivitytype (name, description) VALUES 
('Development', 'Software development tasks'),
('Testing', 'Quality assurance and testing activities'),
('Design', 'UI/UX and graphic design work'),
('Documentation', 'Technical and user documentation'),
('Meeting', 'Team meetings and discussions'),
('Research', 'Research and analysis activities');

-- Insert sample activity statuses
INSERT INTO cactivitystatus (name, description) VALUES 
('TODO', 'Task is planned but not started yet'),
('IN_PROGRESS', 'Task is currently being worked on'),
('REVIEW', 'Task is completed and waiting for review'),
('DONE', 'Task is completed and approved'),
('BLOCKED', 'Task is blocked by external dependencies'),
('ON_HOLD', 'Task is temporarily paused');

-- check application.java to see WHEN this file is run
-- Insert a sample login user into cuser table
INSERT INTO cuser (
    created_date,        -- Timestamp when the user was created
    email,               -- User's email address
    enabled,             -- Account enabled status (boolean)
    lastname,            -- User's last name
    login,               -- Username for login
    name,                -- User's first name
    password,            -- BCrypt encoded password hash
    phone,               -- User's phone number
    roles,               -- User roles (e.g., 'USER')
    updated_date,        -- Timestamp when the user was last updated
    cusertype_id         -- Reference to user type (updated to match new classname_id convention)
) VALUES (
    '2025-07-18 15:58:12.244818',
    'test@example.com',
    TRUE,
    'Lova',
    'user',
    'user',
    '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu',
    '1234567890',
    'USER',
    '2025-07-18 15:58:12.244818',
    1  -- Developer type
);
	
INSERT INTO cuser (
    created_date,        -- Timestamp when the user was created
    email,               -- User's email address
    enabled,             -- Account enabled status (boolean)
    lastname,            -- User's last name
    login,               -- Username for login
    name,                -- User's first name
    password,            -- BCrypt encoded password hash
    phone,               -- User's phone number
    roles,               -- User roles (e.g., 'USER')
    updated_date,        -- Timestamp when the user was last updated
    cusertype_id         -- Reference to user type (updated to match new classname_id convention)
) VALUES (
    '2025-07-18 15:58:12.244818',
    'test@example.com',
    TRUE,
    'Lova2',
    'user2',
    'user2',
    '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu',
    '1234567890',
    'USER2',
    '2025-07-18 15:58:12.244818',
    2  -- Manager type
);

-- Insert sample projects
INSERT INTO cproject (name) VALUES 
('Derbent Project'),
('Website Redesign'),
('Mobile App Development');

-- Insert sample meeting types
INSERT INTO cmeetingtype (name, description) VALUES 
('Stand-up', 'Daily stand-up meetings'),
('Planning', 'Sprint planning and project planning meetings'),
('Retrospective', 'Sprint retrospective and review meetings'),
('Demo', 'Product demo and showcase meetings'),
('Review', 'Code review and design review meetings'),
('All-hands', 'Company-wide or team all-hands meetings');

-- Insert sample meetings
INSERT INTO cmeeting (name, description, meeting_date, end_date, project_id, cmeetingtype_id) VALUES 
('Daily Standup', 'Daily team synchronization meeting', '2025-07-23 09:00:00', '2025-07-23 09:30:00', 1, 1),
('Sprint Planning', 'Planning for the upcoming sprint', '2025-07-24 10:00:00', '2025-07-24 12:00:00', 1, 2),
('Sprint Demo', 'Demonstration of completed features', '2025-07-25 14:00:00', '2025-07-25 15:30:00', 1, 4),
('Website Planning', 'Initial planning for website redesign', '2025-07-26 11:00:00', '2025-07-26 13:00:00', 2, 2),
('Mobile App Review', 'Review of mobile app progress', '2025-07-27 15:00:00', '2025-07-27 16:00:00', 3, 5);

-- Insert sample meeting participants (many-to-many relationship)
INSERT INTO cmeeting_participants (meeting_id, user_id) VALUES 
-- Daily Standup (meeting_id = 1) - both users
(1, 1),
(1, 2),
-- Sprint Planning (meeting_id = 2) - both users
(2, 1),
(2, 2),
-- Sprint Demo (meeting_id = 3) - user 1 only
(3, 1),
-- Website Planning (meeting_id = 4) - user 2 only
(4, 2),
-- Mobile App Review (meeting_id = 5) - both users
(5, 1),
(5, 2);

-- Insert sample user-project relationships
-- Link users to projects with specific roles
INSERT INTO cuserprojectsettings (user_id, project_id, role, permission) VALUES 
-- User 1 (user) in multiple projects
(1, 1, 'Developer', 'READ_WRITE'),  -- user in Derbent Project as Developer
(1, 3, 'Lead Developer', 'READ_WRITE'),  -- user in Mobile App Development as Lead Developer
-- User 2 (user2) in multiple projects  
(2, 1, 'Manager', 'ADMIN'),  -- user2 in Derbent Project as Manager
(2, 2, 'Project Manager', 'ADMIN');  -- user2 in Website Redesign as Project Manager

-- Insert sample activities
-- Activities for Derbent Project (project_id = 1)
INSERT INTO cactivity (name, project_id, cactivitytype_id) VALUES 
('Setup Development Environment', 1, 1),
('Design User Interface', 1, 3),
('Implement Authentication', 1, 1),
('Write Unit Tests', 1, 2),
('Project Planning Meeting', 1, 5),

-- Activities for Website Redesign (project_id = 2)
('Analyze Current Website', 2, 6),
('Create New Design Concepts', 2, 3),
('User Experience Research', 2, 6),

-- Activities for Mobile App Development (project_id = 3)
('Mobile App Architecture', 3, 1),
('iOS Development Setup', 3, 1),
('Android Development Setup', 3, 1),
('Mobile UI Design', 3, 3),
('Mobile Testing Strategy', 3, 2);

-- Insert sample companies
-- ccompany table with company_id as primary key
INSERT INTO ccompany (
    name,                -- Company name (required, unique)
    description,         -- Company description (optional)
    address,            -- Company address (optional)
    phone,              -- Company phone number (optional)
    email,              -- Company email address (optional)
    website,            -- Company website URL (optional)
    tax_number,         -- Company tax identification number (optional)
    enabled             -- Company active status (boolean, default true)
) VALUES 
('Tech Solutions Inc.', 'Leading software development company specializing in enterprise solutions', '123 Innovation Drive, Tech Valley, CA 94000', '+1-555-0100', 'contact@techsolutions.com', 'https://www.techsolutions.com', 'TAX-2025-001', TRUE),
('Digital Dynamics LLC', 'Creative digital agency focusing on web and mobile applications', '456 Creative Boulevard, Design City, NY 10001', '+1-555-0200', 'info@digitaldynamics.com', 'https://www.digitaldynamics.com', 'TAX-2025-002', TRUE),
('Innovation Labs Ltd.', 'Research and development company for emerging technologies', '789 Research Park, Innovation Hub, TX 75001', '+1-555-0300', 'contact@innovationlabs.com', 'https://www.innovationlabs.com', 'TAX-2025-003', TRUE),
('Global Systems Corp.', 'International consulting firm for business process optimization', '321 Business Center, Corporate Plaza, FL 33101', '+1-555-0400', 'contact@globalsystems.com', 'https://www.globalsystems.com', 'TAX-2025-004', TRUE),
('Startup Accelerator Inc.', 'Venture capital and startup incubation company', '654 Startup Street, Entrepreneur District, WA 98101', '+1-555-0500', 'contact@startupaccelerator.com', 'https://www.startupaccelerator.com', 'TAX-2025-005', FALSE);