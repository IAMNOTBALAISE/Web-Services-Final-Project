DROP TABLE IF EXISTS watch_accessories;
DROP TABLE IF EXISTS watches;

DROP TABLE IF EXISTS catalogs;

CREATE TABLE IF NOT EXISTS catalogs(

                                       id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                       catalog_id VARCHAR(50) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(50) NOT NULL

    );

CREATE TABLE IF NOT EXISTS watches (
                                       id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                       watch_id VARCHAR(50) UNIQUE NOT NULL,
    catalog_id VARCHAR(50) NOT NULL,
    quantity          INT         NOT NULL DEFAULT 0,
    usage_type VARCHAR(20) NOT NULL,
    model VARCHAR(100) NOT NULL,
    material VARCHAR(100) NOT NULL,
    brand_name VARCHAR(100) NOT NULL,
    brand_country VARCHAR(100) NOT NULL,
    msrp DECIMAL(10,2) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    total_options_cost DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (catalog_id) REFERENCES catalogs(catalog_id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS watch_accessories (
                                                 watch_id VARCHAR(50) NOT NULL,
    accessory_name VARCHAR(100) NOT NULL,
    accessory_cost DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (watch_id) REFERENCES watches(watch_id) ON DELETE CASCADE
    );