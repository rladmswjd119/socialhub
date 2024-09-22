package com.allclear.socialhub.user;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.allclear.socialhub.auth.dto.UserDetailsImpl;
import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.common.exception.ErrorCode;
import com.allclear.socialhub.auth.util.AccessTokenUtil;
import com.allclear.socialhub.user.domain.User;
import com.allclear.socialhub.user.dto.UserInfoUpdateRequest;
import com.allclear.socialhub.user.dto.UserInfoUpdateResponse;
import com.allclear.socialhub.user.dto.UserJoinRequest;
import com.allclear.socialhub.user.dto.UserLoginRequest;
import com.allclear.socialhub.user.exception.DuplicateUserInfoException;
import com.allclear.socialhub.user.repository.UserRepository;
import com.allclear.socialhub.user.service.UserServiceImpl;
import com.allclear.socialhub.user.type.UserCertifyStatus;
import com.allclear.socialhub.user.type.UserStatus;
import com.allclear.socialhub.user.type.UsernameDupStatus;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;
	@InjectMocks
	private UserServiceImpl userService;
	@Mock
	private AccessTokenUtil accessTokenUtil;
	@Mock
	private PasswordEncoder passwordEncoder;
	private UserJoinRequest request;

	private UserLoginRequest loginRequest;
	private User user;

	@BeforeEach
	public void setUp() {

		loginRequest = new UserLoginRequest("oldUsername", "encodedPassword");
		request = new UserJoinRequest();
		request.setUsername("validUser");
		request.setEmail("valid@example.com");
		request.setPassword("ValidPass123!");

		// 초기화된 사용자 객체 생성
		user = User.builder()
				.id(1L)
				.username("oldUsername")
				.email("test@example.com")
				.password("encodedPassword")
				.build();
	}

	@Test
	@DisplayName("로그인 시 사용자가 전달한 계정과 일치하는 계정이 있는 경우")
	public void checkUsernameSuccessTest() {
		// given
		given(userRepository.findByUsername(loginRequest.getUsername())).willReturn(mock(User.class));

		// when
		userService.getUserByUsername(loginRequest.getUsername());

		// then
		verify(userRepository, times(1)).findByUsername(loginRequest.getUsername());
	}


	@Test
	@DisplayName("로그인 시 사용자 전달한 계정과 일치하지 않는 경우를 테스트합니다.")
	public void checkUsernameFailTest() {
		// given
		String username = loginRequest.getUsername();
		given(userService.getUserByUsername(any())).willReturn(user);

		// when
		User user = userService.getUserByUsername(username);

		// then
		assertThat(user.getUsername()).isEqualTo(username);
		verify(userRepository, times(1)).findByUsername(loginRequest.getUsername());
	}

	@Test
	@DisplayName("회원가입 시 이메일이 이미 존재하는 경우를 테스트합니다.")
	public void testJoinUserEmailAlreadyExists() {
		// given
		when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

		// when & then
		assertThrows(DuplicateUserInfoException.class, () -> userService.joinUser(request));

		verify(userRepository, never()).save(Mockito.any(User.class));
	}


	@Test
	@DisplayName("회원가입 시 사용자명이 이미 존재하는 경우를 테스트합니다.")
	public void testJoinUserUsernameAlreadyExists() {
		// given
		when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

		// when & then
		assertThrows(DuplicateUserInfoException.class, () -> userService.joinUser(request));

		verify(userRepository, never()).save(Mockito.any(User.class));
	}

	@Test
	@DisplayName("회원가입 시 비밀번호 길이 유효하지 않은 경우를 테스트합니다.")
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


	@Test
	@DisplayName("회원가입 시 비밀번호 조건에 두 가지 이상이 포함되지 않은 경우를 테스트합니다.")
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

	@Test
	@DisplayName("회원가입 시 사용하고자 하는 계정을 이미 다른 사용자가 사용한 경우를 테스트합니다.")
	public void duplicateAccountExistsTest() {
		//given
		User user = User.builder()
				.username("username")
				.email("welfjlkd@gmail.com")
				.password("padlfjdl")
				.status(UserStatus.ACTIVE)
				.certifyStatus(UserCertifyStatus.AUTHENTICATED)
				.build();
		given(userRepository.findByUsername(any())).willReturn(user);


		//when
		Throwable ex = assertThrows(CustomException.class, () -> userService.userDuplicateCheck(any()));

		//then
		assertThat(ex.getMessage()).isEqualTo(ErrorCode.USERNAME_DUPLICATION.getMessage());
		verify(userRepository, times(1)).findByUsername(any());
	}

	@DisplayName("회원가입 시 사용하고자 하는 계정을 다른 사용자가 사용하지 않는 경우를 테스트합니다.")
	@Test
	public void duplicateAccountNoExistsTest() {
		//when
		String result = userService.userDuplicateCheck(any());

		//then
		assertThat(result).isEqualTo(UsernameDupStatus.USERNAME_AVAILABLE.getMessage());
		verify(userRepository, times(1)).findByUsername(any());
	}

	@Test
	@DisplayName("회원 정보 수정 성공 테스트")
	public void updateUserInfo_Success() {
		// given
		String token = "validToken";
		UserDetailsImpl userDetails = new UserDetailsImpl(user);
		UserInfoUpdateRequest request = new UserInfoUpdateRequest("newUsername", "NewValidPass123!");

		given(userRepository.findByEmailAndUsername(user.getEmail(), user.getUsername())).willReturn(Optional.of(user));
		given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);
		given(passwordEncoder.encode(request.getPassword())).willReturn("encodedNewPassword");

		// when
		UserInfoUpdateResponse response = userService.updateUserInfo(request, userDetails);

		// then
		verify(userRepository, times(1)).save(any(User.class));
		assertThat(response.getUsername()).isEqualTo(request.getUsername());
		assertThat(response.getMessage()).isEqualTo("회원가입 수정이 완료되었습니다.");
	}

	@Test
	@DisplayName("회원 정보 수정 실패 테스트 - 기존 비밀번호 재사용")
	public void updateUserInfo_Failure_PasswordReused() {
		// given
		UserInfoUpdateRequest request = new UserInfoUpdateRequest("newUsername", "NewValidPass123!");
		UserDetailsImpl userDetails = new UserDetailsImpl(user);

		given(userRepository.findByEmailAndUsername(user.getEmail(), user.getUsername())).willReturn(Optional.of(user));
		given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);

		// when & then
		CustomException exception = assertThrows(CustomException.class,
				() -> userService.updateUserInfo(request, userDetails));

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_REUSED);
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("회원 정보 수정 실패 테스트 - 사용자 존재하지 않음")
	public void updateUserInfo_Failure_UserNotExist() {
		// given
		UserInfoUpdateRequest request = new UserInfoUpdateRequest("newUsername", "NewValidPass123!");
		UserDetailsImpl userDetails = new UserDetailsImpl(user);

		// when & then
		CustomException exception = assertThrows(CustomException.class,
				() -> userService.updateUserInfo(request, userDetails));

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_EXIST);
		verify(userRepository, never()).save(any(User.class));
	}


}
