-- ============================================
--   CRIME DATA MANAGEMENT SYSTEM
--   Database Setup Script
-- ============================================

CREATE DATABASE IF NOT EXISTS crimedb;
USE crimedb;

DROP TABLE IF EXISTS crimes;

CREATE TABLE crimes (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    type        VARCHAR(100) NOT NULL,
    location    VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(50)  NOT NULL DEFAULT 'Open'
);

-- Sample data
INSERT INTO crimes (type, location, description, status) VALUES
('Theft',   'Anna Nagar, Chennai',     'Mobile phone snatched near bus stop.',         'Open'),
('Assault', 'Gandhipuram, Coimbatore', 'Physical altercation reported outside mall.',   'Under Investigation'),
('Fraud',   'RS Puram, Coimbatore',    'Online banking fraud reported by victim.',      'Closed'),
('Robbery', 'Peelamedu, Coimbatore',   'Armed robbery at a jewellery shop at night.',   'Open');
