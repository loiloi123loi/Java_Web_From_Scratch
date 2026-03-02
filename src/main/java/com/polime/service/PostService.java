package com.polime.service;

import java.sql.SQLException;
import java.time.OffsetDateTime;

import com.polime.core.DatabaseManager;
import com.polime.dto.BaseResponseDto;
import com.polime.dto.post.request.PostCreateDto;
import com.polime.dto.post.response.PostCreateResponseDto;
import com.polime.enums.EPostAudience;
import com.polime.enums.EPostType;
import com.polime.enums.EResponseCode;
import com.polime.exception.ResourceNotFoundException;
import com.polime.model.Post;
import com.polime.repository.PostRepository;

public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public BaseResponseDto<PostCreateResponseDto> createPost(Long userId, PostCreateDto dto) throws SQLException {
        try {
            DatabaseManager.beginTransaction();

            if (dto.getParentId() != null && !dto.getParentId().isBlank()) {
                Long parentId = Long.parseLong(dto.getParentId());
                Post parentPost = postRepository.findById(parentId);
                if (parentPost == null) {
                    throw new ResourceNotFoundException("Parent post not found");
                }
            }

            Post post = new Post();
            post.setUserId(userId);
            post.setType(EPostType.valueOf(dto.getType()));
            post.setAudience(EPostAudience.valueOf(dto.getAudience()));
            post.setContent(dto.getContent());
            post.setParentId(
                    dto.getParentId() != null && !dto.getParentId().isBlank() ? Long.parseLong(dto.getParentId())
                            : null);
            post.setCreatedAt(OffsetDateTime.now());
            post.setUpdatedAt(OffsetDateTime.now());

            Post savedPost = postRepository.save(post);

            DatabaseManager.commit();

            PostCreateResponseDto result = new PostCreateResponseDto(savedPost);
            return new BaseResponseDto<>("Post created successfully", EResponseCode.SUCCESS, result);
        } catch (Exception e) {
            DatabaseManager.rollback();
            throw e;
        }
    }
}
