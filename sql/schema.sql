-- IPOS-PU Database Schema — Multi-Schema Local Setup
-- Creates 3 local schemas: ipos_pu (Team C), ipos_sa (Team A foreign), ipos_ca (Team B foreign)
-- Run: mysql -u root -p < sql/schema.sql
-- Setup: GRANT ALL ON ipos_pu.* TO 'root'@'localhost'; GRANT ALL ON ipos_sa.* TO 'root'@'localhost'; GRANT ALL ON ipos_ca.* TO 'root'@'localhost';

CREATE DATABASE IF NOT EXISTS ipos_pu;
CREATE DATABASE IF NOT EXISTS ipos_sa;
CREATE DATABASE IF NOT EXISTS ipos_ca;
USE ipos_pu;

-- Drop all tables for a clean reset
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS pu_campaign_counters;
DROP TABLE IF EXISTS pu_campaign_items;
DROP TABLE IF EXISTS pu_campaigns;
DROP TABLE IF EXISTS pu_order_items;
DROP TABLE IF EXISTS pu_order_status_history;
DROP TABLE IF EXISTS pu_orders;
DROP TABLE IF EXISTS pu_payment_log;
DROP TABLE IF EXISTS pu_email_outbox;
DROP TABLE IF EXISTS pu_products;
DROP TABLE IF EXISTS pu_commercial_applications;
DROP TABLE IF EXISTS pu_member_applications;
DROP TABLE IF EXISTS pu_users;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- MEMBERS TABLES
-- ============================================================

CREATE TABLE IF NOT EXISTS pu_users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            ENUM('ADMIN','MEMBER') NOT NULL DEFAULT 'MEMBER',
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pu_member_applications (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    type            ENUM('COMMERCIAL','NON_COMMERCIAL') NOT NULL,
    email           VARCHAR(255) NOT NULL,
    status          ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    submitted_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at    TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS pu_commercial_applications (
    application_id  INT PRIMARY KEY,
    company_name    VARCHAR(255),
    company_reg_no  VARCHAR(100) NOT NULL,
    directors       TEXT NOT NULL,
    business_type   VARCHAR(100) NOT NULL,
    address         TEXT NOT NULL,
    email           VARCHAR(255) NOT NULL,
    FOREIGN KEY (application_id) REFERENCES pu_member_applications(id)
);

-- ============================================================
-- SALES TABLES
-- ============================================================

CREATE TABLE IF NOT EXISTS pu_products (
    item_id         INT AUTO_INCREMENT PRIMARY KEY,
    description     VARCHAR(500) NOT NULL,
    unit_price      DECIMAL(10,2) NOT NULL,
    stock_qty       INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS pu_orders (
    order_id            VARCHAR(36) PRIMARY KEY,
    user_id             INT NULL,
    status              ENUM('RECEIVED','DISPATCHED','DELIVERED') NOT NULL DEFAULT 'RECEIVED',
    total               DECIMAL(10,2) NOT NULL,
    delivery_address    TEXT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES pu_users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS pu_order_items (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    order_id            VARCHAR(36) NOT NULL,
    item_id             INT NOT NULL,
    qty                 INT NOT NULL,
    unit_price_at_time  DECIMAL(10,2) NOT NULL,
    discount_percent    DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    line_total          DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES pu_orders(order_id),
    FOREIGN KEY (item_id) REFERENCES pu_products(item_id)
);

CREATE TABLE IF NOT EXISTS pu_order_status_history (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    order_id    VARCHAR(36) NOT NULL,
    status      ENUM('RECEIVED','DISPATCHED','DELIVERED') NOT NULL,
    changed_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note        VARCHAR(500),
    FOREIGN KEY (order_id) REFERENCES pu_orders(order_id)
);

-- ============================================================
-- PROMOTIONS TABLES
-- ============================================================

CREATE TABLE IF NOT EXISTS pu_campaigns (
    campaign_id     INT AUTO_INCREMENT PRIMARY KEY,
    description     VARCHAR(500) NOT NULL,
    start_dt        DATETIME NOT NULL,
    end_dt          DATETIME NOT NULL,
    status          ENUM('ACTIVE','CANCELLED','ENDED') NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS pu_campaign_items (
    campaign_id     INT NOT NULL,
    item_id         INT NOT NULL,
    discount_percent DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (campaign_id, item_id),
    FOREIGN KEY (campaign_id) REFERENCES pu_campaigns(campaign_id),
    FOREIGN KEY (item_id) REFERENCES pu_products(item_id)
);

CREATE TABLE IF NOT EXISTS pu_campaign_counters (
    campaign_id         INT NOT NULL,
    item_id             INT NOT NULL,
    campaign_hits       INT NOT NULL DEFAULT 0,
    item_hits_qty       INT NOT NULL DEFAULT 0,
    item_purchased_qty  INT NOT NULL DEFAULT 0,
    PRIMARY KEY (campaign_id, item_id),
    FOREIGN KEY (campaign_id) REFERENCES pu_campaigns(campaign_id),
    FOREIGN KEY (item_id) REFERENCES pu_products(item_id)
);

-- ============================================================
-- COMMS TABLES
-- ============================================================

CREATE TABLE IF NOT EXISTS pu_email_outbox (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    to_email    VARCHAR(255) NOT NULL,
    subject     VARCHAR(500) NOT NULL,
    body        TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status      ENUM('PENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING',
    last_error  TEXT NULL
);

CREATE TABLE IF NOT EXISTS pu_payment_log (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    payee       VARCHAR(255),
    reference   VARCHAR(255) NOT NULL,
    amount      DECIMAL(10,2) NOT NULL,
    paid_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    card_type   VARCHAR(50),
    card_first4 VARCHAR(4),
    card_last4  VARCHAR(4),
    expiry      VARCHAR(7),
    status      ENUM('SUCCESS','FAILED') NOT NULL,
    provider    ENUM('PAYPAL_SANDBOX','MOCK') NOT NULL DEFAULT 'MOCK'
);

-- ============================================================
-- FOREIGN INTEGRATION TABLES (minimal copies, owned by other teams)
-- ipos_sa is owned by Team A (IPOS-SA); ipos_ca is owned by Team B (IPOS-CA)
-- PU writes PENDING rows; the other team reads and processes them locally
-- ============================================================

CREATE TABLE IF NOT EXISTS ipos_sa.sa_commercial_applications (
    application_id  INT AUTO_INCREMENT PRIMARY KEY,
    company_name    VARCHAR(255),
    company_reg_no  VARCHAR(100) NOT NULL,
    directors       TEXT NOT NULL,
    business_type   VARCHAR(100) NOT NULL,
    address         TEXT NOT NULL,
    email           VARCHAR(255) NOT NULL,
    status          ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    submitted_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    processed_at    DATETIME NULL,
    notes           TEXT NULL
);

CREATE TABLE IF NOT EXISTS ipos_ca.ca_inventory_adjustments (
    adjustment_id   INT AUTO_INCREMENT PRIMARY KEY,
    order_id        VARCHAR(100) NOT NULL,
    merchant_id     VARCHAR(100) NOT NULL DEFAULT 'IPOS_PU',
    item_id         INT NOT NULL,
    qty             INT NOT NULL,
    status          ENUM('PENDING','APPLIED','FAILED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    processed_at    DATETIME NULL,
    error           TEXT NULL
);

-- ============================================================
-- SEED DATA (matching IPOS_SampleData_2026.pdf)
-- ============================================================

-- Drop old shared tables from previous schema layout
DROP TABLE IF EXISTS shared_inventory_adjustments;
DROP TABLE IF EXISTS shared_commercial_applications;

-- ---------------------------------------------------------------
-- Users — use ON DUPLICATE KEY UPDATE so re-running fixes passwords
-- ---------------------------------------------------------------

-- Administrator: login sysdba / masterkey
INSERT INTO pu_users (email, password_hash, role, must_change_password)
VALUES ('sysdba', '$2a$10$g/0jO4fvG1ZrCznZ6kOX6Oud2UZPlfTtq/Li3QVO/NuOS8EvkchGm', 'ADMIN', FALSE)
ON DUPLICATE KEY UPDATE password_hash = '$2a$10$g/0jO4fvG1ZrCznZ6kOX6Oud2UZPlfTtq/Li3QVO/NuOS8EvkchGm', role = 'ADMIN';

-- PU-Admin: login manager / GetPU_it_done
INSERT INTO pu_users (email, password_hash, role, must_change_password)
VALUES ('manager', '$2a$10$U.21UjSQBMNJzVB6KqBsreilAfLhcCvwlWtxYAqh0NZoPhYX6LzuW', 'ADMIN', FALSE)
ON DUPLICATE KEY UPDATE password_hash = '$2a$10$U.21UjSQBMNJzVB6KqBsreilAfLhcCvwlWtxYAqh0NZoPhYX6LzuW', role = 'ADMIN';

-- Non-commercial Member PU0001: cool@example.com / 12ss_56_SS
INSERT INTO pu_users (email, password_hash, role, must_change_password)
VALUES ('cool@example.com', '$2a$10$iS5YPKGTsvqyroQn0Sx0T.secobbDmVVoCS7y.ytWVCKxkoYJQ.fK', 'MEMBER', FALSE)
ON DUPLICATE KEY UPDATE password_hash = '$2a$10$iS5YPKGTsvqyroQn0Sx0T.secobbDmVVoCS7y.ytWVCKxkoYJQ.fK', must_change_password = FALSE;

-- Non-commercial Member PU0002: cool1@example.com / 34pp_78_LL
INSERT INTO pu_users (email, password_hash, role, must_change_password)
VALUES ('cool1@example.com', '$2a$10$Dn2QAtORNtS79Ot4DdqyuuIH9LsToblILIR1j2zPNh.ZhZGQ6MuGG', 'MEMBER', FALSE)
ON DUPLICATE KEY UPDATE password_hash = '$2a$10$Dn2QAtORNtS79Ot4DdqyuuIH9LsToblILIR1j2zPNh.ZhZGQ6MuGG', must_change_password = FALSE;


-- ---------------------------------------------------------------
-- Products — retail price = 100% markup on CA Package Cost
-- (Source: IPOS_SampleData_2026.pdf, Stock availability at Cosymed Ltd)
-- ---------------------------------------------------------------
INSERT INTO pu_products (item_id, description, unit_price, stock_qty) VALUES
(1,  'Paracetamol (box, 20 caps)',            0.20, 121),
(2,  'Aspirin (box, 20 caps)',                1.00, 201),
(3,  'Analgin (box, 10 caps)',                2.40,  25),
(4,  'Celebrex, caps 100 mg (box, 10)',      20.00,  43),
(5,  'Celebrex, caps 200 mg (box, 10)',      37.00,  35),
(6,  'Retin-A Tretin, 30 g (box, 20 caps)',  50.00,  28),
(7,  'Lipitor TB, 20 mg (box, 30 caps)',     31.00,  10),
(8,  'Claritin CR, 60g (box, 20 caps)',      39.00,  21),
(9,  'Iodine tincture (bottle, 100 ml)',      0.60,  35),
(10, 'Rhynol (bottle, 200 ml)',               5.00,  14),
(11, 'Ospen (box, 20 caps)',                 21.00,  78),
(12, 'Amopen (box, 30 caps)',                30.00,  90),
(13, 'Vitamin C (box, 30 caps)',              2.40,  22),
(14, 'Vitamin B12 (box, 30 caps)',            2.60,  43);

-- ---------------------------------------------------------------
-- Member applications for PU0001, PU0002, PU0003
-- ---------------------------------------------------------------
INSERT INTO pu_member_applications (id, type, email, status, submitted_at, processed_at) VALUES
(1, 'NON_COMMERCIAL', 'cool@example.com',       'APPROVED', '2026-03-01 10:00:00', '2026-03-01 10:00:00'),
(2, 'NON_COMMERCIAL', 'cool1@example.com',      'APPROVED', '2026-03-01 10:00:00', '2026-03-01 10:00:00'),
(3, 'COMMERCIAL',     'pondPharma@example.com', 'PENDING',  '2026-03-01 10:00:00', NULL);

-- Commercial member PU0003: Pond Pharmacy
INSERT INTO pu_commercial_applications (application_id, company_name, company_reg_no, directors, business_type, address, email)
VALUES (3, 'Pond Pharmacy', 'UK10003429CompH', 'Director, Pond Pharmacy Ltd', 'Pharmacy', '25, High Street, Chislehurst, BR7 5BN', 'pondPharma@example.com');

-- ---------------------------------------------------------------
-- Campaigns from scenarios 17 & 18 (IPOS_SampleData_2026.pdf)
-- ---------------------------------------------------------------
-- Scenario 17: March Promotion (created 15 March 2026)
INSERT INTO pu_campaigns (campaign_id, description, start_dt, end_dt, status)
VALUES (1, 'March Promotion', '2026-03-15 00:00:00', '2026-04-20 23:59:59', 'ACTIVE');

-- Scenario 18: April Promotion (created 30 March 2026)
INSERT INTO pu_campaigns (campaign_id, description, start_dt, end_dt, status)
VALUES (2, 'April Promotion', '2026-04-05 00:00:00', '2026-04-10 23:59:59', 'ACTIVE');

-- March Promotion items: Aspirin 5%, Analgin 10%, Celebrex 100mg 10%, Retin-A Tretin 20%
INSERT INTO pu_campaign_items (campaign_id, item_id, discount_percent) VALUES
(1,  2,  5.00),
(1,  3, 10.00),
(1,  4, 10.00),
(1,  6, 20.00);

-- April Promotion items: Ospen 20%, Vitamin C 10%
INSERT INTO pu_campaign_items (campaign_id, item_id, discount_percent) VALUES
(2, 11, 20.00),
(2, 13, 10.00);

-- ---------------------------------------------------------------
-- Seed orders for PU0001 (9 orders total so next checkout = 10th = 10% discount)
-- Scenario 20: 8 purchases between 1-20 March + 9th on 8 April (Ospen)
-- ---------------------------------------------------------------
SET @pu0001 = (SELECT id FROM pu_users WHERE email = 'cool@example.com');

INSERT INTO pu_orders (order_id, user_id, status, total, delivery_address, created_at) VALUES
('pu0001-hist-0001', @pu0001, 'DELIVERED',  1.00, '10 Test St, London EC1V 0HB', '2026-03-01 10:00:00'),
('pu0001-hist-0002', @pu0001, 'DELIVERED',  2.40, '10 Test St, London EC1V 0HB', '2026-03-03 11:00:00'),
('pu0001-hist-0003', @pu0001, 'DELIVERED',  1.00, '10 Test St, London EC1V 0HB', '2026-03-05 12:00:00'),
('pu0001-hist-0004', @pu0001, 'DELIVERED',  2.40, '10 Test St, London EC1V 0HB', '2026-03-07 10:00:00'),
('pu0001-hist-0005', @pu0001, 'DELIVERED',  1.00, '10 Test St, London EC1V 0HB', '2026-03-10 09:00:00'),
('pu0001-hist-0006', @pu0001, 'DELIVERED',  2.40, '10 Test St, London EC1V 0HB', '2026-03-12 14:00:00'),
('pu0001-hist-0007', @pu0001, 'DELIVERED',  1.00, '10 Test St, London EC1V 0HB', '2026-03-15 11:00:00'),
('pu0001-hist-0008', @pu0001, 'DELIVERED',  2.40, '10 Test St, London EC1V 0HB', '2026-03-18 10:00:00'),
-- 9th order: Scenario 20 — 8 April, Ospen 1 box, April Promotion 20% off (21.00 * 0.80 = 16.80)
('pu0001-ospen-0009', @pu0001, 'RECEIVED',  16.80, '10 Test St, London EC1V 0HB', '2026-04-08 14:00:00');

INSERT INTO pu_order_status_history (order_id, status, changed_at, note) VALUES
('pu0001-hist-0001', 'RECEIVED',   '2026-03-01 10:00:00', 'Order placed'),
('pu0001-hist-0001', 'DISPATCHED', '2026-03-02 09:00:00', 'Dispatched'),
('pu0001-hist-0001', 'DELIVERED',  '2026-03-03 11:00:00', 'Delivered'),
('pu0001-hist-0002', 'RECEIVED',   '2026-03-03 11:00:00', 'Order placed'),
('pu0001-hist-0002', 'DELIVERED',  '2026-03-05 10:00:00', 'Delivered'),
('pu0001-hist-0003', 'RECEIVED',   '2026-03-05 12:00:00', 'Order placed'),
('pu0001-hist-0003', 'DELIVERED',  '2026-03-07 08:00:00', 'Delivered'),
('pu0001-hist-0004', 'RECEIVED',   '2026-03-07 10:00:00', 'Order placed'),
('pu0001-hist-0004', 'DELIVERED',  '2026-03-09 10:00:00', 'Delivered'),
('pu0001-hist-0005', 'RECEIVED',   '2026-03-10 09:00:00', 'Order placed'),
('pu0001-hist-0005', 'DELIVERED',  '2026-03-12 08:00:00', 'Delivered'),
('pu0001-hist-0006', 'RECEIVED',   '2026-03-12 14:00:00', 'Order placed'),
('pu0001-hist-0006', 'DELIVERED',  '2026-03-14 10:00:00', 'Delivered'),
('pu0001-hist-0007', 'RECEIVED',   '2026-03-15 11:00:00', 'Order placed'),
('pu0001-hist-0007', 'DELIVERED',  '2026-03-17 10:00:00', 'Delivered'),
('pu0001-hist-0008', 'RECEIVED',   '2026-03-18 10:00:00', 'Order placed'),
('pu0001-hist-0008', 'DELIVERED',  '2026-03-20 10:00:00', 'Delivered'),
('pu0001-ospen-0009', 'RECEIVED',  '2026-04-08 14:00:00', 'Order placed online');

-- Order item for PU0001's 9th order: Ospen × 1, April Promotion 20% discount
INSERT INTO pu_order_items (order_id, item_id, qty, unit_price_at_time, discount_percent, line_total)
VALUES ('pu0001-ospen-0009', 11, 1, 21.00, 20.00, 16.80);

-- ---------------------------------------------------------------
-- Scenario 19: Peter Popov's order (20 March 2026, guest checkout)
-- Aspirin ×1 at 5% discount + Retin-A Tretin ×1 at 20% discount
-- Card: AmEx 0000 000000 0000 0001, CVV 3245, valid 08/2030
-- ---------------------------------------------------------------
INSERT INTO pu_orders (order_id, user_id, status, total, delivery_address, created_at)
VALUES ('peter-popov-order-0001', NULL, 'RECEIVED', 40.95, '(AmEx order — delivery address provided at checkout)', '2026-03-20 15:00:00');

INSERT INTO pu_order_items (order_id, item_id, qty, unit_price_at_time, discount_percent, line_total) VALUES
('peter-popov-order-0001', 2,  1, 1.00,  5.00,  0.95),   -- Aspirin × 1, 5% off
('peter-popov-order-0001', 6,  1, 50.00, 20.00, 40.00);  -- Retin-A × 1, 20% off

INSERT INTO pu_order_status_history (order_id, status, changed_at, note)
VALUES ('peter-popov-order-0001', 'RECEIVED', '2026-03-20 15:00:00', 'Online order — payment via AmEx');

-- Payment log for Peter Popov's order
INSERT INTO pu_payment_log (payee, reference, amount, paid_at, card_type, card_first4, card_last4, expiry, status, provider)
VALUES ('Peter Popov', 'peter-popov-order-0001', 40.95, '2026-03-20 15:00:00', 'AMEX', '0000', '0001', '08/2030', 'SUCCESS', 'MOCK');

-- Payment log for PU0001's 9th order (Ospen, April Promotion)
INSERT INTO pu_payment_log (payee, reference, amount, paid_at, card_type, card_first4, card_last4, expiry, status, provider)
VALUES ('PU0001', 'pu0001-ospen-0009', 16.80, '2026-04-08 14:00:00', 'MASTERCARD', '0001', '0005', '09/2028', 'SUCCESS', 'MOCK');

-- ---------------------------------------------------------------
-- Campaign counters — pre-seeded from scenarios 19 & 20
-- ---------------------------------------------------------------
-- March Promotion counters:
-- Peter Popov clicked on the campaign link → campaign_hits for all items = 1
-- He added Aspirin + Retin-A to cart → item_hits_qty
-- Payment succeeded → item_purchased_qty
INSERT INTO pu_campaign_counters (campaign_id, item_id, campaign_hits, item_hits_qty, item_purchased_qty) VALUES
(1,  2,  1, 1, 1),   -- Aspirin: 1 click, 1 added, 1 bought (Peter Popov)
(1,  3,  1, 0, 0),   -- Analgin: 1 click, not added
(1,  4,  1, 0, 0),   -- Celebrex 100mg: 1 click, not added
(1,  6,  1, 1, 1);   -- Retin-A: 1 click, 1 added, 1 bought (Peter Popov)

-- April Promotion counters:
-- PU0001 visited promotions on 8 April → campaign_hits = 1
-- PU0001 added Ospen to cart → item_hits_qty = 1
-- PU0001 paid → item_purchased_qty = 1
INSERT INTO pu_campaign_counters (campaign_id, item_id, campaign_hits, item_hits_qty, item_purchased_qty) VALUES
(2, 11, 1, 1, 1),   -- Ospen: 1 click, 1 added, 1 bought (PU0001)
(2, 13, 1, 0, 0);   -- Vitamin C: 1 click, not added

-- ============================================================
-- OPTIONAL: Integration seed rows for cross-subsystem demo
-- ============================================================

-- Pond Pharmacy commercial application visible to SA (GAP 4)
-- Mirrors what CommercialApplicationClient.submitCommercialApplication() would have written
INSERT IGNORE INTO ipos_sa.sa_commercial_applications
  (company_name, company_reg_no, directors, business_type, address, email, status, submitted_at)
VALUES ('Pond Pharmacy', 'UK10003429CompH', 'Director, Pond Pharmacy Ltd',
        'Pharmacy', '25, High Street, Chislehurst, BR7 5BN',
        'pondPharma@example.com', 'PENDING', '2026-03-01 10:00:00');

-- Inventory adjustments for pre-seeded orders visible to CA (GAP 5)
-- Mirrors what MerchantInventoryClient.deductStock() would have written
INSERT IGNORE INTO ipos_ca.ca_inventory_adjustments
  (order_id, merchant_id, item_id, qty, status, created_at)
VALUES
  ('peter-popov-order-0001', 'IPOS_PU', 2,  1, 'PENDING', '2026-03-20 15:00:00'),  -- Aspirin x1
  ('peter-popov-order-0001', 'IPOS_PU', 6,  1, 'PENDING', '2026-03-20 15:00:00'),  -- Retin-A x1
  ('pu0001-ospen-0009',      'IPOS_PU', 11, 1, 'PENDING', '2026-04-08 14:00:00');  -- Ospen x1
