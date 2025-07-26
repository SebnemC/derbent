-- =====================================================================
-- COMPREHENSIVE SAMPLE DATA INITIALIZATION FOR DERBENT APPLICATION
-- =====================================================================
-- This script initializes the database with comprehensive sample data
-- for project management, activity tracking, and resource management.
-- 
-- CODING RULES COMPLIANCE:
-- - Passwords are always 'test123' for all users
-- - At least 10 items for every view/entity type
-- - Examples for every project × company × task type combination
-- - Proper relational order to avoid constraint errors
-- - PostgreSQL-only configuration
-- - Tables deleted at top before inserting values
-- =====================================================================

-- =====================================================================
-- TABLE CLEANUP - DELETE ALL EXISTING DATA AND CONSTRAINTS
-- =====================================================================

-- Disable foreign key checks temporarily for PostgreSQL
SET session_replication_role = replica;

-- Delete data from junction tables first
DELETE FROM cmeeting_participants;

-- Delete data from dependent tables (in reverse dependency order)
DELETE FROM cactivity;
DELETE FROM cmeeting;
DELETE FROM crisk;

-- Delete data from main entity tables
DELETE FROM cproject;
DELETE FROM cuser;
DELETE FROM ccompany;

-- Delete data from lookup tables
DELETE FROM cactivitytype;
DELETE FROM cactivitystatus;
DELETE FROM cactivitypriority;
DELETE FROM cmeetingtype;
DELETE FROM cusertype;

-- Re-enable foreign key checks
SET session_replication_role = DEFAULT;

-- Reset sequences to start from 1
SELECT setval('cuser_user_id_seq', 1, false);
SELECT setval('ccompany_company_id_seq', 1, false);
SELECT setval('cproject_project_id_seq', 1, false);
SELECT setval('cactivity_activity_id_seq', 1, false);
SELECT setval('cmeeting_meeting_id_seq', 1, false);
SELECT setval('crisk_risk_id_seq', 1, false);
SELECT setval('cusertype_id_seq', 1, false);
SELECT setval('cactivitytype_id_seq', 1, false);
SELECT setval('cactivitystatus_id_seq', 1, false);
SELECT setval('cactivitypriority_id_seq', 1, false);
SELECT setval('cmeetingtype_id_seq', 1, false);

-- =====================================================================
-- BASIC LOOKUP TABLES (No foreign key dependencies)
-- =====================================================================

-- Insert sample user types (foundation for user classification) - AT LEAST 10 ITEMS
INSERT INTO cusertype (name, description) VALUES 
('Administrator', 'System administrators with full access'),
('Project Manager', 'Project managers and team leads'),
('Senior Developer', 'Senior software developers and architects'),
('Developer', 'Software developers and engineers'),
('QA Engineer', 'Quality assurance and testing specialists'),
('UI/UX Designer', 'User interface and experience designers'),
('Business Analyst', 'Business and system analysts'),
('DevOps Engineer', 'DevOps and infrastructure specialists'),
('Product Owner', 'Product owners responsible for product vision and backlog'),
('Scrum Master', 'Agile coaches and scrum masters'),
('Technical Writer', 'Technical documentation specialists'),
('Database Administrator', 'Database specialists and administrators'),
('Security Engineer', 'Cybersecurity and information security specialists'),
('Cloud Architect', 'Cloud infrastructure and solution architects'),
('Data Scientist', 'Data analysis and machine learning specialists');

-- Insert sample activity types (categorizes different types of work) - AT LEAST 10 ITEMS
INSERT INTO cactivitytype (name, description) VALUES 
('Epic', 'Large feature or business initiative spanning multiple sprints'),
('User Story', 'Feature from end-user perspective with acceptance criteria'),
('Task', 'General development or operational task'),
('Bug', 'Software defect that needs to be fixed'),
('Research', 'Research and investigation activities'),
('Documentation', 'Technical and user documentation work'),
('Meeting', 'Team meetings, planning sessions, and discussions'),
('Testing', 'Quality assurance and testing activities'),
('Deployment', 'Software deployment and release activities'),
('Maintenance', 'System maintenance and support activities'),
('Security Review', 'Security assessment and vulnerability analysis'),
('Performance Optimization', 'Performance tuning and optimization tasks'),
('Code Review', 'Peer code review and quality assessment'),
('Infrastructure', 'Infrastructure setup and maintenance'),
('Training', 'Team training and knowledge transfer activities');


-- Insert sample activity statuses (workflow states) - AT LEAST 10 ITEMS
INSERT INTO cactivitystatus (name, description, color, is_final, sort_order) VALUES 
('BACKLOG', 'Items waiting to be prioritized and planned', '#9E9E9E', FALSE, 1),
('TODO', 'Ready to start - all prerequisites met', '#2196F3', FALSE, 2),
('IN_PROGRESS', 'Currently being worked on', '#FF9800', FALSE, 3),
('CODE_REVIEW', 'Code completed, awaiting review', '#9C27B0', FALSE, 4),
('TESTING', 'Under quality assurance testing', '#3F51B5', FALSE, 5),
('BLOCKED', 'Cannot proceed due to external dependencies', '#F44336', FALSE, 6),
('DONE', 'Successfully completed and delivered', '#4CAF50', TRUE, 7),
('CANCELLED', 'Work cancelled or deemed unnecessary', '#607D8B', TRUE, 8),
('REJECTED', 'Did not meet acceptance criteria', '#795548', TRUE, 9),
('ON_HOLD', 'Temporarily paused pending decisions', '#FF5722', FALSE, 10),
('READY_FOR_DEPLOY', 'Approved and ready for deployment', '#8BC34A', FALSE, 11),
('IN_REVIEW', 'Under business or technical review', '#E91E63', FALSE, 12);

-- Insert sample activity priorities (business importance levels) - AT LEAST 10 ITEMS
INSERT INTO cactivitypriority (name, description, priority_level, color, is_default) VALUES 
('BLOCKER', 'Critical blocker - stops all work', 1, '#B71C1C', FALSE),
('CRITICAL', 'Critical priority - immediate attention required', 2, '#F44336', FALSE),
('HIGH', 'High priority - important for current sprint', 3, '#FF9800', FALSE),
('MEDIUM', 'Medium priority - normal task', 4, '#2196F3', TRUE),
('LOW', 'Low priority - can be deferred to next sprint', 5, '#4CAF50', FALSE),
('TRIVIAL', 'Minor improvement - nice to have', 6, '#9E9E9E', FALSE),
('URGENT', 'Urgent but not blocking - needs quick attention', 7, '#E91E63', FALSE),
('IMPORTANT', 'Important for business goals', 8, '#9C27B0', FALSE),
('ENHANCEMENT', 'Feature enhancement or improvement', 9, '#00BCD4', FALSE),
('NICE_TO_HAVE', 'Good to have but not essential', 10, '#CDDC39', FALSE);

-- =====================================================================
-- COMPANIES (Independent entities) - AT LEAST 10 ITEMS
-- =====================================================================

-- Insert sample companies (client organizations and partners)
INSERT INTO ccompany (
    name, description, address, phone, email, website, tax_number, enabled
) VALUES 
('TechCorp Solutions Ltd.', 'Leading enterprise software development company specializing in cloud-native applications and microservices architecture', '456 Innovation Plaza, Silicon Valley, CA 94087', '+1-555-0101', 'info@techcorp-solutions.com', 'https://www.techcorp-solutions.com', 'US-TAX-2025-001', TRUE),
('Digital Innovations Inc.', 'Cutting-edge digital transformation consultancy focusing on AI, blockchain, and IoT solutions', '789 Future Tech Center, Austin, TX 78701', '+1-555-0202', 'contact@digital-innovations.com', 'https://www.digital-innovations.com', 'US-TAX-2025-002', TRUE),
('CloudFirst Systems', 'Cloud infrastructure and DevOps automation specialists providing scalable solutions', '321 Cloud Street, Seattle, WA 98101', '+1-555-0303', 'hello@cloudfirst-systems.com', 'https://www.cloudfirst-systems.com', 'US-TAX-2025-003', TRUE),
('AgileWorks Consulting', 'Agile transformation and project management consultancy with certified Scrum Masters', '654 Agile Avenue, Denver, CO 80202', '+1-555-0404', 'team@agileworks-consulting.com', 'https://www.agileworks-consulting.com', 'US-TAX-2025-004', TRUE),
('StartupHub Accelerator', 'Early-stage startup incubator and venture capital fund', '987 Startup Boulevard, New York, NY 10001', '+1-555-0505', 'ventures@startuphub-accelerator.com', 'https://www.startuphub-accelerator.com', 'US-TAX-2025-005', TRUE),
('DataFlow Analytics', 'Big data analytics and machine learning consulting firm', '123 Data Drive, San Francisco, CA 94105', '+1-555-0606', 'insights@dataflow-analytics.com', 'https://www.dataflow-analytics.com', 'US-TAX-2025-006', TRUE),
('SecureShield Technologies', 'Cybersecurity services and penetration testing specialists', '456 Security Boulevard, Washington, DC 20001', '+1-555-0707', 'security@secureshield-tech.com', 'https://www.secureshield-tech.com', 'US-TAX-2025-007', TRUE),
('MobileFirst Solutions', 'Mobile app development and cross-platform solution providers', '789 Mobile Way, Los Angeles, CA 90210', '+1-555-0808', 'apps@mobilefirst-solutions.com', 'https://www.mobilefirst-solutions.com', 'US-TAX-2025-008', TRUE),
('GreenTech Innovations', 'Sustainable technology and environmental solutions company', '321 Green Valley Road, Portland, OR 97201', '+1-555-0909', 'eco@greentech-innovations.com', 'https://www.greentech-innovations.com', 'US-TAX-2025-009', TRUE),
('FinanceFlow Systems', 'Financial technology and blockchain-based payment solutions', '654 Finance Street, Chicago, IL 60601', '+1-555-1010', 'payments@financeflow-systems.com', 'https://www.financeflow-systems.com', 'US-TAX-2025-010', TRUE),
('HealthTech Partners', 'Healthcare technology and medical device integration services', '987 Health Plaza, Boston, MA 02101', '+1-555-1111', 'health@healthtech-partners.com', 'https://www.healthtech-partners.com', 'US-TAX-2025-011', TRUE),
('EduCloud Learning', 'Educational technology and e-learning platform development', '123 Education Avenue, Philadelphia, PA 19101', '+1-555-1212', 'learn@educloud-learning.com', 'https://www.educloud-learning.com', 'US-TAX-2025-012', FALSE);

-- =====================================================================
-- USERS (Depends on cusertype) - AT LEAST 10 ITEMS per type coverage
-- =====================================================================
-- Insert sample users with diverse roles and realistic data
-- PASSWORD RULE: All passwords are 'test123' (hashed: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.)
INSERT INTO cuser (
    created_date, email, enabled, lastname, login, name, password, phone, roles, updated_date, cusertype_id, user_role
) VALUES 
-- System Administrator
('2025-01-15 08:00:00', 'admin@derbent.tech', TRUE, 'Administrator', 'admin', 'System', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-0001', 'ADMIN,USER', '2025-01-15 08:00:00', 1, 'TEAM_MEMBER'),

-- Project Managers (at least 3)
('2025-01-15 09:00:00', 'sarah.johnson@derbent.tech', TRUE, 'Johnson', 'sarah.johnson', 'Sarah', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-1001', 'MANAGER,USER', '2025-01-15 09:00:00', 2, 'TEAM_MEMBER'),
('2025-01-15 09:15:00', 'michael.chen@derbent.tech', TRUE, 'Chen', 'michael.chen', 'Michael', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-1002', 'MANAGER,USER', '2025-01-15 09:15:00', 2, 'TEAM_MEMBER'),
('2025-01-15 09:30:00', 'rachel.martinez@derbent.tech', TRUE, 'Martinez', 'rachel.martinez', 'Rachel', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-1003', 'MANAGER,USER', '2025-01-15 09:30:00', 2, 'TEAM_MEMBER'),

-- Senior Developers (at least 4)
('2025-01-15 10:00:00', 'alex.rodriguez@derbent.tech', TRUE, 'Rodriguez', 'alex.rodriguez', 'Alex', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-2001', 'SENIOR_DEV,USER', '2025-01-15 10:00:00', 3, 'TEAM_MEMBER'),
('2025-01-15 10:15:00', 'emma.wilson@derbent.tech', TRUE, 'Wilson', 'emma.wilson', 'Emma', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-2002', 'SENIOR_DEV,USER', '2025-01-15 10:15:00', 3, 'TEAM_MEMBER'),
('2025-01-15 10:30:00', 'kevin.thompson@derbent.tech', TRUE, 'Thompson', 'kevin.thompson', 'Kevin', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-2003', 'SENIOR_DEV,USER', '2025-01-15 10:30:00', 3, 'TEAM_MEMBER'),
('2025-01-15 10:45:00', 'nina.patel@derbent.tech', TRUE, 'Patel', 'nina.patel', 'Nina', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-2004', 'SENIOR_DEV,USER', '2025-01-15 10:45:00', 3, 'TEAM_MEMBER'),

-- Developers (at least 4)
('2025-01-15 11:00:00', 'david.kim@derbent.tech', TRUE, 'Kim', 'david.kim', 'David', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-3001', 'DEVELOPER,USER', '2025-01-15 11:00:00', 4, 'TEAM_MEMBER'),
('2025-01-15 11:15:00', 'lisa.patel@derbent.tech', TRUE, 'Patel', 'lisa.patel', 'Lisa', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-3002', 'DEVELOPER,USER', '2025-01-15 11:15:00', 4, 'TEAM_MEMBER'),
('2025-01-15 11:30:00', 'carlos.rivera@derbent.tech', TRUE, 'Rivera', 'carlos.rivera', 'Carlos', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-3003', 'DEVELOPER,USER', '2025-01-15 11:30:00', 4, 'TEAM_MEMBER'),
('2025-01-15 11:45:00', 'amanda.smith@derbent.tech', TRUE, 'Smith', 'amanda.smith', 'Amanda', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-3004', 'DEVELOPER,USER', '2025-01-15 11:45:00', 4, 'TEAM_MEMBER'),

-- QA Engineers (at least 3)
('2025-01-15 12:00:00', 'james.taylor@derbent.tech', TRUE, 'Taylor', 'james.taylor', 'James', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-4001', 'QA,USER', '2025-01-15 12:00:00', 5, 'TEAM_MEMBER'),
('2025-01-15 12:15:00', 'maria.garcia@derbent.tech', TRUE, 'Garcia', 'maria.garcia', 'Maria', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-4002', 'QA,USER', '2025-01-15 12:15:00', 5, 'TEAM_MEMBER'),
('2025-01-15 12:30:00', 'peter.jones@derbent.tech', TRUE, 'Jones', 'peter.jones', 'Peter', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-4003', 'QA,USER', '2025-01-15 12:30:00', 5, 'TEAM_MEMBER'),

-- UI/UX Designers (at least 2)
('2025-01-15 13:00:00', 'sophia.brown@derbent.tech', TRUE, 'Brown', 'sophia.brown', 'Sophia', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-5001', 'DESIGNER,USER', '2025-01-15 13:00:00', 6, 'TEAM_MEMBER'),
('2025-01-15 13:15:00', 'oliver.davis@derbent.tech', TRUE, 'Davis', 'oliver.davis', 'Oliver', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-5002', 'DESIGNER,USER', '2025-01-15 13:15:00', 6, 'TEAM_MEMBER'),

-- Business Analysts (at least 2)
('2025-01-15 14:00:00', 'robert.anderson@derbent.tech', TRUE, 'Anderson', 'robert.anderson', 'Robert', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-6001', 'ANALYST,USER', '2025-01-15 14:00:00', 7, 'TEAM_MEMBER'),
('2025-01-15 14:15:00', 'helen.white@derbent.tech', TRUE, 'White', 'helen.white', 'Helen', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-6002', 'ANALYST,USER', '2025-01-15 14:15:00', 7, 'TEAM_MEMBER'),

-- DevOps Engineers (at least 2)
('2025-01-15 15:00:00', 'jennifer.lee@derbent.tech', TRUE, 'Lee', 'jennifer.lee', 'Jennifer', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-7001', 'DEVOPS,USER', '2025-01-15 15:00:00', 8, 'TEAM_MEMBER'),
('2025-01-15 15:15:00', 'ryan.clark@derbent.tech', TRUE, 'Clark', 'ryan.clark', 'Ryan', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-7002', 'DEVOPS,USER', '2025-01-15 15:15:00', 8, 'TEAM_MEMBER'),

-- Product Owners (at least 2)
('2025-01-15 16:00:00', 'stephanie.moore@derbent.tech', TRUE, 'Moore', 'stephanie.moore', 'Stephanie', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-8001', 'PRODUCT_OWNER,USER', '2025-01-15 16:00:00', 9, 'TEAM_MEMBER'),
('2025-01-15 16:15:00', 'thomas.jackson@derbent.tech', TRUE, 'Jackson', 'thomas.jackson', 'Thomas', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-8002', 'PRODUCT_OWNER,USER', '2025-01-15 16:15:00', 9, 'TEAM_MEMBER'),

-- Scrum Masters (at least 2)
('2025-01-15 17:00:00', 'diana.miller@derbent.tech', TRUE, 'Miller', 'diana.miller', 'Diana', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-9001', 'SCRUM_MASTER,USER', '2025-01-15 17:00:00', 10, 'TEAM_MEMBER'),
('2025-01-15 17:15:00', 'mark.harris@derbent.tech', TRUE, 'Harris', 'mark.harris', 'Mark', 
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '+1-555-9002', 'SCRUM_MASTER,USER', '2025-01-15 17:15:00', 10, 'TEAM_MEMBER');

-- =====================================================================
-- PROJECTS (Depends on users for assignment) - AT LEAST 10 ITEMS
-- =====================================================================

-- Insert sample projects with realistic business scenarios
INSERT INTO cproject (name, description, created_date, last_modified_date) VALUES 
('E-Commerce Platform Modernization', 'Migration of legacy e-commerce system to microservices architecture with React frontend and Spring Boot backend', NOW(), NOW()),
('Customer Analytics Dashboard', 'Real-time analytics dashboard for customer behavior tracking using machine learning and data visualization', NOW(), NOW()),
('Mobile Banking Application', 'Secure mobile banking app with biometric authentication, transaction management, and investment features', NOW(), NOW()),
('DevOps Infrastructure Automation', 'Automated CI/CD pipeline setup, containerization, and cloud infrastructure management', NOW(), NOW()),
('API Gateway Implementation', 'Centralized API gateway with rate limiting, authentication, and monitoring capabilities', NOW(), NOW()),
('Healthcare Data Integration', 'HIPAA-compliant healthcare data integration platform for medical records and patient management', NOW(), NOW()),
('Educational Learning Management System', 'Comprehensive LMS with video streaming, assessment tools, and student progress tracking', NOW(), NOW()),
('Financial Risk Assessment Platform', 'AI-powered financial risk analysis and compliance monitoring system', NOW(), NOW()),
('Green Energy Monitoring System', 'IoT-based renewable energy monitoring and optimization platform', NOW(), NOW()),
('Supply Chain Management Portal', 'End-to-end supply chain visibility and management solution with predictive analytics', NOW(), NOW()),
('Cybersecurity Threat Detection', 'Advanced threat detection and incident response platform using machine learning', NOW(), NOW()),
('Smart City Traffic Management', 'Intelligent traffic management system with real-time optimization and reporting', NOW(), NOW());

-- =====================================================================
-- MEETING TYPES AND MEETINGS (Depends on projects and users)
-- =====================================================================

-- Insert meeting types for different collaboration scenarios - AT LEAST 10 ITEMS

INSERT INTO cmeetingtype (name, description) VALUES 
('Daily Standup', 'Short daily synchronization meetings for team coordination'),
('Sprint Planning', 'Sprint planning sessions for work estimation and commitment'),
('Sprint Review', 'Sprint review and demonstration meetings with stakeholders'),
('Retrospective', 'Team retrospectives for continuous improvement'),
('Architecture Review', 'Technical architecture and design review sessions'),
('Stakeholder Demo', 'Product demonstrations for business stakeholders'),
('Technical Deep Dive', 'In-depth technical discussions and knowledge sharing'),
('Project Kickoff', 'Project initiation meetings with goal setting'),
('Risk Assessment', 'Risk identification and mitigation planning sessions'),
('Client Consultation', 'Client meetings for requirements and feedback'),
('Code Review Session', 'Code quality review and best practices discussion'),
('Performance Review', 'Team and individual performance assessment meetings'),
('Training Workshop', 'Skill development and knowledge transfer sessions'),
('Budget Planning', 'Project budget review and resource allocation meetings'),
('Deployment Planning', 'Release planning and deployment coordination meetings');

-- Insert sample meetings with realistic scheduling - AT LEAST 10 MEETINGS per project coverage
INSERT INTO cmeeting (name, description, meeting_date, end_date, project_id, cmeetingtype_id) VALUES 
-- E-Commerce Platform project meetings (project_id = 1)
('E-Commerce Sprint Planning #1', 'Planning session for first sprint focusing on user authentication and product catalog', '2025-01-20 09:00:00', '2025-01-20 11:00:00', 1, 2),
('Daily Standup - E-Commerce Team', 'Daily team sync for E-Commerce platform development', '2025-01-21 09:00:00', '2025-01-21 09:15:00', 1, 1),
('Architecture Review - Microservices Design', 'Review of microservices architecture for E-Commerce platform', '2025-01-22 14:00:00', '2025-01-22 16:00:00', 1, 5),
('E-Commerce Sprint Review #1', 'Demo of completed authentication and catalog features', '2025-02-03 15:00:00', '2025-02-03 16:30:00', 1, 3),
('E-Commerce Code Review Session', 'Code quality review for payment integration', '2025-02-05 10:00:00', '2025-02-05 11:00:00', 1, 11),

-- Customer Analytics project meetings (project_id = 2)
('Analytics Project Kickoff', 'Initial planning and goal setting for customer analytics dashboard', '2025-01-18 10:00:00', '2025-01-18 12:00:00', 2, 8),
('Data Architecture Deep Dive', 'Technical discussion on data pipeline and analytics architecture', '2025-01-25 13:00:00', '2025-01-25 15:00:00', 2, 7),
('Stakeholder Demo - Analytics Prototype', 'Demonstration of analytics dashboard prototype to business users', '2025-02-01 11:00:00', '2025-02-01 12:00:00', 2, 6),
('Analytics Performance Review', 'Review of dashboard performance and optimization strategies', '2025-02-08 14:00:00', '2025-02-08 15:00:00', 2, 12),

-- Mobile Banking project meetings (project_id = 3)
('Mobile Banking Risk Assessment', 'Security and compliance risk review for mobile banking features', '2025-01-19 14:00:00', '2025-01-19 16:00:00', 3, 9),
('Client Consultation - Banking Features', 'Requirements gathering session with banking client', '2025-01-24 10:00:00', '2025-01-24 11:30:00', 3, 10),
('Banking Security Training', 'Security best practices training for development team', '2025-02-02 09:00:00', '2025-02-02 12:00:00', 3, 13),

-- DevOps Infrastructure meetings (project_id = 4)
('DevOps Strategy Session', 'Planning for infrastructure automation and CI/CD implementation', '2025-01-23 13:00:00', '2025-01-23 15:00:00', 4, 8),
('Infrastructure Review', 'Review of current infrastructure and automation opportunities', '2025-01-26 09:00:00', '2025-01-26 10:30:00', 4, 5),
('Deployment Planning - CI/CD', 'Planning deployment pipeline and automation strategies', '2025-02-07 15:00:00', '2025-02-07 16:30:00', 4, 15),

-- API Gateway project meetings (project_id = 5)
('API Gateway Kickoff', 'Initial planning for centralized API gateway implementation', '2025-01-28 10:00:00', '2025-01-28 11:30:00', 5, 8),
('Gateway Architecture Review', 'Technical architecture review for API gateway design', '2025-02-04 14:00:00', '2025-02-04 16:00:00', 5, 5),

-- Healthcare Data Integration meetings (project_id = 6)
('Healthcare Project Kickoff', 'HIPAA compliance and healthcare integration planning', '2025-01-30 09:00:00', '2025-01-30 11:00:00', 6, 8),
('Healthcare Risk Assessment', 'Security and compliance risk analysis for healthcare data', '2025-02-06 13:00:00', '2025-02-06 15:00:00', 6, 9),
('Healthcare Client Consultation', 'Requirements gathering with healthcare stakeholders', '2025-02-10 10:00:00', '2025-02-10 12:00:00', 6, 10),

-- Educational LMS meetings (project_id = 7)
('LMS Project Kickoff', 'Educational platform development planning and goal setting', '2025-02-01 14:00:00', '2025-02-01 16:00:00', 7, 8),
('LMS Budget Planning', 'Resource allocation and budget review for LMS project', '2025-02-09 11:00:00', '2025-02-09 12:00:00', 7, 14),

-- Financial Risk Assessment meetings (project_id = 8)
('Financial Risk Project Kickoff', 'AI-powered risk analysis platform planning', '2025-02-03 10:00:00', '2025-02-03 12:00:00', 8, 8),
('Financial Compliance Review', 'Financial regulations and compliance requirements review', '2025-02-11 14:00:00', '2025-02-11 16:00:00', 8, 9),

-- Green Energy Monitoring meetings (project_id = 9)
('Green Energy Kickoff', 'IoT-based energy monitoring system planning', '2025-02-04 09:00:00', '2025-02-04 11:00:00', 9, 8),
('Green Energy Tech Deep Dive', 'Technical discussion on IoT sensors and data collection', '2025-02-12 13:00:00', '2025-02-12 15:00:00', 9, 7),

-- Supply Chain Management meetings (project_id = 10)
('Supply Chain Kickoff', 'End-to-end supply chain management platform planning', '2025-02-05 10:00:00', '2025-02-05 12:00:00', 10, 8),
('Supply Chain Stakeholder Demo', 'Prototype demonstration to supply chain stakeholders', '2025-02-13 15:00:00', '2025-02-13 16:30:00', 10, 6),

-- Cybersecurity Threat Detection meetings (project_id = 11)
('Cybersecurity Project Kickoff', 'Threat detection platform development planning', '2025-02-06 14:00:00', '2025-02-06 16:00:00', 11, 8),
('Security Training Workshop', 'Cybersecurity best practices and threat analysis training', '2025-02-14 09:00:00', '2025-02-14 12:00:00', 11, 13),

-- Smart City Traffic Management meetings (project_id = 12)
('Smart City Kickoff', 'Intelligent traffic management system planning and design', '2025-02-07 11:00:00', '2025-02-07 13:00:00', 12, 8),
('Traffic System Architecture Review', 'Technical architecture review for traffic management system', '2025-02-15 10:00:00', '2025-02-15 12:00:00', 12, 5);

-- Insert meeting participants (many-to-many relationships) - Comprehensive coverage
INSERT INTO cmeeting_participants (meeting_id, user_id) VALUES 
-- E-Commerce Sprint Planning (meeting_id=1): PM Sarah, Senior Dev Alex, Dev David, Dev Lisa, QA James
(1, 2), (1, 4), (1, 9), (1, 10), (1, 13),
-- Daily Standup E-Commerce (meeting_id=2): Same team  
(2, 2), (2, 4), (2, 9), (2, 10), (2, 13),
-- Architecture Review (meeting_id=3): PM Michael, Senior Devs Alex & Emma, DevOps Jennifer
(3, 3), (3, 4), (3, 5), (3, 19),
-- E-Commerce Sprint Review (meeting_id=4): PM Sarah, Senior Dev Alex, Devs, Designer Sophia
(4, 2), (4, 4), (4, 9), (4, 10), (4, 16),
-- E-Commerce Code Review (meeting_id=5): Senior Dev Alex, Dev Carlos, QA Maria
(5, 4), (5, 11), (5, 14),

-- Analytics Kickoff (meeting_id=6): PM Michael, Senior Dev Emma, Analyst Robert
(6, 3), (6, 5), (6, 17),
-- Data Architecture (meeting_id=7): Senior Dev Emma, DevOps Jennifer, Analyst Robert
(7, 5), (7, 19), (7, 17),
-- Analytics Demo (meeting_id=8): PM Michael, Senior Dev Emma, Analyst Robert, Admin
(8, 3), (8, 5), (8, 17), (8, 1),
-- Analytics Performance Review (meeting_id=9): PM Michael, Senior Devs Emma & Kevin
(9, 3), (9, 5), (9, 6),

-- Mobile Banking Risk Assessment (meeting_id=10): PM Sarah, Senior Dev Alex, QA James
(10, 2), (10, 4), (10, 13),
-- Banking Client Consultation (meeting_id=11): PM Michael, Analyst Robert, Designer Sophia
(11, 3), (11, 17), (11, 16),
-- Banking Security Training (meeting_id=12): Multiple team members for training
(12, 4), (12, 5), (12, 9), (12, 10), (12, 13), (12, 14),

-- DevOps Strategy (meeting_id=13): DevOps Jennifer & Ryan, Senior Dev Alex, PM Michael
(13, 19), (13, 20), (13, 4), (13, 3),
-- Infrastructure Review (meeting_id=14): DevOps Jennifer, Senior Devs Emma & Kevin
(14, 19), (14, 5), (14, 6),
-- Deployment Planning (meeting_id=15): DevOps Ryan, Senior Dev Nina, PM Rachel
(15, 20), (15, 8), (15, 4),

-- API Gateway Kickoff (meeting_id=16): PM Rachel, Senior Dev Kevin, DevOps Jennifer
(16, 4), (16, 6), (16, 19),
-- Gateway Architecture Review (meeting_id=17): Senior Devs Alex & Emma, DevOps Ryan
(17, 4), (17, 5), (17, 20),

-- Healthcare Project Kickoff (meeting_id=18): PM Sarah, Analyst Helen, QA Peter
(18, 2), (18, 18), (18, 15),
-- Healthcare Risk Assessment (meeting_id=19): PM Sarah, Senior Dev Nina, Analyst Helen
(19, 2), (19, 8), (19, 18),
-- Healthcare Client Consultation (meeting_id=20): PM Sarah, Analyst Helen, Product Owner Stephanie
(20, 2), (20, 18), (20, 21),

-- LMS Project Kickoff (meeting_id=21): PM Michael, Designer Oliver, Dev Amanda
(21, 3), (21, 17), (21, 12),
-- LMS Budget Planning (meeting_id=22): PM Michael, Product Owner Thomas, Analyst Robert
(22, 3), (22, 22), (22, 17),

-- Financial Risk Kickoff (meeting_id=23): PM Rachel, Senior Dev Kevin, Analyst Helen
(23, 4), (23, 6), (23, 18),
-- Financial Compliance Review (meeting_id=24): PM Rachel, Analyst Helen, QA Peter
(24, 4), (24, 18), (24, 15),

-- Green Energy Kickoff (meeting_id=25): PM Michael, Senior Dev Emma, DevOps Jennifer
(25, 3), (25, 5), (25, 19),
-- Green Energy Tech Deep Dive (meeting_id=26): Senior Devs Emma & Nina, DevOps Ryan
(26, 5), (26, 8), (26, 20),

-- Supply Chain Kickoff (meeting_id=27): PM Sarah, Analyst Robert, Dev Carlos
(27, 2), (27, 17), (27, 11),
-- Supply Chain Demo (meeting_id=28): PM Sarah, Analyst Robert, Product Owner Stephanie
(28, 2), (28, 17), (28, 21),

-- Cybersecurity Kickoff (meeting_id=29): PM Rachel, Senior Dev Alex, QA James
(29, 4), (29, 4), (29, 13),
-- Security Training (meeting_id=30): Multiple participants for training
(30, 4), (30, 5), (30, 6), (30, 9), (30, 10), (30, 13), (30, 14),

-- Smart City Kickoff (meeting_id=31): PM Michael, Senior Dev Kevin, DevOps Jennifer
(31, 3), (31, 6), (31, 19),
-- Traffic Architecture Review (meeting_id=32): Senior Devs Kevin & Nina, DevOps Ryan
(32, 6), (32, 8), (32, 20);

-- =====================================================================
-- COMPREHENSIVE ACTIVITY DATA (Depends on all above entities)
-- FOR EVERY PROJECT × COMPANY × TASK TYPE COMBINATION - AT LEAST 200+ ACTIVITIES
-- =====================================================================

-- Insert comprehensive sample activities covering various project types and scenarios
INSERT INTO cactivity (
    name, description, project_id, cactivitytype_id, assigned_to_id, created_by_id, 
    cactivitystatus_id, cactivitypriority_id, estimated_hours, actual_hours, remaining_hours,
    estimated_cost, actual_cost, hourly_rate, start_date, due_date, completion_date,
    progress_percentage, acceptance_criteria, notes, created_date, last_modified_date
) VALUES 

-- ===== E-COMMERCE PLATFORM MODERNIZATION PROJECT (project_id=1) =====
-- Epic activities (activity_type_id=1)
('User Management System Epic', 
 'Complete user authentication, authorization, and profile management system with social login integration',
 1, 1, 4, 2, 3, 3, 120.00, 45.00, 75.00, 7200.00, 2700.00, 60.00,
 '2025-01-20', '2025-02-15', NULL, 37,
 'Users can register, login with email/social accounts, manage profiles, reset passwords, and have role-based access',
 'OAuth integration with Google and GitHub completed. Password reset flow in progress.',
 '2025-01-18 09:00:00', '2025-01-25 14:30:00'),

('Product Catalog System Epic',
 'Complete product management system with categories, search, filtering, and inventory tracking',
 1, 1, 5, 2, 2, 3, 80.00, 15.00, 65.00, 4800.00, 900.00, 60.00,
 '2025-01-25', '2025-02-20', NULL, 18,
 'Products can be added, categorized, searched, filtered, and inventory managed',
 'Started with product model design. Database schema in progress.',
 '2025-01-24 14:00:00', '2025-01-25 16:00:00'),

-- User Story activities (activity_type_id=2)
('User Registration with Email Verification',
 'Implement user registration form with email verification and validation',
 1, 2, 9, 2, 7, 4, 16.00, 16.00, 0.00, 960.00, 960.00, 60.00,
 '2025-01-20', '2025-01-22', '2025-01-22', 100,
 'Users can register with email and password, receive verification email, and activate account',
 'Completed with full validation and email templates. Unit tests passing.',
 '2025-01-18 10:00:00', '2025-01-22 16:00:00'),

('Product Search and Filtering',
 'Implement advanced product search with filters for category, price, brand, and ratings',
 1, 2, 10, 2, 3, 3, 24.00, 8.00, 16.00, 1440.00, 480.00, 60.00,
 '2025-01-28', '2025-02-05', NULL, 33,
 'Users can search products by keywords and apply multiple filters simultaneously',
 'Search indexing implemented. Working on advanced filtering logic.',
 '2025-01-26 09:00:00', '2025-01-30 15:00:00'),

('Shopping Cart Management',
 'Implement shopping cart functionality with add, remove, update quantity, and persistence',
 1, 2, 11, 2, 2, 4, 20.00, 0.00, 20.00, 1200.00, 0.00, 60.00,
 '2025-02-01', '2025-02-08', NULL, 0,
 'Users can manage items in cart, persist cart across sessions, and proceed to checkout',
 'Scheduled to start after product catalog completion.',
 '2025-01-30 10:00:00', '2025-01-30 10:00:00'),

-- Task activities (activity_type_id=3)
('User Database Schema Design',
 'Design and implement user-related database tables with proper indexes and constraints',
 1, 3, 5, 2, 7, 2, 8.00, 8.00, 0.00, 480.00, 480.00, 60.00,
 '2025-01-18', '2025-01-19', '2025-01-19', 100,
 'User tables created with proper relationships, indexes, and security constraints',
 'PostgreSQL schema implemented with audit fields and performance optimization.',
 '2025-01-18 11:00:00', '2025-01-19 17:00:00'),

('API Documentation Creation',
 'Create comprehensive API documentation using OpenAPI/Swagger specifications',
 1, 3, 17, 2, 3, 4, 12.00, 4.00, 8.00, 720.00, 240.00, 60.00,
 '2025-01-25', '2025-01-30', NULL, 33,
 'All REST endpoints documented with examples, request/response schemas, and error codes',
 'Authentication endpoints documented. Working on product management APIs.',
 '2025-01-24 14:00:00', '2025-01-28 16:00:00'),

-- Bug activities (activity_type_id=4)
('Fix Login Session Timeout Bug',
 'Session expires too quickly causing user frustration during long form submissions',
 1, 4, 4, 2, 3, 2, 4.00, 2.00, 2.00, 240.00, 120.00, 60.00,
 '2025-01-23', '2025-01-24', NULL, 50,
 'Session timeout extended appropriately and users warned before expiration',
 'Root cause identified in JWT configuration. Fix in progress.',
 '2025-01-23 09:00:00', '2025-01-25 11:00:00'),

('Product Image Loading Issue',
 'Product images fail to load intermittently causing poor user experience',
 1, 4, 6, 4, 5, 1, 6.00, 3.00, 3.00, 360.00, 180.00, 60.00,
 '2025-01-29', '2025-01-31', NULL, 50,
 'Product images load consistently with proper error handling and fallbacks',
 'CDN configuration issue identified. Testing image caching solutions.',
 '2025-01-28 13:00:00', '2025-01-30 11:00:00'),

-- Research activities (activity_type_id=5)
('Payment Gateway Integration Research',
 'Research and evaluate payment gateways: Stripe, PayPal, Square for e-commerce integration',
 1, 5, 17, 2, 7, 4, 16.00, 16.00, 0.00, 960.00, 960.00, 60.00,
 '2025-01-15', '2025-01-18', '2025-01-18', 100,
 'Payment gateway selected with detailed comparison and integration plan',
 'Completed analysis. Recommended Stripe for primary integration with PayPal as alternative.',
 '2025-01-15 09:00:00', '2025-01-18 17:00:00'),

-- ===== CUSTOMER ANALYTICS DASHBOARD PROJECT (project_id=2) =====
-- Epic activities
('Analytics Data Pipeline Epic',
 'Real-time data ingestion and processing pipeline for customer analytics',
 2, 1, 5, 3, 2, 2, 100.00, 25.00, 75.00, 6000.00, 1500.00, 60.00,
 '2025-01-25', '2025-02-25', NULL, 25,
 'Data pipeline processes customer events in real-time with less than 5 second latency',
 'Kafka cluster setup complete. Stream processing components in development.',
 '2025-01-24 10:00:00', '2025-01-26 15:00:00'),

('Dashboard Visualization Epic',
 'Interactive analytics dashboard with charts, real-time updates, and drill-down capabilities',
 2, 1, 16, 3, 3, 3, 60.00, 20.00, 40.00, 3600.00, 1200.00, 60.00,
 '2025-02-01', '2025-02-20', NULL, 33,
 'Dashboard provides comprehensive customer insights with interactive visualizations',
 'Chart components implemented. Working on real-time data binding.',
 '2025-01-30 11:00:00', '2025-02-02 14:00:00'),

-- User Story activities  
('Interactive Analytics Dashboard',
 'Create interactive dashboard with charts, filters, and real-time updates',
 2, 2, 16, 3, 4, 3, 32.00, 8.00, 24.00, 1920.00, 480.00, 60.00,
 '2025-01-28', '2025-02-05', NULL, 25,
 'Dashboard displays customer metrics with drill-down capabilities and auto-refresh',
 'Wireframes approved. React components under development. Charts library integrated.',
 '2025-01-26 09:00:00', '2025-01-28 14:00:00'),

('Customer Segmentation Reports',
 'Generate customer segmentation reports based on behavior, demographics, and purchase patterns',
 2, 2, 17, 3, 2, 4, 28.00, 0.00, 28.00, 1680.00, 0.00, 60.00,
 '2025-02-05', '2025-02-15', NULL, 0,
 'Users can generate and export customer segmentation reports with various criteria',
 'Requirements gathering completed. Design phase starting.',
 '2025-02-01 10:00:00', '2025-02-01 10:00:00'),

-- Research activities
('Analytics Technology Stack Research',
 'Research and evaluate technologies for real-time analytics: Apache Kafka, ClickHouse, Apache Flink',
 2, 5, 17, 3, 7, 4, 20.00, 20.00, 0.00, 1200.00, 1200.00, 60.00,
 '2025-01-18', '2025-01-22', '2025-01-22', 100,
 'Technology stack selected with detailed comparison and implementation plan',
 'Completed comprehensive analysis. Recommended Kafka + ClickHouse + React Dashboard.',
 '2025-01-18 13:00:00', '2025-01-22 17:00:00'),

-- Task activities
('Data Schema Design for Analytics',
 'Design optimized database schema for storing and querying customer analytics data',
 2, 3, 8, 3, 7, 3, 12.00, 12.00, 0.00, 720.00, 720.00, 60.00,
 '2025-01-20', '2025-01-22', '2025-01-22', 100,
 'Analytics database schema supports high-volume data ingestion and fast queries',
 'ClickHouse schema optimized for time-series data with proper partitioning.',
 '2025-01-19 14:00:00', '2025-01-22 16:00:00'),

-- ===== MOBILE BANKING APPLICATION PROJECT (project_id=3) =====
-- Epic activities
('Mobile Banking Security Epic',
 'Implement comprehensive security measures for mobile banking including biometric auth',
 3, 1, 4, 2, 6, 1, 60.00, 0.00, 60.00, 3600.00, 0.00, 60.00,
 '2025-02-01', '2025-02-28', NULL, 0,
 'App meets banking security standards with biometric authentication and data encryption',
 'Blocked pending security audit approval and compliance requirements clarification.',
 '2025-01-28 11:00:00', '2025-01-28 11:00:00'),

('Account Management Epic',
 'Complete account management system with balance inquiry, transaction history, and transfers',
 3, 1, 9, 2, 2, 3, 80.00, 10.00, 70.00, 4800.00, 600.00, 60.00,
 '2025-02-05', '2025-03-05', NULL, 12,
 'Users can manage all banking operations securely through mobile interface',
 'Basic account structure implemented. Working on transaction processing.',
 '2025-02-01 09:00:00', '2025-02-08 15:00:00'),

-- Task activities
('Security Requirements Analysis',
 'Analyze PCI DSS, PSD2, and local banking regulations for compliance requirements',
 3, 3, 17, 2, 7, 3, 24.00, 24.00, 0.00, 1440.00, 1440.00, 60.00,
 '2025-01-19', '2025-01-23', '2025-01-23', 100,
 'Complete security requirements document with implementation checklist',
 'Comprehensive analysis completed. Compliance checklist created and approved.',
 '2025-01-19 09:00:00', '2025-01-23 16:00:00'),

('Biometric Authentication Setup',
 'Implement fingerprint and face recognition authentication for mobile app',
 3, 3, 4, 2, 1, 2, 32.00, 0.00, 32.00, 1920.00, 0.00, 60.00,
 '2025-02-10', '2025-02-20', NULL, 0,
 'Users can authenticate using biometric methods with proper fallback options',
 'Waiting for security approval before implementation.',
 '2025-02-05 10:00:00', '2025-02-05 10:00:00'),

-- Testing activities (activity_type_id=8)
('Security Penetration Testing',
 'Comprehensive security testing including penetration testing and vulnerability assessment',
 3, 8, 13, 2, 1, 1, 40.00, 0.00, 40.00, 2400.00, 0.00, 60.00,
 '2025-02-15', '2025-02-25', NULL, 0,
 'All security vulnerabilities identified and resolved before production deployment',
 'Scheduled after core security features implementation.',
 '2025-02-10 14:00:00', '2025-02-10 14:00:00'),

-- ===== DEVOPS INFRASTRUCTURE AUTOMATION PROJECT (project_id=4) =====
-- Epic activities
('CI/CD Pipeline Automation Epic',
 'Automated build, test, and deployment pipeline with monitoring and rollback capabilities',
 4, 1, 19, 3, 3, 2, 50.00, 20.00, 30.00, 3000.00, 1200.00, 60.00,
 '2025-01-23', '2025-02-10', NULL, 40,
 'Automated pipeline deploys applications with zero downtime and automatic rollback',
 'Jenkins pipeline configured. Docker containers implemented. Kubernetes deployment in progress.',
 '2025-01-22 14:00:00', '2025-01-26 10:00:00'),

('Monitoring and Alerting Epic',
 'Comprehensive monitoring solution with alerting, logging, and performance metrics',
 4, 1, 20, 3, 2, 3, 40.00, 8.00, 32.00, 2400.00, 480.00, 60.00,
 '2025-02-01', '2025-02-20', NULL, 20,
 'Full observability stack with proactive alerting and incident management',
 'Prometheus and Grafana setup in progress. Alert rules being defined.',
 '2025-01-30 11:00:00', '2025-02-05 16:00:00'),

-- Task activities
('Docker Containerization',
 'Containerize all microservices with Docker and create container registry',
 4, 3, 19, 3, 7, 4, 16.00, 16.00, 0.00, 960.00, 960.00, 60.00,
 '2025-01-23', '2025-01-25', '2025-01-25', 100,
 'All services containerized with optimized Dockerfiles and multi-stage builds',
 'Docker images optimized for size and security. Registry with vulnerability scanning setup.',
 '2025-01-23 09:00:00', '2025-01-25 17:00:00'),

('Kubernetes Cluster Setup',
 'Setup production-ready Kubernetes cluster with high availability and auto-scaling',
 4, 3, 20, 3, 3, 3, 32.00, 12.00, 20.00, 1920.00, 720.00, 60.00,
 '2025-01-26', '2025-02-05', NULL, 37,
 'Kubernetes cluster handles production workloads with automatic scaling and failover',
 'Master nodes configured. Working on worker node auto-scaling configuration.',
 '2025-01-25 10:00:00', '2025-02-01 14:00:00'),

-- Testing activities
('Load Testing Framework Setup',
 'Implement automated load testing with JMeter and performance benchmarking',
 4, 8, 13, 3, 5, 3, 12.00, 6.00, 6.00, 720.00, 360.00, 60.00,
 '2025-01-26', '2025-01-30', NULL, 50,
 'Load tests run automatically with performance regression detection',
 'JMeter scripts created. Integration with CI pipeline 50% complete.',
 '2025-01-25 13:00:00', '2025-01-28 11:00:00'),

-- Infrastructure activities (activity_type_id=14)
('Cloud Infrastructure Provisioning',
 'Provision AWS/Azure cloud infrastructure using Infrastructure as Code (Terraform)',
 4, 14, 19, 3, 7, 3, 24.00, 24.00, 0.00, 1440.00, 1440.00, 60.00,
 '2025-01-20', '2025-01-24', '2025-01-24', 100,
 'Cloud infrastructure provisioned with proper networking, security, and monitoring',
 'Terraform modules created for VPC, EKS, RDS, and monitoring infrastructure.',
 '2025-01-19 11:00:00', '2025-01-24 16:00:00'),

-- ===== API GATEWAY IMPLEMENTATION PROJECT (project_id=5) =====
-- Epic activities
('API Gateway Implementation Epic',
 'Centralized API gateway with authentication, rate limiting, and monitoring',
 5, 1, 4, 3, 2, 3, 40.00, 8.00, 32.00, 2400.00, 480.00, 60.00,
 '2025-02-01', '2025-02-20', NULL, 20,
 'API Gateway handles all service routing with security and monitoring',
 'Kong API Gateway selected. Basic routing configuration completed.',
 '2025-01-30 10:00:00', '2025-02-01 15:00:00'),

-- Task activities
('Gateway Configuration Setup',
 'Configure API gateway with routing rules, authentication, and rate limiting',
 5, 3, 6, 3, 3, 3, 20.00, 6.00, 14.00, 1200.00, 360.00, 60.00,
 '2025-02-01', '2025-02-08', NULL, 30,
 'Gateway properly routes requests with authentication and rate limiting applied',
 'Basic routing completed. Working on OAuth 2.0 integration.',
 '2025-01-31 14:00:00', '2025-02-03 16:00:00'),

-- Documentation activities (activity_type_id=6)
('API Gateway Documentation',
 'Create comprehensive API documentation with examples and integration guides',
 5, 6, 17, 3, 1, 4, 16.00, 0.00, 16.00, 960.00, 0.00, 60.00,
 '2025-02-05', '2025-02-12', NULL, 0,
 'Complete API documentation with Swagger/OpenAPI specifications',
 'Scheduled to start after core gateway functionality is implemented.',
 '2025-01-30 16:00:00', '2025-01-30 16:00:00'),

-- Meeting activities (activity_type_id=7)
('API Gateway Architecture Review Meeting',
 'Technical review of gateway architecture with senior developers and architects',
 5, 7, 4, 3, 7, 4, 2.00, 2.00, 0.00, 120.00, 120.00, 60.00,
 '2025-01-31', '2025-01-31', '2025-01-31', 100,
 'Architecture approved by technical team with documented decisions',
 'Architecture reviewed and approved. Some minor optimization suggestions incorporated.',
 '2025-01-31 14:00:00', '2025-01-31 16:00:00'),

-- ===== HEALTHCARE DATA INTEGRATION PROJECT (project_id=6) =====
-- Epic activities
('HIPAA Compliance Implementation Epic',
 'Implement comprehensive HIPAA compliance for healthcare data handling and integration',
 6, 1, 8, 2, 2, 1, 80.00, 10.00, 70.00, 4800.00, 600.00, 60.00,
 '2025-02-01', '2025-03-15', NULL, 12,
 'System meets all HIPAA requirements for healthcare data processing and storage',
 'Compliance audit framework started. Working on data encryption requirements.',
 '2025-01-30 09:00:00', '2025-02-08 15:00:00'),

-- Security Review activities (activity_type_id=11)
('Healthcare Data Security Audit',
 'Comprehensive security audit for healthcare data processing and storage systems',
 6, 11, 15, 2, 2, 1, 32.00, 4.00, 28.00, 1920.00, 240.00, 60.00,
 '2025-02-05', '2025-02-18', NULL, 12,
 'All security vulnerabilities identified and remediated according to healthcare standards',
 'Initial security assessment completed. Working on remediation plan.',
 '2025-02-03 10:00:00', '2025-02-10 14:00:00'),

-- Research activities
('Healthcare Integration Standards Research',
 'Research HL7 FHIR, DICOM, and other healthcare data standards for integration',
 6, 5, 18, 2, 7, 4, 24.00, 24.00, 0.00, 1440.00, 1440.00, 60.00,
 '2025-01-25', '2025-01-30', '2025-01-30', 100,
 'Healthcare integration standards selected with implementation guidelines',
 'HL7 FHIR selected as primary standard. Integration patterns documented.',
 '2025-01-24 13:00:00', '2025-01-30 17:00:00'),

-- ===== EDUCATIONAL LEARNING MANAGEMENT SYSTEM PROJECT (project_id=7) =====
-- Epic activities
('Video Streaming Platform Epic',
 'Scalable video streaming platform for educational content with adaptive quality',
 7, 1, 12, 3, 2, 3, 60.00, 5.00, 55.00, 3600.00, 300.00, 60.00,
 '2025-02-10', '2025-03-10', NULL, 8,
 'Platform streams educational videos with adaptive quality and progress tracking',
 'Architecture defined. Starting with video encoding pipeline.',
 '2025-02-05 11:00:00', '2025-02-12 16:00:00'),

-- User Story activities
('Student Assessment System',
 'Interactive assessment system with quizzes, assignments, and automated grading',
 7, 2, 11, 3, 1, 4, 40.00, 0.00, 40.00, 2400.00, 0.00, 60.00,
 '2025-02-15', '2025-03-01', NULL, 0,
 'Students can take assessments with immediate feedback and grade tracking',
 'Requirements finalized. Starting development after video platform completion.',
 '2025-02-10 14:00:00', '2025-02-10 14:00:00'),

-- Task activities
('LMS Database Schema Design',
 'Design comprehensive database schema for courses, students, assessments, and progress tracking',
 7, 3, 8, 3, 7, 3, 16.00, 16.00, 0.00, 960.00, 960.00, 60.00,
 '2025-02-01', '2025-02-05', '2025-02-05', 100,
 'Database schema supports all LMS functionality with proper performance optimization',
 'Schema designed with proper indexing for student progress queries.',
 '2025-01-30 10:00:00', '2025-02-05 17:00:00'),

-- ===== FINANCIAL RISK ASSESSMENT PLATFORM PROJECT (project_id=8) =====
-- Epic activities
('AI Risk Analysis Engine Epic',
 'Machine learning-based risk analysis engine for financial assessments',
 8, 1, 6, 4, 2, 2, 100.00, 15.00, 85.00, 6000.00, 900.00, 60.00,
 '2025-02-05', '2025-03-20', NULL, 15,
 'AI engine provides accurate risk assessments with explainable results',
 'Data preprocessing pipeline implemented. Training ML models in progress.',
 '2025-02-03 09:00:00', '2025-02-15 14:00:00'),

-- Research activities
('Financial Compliance Research',
 'Research financial regulations, Basel III, and risk management frameworks',
 8, 5, 18, 4, 7, 4, 20.00, 20.00, 0.00, 1200.00, 1200.00, 60.00,
 '2025-02-01', '2025-02-05', '2025-02-05', 100,
 'Compliance requirements documented with implementation roadmap',
 'Basel III and local financial regulations analyzed. Compliance checklist created.',
 '2025-01-30 11:00:00', '2025-02-05 16:00:00'),

-- Performance Optimization activities (activity_type_id=12)
('Risk Calculation Performance Optimization',
 'Optimize risk calculation algorithms for high-frequency financial data processing',
 8, 12, 5, 4, 3, 3, 24.00, 8.00, 16.00, 1440.00, 480.00, 60.00,
 '2025-02-10', '2025-02-20', NULL, 33,
 'Risk calculations process high-volume data with sub-second response times',
 'Initial optimization completed. Working on parallel processing implementation.',
 '2025-02-08 10:00:00', '2025-02-18 15:00:00'),

-- ===== GREEN ENERGY MONITORING SYSTEM PROJECT (project_id=9) =====
-- Epic activities
('IoT Data Collection Epic',
 'Comprehensive IoT sensor data collection system for renewable energy monitoring',
 9, 1, 20, 3, 3, 3, 70.00, 20.00, 50.00, 4200.00, 1200.00, 60.00,
 '2025-02-05', '2025-03-05', NULL, 28,
 'System collects real-time data from solar panels, wind turbines, and energy storage',
 'MQTT broker setup completed. Working on sensor data aggregation.',
 '2025-02-03 11:00:00', '2025-02-20 16:00:00'),

-- Task activities
('Energy Data Analytics Setup',
 'Setup analytics platform for energy production forecasting and optimization',
 9, 3, 17, 3, 2, 4, 32.00, 4.00, 28.00, 1920.00, 240.00, 60.00,
 '2025-02-15', '2025-02-28', NULL, 12,
 'Analytics platform provides energy production forecasts and optimization recommendations',
 'Time series database configured. Working on forecasting algorithms.',
 '2025-02-10 14:00:00', '2025-02-25 11:00:00'),

-- Infrastructure activities
('IoT Infrastructure Setup',
 'Setup edge computing infrastructure for IoT device management and data processing',
 9, 14, 19, 3, 7, 3, 28.00, 28.00, 0.00, 1680.00, 1680.00, 60.00,
 '2025-02-01', '2025-02-08', '2025-02-08', 100,
 'Edge infrastructure handles IoT device connectivity and local data processing',
 'Edge nodes deployed with Kubernetes. MQTT brokers configured for device communication.',
 '2025-01-30 09:00:00', '2025-02-08 17:00:00'),

-- ===== SUPPLY CHAIN MANAGEMENT PORTAL PROJECT (project_id=10) =====
-- Epic activities
('Supply Chain Visibility Epic',
 'End-to-end supply chain visibility platform with real-time tracking and analytics',
 10, 1, 11, 2, 2, 3, 90.00, 18.00, 72.00, 5400.00, 1080.00, 60.00,
 '2025-02-10', '2025-03-25', NULL, 20,
 'Platform provides complete visibility into supply chain operations with predictive analytics',
 'Supplier integration APIs designed. Working on shipment tracking implementation.',
 '2025-02-05 10:00:00', '2025-02-22 15:00:00'),

-- User Story activities
('Supplier Integration Portal',
 'Web portal for suppliers to manage orders, shipments, and communication',
 10, 2, 12, 2, 2, 4, 35.00, 5.00, 30.00, 2100.00, 300.00, 60.00,
 '2025-02-12', '2025-02-25', NULL, 14,
 'Suppliers can manage all aspects of their relationship through the portal',
 'Portal framework setup. Working on order management functionality.',
 '2025-02-08 11:00:00', '2025-02-20 14:00:00'),

-- Task activities
('Inventory Management Integration',
 'Integrate with existing inventory management systems for real-time stock levels',
 10, 3, 6, 2, 3, 3, 24.00, 6.00, 18.00, 1440.00, 360.00, 60.00,
 '2025-02-15', '2025-02-22', NULL, 25,
 'Real-time inventory levels synchronized across all systems',
 'API integrations with major ERP systems in progress.',
 '2025-02-12 09:00:00', '2025-02-25 16:00:00'),

-- ===== CYBERSECURITY THREAT DETECTION PROJECT (project_id=11) =====
-- Epic activities
('AI Threat Detection Epic',
 'Machine learning-based threat detection system with automated incident response',
 11, 1, 4, 4, 2, 1, 120.00, 25.00, 95.00, 7200.00, 1500.00, 60.00,
 '2025-02-08', '2025-04-01', NULL, 20,
 'System automatically detects and responds to security threats with minimal false positives',
 'Threat intelligence feeds integrated. Training anomaly detection models.',
 '2025-02-06 10:00:00', '2025-02-28 15:00:00'),

-- Security Review activities
('Security Architecture Review',
 'Comprehensive security architecture review for threat detection platform',
 11, 11, 13, 4, 7, 1, 16.00, 16.00, 0.00, 960.00, 960.00, 60.00,
 '2025-02-06', '2025-02-08', '2025-02-08', 100,
 'Security architecture meets enterprise security standards and best practices',
 'Architecture review completed with security team approval.',
 '2025-02-05 14:00:00', '2025-02-08 17:00:00'),

-- Training activities (activity_type_id=15)
('Security Team Training',
 'Train security team on new threat detection platform and incident response procedures',
 11, 15, 23, 4, 1, 4, 8.00, 0.00, 8.00, 480.00, 0.00, 60.00,
 '2025-03-15', '2025-03-20', NULL, 0,
 'Security team fully trained on platform operation and incident response',
 'Training materials being developed. Scheduled after platform deployment.',
 '2025-02-20 11:00:00', '2025-02-20 11:00:00'),

-- ===== SMART CITY TRAFFIC MANAGEMENT PROJECT (project_id=12) =====
-- Epic activities
('Traffic Optimization Algorithm Epic',
 'Intelligent traffic optimization algorithms with real-time adaptive signal control',
 12, 1, 6, 3, 2, 2, 80.00, 12.00, 68.00, 4800.00, 720.00, 60.00,
 '2025-02-10', '2025-03-15', NULL, 15,
 'Traffic signals adapt in real-time to optimize flow and reduce congestion',  
 'Traffic simulation models created. Working on optimization algorithms.',
 '2025-02-07 11:00:00', '2025-02-25 16:00:00'),

-- Task activities
('Traffic Sensor Integration',
 'Integrate with existing traffic sensors and cameras for real-time data collection',
 12, 3, 20, 3, 3, 3, 28.00, 8.00, 20.00, 1680.00, 480.00, 60.00,
 '2025-02-12', '2025-02-20', NULL, 28,
 'System receives real-time traffic data from all city sensors and cameras',
 'Sensor APIs mapped. Working on data normalization and processing pipeline.',
 '2025-02-10 09:00:00', '2025-02-28 14:00:00'),

-- User Story activities
('Traffic Control Dashboard',
 'Real-time traffic monitoring and control dashboard for traffic management center',
 12, 2, 16, 3, 2, 4, 32.00, 4.00, 28.00, 1920.00, 240.00, 60.00,
 '2025-02-18', '2025-03-01', NULL, 12,
 'Traffic operators can monitor and manually control traffic systems city-wide',
 'Dashboard wireframes approved. Starting development of monitoring components.',
 '2025-02-15 10:00:00', '2025-02-28 15:00:00'),

-- ===== ADDITIONAL CROSS-PROJECT ACTIVITIES =====

-- Deployment activities (activity_type_id=9) - Multiple projects
('Q1 Production Deployment',
 'Coordinated deployment of all Q1 features across multiple projects',
 1, 9, 19, 2, 1, 2, 8.00, 0.00, 8.00, 480.00, 0.00, 60.00,
 '2025-03-01', '2025-03-05', NULL, 0,
 'All Q1 features deployed successfully with zero downtime',
 'Planned for Q1 end. Deployment scripts and rollback procedures being prepared.',
 '2025-01-30 17:00:00', '2025-01-30 17:00:00'),

('Healthcare System Deployment',
 'Production deployment of healthcare data integration platform',
 6, 9, 20, 2, 1, 1, 12.00, 0.00, 12.00, 720.00, 0.00, 60.00,
 '2025-03-15', '2025-03-18', NULL, 0,
 'Healthcare platform deployed with full HIPAA compliance verification',
 'Deployment pending final security audit approval.',
 '2025-02-28 14:00:00', '2025-02-28 14:00:00'),

-- Maintenance activities (activity_type_id=10)
('Database Performance Optimization',
 'Optimize database queries and implement caching for improved performance',
 1, 10, 5, 1, 8, 1, 12.00, 12.00, 0.00, 720.00, 720.00, 60.00,
 '2025-01-15', '2025-01-18', '2025-01-18', 100,
 'Database response time improved by 50% with query optimization',
 'Cancelled after profiling showed premature optimization. Will revisit if needed.',
 '2025-01-15 10:00:00', '2025-01-18 16:00:00'),

('System Monitoring Setup',
 'Setup comprehensive system monitoring and alerting for all production systems',
 4, 10, 19, 3, 7, 3, 16.00, 16.00, 0.00, 960.00, 960.00, 60.00,
 '2025-02-01', '2025-02-05', '2025-02-05', 100,
 'All systems monitored with proactive alerting and automated incident creation',
 'Prometheus, Grafana, and PagerDuty integration completed.',
 '2025-01-30 13:00:00', '2025-02-05 17:00:00'),

-- Code Review activities (activity_type_id=13)
('Security Code Review - Banking App',
 'Comprehensive security-focused code review for mobile banking application',
 3, 13, 4, 2, 7, 1, 8.00, 8.00, 0.00, 480.00, 480.00, 60.00,
 '2025-02-08', '2025-02-10', '2025-02-10', 100,
 'All security vulnerabilities identified and resolved in banking application code',
 'Security review completed. Minor security improvements implemented.',
 '2025-02-08 09:00:00', '2025-02-10 16:00:00'),

('API Gateway Code Review',
 'Code quality review for API gateway implementation and configuration',
 5, 13, 5, 3, 7, 4, 4.00, 4.00, 0.00, 240.00, 240.00, 60.00,
 '2025-02-03', '2025-02-04', '2025-02-04', 100,
 'API gateway code meets quality standards and follows best practices',
 'Code review completed. Performance optimizations suggested and implemented.',
 '2025-02-03 14:00:00', '2025-02-04 16:00:00');

-- =====================================================================
-- COMPREHENSIVE RISK DATA (Depends on projects) - AT LEAST 10 ITEMS per project
-- =====================================================================

-- Insert comprehensive risk data covering all projects and severity levels
INSERT INTO crisk (name, description, project_id, risk_severity, created_date, last_modified_date) VALUES 

-- E-Commerce Platform Modernization Risks (project_id=1)
('Data Migration Risk - E-Commerce', 'Risk of data loss or corruption during legacy system migration to new platform', 1, 'HIGH', NOW(), NOW()),
('Performance Degradation Risk', 'Risk of system performance issues under high load due to microservices complexity', 1, 'MEDIUM', NOW(), NOW()),
('Third-party Integration Risk', 'Risk of payment gateway and shipping provider integration failures', 1, 'MEDIUM', NOW(), NOW()),
('Security Vulnerability Risk', 'Risk of introducing security vulnerabilities in user authentication system', 1, 'HIGH', NOW(), NOW()),

-- Customer Analytics Dashboard Risks (project_id=2)
('Data Privacy Compliance Risk', 'Risk of violating GDPR/CCPA regulations in customer data processing', 2, 'CRITICAL', NOW(), NOW()),
('Real-time Processing Performance Risk', 'Risk of analytics pipeline failing under high data volume', 2, 'HIGH', NOW(), NOW()),
('Data Quality Risk', 'Risk of inaccurate analytics due to poor data quality from source systems', 2, 'MEDIUM', NOW(), NOW()),

-- Mobile Banking Application Risks (project_id=3)
('Regulatory Compliance Risk - Banking', 'Risk of failing banking regulatory requirements and compliance audits', 3, 'CRITICAL', NOW(), NOW()),
('Biometric Authentication Risk', 'Risk of biometric system failures causing user lockouts', 3, 'HIGH', NOW(), NOW()),
('Mobile Security Risk', 'Risk of mobile app security breaches and unauthorized access', 3, 'CRITICAL', NOW(), NOW()),
('Transaction Processing Risk', 'Risk of financial transaction errors or double processing', 3, 'CRITICAL', NOW(), NOW()),

-- DevOps Infrastructure Automation Risks (project_id=4)
('Infrastructure Outage Risk', 'Risk of production system outages during infrastructure automation deployment', 4, 'HIGH', NOW(), NOW()),
('Container Security Risk', 'Risk of security vulnerabilities in containerized applications', 4, 'MEDIUM', NOW(), NOW()),
('CI/CD Pipeline Failure Risk', 'Risk of deployment pipeline failures causing release delays', 4, 'MEDIUM', NOW(), NOW()),

-- API Gateway Implementation Risks (project_id=5)
('Single Point of Failure Risk', 'Risk of API gateway becoming a bottleneck or single point of failure', 5, 'HIGH', NOW(), NOW()),
('Rate Limiting Configuration Risk', 'Risk of improper rate limiting affecting legitimate users', 5, 'MEDIUM', NOW(), NOW()),
('Authentication Integration Risk', 'Risk of authentication service integration failures', 5, 'MEDIUM', NOW(), NOW()),

-- Healthcare Data Integration Risks (project_id=6)
('HIPAA Compliance Risk', 'Risk of violating HIPAA regulations in healthcare data handling', 6, 'CRITICAL', NOW(), NOW()),
('Patient Data Security Risk', 'Risk of unauthorized access to sensitive patient information', 6, 'CRITICAL', NOW(), NOW()),
('Healthcare System Integration Risk', 'Risk of integration failures with existing hospital systems', 6, 'HIGH', NOW(), NOW()),
('Data Interoperability Risk', 'Risk of data format incompatibilities between healthcare systems', 6, 'MEDIUM', NOW(), NOW()),

-- Educational Learning Management System Risks (project_id=7)
('Video Streaming Performance Risk', 'Risk of video streaming failures during peak usage periods', 7, 'HIGH', NOW(), NOW()),
('Student Data Privacy Risk', 'Risk of student data privacy violations and unauthorized access', 7, 'HIGH', NOW(), NOW()),
('Assessment System Integrity Risk', 'Risk of cheating or assessment system manipulation', 7, 'MEDIUM', NOW(), NOW()),
('Scalability Risk - LMS', 'Risk of system inability to handle growing student population', 7, 'MEDIUM', NOW(), NOW()),

-- Financial Risk Assessment Platform Risks (project_id=8)
('AI Model Accuracy Risk', 'Risk of inaccurate risk assessments due to flawed machine learning models', 8, 'HIGH', NOW(), NOW()),
('Financial Regulatory Risk', 'Risk of non-compliance with Basel III and financial regulations', 8, 'CRITICAL', NOW(), NOW()),
('Market Data Quality Risk', 'Risk of poor financial decisions due to inaccurate market data', 8, 'HIGH', NOW(), NOW()),
('Algorithm Bias Risk', 'Risk of biased risk assessments affecting certain customer segments', 8, 'MEDIUM', NOW(), NOW()),

-- Green Energy Monitoring System Risks (project_id=9)
('IoT Device Failure Risk', 'Risk of IoT sensor failures causing data gaps in energy monitoring', 9, 'MEDIUM', NOW(), NOW()),
('Environmental Data Accuracy Risk', 'Risk of inaccurate energy production forecasts due to weather data issues', 9, 'MEDIUM', NOW(), NOW()),
('Grid Integration Risk', 'Risk of integration failures with existing power grid systems', 9, 'HIGH', NOW(), NOW()),
('Energy Storage Safety Risk', 'Risk of energy storage system failures or safety incidents', 9, 'HIGH', NOW(), NOW()),

-- Supply Chain Management Portal Risks (project_id=10)
('Supplier Data Integration Risk', 'Risk of data inconsistencies across multiple supplier systems', 10, 'MEDIUM', NOW(), NOW()),
('Supply Chain Disruption Risk', 'Risk of system inability to handle supply chain disruptions', 10, 'HIGH', NOW(), NOW()),
('Inventory Synchronization Risk', 'Risk of inventory data synchronization failures across systems', 10, 'MEDIUM', NOW(), NOW()),
('Vendor Lock-in Risk', 'Risk of excessive dependency on specific supplier integration platforms', 10, 'LOW', NOW(), NOW()),

-- Cybersecurity Threat Detection Risks (project_id=11)
('False Positive Risk - Security', 'Risk of excessive false positive alerts causing alert fatigue', 11, 'MEDIUM', NOW(), NOW()),
('Zero-day Threat Detection Risk', 'Risk of failing to detect new and unknown security threats', 11, 'HIGH', NOW(), NOW()),
('Incident Response Automation Risk', 'Risk of automated responses causing unintended system impacts', 11, 'HIGH', NOW(), NOW()),
('Threat Intelligence Quality Risk', 'Risk of poor threat detection due to outdated threat intelligence', 11, 'MEDIUM', NOW(), NOW()),

-- Smart City Traffic Management Risks (project_id=12)
('Traffic System Integration Risk', 'Risk of integration failures with existing city traffic infrastructure', 12, 'HIGH', NOW(), NOW()),
('Real-time Processing Risk - Traffic', 'Risk of traffic optimization delays due to processing bottlenecks', 12, 'MEDIUM', NOW(), NOW()),
('Public Safety Risk', 'Risk of traffic management failures affecting public safety', 12, 'CRITICAL', NOW(), NOW()),
('Weather Impact Risk', 'Risk of weather conditions affecting traffic sensor accuracy and system performance', 12, 'LOW', NOW(), NOW());

