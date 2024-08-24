package com.allclear.socialhub.user;

import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.user.domain.User;
import com.allclear.socialhub.user.dto.UserJoinRequest;
import com.allclear.socialhub.user.exception.DuplicateUserInfoException;
import com.allclear.socialhub.user.repository.UserRepository;
import com.allclear.socialhub.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserJoinRequest request;

    /**
     * 각 테스트 전에 실행되며, 테스트에 필요한 기본 데이터를 초기화합니다.
     */
    @BeforeEach
    public void setUp() {

        request = new UserJoinRequest();
        request.setUsername("validUser");
        request.setEmail("valid@example.com");
        request.setPassword("ValidPass123!");
    }

    /**
     * 회원가입이 성공적으로 수행되는 경우를 테스트합니다.
     * 작성자: 배서진
     *
     * @given 이메일과 계정명이 중복되지 않은 경우
     * @when joinUser 메서드가 호출될 때
     * @then UserRepository의 save 메서드가 한 번 호출되어야 합니다.
     */
    @Test
    public void testJoinUserSuccess() {
        // given
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);

        // when
        userService.joinUser(request);

        // then
        verify(userRepository, times(1)).save(Mockito.any(User.class));
    }

    /**
     * 회원가입 시 이메일이 이미 존재하는 경우를 테스트합니다.
     * 작성자: 배서진
     *
     * @given 이메일이 이미 존재하는 경우
     * @when joinUser 메서드가 호출될 때
     * @then DuplicateUserInfoException 예외가 발생해야 하며, UserRepository의 save 메서드는 호출되지 않아야 합니다.
     */
    @Test
    public void testJoinUserEmailAlreadyExists() {
        // given
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // when & then
        assertThrows(DuplicateUserInfoException.class, () -> userService.joinUser(request));

        verify(userRepository, never()).save(Mockito.any(User.class));
    }

    /**
     * 회원가입 시 사용자명이 이미 존재하는 경우를 테스트합니다.
     * 작성자: 배서진
     *
     * @given 사용자명이 이미 존재하는 경우
     * @when joinUser 메서드가 호출될 때
     * @then DuplicateUserInfoException 예외가 발생해야 하며, UserRepository의 save 메서드는 호출되지 않아야 합니다.
     */
    @Test
    public void testJoinUserUsernameAlreadyExists() {
        // given
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // when & then
        assertThrows(DuplicateUserInfoException.class, () -> userService.joinUser(request));

        verify(userRepository, never()).save(Mockito.any(User.class));
    }

    /**
     * 회원가입 시 비밀번호 길이가 유효하지 않은 경우를 테스트합니다.
     * 작성자: 배서진
     *
     * @given 비밀번호 길이가 10자 미만이거나 20자를 초과하는 경우
     * @when joinUser 메서드가 호출될 때
     * @then CustomException 예외가 발생해야 하며, UserRepository의 save 메서드는 호출되지 않아야 합니다.
     */
    @Test
    public void testJoinUserInvalidPasswordLength() {
        // given
        UserJoinRequest request = new UserJoinRequest();
        request.setUsername("newUser");
        request.setEmail("new@example.com");
        request.setPassword("short");

        // when & then
        assertThrows(CustomException.class, () -> userService.joinUser(request));

        verify(userRepository, never()).save(Mockito.any(User.class));
    }

    /**
     * 회원가입 시 비밀번호에 숫자, 문자, 특수문자 중 두 가지 이상이 포함되지 않은 경우를 테스트합니다.
     * 작성자: 배서진
     *
     * @given 비밀번호가 숫자, 문자, 특수문자 중 두 가지 이상을 포함하지 않은 경우
     * @when joinUser 메서드가 호출될 때
     * @then CustomException 예외가 발생해야 하며, UserRepository의 save 메서드는 호출되지 않아야 합니다.
     */
    @Test
    public void testJoinUserInvalidPasswordPattern() {
        // given
        UserJoinRequest request = new UserJoinRequest();
        request.setUsername("newUser");
        request.setEmail("new@example.com");
        request.setPassword("onlyletters");

        // when & then
        assertThrows(CustomException.class, () -> userService.joinUser(request));

        verify(userRepository, never()).save(Mockito.any(User.class));
    }

}
