CREATE TABLE public.user_fcm_tokens (
    id bigserial NOT NULL,
    user_id bigint NOT NULL,
    fcm_token varchar(500) NOT NULL,
    created_at timestamp DEFAULT now() NULL,
    CONSTRAINT pk_user_fcm_tokens PRIMARY KEY (id),
    CONSTRAINT uk_user_fcm_tokens_token UNIQUE (fcm_token),
    CONSTRAINT fk_user_fcm_tokens_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_fcm_tokens_user ON public.user_fcm_tokens(user_id);
