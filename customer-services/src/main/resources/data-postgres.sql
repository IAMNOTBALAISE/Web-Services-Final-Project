INSERT INTO customers (customer_id, last_name, first_name, email_address, street_address, postal_code, city, province,
                       username, password)
VALUES
    ( '123e4567-e89b-12d3-a456-556642440000', 'Smith', 'John', 'john.smith@example.com', '123 Maple St', 'M1M 1M1', 'Toronto', 'Ontario', 'sjohn', 'pwd1'),
    ( '223e4567-e89b-12d3-a456-556642440001', 'Johnson', 'Emily', 'emily.johnson@example.com', '456 Oak Ave', 'V6B 2W1', 'Vancouver', 'British Columbia', 'ejohnson', 'pwd2'),
    ( '323e4567-e89b-12d3-a456-556642440002', 'Wong', 'Michael', 'michael.wong@example.com', '789 Elm St', 'H3H 2N2', 'Montreal', 'Quebec', 'mwong', 'pwd3'),
    ( '423e4567-e89b-12d3-a456-556642440003', 'Patel', 'Sara', 'sara.patel@example.com', '321 Pine St', 'T2N 4T4', 'Calgary', 'Alberta', 'spatel', 'pwd4'),
    ( '523e4567-e89b-12d3-a456-556642440004', 'Lee', 'David', 'david.lee@example.com', '987 Cedar Ave', 'K1K 7L7', 'Ottawa', 'Ontario', 'dlee', 'pwd5'),
    ('623e4567-e89b-12d3-a456-556642440005', 'Singh', 'Alisha', 'alisha.singh@example.com', '741 Birch St', 'L5A 1X2', 'Mississauga', 'Ontario', 'asingh', 'pwd6'),
    ( '723e4567-e89b-12d3-a456-556642440006', 'Chen', 'Jason', 'jason.chen@example.com', '852 Elmwood Dr', 'B3A 2K6', 'Halifax', 'Nova Scotia', 'jchen', 'pwd7'),
    ( '823e4567-e89b-12d3-a456-556642440007', 'Garcia', 'Sophia', 'sophia.garcia@example.com', '963 Spruce Rd', 'G1P 3T5', 'Quebec City', 'Quebec', 'sgarcia', 'pwd8'),
    ( '923e4567-e89b-12d3-a456-556642440008', 'Martinez', 'Daniel', 'daniel.martinez@example.com', '654 Oak Ln', 'E1A 4R7', 'Fredericton', 'New Brunswick', 'dmartinez', 'pwd9'),
    ( 'a23e4567-e89b-12d3-a456-556642440009', 'Kim', 'Jessica', 'jessica.kim@example.com', '852 Pinecrest Blvd', 'A1A 5W3', 'St. Johns', 'Newfoundland', 'jkim', 'pwd10');

-- Insert 10 phone numbers (one per customer)
INSERT INTO customer_phonenumbers(customer_id, type, number)
VALUES
    ('123e4567-e89b-12d3-a456-556642440000', 'MOBILE', '555-1000'),
    ('223e4567-e89b-12d3-a456-556642440001', 'HOME', '555-2000'),
    ('323e4567-e89b-12d3-a456-556642440002', 'MOBILE', '555-3000'),
    ('423e4567-e89b-12d3-a456-556642440003', 'FAX', '555-4000'),
    ('523e4567-e89b-12d3-a456-556642440004', 'HOME', '555-5000'),
    ('623e4567-e89b-12d3-a456-556642440005', 'MOBILE', '555-6000'),
    ('723e4567-e89b-12d3-a456-556642440006', 'FAX', '555-7000'),
    ('823e4567-e89b-12d3-a456-556642440007', 'MOBILE', '555-8000'),
    ('923e4567-e89b-12d3-a456-556642440008', 'FAX', '555-9000'),
    ('a23e4567-e89b-12d3-a456-556642440009', 'HOME', '555-0000');