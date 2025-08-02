-- Create airlines table
CREATE TABLE airlines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL UNIQUE,
    country VARCHAR(100) NOT NULL,
    description TEXT,
    website VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_airlines_code ON airlines(code);
CREATE INDEX idx_airlines_country ON airlines(country);
CREATE INDEX idx_airlines_active ON airlines(active);
CREATE INDEX idx_airlines_name ON airlines(name);

-- Add constraints
ALTER TABLE airlines ADD CONSTRAINT chk_airline_code_length CHECK (LENGTH(code) >= 2);
ALTER TABLE airlines ADD CONSTRAINT chk_airline_name_not_empty CHECK (LENGTH(TRIM(name)) > 0);