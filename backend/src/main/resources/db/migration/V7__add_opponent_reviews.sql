CREATE TABLE public.opponent_reviews (
    id BIGSERIAL NOT NULL,
    match_id BIGINT,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    rating_type VARCHAR(50) NOT NULL, -- GOOD, NO_SHOW, BAD_BEHAVIOR
    comment TEXT,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, RESOLVED, REJECTED
    points_applied INT DEFAULT 0,
    image_url VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT opponent_reviews_pkey PRIMARY KEY (id),
    CONSTRAINT fk_or_reviewer FOREIGN KEY (reviewer_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_or_reviewee FOREIGN KEY (reviewee_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_or_match FOREIGN KEY (match_id) REFERENCES public.match_posts(id) ON DELETE SET NULL
);
