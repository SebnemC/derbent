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

-- Insert sample activities for demonstration of Kanban board
-- cactivity table with activity_id as primary key
INSERT INTO cactivity (
    name,                    -- Activity name (required)
    project_id,             -- Reference to project (required)
    cactivitytype_id,       -- Reference to activity type (optional)
    cactivitystatus_id      -- Reference to activity status (optional)
) VALUES 
-- Development activities for Derbent Project
('Implement User Authentication', 1, 1, 2),
('Create Database Schema', 1, 1, 4),
('Develop REST API', 1, 1, 2),
('Build Frontend Components', 1, 1, 1),

-- Testing activities for Derbent Project  
('Unit Testing for Auth Module', 1, 2, 3),
('Integration Testing', 1, 2, 1),
('Performance Testing', 1, 2, 1),

-- Design activities for Derbent Project
('Design System Setup', 1, 3, 4),
('Create UI Mockups', 1, 3, 2),
('Design Login Page', 1, 3, 3),

-- Documentation activities for Derbent Project
('API Documentation', 1, 4, 2),
('User Manual', 1, 4, 1),

-- Meeting activities for Derbent Project
('Daily Standups', 1, 5, 2),
('Sprint Planning Session', 1, 5, 4),

-- Research activities for Derbent Project
('Technology Stack Research', 1, 6, 4),
('Security Best Practices Study', 1, 6, 2),

-- Website Redesign Project activities
('Homepage Redesign', 2, 3, 1),
('Content Migration', 2, 1, 1),
('SEO Optimization', 2, 4, 1),

-- Mobile App Development Project activities
('Mobile UI Framework Setup', 3, 1, 2),
('App Icon Design', 3, 3, 4),
('Mobile Testing Strategy', 3, 2, 3);

-- Activities without a type (to test "No Type" column)
INSERT INTO cactivity (
    name,
    project_id, 
    cactivitystatus_id
) VALUES 
('General Project Setup', 1, 1),
('Team Coordination', 1, 2);