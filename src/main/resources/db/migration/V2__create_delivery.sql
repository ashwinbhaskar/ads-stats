CREATE TABLE delivery(
    delivery_id UUID PRIMARY KEY,
    advertisement_id UUID NOT NULL,
    t TIMESTAMP WITH TIME ZONE NOT NULL,
    browser TEXT,
    os TEXT,
    site TEXT NOT NULL
)