package com.allclear.socialhub.post.controller;

import com.allclear.socialhub.auth.util.AccessTokenUtil;
import com.allclear.socialhub.post.common.like.dto.PostLikeResponse;
import com.allclear.socialhub.post.common.share.dto.PostShareResponse;
import com.allclear.socialhub.post.dto.PostDetailResponse;
import com.allclear.socialhub.post.dto.PostPaging;
import com.allclear.socialhub.post.dto.PostResponse;
import com.allclear.socialhub.post.service.PostServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static com.allclear.socialhub.post.domain.PostType.FACEBOOK;
import static com.allclear.socialhub.post.domain.PostType.INSTAGRAM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostServiceImpl postService;

    @MockBean
    private AccessTokenUtil accessTokenUtil;

    private String jwt;
    private String username = "test";

    @Mock
    private Claims mockClaims;

    @BeforeEach
    void setUp() {

        jwt = "mockJwtToken"; // 실제 JWT 생성 로직을 대체하는 Mock 값
        when(accessTokenUtil.getClaims(anyString())).thenReturn(mockClaims);
        when(accessTokenUtil.extractUsername(any(Claims.class))).thenReturn("test");
    }

    @DisplayName("게시물 목록을 조회합니다.")
    @Test
    void getPosts() throws Exception {

        // given
        List<PostResponse> postList = List.of(
                new PostResponse(1L, 1L, INSTAGRAM, "망원동 맛집", "맛집 소개해요",
                        List.of("#망원동", "#맛집"), 10, 10, 10,
                        LocalDateTime.of(2024, 8, 25, 12, 0, 0),
                        LocalDateTime.of(2024, 8, 25, 12, 0, 0)),
                new PostResponse(2L, 1L, FACEBOOK, "영화 추천", "영화 추천합니다",
                        List.of("#영화", "#해리포터"), 20, 20, 20,
                        LocalDateTime.of(2024, 8, 25, 12, 0, 0),
                        LocalDateTime.of(2024, 8, 25, 12, 0, 0))
        );

        PostPaging postPaging = new PostPaging(2, postList, 10, 0, 1);
        ResponseEntity<PostPaging> result = new ResponseEntity<>(postPaging, HttpStatus.OK);

        when(postService.getPosts(any(Pageable.class))).thenReturn(result.getBody());

        // when // then
        mockMvc.perform(get("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("pageSize", "10")
                        .param("sort", "postId,DESC")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.postCnt").value(2))
                .andExpect(jsonPath("$.postList[0].postId").value(1))
                .andExpect(jsonPath("$.postList[0].type").value(INSTAGRAM.toString()))
                .andExpect(jsonPath("$.postList[0].title").value("망원동 맛집"))
                .andExpect(jsonPath("$.postList[0].content").value("맛집 소개해요"))
                .andExpect(jsonPath("$.postList[1].postId").value(2))
                .andExpect(jsonPath("$.postList[1].type").value(FACEBOOK.toString()))
                .andExpect(jsonPath("$.postList[1].title").value("영화 추천"))
                .andExpect(jsonPath("$.postList[1].content").value("영화 추천합니다"));

        verify(postService).getPosts(any(Pageable.class));
    }

    @DisplayName("게시물 상세를 조회합니다.")
    @Test
    void getPostDetail() throws Exception {
        // given
        PostDetailResponse response = createPostDetailResponse();

        when(postService.getPostDetail(response.getPostId(), "test")).thenReturn(response);

        // when // then
        mockMvc.perform(
                        get("/api/posts/{postId}", response.getPostId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(response.getPostId()))
                .andExpect(jsonPath("$.userId").value(response.getUserId()))
                .andExpect(jsonPath("$.type").value(response.getType().toString()))
                .andExpect(jsonPath("$.title").value(response.getTitle()))
                .andExpect(jsonPath("$.content").value(response.getContent()))
                .andExpect(jsonPath("$.hashtagList[0]").value(response.getHashtagList().get(0)))
                .andExpect(jsonPath("$.viewCnt").value(response.getViewCnt()))
                .andExpect(jsonPath("$.likeCnt").value(response.getLikeCnt()))
                .andExpect(jsonPath("$.shareCnt").value(response.getShareCnt()));
    }

    @DisplayName("게시물 좋아요를 추가합니다.")
    @Test
    void likePost() throws Exception {
        // given
        Long postId = 1L;

        String url = "https://www.facebook.com/likes/facebook";

        PostLikeResponse response = PostLikeResponse.builder()
                .postId(postId)
                .likeCnt(10)
                .url(url)
                .build();

        when(postService.likePost(postId, username)).thenReturn(response);

        // when // then
        mockMvc.perform(
                        post("/api/posts/like/{postId}", postId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.likeCnt").value(10))
                .andExpect(jsonPath("$.url").value(url));
    }

    @DisplayName("게시물 공유를 추가합니다.")
    @Test
    void sharePost() throws Exception {
        // given
        Long postId = 1L;
        String url = "https://www.facebook.com/share/facebook";

        PostShareResponse response = PostShareResponse.builder()
                .postId(postId)
                .shareCnt(10)
                .url(url)
                .build();

        when(postService.sharePost(postId, username)).thenReturn(response);

        // when // then
        mockMvc.perform(
                        post("/api/posts/share/{postId}", postId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.shareCnt").value(10))
                .andExpect(jsonPath("$.url").value(url));
    }

    // 게시물 상세 응답 빌더 생성
    private static PostDetailResponse createPostDetailResponse() {

        return PostDetailResponse.builder()
                .postId(1L)
                .userId(1L)
                .type(FACEBOOK)
                .title("게시물 상세 제목")
                .content("게시물 상세 내용")
                .hashtagList(List.of("#해시태그"))
                .viewCnt(10)
                .likeCnt(20)
                .shareCnt(30)
                .build();
    }

}
