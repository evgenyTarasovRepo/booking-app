CREATE TABLE IF NOT EXISTS properties(
    id UUID NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    property_type VARCHAR(255) NOT NULL,
    price_per_night DECIMAL(5,2) NOT NULL,
    max_guests INT NOT NULL,
    owner_id UUID NOT NULL,
    is_active bool NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    )