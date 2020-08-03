CREATE TABLE delivery(
    delivery_id UUID PRIMARY KEY,
    advertisement_id INTEGER NOT NULL,
    t TIMESTAMP WITH TIME ZONE NOT NULL,
    browser TEXT,
    os TEXT,
    site TEXT NOT NULL
)