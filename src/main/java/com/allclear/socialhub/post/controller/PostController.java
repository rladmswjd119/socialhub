package com.allclear.socialhub.post.controller;

import com.allclear.socialhub.auth.dto.UserDetailsImpl;
import com.allclear.socialhub.auth.util.AccessTokenUtil;
import com.allclear.socialhub.post.common.like.dto.PostLikeResponse;
import com.allclear.socialhub.post.common.share.dto.PostShareResponse;
import com.allclear.socialhub.post.domain.PostType;
import com.allclear.socialhub.post.dto.*;
import com.allclear.socialhub.post.service.PostService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Tag(name = "Post", description = "게시물 API")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시물 등록", description = "게시물을 등록합니다.")
    @PostMapping
    public ResponseEntity<PostResponse> creatPost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PostCreateRequest requestDto) {

        String username = userDetails.getUsername();
        return ResponseEntity.status(201).body(postService.createPost(username, requestDto));
    }

    @Operation(summary = "게시물 수정", description = "게시물을 수정합니다.")
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PostUpdateRequest updateRequest,
            @PathVariable("postId") Long postId) {

        String username = userDetails.getUsername();
        return ResponseEntity.status(200).body(postService.updatePost(username, postId, updateRequest));
    }

    @Operation(summary = "게시글 삭제", description = "게시물을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("postId") Long postId) {

        String username = userDetails.getUsername();
        postService.deletePost(username, postId);
        return ResponseEntity.status(200).body("성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "게시물 검색 목록 조회", description = "게시물 검색 목록을 조회합니다.")
    @GetMapping("/search")
    public ResponseEntity<PostPaging> searchPosts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault Pageable pageable,
            @RequestParam(value = "hashtag", required = false) String hashtag,
            @RequestParam(value = "type", required = false) PostType type,
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "orderBy", required = false, defaultValue = "created_at") String orderBy,
            @RequestParam(value = "sort", required = false, defaultValue = "desc") String sort,
            @RequestParam(value = "searchBy", required = false, defaultValue = "title") String searchBy) {

        String username = userDetails.getUsername();

        return ResponseEntity.status(200)
                .body(postService.searchPosts(pageable, username, hashtag, type, query, orderBy, sort, searchBy));
    }

    @Operation(summary = "게시물 목록 조회", description = "게시물 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<PostPaging> getPosts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault Pageable pageable) {

        return ResponseEntity.status(200).body(postService.getPosts(pageable));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시물 상세 조회", description = "게시물 상세를 조회합니다.")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("postId") Long postId) {

        String username = userDetails.getUsername();
        return ResponseEntity.status(200).body(postService.getPostDetail(postId, username));
    }

    @Operation(summary = "게시물 좋아요", description = "게시물 좋아요를 추가합니다.")
    @PostMapping("/like/{postId}")
    public ResponseEntity<PostLikeResponse> likePost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("postId") Long postId) {

        String username = userDetails.getUsername();
        return ResponseEntity.status(201).body(postService.likePost(postId, username));
    }

    @Operation(summary = "게시물 공유", description = "게시물 공유를 추가합니다.")
    @PostMapping("/share/{postId}")
    public ResponseEntity<PostShareResponse> sharePost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("postId") Long postId) {

        String username = userDetails.getUsername();
        return ResponseEntity.status(201).body(postService.sharePost(postId, username));
    }

}
