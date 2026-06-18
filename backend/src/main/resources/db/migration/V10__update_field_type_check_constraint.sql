-- Drop old check constraint on field types
ALTER TABLE fields DROP CONSTRAINT IF EXISTS chk_fields_type;

-- Update any existing ELEVEN_A_SIDE fields to SEVEN_A_SIDE to prevent validation issues
UPDATE fields SET type = 'SEVEN_A_SIDE' WHERE type = 'ELEVEN_A_SIDE';

-- Add new check constraint allowing only FIVE_A_SIDE and SEVEN_A_SIDE
ALTER TABLE fields ADD CONSTRAINT chk_fields_type CHECK (type IN ('FIVE_A_SIDE', 'SEVEN_A_SIDE'));
