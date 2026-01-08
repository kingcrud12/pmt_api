-- V3: Create RBAC (Role-Based Access Control) tables and default data

-- Create role table
CREATE TABLE role (
    id CHAR(36) NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create permission table
CREATE TABLE permission (
    id CHAR(36) NOT NULL,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY unique_permission (resource, action),
    INDEX idx_resource (resource),
    INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create user_role join table
CREATE TABLE user_role (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    assigned_at DATETIME(6) NOT NULL,
    assigned_by CHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY unique_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_assigned_by FOREIGN KEY (assigned_by) REFERENCES user (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create role_permission join table
CREATE TABLE role_permission (
    id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    permission_id CHAR(36) NOT NULL,
    assigned_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permission (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create permission_audit_log table
CREATE TABLE permission_audit_log (
    id CHAR(36) NOT NULL,
    user_id CHAR(36),
    role_id CHAR(36),
    permission_id CHAR(36),
    action VARCHAR(20) NOT NULL,
    performed_by CHAR(36),
    performed_at DATETIME(6) NOT NULL,
    reason VARCHAR(500),
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_performed_at (performed_at),
    INDEX idx_action (action),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_permission FOREIGN KEY (permission_id) REFERENCES permission (id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_performed_by FOREIGN KEY (performed_by) REFERENCES user (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default roles
INSERT INTO role (id, name, description, active, created_at) VALUES
(UUID(), 'USER', 'Basic user with standard read permissions', TRUE, NOW()),
(UUID(), 'TEAM_LEAD', 'Team lead with task management and assignment permissions', TRUE, NOW()),
(UUID(), 'PROJECT_MANAGER', 'Project manager with full project and task management permissions', TRUE, NOW()),
(UUID(), 'ADMIN', 'System administrator with complete access to all resources', TRUE, NOW());

-- Insert default permissions for USER resource
INSERT INTO permission (id, resource, action, description, active) VALUES
(UUID(), 'USER', 'READ', 'View user information', TRUE),
(UUID(), 'USER', 'CREATE', 'Create new users', TRUE),
(UUID(), 'USER', 'UPDATE', 'Update user information', TRUE),
(UUID(), 'USER', 'DELETE', 'Delete users', TRUE),
(UUID(), 'USER', 'LIST', 'List all users', TRUE);

-- Insert default permissions for PROJECT resource
INSERT INTO permission (id, resource, action, description, active) VALUES
(UUID(), 'PROJECT', 'READ', 'View project information', TRUE),
(UUID(), 'PROJECT', 'CREATE', 'Create new projects', TRUE),
(UUID(), 'PROJECT', 'UPDATE', 'Update project information', TRUE),
(UUID(), 'PROJECT', 'DELETE', 'Delete projects', TRUE),
(UUID(), 'PROJECT', 'LIST', 'List all projects', TRUE),
(UUID(), 'PROJECT', 'ADMIN', 'Full project administration rights', TRUE);

-- Insert default permissions for TASK resource
INSERT INTO permission (id, resource, action, description, active) VALUES
(UUID(), 'TASK', 'READ', 'View task information', TRUE),
(UUID(), 'TASK', 'CREATE', 'Create new tasks', TRUE),
(UUID(), 'TASK', 'UPDATE', 'Update task information', TRUE),
(UUID(), 'TASK', 'DELETE', 'Delete tasks', TRUE),
(UUID(), 'TASK', 'ASSIGN', 'Assign tasks to users', TRUE),
(UUID(), 'TASK', 'LIST', 'List all tasks', TRUE);

-- Insert default permissions for ROLE resource (RBAC management)
INSERT INTO permission (id, resource, action, description, active) VALUES
(UUID(), 'ROLE', 'READ', 'View role information', TRUE),
(UUID(), 'ROLE', 'CREATE', 'Create new roles', TRUE),
(UUID(), 'ROLE', 'UPDATE', 'Update role information', TRUE),
(UUID(), 'ROLE', 'DELETE', 'Delete roles', TRUE),
(UUID(), 'ROLE', 'ASSIGN', 'Assign roles to users', TRUE);

-- Insert default permissions for PERMISSION resource (RBAC management)
INSERT INTO permission (id, resource, action, description, active) VALUES
(UUID(), 'PERMISSION', 'READ', 'View permission information', TRUE),
(UUID(), 'PERMISSION', 'GRANT', 'Grant permissions to roles', TRUE),
(UUID(), 'PERMISSION', 'REVOKE', 'Revoke permissions from roles', TRUE);

-- Assign permissions to USER role (basic read-only permissions)
INSERT INTO role_permission (id, role_id, permission_id, assigned_at)
SELECT UUID(), r.id, p.id, NOW()
FROM role r
CROSS JOIN permission p
WHERE r.name = 'USER'
AND (
    (p.resource = 'USER' AND p.action IN ('READ', 'LIST'))
    OR (p.resource = 'PROJECT' AND p.action IN ('READ', 'LIST'))
    OR (p.resource = 'TASK' AND p.action IN ('READ', 'LIST'))
);

-- Assign permissions to TEAM_LEAD role
INSERT INTO role_permission (id, role_id, permission_id, assigned_at)
SELECT UUID(), r.id, p.id, NOW()
FROM role r
CROSS JOIN permission p
WHERE r.name = 'TEAM_LEAD'
AND (
    (p.resource = 'USER' AND p.action IN ('READ', 'LIST'))
    OR (p.resource = 'PROJECT' AND p.action IN ('READ', 'LIST'))
    OR (p.resource = 'TASK' AND p.action IN ('READ', 'CREATE', 'UPDATE', 'ASSIGN', 'LIST'))
);

-- Assign permissions to PROJECT_MANAGER role
INSERT INTO role_permission (id, role_id, permission_id, assigned_at)
SELECT UUID(), r.id, p.id, NOW()
FROM role r
CROSS JOIN permission p
WHERE r.name = 'PROJECT_MANAGER'
AND (
    (p.resource = 'USER' AND p.action IN ('READ', 'LIST'))
    OR (p.resource = 'PROJECT' AND p.action IN ('READ', 'CREATE', 'UPDATE', 'DELETE', 'LIST', 'ADMIN'))
    OR (p.resource = 'TASK' AND p.action IN ('READ', 'CREATE', 'UPDATE', 'DELETE', 'ASSIGN', 'LIST'))
);

-- Assign ALL permissions to ADMIN role
INSERT INTO role_permission (id, role_id, permission_id, assigned_at)
SELECT UUID(), r.id, p.id, NOW()
FROM role r
CROSS JOIN permission p
WHERE r.name = 'ADMIN';
