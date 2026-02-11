ALTER TABLE user_profiles
ADD COLUMN avatar_media_id BIGINT;

ALTER TABLE user_profiles
ADD CONSTRAINT fk_user_profile_avatar_media
    FOREIGN KEY (avatar_media_id)
    REFERENCES medias(id)
    ON DELETE SET NULL;
