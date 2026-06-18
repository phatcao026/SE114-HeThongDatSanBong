-- Thêm các cột quản lý số lượng thành viên ghép kèo
ALTER TABLE public.match_posts ADD COLUMN needed_members INT DEFAULT 1;
ALTER TABLE public.match_posts ADD COLUMN joined_members INT DEFAULT 0;

-- Thêm liên kết nhóm chat vào bài đăng
ALTER TABLE public.match_posts ADD COLUMN conversation_id BIGINT;
ALTER TABLE public.match_posts ADD CONSTRAINT fk_match_posts_conversation 
    FOREIGN KEY (conversation_id) REFERENCES public.conversations(id) ON DELETE SET NULL;

-- Thêm các trường hỗ trợ đặc thù của 2 loại bài đăng
ALTER TABLE public.match_posts ADD COLUMN has_field BOOLEAN DEFAULT TRUE;
ALTER TABLE public.match_posts ADD COLUMN target_positions VARCHAR(100);
ALTER TABLE public.match_posts ADD COLUMN age_range VARCHAR(50);
