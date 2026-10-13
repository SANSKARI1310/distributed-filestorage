

CREATE TABLE IF NOT EXISTS organization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    storage_quota_bytes BIGINT NOT NULL DEFAULT 10737418240,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE user_role AS ENUM ('ADMIN', 'READER' , 'WRITER');

CREATE TABLE IF NOT EXISTS user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP.
);

CREATE TABLE IF NOT EXISTS membership (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES user(id) ON DELETE CASCADE,
    role user_role NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    CONSTRAINT uk_user_org UNIQUE (user_id, organization_id)
);

CREATE TABLE IF NOT EXISTS file_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    uploaded_by_id UUID NOT NULL REFERENCES user(id) ON DELETE CASCADE,
    storage_key VARCHAR(1024) NOT NULL UNIQUE,
    file_name VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(255) NOT NULL,
    checksum_sha256 VARCHAR(64) NOT NULL,
    version BIGINT NOT NULL DEFAULT 1,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
);

CREATE TABLE storage_data(

    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    used_bytes BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX idx_memberships_org_id ON membership(organization_id);
CREATE INDEX idx_memberships_user_id ON membership(user_id);
CREATE INDEX idx_file_metadata_org_id ON file_metadata(organization_id);
