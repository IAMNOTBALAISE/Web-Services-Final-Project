INSERT INTO catalogs (id, catalog_id, type, description)
VALUES
    (1, 'catalog-001', 'Smart Watch', 'Intelligent connected watches'),
    (2, 'catalog-002', 'Luxury Watch', 'Premium handcrafted watches');

INSERT INTO watches (
    id, watch_id, catalog_id, quantity, usage_type,
    model, material, brand_name, brand_country,
    msrp, cost, total_options_cost
) VALUES
      (1, 'WCH-001', 'catalog-001', 1, 'NEW', 'Apple Watch Ultra', 'Aluminum', 'Apple', 'USA', 1200.00, 1000.00, 200.00),
      (2, 'WCH-002', 'catalog-002', 8, 'USED', 'Omega Speedmaster', 'Titanium', 'Omega', 'Switzerland', 8000.00, 7000.00, 500.00);

INSERT INTO watch_accessories (watch_id, accessory_name, accessory_cost)
VALUES
    ('WCH-001', 'Sapphire Crystal', 120.00),
    ('WCH-001', 'Titanium Sport Band', 80.00),
    ('WCH-002', 'Leather Strap', 250.00),
    ('WCH-002', 'Titanium Bracelet', 250.00);