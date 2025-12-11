CREATE TABLE
    products (
        id UUID PRIMARY KEY NOT NULL,
        name VARCHAR(255) NOT NULL,
        category VARCHAR(100) NOT NULL,
        offer_type VARCHAR(50) NOT NULL CHECK (offer_type IN ('ForSale', 'ForRental', 'ForBoth')),
        price DECIMAL(10, 2),
        deposit DECIMAL(10, 2),
        daily_rate DECIMAL(10, 2),
        CONSTRAINT valid_for_sale CHECK (
            offer_type != 'ForSale'
            OR price IS NOT NULL
        ),
        CONSTRAINT valid_for_rental CHECK (
            offer_type != 'ForRental'
            OR (
                daily_rate IS NOT NULL
                AND deposit IS NOT NULL
            )
        ),
        CONSTRAINT valid_for_both CHECK (
            offer_type != 'ForBoth'
            OR (
                price IS NOT NULL
                AND daily_rate IS NOT NULL
                AND deposit IS NOT NULL
            )
        )
    );