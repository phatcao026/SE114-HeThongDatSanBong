CREATE TABLE public.field_reviews (
    id BIGSERIAL NOT NULL,
    booking_id BIGINT NOT NULL,
    field_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    image_url VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT field_reviews_pkey PRIMARY KEY (id),
    CONSTRAINT uk_field_reviews_booking UNIQUE (booking_id),
    CONSTRAINT chk_field_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_field_reviews_booking FOREIGN KEY (booking_id) REFERENCES public.bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_field_reviews_field FOREIGN KEY (field_id) REFERENCES public.fields(id) ON DELETE CASCADE,
    CONSTRAINT fk_field_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES public.users(id) ON DELETE CASCADE
);
