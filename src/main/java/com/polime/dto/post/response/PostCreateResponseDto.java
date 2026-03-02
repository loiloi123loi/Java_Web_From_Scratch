package com.polime.dto.post.response;

import com.polime.model.Post;

public class PostCreateResponseDto {
    private Long id;
    private Long userId;
    private String type;
    private String audience;
    private String content;
    private Long parentId;
    private String createdAt;
    private String updatedAt;

    public PostCreateResponseDto(Post post) {
        this.id = post.getId();
        this.userId = post.getUserId();
        this.type = post.getType().name();
        this.audience = post.getAudience().name();
        this.content = post.getContent();
        this.parentId = post.getParentId();
        this.createdAt = post.getCreatedAt().toString();
        this.updatedAt = post.getUpdatedAt().toString();
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
