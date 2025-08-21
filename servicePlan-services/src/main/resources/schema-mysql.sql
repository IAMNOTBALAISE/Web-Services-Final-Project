DROP TABLE IF EXISTS service_plans;

CREATE TABLE IF NOT EXISTS service_plans (
                                             id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                             plan_id VARCHAR(50) UNIQUE NOT NULL,
    coverage_details VARCHAR(255) NOT NULL,
    expiration_date DATE NOT NULL -- Store actual expiration date (YYYY-MM-DD)
    );