INSERT INTO customers (customer_id, last_name, first_name, email_address, street_address, postal_code, city, province, username, password)
VALUES
    ('123e4567-e89b-12d3-a456-556642440000','Smith','John','john.smith@example.com','123 Maple St','M1M 1M1','Toronto','Ontario','sjohn','pwd1'),
    ('223e4567-e89b-12d3-a456-556642440001','Johnson','Emily','emily.johnson@example.com','456 Oak Ave','V6B 2W1','Vancouver','British Columbia','ejohnson','pwd2');

INSERT INTO customer_phonenumbers (customer_id, type, number)
VALUES
    ('123e4567-e89b-12d3-a456-556642440000','MOBILE','555-1000'),
    ('223e4567-e89b-12d3-a456-556642440001','HOME','555-2000');