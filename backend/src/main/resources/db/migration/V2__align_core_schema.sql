ALTER TABLE public.fields
    ADD COLUMN owner_id BIGINT NULL,
    ADD COLUMN address varchar(255) NULL,
    ADD COLUMN description text NULL,
    ADD COLUMN status varchar(255) NULL;

ALTER TABLE public.fields
    ADD CONSTRAINT chk_fields_status
        CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'MAINTENANCE'::character varying, 'BOOKED'::character varying])::text[]))),
    ADD CONSTRAINT fk_fields_owner
        FOREIGN KEY (owner_id) REFERENCES public.users(id);

ALTER TABLE public.time_slots
    ALTER COLUMN start_time TYPE time(6) USING start_time::time,
    ALTER COLUMN end_time TYPE time(6) USING end_time::time;

ALTER TABLE public.match_posts
    ALTER COLUMN time_start TYPE time(6) USING time_start::time,
    ALTER COLUMN time_end TYPE time(6) USING time_end::time;

ALTER TABLE public.match_posts
    ADD CONSTRAINT fk_match_posts_team
        FOREIGN KEY (team_id) REFERENCES public.teams(id),
    ADD CONSTRAINT fk_match_posts_field
        FOREIGN KEY (field_id) REFERENCES public.fields(id),
    ADD CONSTRAINT fk_match_posts_booking
        FOREIGN KEY (booking_id) REFERENCES public.bookings(id);

CREATE INDEX idx_users_role ON public.users(role);
CREATE INDEX idx_users_trust_score ON public.users(trust_score);

CREATE INDEX idx_fields_owner_id ON public.fields(owner_id);
CREATE INDEX idx_fields_type ON public.fields(type);
CREATE INDEX idx_fields_status ON public.fields(status);

CREATE INDEX idx_time_slots_field_id ON public.time_slots(field_id);
CREATE INDEX idx_time_slots_status ON public.time_slots(status);

CREATE INDEX idx_bookings_user_id ON public.bookings(user_id);
CREATE INDEX idx_bookings_field_id ON public.bookings(field_id);
CREATE INDEX idx_bookings_time_slot_id ON public.bookings(time_slot_id);
CREATE INDEX idx_bookings_status ON public.bookings(status);
CREATE INDEX idx_bookings_field_date ON public.bookings(field_id, booking_date);

CREATE UNIQUE INDEX uk_bookings_active_slot
    ON public.bookings(field_id, time_slot_id, booking_date)
    WHERE status IN ('PENDING', 'DEPOSIT_PAID', 'CONFIRMED', 'COMPLETED');

CREATE INDEX idx_payments_booking_id ON public.payments(booking_id);
CREATE INDEX idx_payments_user_id ON public.payments(user_id);
CREATE UNIQUE INDEX uk_payments_stripe_payment_intent_id
    ON public.payments(stripe_payment_intent_id)
    WHERE stripe_payment_intent_id IS NOT NULL;

CREATE INDEX idx_match_posts_user_id ON public.match_posts(user_id);
CREATE INDEX idx_match_posts_team_id ON public.match_posts(team_id);
CREATE INDEX idx_match_posts_field_id ON public.match_posts(field_id);
CREATE INDEX idx_match_posts_booking_id ON public.match_posts(booking_id);
CREATE INDEX idx_match_posts_status_date ON public.match_posts(status, date);

CREATE INDEX idx_match_requests_post_id ON public.match_requests(post_id);
CREATE INDEX idx_match_requests_requester_id ON public.match_requests(requester_id);
CREATE INDEX idx_match_requests_status ON public.match_requests(status);

CREATE INDEX idx_messages_conversation_id ON public.messages(conversation_id);
CREATE INDEX idx_messages_sender_id ON public.messages(sender_id);

CREATE INDEX idx_notifications_user_id ON public.notifications(user_id);
CREATE INDEX idx_notifications_user_read ON public.notifications(user_id, is_read);

CREATE INDEX idx_reviews_reviewer_id ON public.reviews(reviewer_id);
CREATE INDEX idx_reviews_reviewee_id ON public.reviews(reviewee_id);
CREATE INDEX idx_reviews_match_request_id ON public.reviews(match_request_id);
