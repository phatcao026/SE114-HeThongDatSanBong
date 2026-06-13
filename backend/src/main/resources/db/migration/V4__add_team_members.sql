CREATE TABLE IF NOT EXISTS public.team_members (
    id BIGSERIAL NOT NULL,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status varchar(50) NOT NULL,
    created_at timestamp(6) NULL,
    CONSTRAINT team_members_pkey PRIMARY KEY (id),
    CONSTRAINT uk_team_members_team_user UNIQUE (team_id, user_id),
    CONSTRAINT chk_team_members_status CHECK (
        (status)::text = ANY (
            (ARRAY[
                'PENDING'::character varying,
                'ACCEPTED'::character varying,
                'REJECTED'::character varying
            ])::text[]
        )
    ),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES public.teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_team_members_team_id ON public.team_members(team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user_id ON public.team_members(user_id);
CREATE INDEX IF NOT EXISTS idx_team_members_status ON public.team_members(status);

INSERT INTO public.team_members (team_id, user_id, status, created_at)
SELECT id, captain_id, 'ACCEPTED', COALESCE(created_at, NOW())
FROM public.teams
WHERE captain_id IS NOT NULL
ON CONFLICT (team_id, user_id) DO NOTHING;

ALTER TABLE public.notifications DROP CONSTRAINT IF EXISTS chk_notifications_type;
ALTER TABLE public.notifications ADD CONSTRAINT chk_notifications_type CHECK (
    (type)::text = ANY (
        (ARRAY[
            'SYSTEM'::character varying,
            'BOOKING_UPDATE'::character varying,
            'MATCH_REQUEST'::character varying,
            'NEW_MESSAGE'::character varying,
            'USER_UPDATE'::character varying,
            'PAYMENT_UPDATE'::character varying,
            'TEAM_INVITE'::character varying
        ])::text[]
    )
);
