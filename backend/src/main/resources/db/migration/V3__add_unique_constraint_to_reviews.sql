CREATE UNIQUE INDEX uk_reviews_match_request_reviewer
    ON public.reviews(match_request_id, reviewer_id)
    WHERE match_request_id IS NOT NULL AND reviewer_id IS NOT NULL;
