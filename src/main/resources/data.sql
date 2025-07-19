-- delete everything is table
DELETE FROM cuser_project_settings;
DELETE FROM cproject;
DELETE FROM cuser;

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
    updated_date         -- Timestamp when the user was last updated
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
    '2025-07-18 15:58:12.244818'
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
    updated_date         -- Timestamp when the user was last updated
) VALUES (
    '2025-07-18 15:58:12.244818',
    'test2@example.com',
    TRUE,
    'Lova2',
    'user2',
    'user2',
    '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu',
    '1234567890',
    'USER',
    '2025-07-18 15:58:12.244818'
);

-- Insert test projects
INSERT INTO cproject (name) VALUES ('Project Alpha');
INSERT INTO cproject (name) VALUES ('Project Beta');
INSERT INTO cproject (name) VALUES ('Project Gamma');
INSERT INTO cproject (name) VALUES ('Project Delta');

-- Insert user project settings
-- User 'user' has access to projects 1, 2, 3
INSERT INTO cuser_project_settings (user_id, project_id) VALUES (1, 1);
INSERT INTO cuser_project_settings (user_id, project_id) VALUES (1, 2);
INSERT INTO cuser_project_settings (user_id, project_id) VALUES (1, 3);

-- User 'user2' has access to projects 2, 4
INSERT INTO cuser_project_settings (user_id, project_id) VALUES (2, 2);
INSERT INTO cuser_project_settings (user_id, project_id) VALUES (2, 4);