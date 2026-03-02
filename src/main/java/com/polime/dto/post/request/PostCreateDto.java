package com.polime.dto.post.request;

import com.polime.enums.EPostAudience;
import com.polime.enums.EPostType;
import com.polime.exception.ValidationException;

public class PostCreateDto {
    private String type;
    private String audience;
    private String content;
    private String parentId;

    public void validate() {
        if (type == null || type.isBlank()) {
            throw new ValidationException("Post type is required");
        }

        EPostType postType;
        try {
            postType = EPostType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid post type: " + type);
        }

        if (audience == null || audience.isBlank()) {
            throw new ValidationException("Post audience is required");
        }

        try {
            EPostAudience.valueOf(audience);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid post audience: " + audience);
        }

        Long parentLongId = null;
        if (parentId != null && !parentId.isBlank()) {
            try {
                parentLongId = Long.parseLong(parentId);
            } catch (NumberFormatException e) {
                throw new ValidationException("Invalid parent id format: " + parentId);
            }
        }

        if (postType != EPostType.Post && parentLongId == null) {
            throw new ValidationException("Parent id is required for this post type");
        }

        if (postType == EPostType.Post && parentLongId != null) {
            throw new ValidationException("Parent id must be null for post type Post");
        }

        if (content == null) {
            content = "";
        }
        content = content.trim();

        if (postType != EPostType.Repost && content.isEmpty()) {
            throw new ValidationException("Content must be a non-empty string");
        }

        if (postType == EPostType.Repost && !content.isEmpty()) {
            throw new ValidationException("Content must be empty for repost type");
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
