
-- Insert 10 catalogs
INSERT INTO catalogs (id, catalog_id, type, description)
VALUES
    (1, 'catalog-001', 'Smart Watch', 'Intelligent connected watches'),
    (2, 'catalog-002', 'Luxury Watch', 'Premium handcrafted watches'),
    (3, 'catalog-003', 'Diving Watch', 'Designed for underwater use'),
    (4, 'catalog-004', 'Vintage Watch', 'Classic watches with authenticity'),
    (5, 'catalog-005', 'Sports Watch', 'High durability for active users'),
    (6, 'catalog-006', 'Hybrid Watch', 'Combination of digital and mechanical'),
    (7, 'catalog-007', 'Chronograph', 'High-precision timekeeping features'),
    (8, 'catalog-008', 'Pocket Watch', 'Traditional pocket-style watches'),
    (9, 'catalog-009', 'Smart Fitness Watch', 'Tracks health metrics'),
    (10, 'catalog-010', 'Aviation Watch', 'Designed for pilots and frequent flyers');




-- Insert 10 watches (linked to catalogs)
INSERT INTO watches (id, watch_id, catalog_id, quantity, usage_type, model, material, brand_name, brand_country, msrp, cost, total_options_cost)
VALUES
    (1, 'WCH-001', 'catalog-001', 1, 'NEW', 'Apple Watch Ultra', 'Aluminum', 'Apple', 'USA', 1200.00, 1000.00, 200.00),
    (2, 'WCH-002', 'catalog-002', 8, 'USED', 'Omega Speedmaster', 'Titanium', 'Omega', 'Switzerland', 8000.00, 7000.00, 500.00),
    (3, 'WCH-003', 'catalog-003', 5, 'NEW', 'Rolex Sea-Dweller', 'Steel', 'Rolex', 'Switzerland', 12000.00, 10000.00, 800.00),
    (4, 'WCH-004', 'catalog-004', 3, 'USED', 'Tag Heuer Monaco', 'Carbon', 'Tag Heuer', 'Switzerland', 6000.00, 5000.00, 400.00),
    (5, 'WCH-005', 'catalog-005', 15, 'NEW', 'Casio G-Shock', 'Rubber', 'Casio', 'Japan', 400.00, 300.00, 100.00),
    (6, 'WCH-006', 'catalog-006', 12, 'NEW', 'Fossil Hybrid', 'Stainless Steel', 'Fossil', 'USA', 300.00, 250.00, 50.00),
    (7, 'WCH-007', 'catalog-007', 4, 'NEW', 'Breitling Navitimer', 'Steel', 'Breitling', 'Switzerland', 10000.00, 8500.00, 600.00),
    (8, 'WCH-008', 'catalog-008', 6, 'USED', 'Hamilton Khaki', 'Brass', 'Hamilton', 'USA', 700.00, 500.00, 200.00),
    (9, 'WCH-009', 'catalog-009', 20, 'NEW', 'Fitbit Charge 5', 'Plastic', 'Fitbit', 'USA', 150.00, 100.00, 50.00),
    (10, 'WCH-010', 'catalog-010', 7, 'NEW', 'Garmin Aviator', 'Titanium', 'Garmin', 'USA', 1500.00, 1200.00, 300.00);


INSERT INTO watch_accessories (watch_id, accessory_name, accessory_cost)
VALUES
    ('WCH-001', 'Sapphire Crystal', 120.00),
    ('WCH-001', 'Titanium Sport Band', 80.00),  -- Total for WCH-001 = 200.00
    ('WCH-002', 'Leather Strap', 250.00),
    ('WCH-002', 'Titanium Bracelet', 250.00),  -- Total for WCH-002 = 500.00
    ('WCH-003', 'Chronograph Movement', 400.00),
    ('WCH-003', 'Anti-Reflective Coating', 400.00),  -- Total for WCH-003 = 800.00
    ('WCH-004', 'Rubber Strap', 200.00),
    ('WCH-004', 'Sapphire Glass', 200.00),  -- Total for WCH-004 = 400.00
    ('WCH-005', 'Shock Resistant Casing', 50.00),
    ('WCH-005', 'Protective Bezel', 50.00),  -- Total for WCH-005 = 100.00
    ('WCH-006', 'Hybrid Digital Display', 50.00),  -- Total for WCH-006 = 50.00
    ('WCH-007', 'Slide Rule Bezel', 300.00),
    ('WCH-007', 'Dual Time Function', 300.00),  -- Total for WCH-007 = 600.00

    ('WCH-008', 'Luminous Hands', 100.00),
    ('WCH-008', 'Scratch Resistant Coating', 100.00),  -- Total for WCH-008 = 200.00

    ('WCH-009', 'Silicone Sports Band', 25.00),
    ('WCH-009', 'Heart Rate Sensor Upgrade', 25.00),  -- Total for WCH-009 = 50.00

    ('WCH-010', 'Aviation Compass Dial', 200.00),
    ('WCH-010', 'Flight Computer Function', 100.00);  -- Total for WCH-010 = 300.00



