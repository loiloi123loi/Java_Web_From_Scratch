package com.polime.model;

import java.time.OffsetDateTime;

import com.polime.enums.EPostAudience;
import com.polime.enums.EPostType;

public class Post {
    private Long id;
    private Long userId;
    private EPostType type;
    private EPostAudience audience;
    private String content;
    private Long parentId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Post() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public EPostType getType() {
        return type;
    }

    public void setType(EPostType type) {
        this.type = type;
    }

    public EPostAudience getAudience() {
        return audience;
    }

    public void setAudience(EPostAudience audience) {
        this.audience = audience;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
