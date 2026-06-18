-- 1. Xóa các bảng liên quan đến Team
DROP TABLE IF EXISTS public.team_members CASCADE;
DROP TABLE IF EXISTS public.teams CASCADE;

-- 2. Xóa cột team_id khỏi match_posts
ALTER TABLE public.match_posts DROP COLUMN IF EXISTS team_id;

-- 3. Cập nhật lại check constraint cho bảng conversations (loại bỏ loại TEAM)
ALTER TABLE public.conversations DROP CONSTRAINT IF EXISTS chk_conversations_type;
ALTER TABLE public.conversations ADD CONSTRAINT chk_conversations_type CHECK (
    ((type)::text = ANY (ARRAY['DIRECT'::character varying, 'MATCH_GROUP'::character varying]::text[]))
);

-- 4. Cập nhật lại check constraint cho bảng notifications (loại bỏ loại TEAM_INVITE)
ALTER TABLE public.notifications DROP CONSTRAINT IF EXISTS chk_notifications_type;
ALTER TABLE public.notifications ADD CONSTRAINT chk_notifications_type CHECK (
    ((type)::text = ANY (ARRAY[
        'SYSTEM'::character varying,
        'BOOKING_UPDATE'::character varying,
        'MATCH_REQUEST'::character varying,
        'NEW_MESSAGE'::character varying,
        'USER_UPDATE'::character varying,
        'PAYMENT_UPDATE'::character varying
    ]::text[]))
);
