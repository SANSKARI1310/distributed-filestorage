ALTER table file_metadata
ADD COLUMN upload_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
