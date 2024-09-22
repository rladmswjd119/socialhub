package com.allclear.socialhub.post.service;

import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.post.common.hashtag.domain.Hashtag;
import com.allclear.socialhub.post.common.hashtag.domain.PostHashtag;
import com.allclear.socialhub.post.common.hashtag.repository.HashtagRepository;
import com.allclear.socialhub.post.common.hashtag.repository.PostHashtagRepository;
import com.allclear.socialhub.post.common.like.dto.PostLikeResponse;
import com.allclear.socialhub.post.common.like.repository.PostLikeRepository;
import com.allclear.socialhub.post.common.share.dto.PostShareResponse;
import com.allclear.socialhub.post.common.share.repository.PostShareRepository;
import com.allclear.socialhub.post.common.view.repository.PostViewRepository;
import com.allclear.socialhub.post.domain.Post;
import com.allclear.socialhub.post.domain.PostType;
import com.allclear.socialhub.post.dto.*;
import com.allclear.socialhub.post.repository.PostRepository;
import com.allclear.socialhub.user.domain.User;
import com.allclear.socialhub.user.repository.UserRepository;
import com.allclear.socialhub.user.type.UserCertifyStatus;
import com.allclear.socialhub.user.type.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.allclear.socialhub.common.exception.ErrorCode.POST_NOT_FOUND;
import static com.allclear.socialhub.common.exception.ErrorCode.POST_OWNER_MISMATCH;
import static com.allclear.socialhub.post.domain.PostType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class PostServiceImplTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostServiceImpl postService;

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostShareRepository postShareRepository;

    @Autowired
    private PostViewRepository postViewRepository;

    static
    List<String> hashtagList = Arrays.asList("#테스트", "#자바", "#스프링");

    @AfterEach
    void tearDown() {

        postShareRepository.deleteAllInBatch();
        postLikeRepository.deleteAllInBatch();
        postHashtagRepository.deleteAllInBatch();
        hashtagRepository.deleteAllInBatch();
        postViewRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("게시물을 등록합니다.")
    void createPost() {
        // given
        User user = createUser();

        PostCreateRequest request = PostCreateRequest.builder()
                .type(INSTAGRAM)
                .title("테스트제목")
                .content("테스트내용")
                .hashtagList(hashtagList)
                .build();

        // when
        PostResponse response = postService.createPost(user.getUsername(), request);

        // then
        assertNotNull(response);
        assertEquals("테스트제목", response.getTitle());
        assertEquals("테스트내용", response.getContent());
        assertEquals(3, response.getHashtagList().size());
    }

    @Test
    @DisplayName("존재하지 않는 게시물 타입으로 게시물을 등록합니다.")
    void createPostWithNonExistType() {
        // given
        User user = createUser();

        PostCreateRequest request = PostCreateRequest.builder()
                .type(null)
                .title("테스트제목")
                .content("테스트내용")
                .hashtagList(hashtagList)
                .build();

        // when & then
        assertThatThrownBy(() -> postService.createPost(user.getUsername(), request)).isInstanceOf(DataIntegrityViolationException.class);

    }

    @Test
    @DisplayName("게시물 등록 시 제목은 필수 입력값입니다.")
    void createPostWithoutTitle() {
        // given
        User user = createUser();

        PostCreateRequest request = PostCreateRequest.builder()
                .type(FACEBOOK)
                .title(null)
                .content("테스트내용")
                .hashtagList(hashtagList)
                .build();

        // when & then
        assertThatThrownBy(() -> postService.createPost(user.getUsername(), request)).isInstanceOf(DataIntegrityViolationException.class);

    }

    @Test
    @DisplayName("게시물 등록 시 내용은 필수 입력값입니다.")
    void createPostWithoutContent() {
        // given
        User user = createUser();

        PostCreateRequest request = PostCreateRequest.builder()
                .type(FACEBOOK)
                .title("테스트제목")
                .content(null)
                .hashtagList(hashtagList)
                .build();

        // when & then
        assertThatThrownBy(() -> postService.createPost(user.getUsername(), request)).isInstanceOf(DataIntegrityViolationException.class);

    }

    @Test
    @DisplayName("게시물을 수정합니다.")
    void updatePost() {
        // given
        User user = createUser();
        userRepository.save(user);

        Post post = createPost(user, "테스트제목", "테스트내용", INSTAGRAM, 0, 0, 0);
        postRepository.save(post);

        Hashtag hashtag = createHashtag("#해시태그");
        hashtagRepository.save(hashtag);

        PostHashtag postHashtag = createPostHashtag(post, hashtag);
        postHashtagRepository.save(postHashtag);

        PostUpdateRequest updateRequest = PostUpdateRequest.builder()
                .title("테스트제목수정")
                .content("테스트내용수정")
                .hashtagList(hashtagList)
                .build();

        // when
        PostResponse response = postService.updatePost(user.getUsername(), post.getId(), updateRequest);

        // then
        assertNotNull(response);
        assertEquals("테스트제목수정", response.getTitle());
        assertEquals("테스트내용수정", response.getContent());
        assertEquals(3, response.getHashtagList().size());

    }

    @Test
    @DisplayName("본인 글이 아닌 게시물을 수정합니다.")
    void updatePostWithNotPostAuthor() {
        // given
        User user = createUser();
        userRepository.save(user);
        User anotherUser = User.builder()
                .username("testUsername2")
                .email("testEmail2@test.com")
                .password("testPassword@2")
                .status(UserStatus.ACTIVE)
                .certifyStatus(UserCertifyStatus.AUTHENTICATED)
                .build();

        userRepository.save(anotherUser);

        Post post = createPost(user, "테스트제목", "테스트내용", INSTAGRAM, 0, 0, 0);
        postRepository.save(post);

        Hashtag hashtag = createHashtag("#해시태그");
        hashtagRepository.save(hashtag);

        PostHashtag postHashtag = createPostHashtag(post, hashtag);
        postHashtagRepository.save(postHashtag);

        PostUpdateRequest updateRequest = PostUpdateRequest.builder()
                .title("테스트제목수정")
                .content("테스트내용수정")
                .hashtagList(hashtagList)
                .build();

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.updatePost(anotherUser.getUsername(), post.getId(), updateRequest));

        assertEquals(POST_OWNER_MISMATCH, exception.getErrorCode());

    }

    @Test
    @DisplayName("존재하지 않는 게시물 id로 게시물을 수정합니다.")
    void updatePostWithNoExistPost() {
        // given
        User user = createUser();
        userRepository.save(user);

        Post post = createPost(user, "테스트제목", "테스트내용", INSTAGRAM, 0, 0, 0);
        postRepository.save(post);

        Hashtag hashtag = createHashtag("#해시태그");
        hashtagRepository.save(hashtag);

        PostHashtag postHashtag = createPostHashtag(post, hashtag);
        postHashtagRepository.save(postHashtag);

        PostUpdateRequest updateRequest = PostUpdateRequest.builder()
                .title("테스트제목수정")
                .content("테스트내용수정")
                .hashtagList(hashtagList)
                .build();

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.updatePost(user.getUsername(), post.getId() + 100, updateRequest));

        assertEquals(POST_NOT_FOUND, exception.getErrorCode());

    }

    @Test
    @DisplayName("게시물 수정 시 제목은 필수 입력값입니다.")
    void updatePostWithoutTitle() {
        // given
        User user = createUser();
        userRepository.save(user);

        Post post = createPost(user, "테스트제목", "테스트내용", TWITTER, 0, 0, 0);
        postRepository.save(post);

        Hashtag hashtag = createHashtag("#해시태그");
        hashtagRepository.save(hashtag);

        PostHashtag postHashtag = createPostHashtag(post, hashtag);
        postHashtagRepository.save(postHashtag);

        PostUpdateRequest updateRequest = PostUpdateRequest.builder()
                .title(null)
                .content("테스트내용수정")
                .hashtagList(hashtagList)
                .build();

        // when & then
        assertThatThrownBy(() -> postService.updatePost(user.getUsername(), post.getId(), updateRequest)).isInstanceOf(DataIntegrityViolationException.class);

    }

    @Test
    @DisplayName("게시물 수정 시 내용은 필수 입력값입니다.")
    void updatePostWithoutContent() {
        // given
        User user = createUser();

        Post post = createPost(user, "테스트제목", "테스트내용", TWITTER, 0, 0, 0);
        postRepository.save(post);

        Hashtag hashtag = createHashtag("#해시태그");
        hashtagRepository.save(hashtag);

        PostHashtag postHashtag = createPostHashtag(post, hashtag);
        postHashtagRepository.save(postHashtag);

        PostUpdateRequest updateRequest = PostUpdateRequest.builder()
                .title(null)
                .content("테스트내용수정")
                .hashtagList(hashtagList)
                .build();

        // when & then
        assertThatThrownBy(() -> postService.updatePost(user.getUsername(), post.getId(), updateRequest)).isInstanceOf(DataIntegrityViolationException.class);

    }

    @Test
    @DisplayName("게시물을 삭제합니다.")
    void deletePost() {
        // given
        User user = createUser();
        userRepository.save(user);

        Post post = createPost(user, "테스트제목", "테스트내용", THREADS, 0, 0, 0);
        postRepository.save(post);

        Hashtag hashtag = createHashtag("#해시태그");
        hashtagRepository.save(hashtag);

        PostHashtag postHashtag = createPostHashtag(post, hashtag);
        postHashtagRepository.save(postHashtag);

        // when
        postService.deletePost(user.getUsername(), post.getId());

        // then
        Optional<Post> deletedEntity = postRepository.findById(post.getId());
        assertThat(deletedEntity).isEmpty(); // 엔티티가 존재하지 않아야 함

    }

    @Test
    @DisplayName("본인 글이 아닌 게시물을 삭제합니다.")
    void deletePostWithNotPostAuthor() {
        // given
        User user = createUser();
        userRepository.save(user);
        User anotherUser = User.builder()
                .username("testUsername2")
                .email("testEmail2@test.com")
                .password("testPassword@2")
                .status(UserStatus.ACTIVE)
                .certifyStatus(UserCertifyStatus.AUTHENTICATED)
                .build();

        userRepository.save(anotherUser);

        Post post = createPost(user, "테스트제목", "테스트내용", INSTAGRAM, 0, 0, 0);
        postRepository.save(post);

        Hashtag hashtag = createHashtag("#해시태그");
        hashtagRepository.save(hashtag);

        PostHashtag postHashtag = createPostHashtag(post, hashtag);
        postHashtagRepository.save(postHashtag);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.deletePost(anotherUser.getUsername(), post.getId()));

        assertEquals(POST_OWNER_MISMATCH, exception.getErrorCode());

    }
  
    @Test
    @DisplayName("게시물 검색 목록을 조회하고 조건에 알맞는 게시물이 조회되는 지 확인합니다.")
    void searchPosts() {
        // given
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Order.desc("id")));

        User user = createUser();
        userRepository.save(user);

        Post post1 = createPost(user, "제목1", "내용1", INSTAGRAM, 10, 10, 10);
        Post post2 = createPost(user, "제목2", "내용2", INSTAGRAM, 20, 20, 20);
        Post post3 = createPost(user, "제목3", "내용3", INSTAGRAM, 30, 30, 30);

        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);

        Hashtag hashtag1 = createHashtag("#해시태그");

        hashtagRepository.save(hashtag1);

        PostHashtag postHashtag1 = createPostHashtag(post1, hashtag1);
        PostHashtag postHashtag2 = createPostHashtag(post2, hashtag1);
        PostHashtag postHashtag3 = createPostHashtag(post3, hashtag1);
        postHashtagRepository.save(postHashtag1);
        postHashtagRepository.save(postHashtag2);
        postHashtagRepository.save(postHashtag3);

        // when
        PostPaging postPaging = postService.searchPosts(pageable, user.getUsername(), hashtag1.getContent(), INSTAGRAM, "", "viewCnt", "desc", "");

        // then
        assertEquals(3, postPaging.getPostCnt());

        // 각 게시물에 '#해시태그'가 포함되어 있는지 검증
        postPaging.getPostList().forEach(post -> assertTrue(((PostListResponse) post).getHashtagList().contains("#해시태그")));

    }

    @DisplayName("게시물 목록을 조회합니다.")
    @Test
    void getPosts() {
        // given
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Order.desc("id")));

        User user = createUser();

        Post post1 = createPost(user, "제목1", "내용1", INSTAGRAM, 10, 10, 10);
        Post post2 = createPost(user, "제목2", "내용2", FACEBOOK, 20, 20, 20);
        Post post3 = createPost(user, "제목3", "내용3", TWITTER, 30, 30, 30);

        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);

        // when // then
        assertThat(postService.getPosts(pageable).getPostList()).hasSize(3)
                .extracting("title", "content", "type", "likeCnt", "shareCnt", "viewCnt")
                .containsExactlyInAnyOrder(
                        tuple("제목3", "내용3", TWITTER, 30, 30, 30),
                        tuple("제목2", "내용2", FACEBOOK, 20, 20, 20),
                        tuple("제목1", "내용1", INSTAGRAM, 10, 10, 10)
                );
    }

    @DisplayName("게시물 상세를 조회합니다.")
    @Test
    void getPostDetail() {
        // given
        User user = createUser();

        Post post = createPost(user, "게시물 상세 제목", "게시물 상세 내용", INSTAGRAM, 10, 20, 30);
        postRepository.save(post);

        Hashtag hashtag = createHashtag("#해시태그");
        hashtagRepository.save(hashtag);

        PostHashtag postHashtag = createPostHashtag(post, hashtag);
        postHashtagRepository.save(postHashtag);

        // when
        PostDetailResponse getDetail = postService.getPostDetail(post.getId(), user.getUsername());

        // then
        assertThat(getDetail).isNotNull();
        assertThat(getDetail.getPostId()).isEqualTo(post.getId());
        assertThat(getDetail.getTitle()).isEqualTo(post.getTitle());
        assertThat(getDetail.getContent()).isEqualTo(post.getContent());
        assertThat(getDetail.getType()).isEqualTo(post.getType());
        assertThat(getDetail.getViewCnt()).isEqualTo(post.getViewCnt() + 1);
        assertThat(getDetail.getLikeCnt()).isEqualTo(post.getLikeCnt());
        assertThat(getDetail.getShareCnt()).isEqualTo(post.getShareCnt());
    }

    @DisplayName("존재하지 않는 게시물 ID로 게시물 상세를 조회합니다.")
    @Test
    void getPostDetailWithNonExistentPostId() {
        // given
        User user = createUser();

        // when // then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.getPostDetail(-1L, user.getUsername()));

        assertEquals(POST_NOT_FOUND, exception.getErrorCode());
    }

    @DisplayName("게시물 좋아요를 추가합니다.")
    @Test
    void likePost() {
        // given
        User user = createUser();

        Post post = createPost(user, "제목1", "내용1", INSTAGRAM, 10, 10, 10);
        postRepository.save(post);

        // when
        PostLikeResponse postLikeResponse = postService.likePost(post.getId(), user.getUsername());

        // then
        assertThat(postLikeResponse)
                .extracting("postId", "likeCnt", "url")
                .contains(post.getId(), 11, "https://www.instagram.com/likes/instagram");
    }

    @DisplayName("존재하지 않는 게시물 ID로 게시물 좋아요를 추가합니다.")
    @Test
    void likePostWithNonExistentPostId() {
        // given
        User user = createUser();

        // when // then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.likePost(-1L, user.getUsername()));

        assertEquals(POST_NOT_FOUND, exception.getErrorCode());
    }

    @DisplayName("게시물 공유를 추가합니다.")
    @Test
    void sharePost() {
        // given
        User user = createUser();

        Post post = createPost(user, "제목1", "내용1", INSTAGRAM, 10, 10, 10);
        postRepository.save(post);

        // when
        PostShareResponse postShareResponse = postService.sharePost(post.getId(), user.getUsername());

        // then
        assertThat(postShareResponse)
                .extracting("postId", "shareCnt", "url")
                .contains(1L, 1L, "https://www.instagram.com/share/instagram");
    }

    @DisplayName("존재하지 않는 게시물 ID로 게시물 공유를 추가합니다.")
    @Test
    void sharePostWithNonExistentPostId() {
        // given
        User user = createUser();

        // when // then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.sharePost(-1L, user.getUsername()));

        assertEquals(POST_NOT_FOUND, exception.getErrorCode());
    }

    // 유저 빌더 생성
    private User createUser() {

        User user = User.builder()
                .username("testUsername")
                .email("testEmail@test.com")
                .password("testPassword@")
                .status(UserStatus.ACTIVE)
                .certifyStatus(UserCertifyStatus.AUTHENTICATED)
                .build();

        return userRepository.save(user);
    }

    // 게시물 빌더 생성
    private Post createPost(User user, String title, String content, PostType type,
                            int viewCnt, int likeCnt, int shareCnt) {

        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .viewCnt(viewCnt)
                .likeCnt(likeCnt)
                .shareCnt(shareCnt)
                .build();
    }

    // 해시태그 빌더 생성
    private Hashtag createHashtag(String content) {

        return Hashtag.builder()
                .content(content)
                .build();
    }

    // 게시물 해시태그 빌더 생성
    private PostHashtag createPostHashtag(Post post, Hashtag hashtag) {

        return PostHashtag.builder()
                .post(post)
                .hashtag(hashtag)
                .build();
    }

}
