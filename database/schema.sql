CREATE DATABASE IF NOT EXISTS fixflow;
USE fixflow;

CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  phone VARCHAR(30),
  organization VARCHAR(160) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id),
  INDEX idx_users_role (role_id)
);

CREATE TABLE issue_categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE asset_categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE locations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  building VARCHAR(80) NOT NULL,
  floor_name VARCHAR(40) NOT NULL,
  room_name VARCHAR(100) NOT NULL,
  UNIQUE KEY uq_location (building, floor_name, room_name)
);

CREATE TABLE assets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  asset_code VARCHAR(40) NOT NULL UNIQUE,
  asset_name VARCHAR(140) NOT NULL,
  category_id BIGINT NOT NULL,
  location_id BIGINT NOT NULL,
  criticality ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM',
  health_score DECIMAL(5,2) NOT NULL DEFAULT 100.00,
  purchase_date DATE,
  warranty_end DATE,
  last_maintenance_date DATE,
  next_maintenance_date DATE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_assets_category FOREIGN KEY (category_id) REFERENCES asset_categories(id),
  CONSTRAINT fk_assets_location FOREIGN KEY (location_id) REFERENCES locations(id),
  INDEX idx_assets_health (health_score),
  INDEX idx_assets_location (location_id)
);

CREATE TABLE technicians (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  availability_status ENUM('AVAILABLE','BUSY','OFFLINE') NOT NULL DEFAULT 'AVAILABLE',
  capacity_limit INT NOT NULL DEFAULT 10,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_technicians_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE skills (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE technician_skills (
  technician_id BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  proficiency ENUM('FOUNDATION','INTERMEDIATE','EXPERT') NOT NULL DEFAULT 'INTERMEDIATE',
  PRIMARY KEY (technician_id, skill_id),
  CONSTRAINT fk_technician_skills_technician FOREIGN KEY (technician_id) REFERENCES technicians(id),
  CONSTRAINT fk_technician_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id)
);

CREATE TABLE issues (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  issue_number VARCHAR(20) NOT NULL UNIQUE,
  reporter_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  location_id BIGINT NOT NULL,
  asset_id BIGINT,
  title VARCHAR(180) NOT NULL,
  description TEXT NOT NULL,
  severity ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL,
  impact_score INT NOT NULL DEFAULT 1,
  affected_users INT NOT NULL DEFAULT 1,
  priority_score INT NOT NULL,
  priority ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL,
  status ENUM('OPEN','TRIAGED','ASSIGNED','IN_PROGRESS','WAITING_FOR_PARTS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
  sla_hours INT NOT NULL DEFAULT 24,
  sla_deadline TIMESTAMP NOT NULL,
  resolved_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_issues_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
  CONSTRAINT fk_issues_category FOREIGN KEY (category_id) REFERENCES issue_categories(id),
  CONSTRAINT fk_issues_location FOREIGN KEY (location_id) REFERENCES locations(id),
  CONSTRAINT fk_issues_asset FOREIGN KEY (asset_id) REFERENCES assets(id),
  INDEX idx_issues_status_priority (status, priority),
  INDEX idx_issues_sla (sla_deadline),
  INDEX idx_issues_asset (asset_id)
);

CREATE TABLE issue_assignments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  issue_id BIGINT NOT NULL,
  technician_id BIGINT NOT NULL,
  assigned_by BIGINT NOT NULL,
  assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  accepted_at TIMESTAMP NULL,
  completed_at TIMESTAMP NULL,
  is_current BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_assignments_issue FOREIGN KEY (issue_id) REFERENCES issues(id),
  CONSTRAINT fk_assignments_technician FOREIGN KEY (technician_id) REFERENCES technicians(id),
  CONSTRAINT fk_assignments_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id),
  INDEX idx_assignments_current (issue_id, is_current),
  INDEX idx_assignments_technician (technician_id, is_current)
);

CREATE TABLE issue_updates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  issue_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  status ENUM('OPEN','TRIAGED','ASSIGNED','IN_PROGRESS','WAITING_FOR_PARTS','RESOLVED','CLOSED'),
  note TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_updates_issue FOREIGN KEY (issue_id) REFERENCES issues(id),
  CONSTRAINT fk_updates_author FOREIGN KEY (author_id) REFERENCES users(id),
  INDEX idx_updates_issue_time (issue_id, created_at)
);

CREATE TABLE maintenance_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  asset_id BIGINT NOT NULL,
  issue_id BIGINT,
  technician_id BIGINT,
  maintenance_type ENUM('CORRECTIVE','PREVENTIVE','INSPECTION') NOT NULL,
  notes TEXT NOT NULL,
  duration_minutes INT,
  performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_history_asset FOREIGN KEY (asset_id) REFERENCES assets(id),
  CONSTRAINT fk_history_issue FOREIGN KEY (issue_id) REFERENCES issues(id),
  CONSTRAINT fk_history_technician FOREIGN KEY (technician_id) REFERENCES technicians(id),
  INDEX idx_history_asset_time (asset_id, performed_at)
);

CREATE TABLE notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  issue_id BIGINT,
  title VARCHAR(160) NOT NULL,
  message VARCHAR(500) NOT NULL,
  notification_type ENUM('INFO','SUCCESS','WARNING','CRITICAL') NOT NULL DEFAULT 'INFO',
  is_read BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_notifications_issue FOREIGN KEY (issue_id) REFERENCES issues(id),
  INDEX idx_notifications_user_read (user_id, is_read, created_at)
);
