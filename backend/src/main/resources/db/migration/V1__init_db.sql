CREATE TABLE public.users (
    id BIGSERIAL NOT NULL,
    created_at timestamp(6) NULL,
    email varchar(255) NULL,
    full_name varchar(255) NULL,
    "password" varchar(255) NULL,
    phone varchar(255) NULL,
    "role" varchar(255) NULL,
    trust_score int4 NULL,
    updated_at timestamp(6) NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone UNIQUE (phone),
    CONSTRAINT chk_users_role CHECK (((role)::text = ANY ((ARRAY['PLAYER'::character varying, 'OWNER'::character varying, 'ADMIN'::character varying])::text[])))
);

CREATE TABLE public.conversations (
    id BIGSERIAL NOT NULL,
    created_at timestamp(6) NULL,
    "type" varchar(255) NULL,
    CONSTRAINT conversations_pkey PRIMARY KEY (id),
    CONSTRAINT chk_conversations_type CHECK (((type)::text = ANY ((ARRAY['DIRECT'::character varying, 'MATCH_GROUP'::character varying])::text[])))
);

CREATE TABLE public.fields (
    id BIGSERIAL NOT NULL,
    cover_image varchar(255) NULL,
    created_at timestamp(6) NULL,
    "name" varchar(255) NULL,
    "type" varchar(255) NULL,
    updated_at timestamp(6) NULL,
    CONSTRAINT fields_pkey PRIMARY KEY (id),
    CONSTRAINT chk_fields_type CHECK (((type)::text = ANY ((ARRAY['FIVE_A_SIDE'::character varying, 'SEVEN_A_SIDE'::character varying, 'ELEVEN_A_SIDE'::character varying])::text[])))
);

CREATE TABLE public.conversation_members (
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT conversation_members_pkey PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT fk_conv_members_conversation FOREIGN KEY (conversation_id) REFERENCES public.conversations(id),
    CONSTRAINT fk_conv_members_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE TABLE public.teams (
    id BIGSERIAL NOT NULL,
    captain_id BIGINT NULL,
    created_at timestamp(6) NULL,
    description text NULL,
    "level" varchar(255) NULL,
    "name" varchar(255) NULL,
    CONSTRAINT teams_pkey PRIMARY KEY (id),
    CONSTRAINT chk_teams_level CHECK (((level)::text = ANY ((ARRAY['BEGINNER'::character varying, 'INTERMEDIATE'::character varying, 'ADVANCED'::character varying])::text[]))),
    CONSTRAINT fk_teams_captain FOREIGN KEY (captain_id) REFERENCES public.users(id)
);

CREATE TABLE public.time_slots (
    id BIGSERIAL NOT NULL,
    end_time timestamp(6) NULL,
    field_id BIGINT NULL,
    price numeric(38, 2) NULL,
    start_time timestamp(6) NULL,
    status varchar(255) NULL,
    CONSTRAINT time_slots_pkey PRIMARY KEY (id),
    CONSTRAINT chk_time_slots_status CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'PENDING'::character varying, 'BOOKED'::character varying])::text[]))),
    CONSTRAINT fk_time_slots_field FOREIGN KEY (field_id) REFERENCES public.fields(id)
);

CREATE TABLE public.match_posts (
    id BIGSERIAL NOT NULL,
    booking_id BIGINT NULL,
    cost_sharing varchar(255) NULL,
    created_at timestamp(6) NULL,
    "date" date NULL,
    field_id BIGINT NULL,
    message text NULL,
    post_type varchar(255) NULL,
    skill_level varchar(255) NULL,
    status varchar(255) NULL,
    team_id BIGINT NULL,
    time_end timestamp(6) NULL,
    time_start timestamp(6) NULL,
    user_id BIGINT NULL,
    CONSTRAINT match_posts_pkey PRIMARY KEY (id),
    CONSTRAINT chk_match_posts_type CHECK (((post_type)::text = ANY ((ARRAY['FIND_OPPONENT'::character varying, 'FIND_MEMBER'::character varying])::text[]))),
    CONSTRAINT chk_match_posts_skill CHECK (((skill_level)::text = ANY ((ARRAY['BEGINNER'::character varying, 'INTERMEDIATE'::character varying, 'ADVANCED'::character varying])::text[]))),
    CONSTRAINT chk_match_posts_status CHECK (((status)::text = ANY ((ARRAY['OPEN'::character varying, 'MATCHED'::character varying, 'CLOSED'::character varying, 'EXPIRED'::character varying])::text[]))),
    CONSTRAINT fk_match_posts_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE TABLE public.match_requests (
    id BIGSERIAL NOT NULL,
    created_at timestamp(6) NULL,
    message text NULL,
    post_id BIGINT NULL,
    requester_id BIGINT NULL,
    status varchar(255) NULL,
    CONSTRAINT match_requests_pkey PRIMARY KEY (id),
    CONSTRAINT chk_match_req_status CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying])::text[]))),
    CONSTRAINT fk_match_req_post FOREIGN KEY (post_id) REFERENCES public.match_posts(id),
    CONSTRAINT fk_match_req_requester FOREIGN KEY (requester_id) REFERENCES public.users(id)
);

CREATE TABLE public.messages (
    id BIGSERIAL NOT NULL,
    "content" text NULL,
    conversation_id BIGINT NULL,
    created_at timestamp(6) NULL,
    sender_id BIGINT NULL,
    CONSTRAINT messages_pkey PRIMARY KEY (id),
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES public.users(id),
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES public.conversations(id)
);

CREATE TABLE public.notifications (
    id BIGSERIAL NOT NULL,
    "content" text NULL,
    created_at timestamp(6) NULL,
    is_read bool NULL,
    title varchar(255) NULL,
    "type" varchar(255) NULL,
    user_id BIGINT NULL,
    CONSTRAINT notifications_pkey PRIMARY KEY (id),
    CONSTRAINT chk_notifications_type CHECK (((type)::text = ANY ((ARRAY['SYSTEM'::character varying, 'BOOKING_UPDATE'::character varying, 'MATCH_REQUEST'::character varying, 'NEW_MESSAGE'::character varying, 'USER_UPDATE'::character varying, 'PAYMENT_UPDATE'::character varying])::text[]))),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE TABLE public.bookings (
    id BIGSERIAL NOT NULL,
    booking_date date NULL,
    created_at timestamp(6) NULL,
    deposit_amount numeric(38, 2) NULL,
    field_id BIGINT NULL,
    note text NULL,
    status varchar(255) NULL,
    time_slot_id BIGINT NULL,
    total_amount numeric(38, 2) NULL,
    updated_at timestamp(6) NULL,
    user_id BIGINT NULL,
    CONSTRAINT bookings_pkey PRIMARY KEY (id),
    CONSTRAINT chk_bookings_status CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'DEPOSIT_PAID'::character varying, 'CONFIRMED'::character varying, 'CANCELLED'::character varying, 'COMPLETED'::character varying])::text[]))),
    CONSTRAINT fk_bookings_field FOREIGN KEY (field_id) REFERENCES public.fields(id),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES public.users(id),
    CONSTRAINT fk_bookings_time_slot FOREIGN KEY (time_slot_id) REFERENCES public.time_slots(id)
);

CREATE TABLE public.payments (
    id BIGSERIAL NOT NULL,
    amount numeric(38, 2) NULL,
    booking_id BIGINT NULL,
    created_at timestamp(6) NULL,
    payment_method varchar(255) NULL,
    status varchar(255) NULL,
    stripe_payment_intent_id varchar(255) NULL,
    user_id BIGINT NULL,
    CONSTRAINT payments_pkey PRIMARY KEY (id),
    CONSTRAINT chk_payments_method CHECK (((payment_method)::text = ANY ((ARRAY['STRIPE'::character varying, 'CASH'::character varying])::text[]))),
    CONSTRAINT chk_payments_status CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'REFUNDED'::character varying])::text[]))),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES public.bookings(id),
    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE TABLE public.reviews (
    id BIGSERIAL NOT NULL,
    ai_suggested_penalty int4 NULL,
    created_at timestamp(6) NULL,
    match_request_id BIGINT NULL,
    reason text NULL,
    reviewee_id BIGINT NULL,
    reviewer_id BIGINT NULL,
    score_change int4 NULL,
    status varchar(50) NULL,
    CONSTRAINT reviews_pkey PRIMARY KEY (id),
    CONSTRAINT chk_reviews_status CHECK (((status)::text = ANY ((ARRAY['AUTO_PASSED'::character varying, 'PENDING_ADMIN_REVIEW'::character varying, 'PENALIZED'::character varying])::text[]))),
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES public.users(id),
    CONSTRAINT fk_reviews_reviewee FOREIGN KEY (reviewee_id) REFERENCES public.users(id),
    CONSTRAINT fk_reviews_match_request FOREIGN KEY (match_request_id) REFERENCES public.match_requests(id)
);
