ALTER TABLE public.conversations DROP CONSTRAINT IF EXISTS chk_conversations_type;
ALTER TABLE public.conversations ADD CONSTRAINT chk_conversations_type CHECK (((type)::text = ANY ((ARRAY['DIRECT'::character varying, 'MATCH_GROUP'::character varying, 'TEAM'::character varying])::text[])));

ALTER TABLE public.conversations ADD COLUMN name varchar(255);

ALTER TABLE public.teams ADD COLUMN conversation_id bigint;
ALTER TABLE public.teams ADD CONSTRAINT fk_teams_conversation FOREIGN KEY (conversation_id) REFERENCES public.conversations(id) ON DELETE SET NULL;
