package com.allclear.socialhub.post.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allclear.socialhub.auth.dto.UserDetailsImpl;
import com.allclear.socialhub.auth.util.AccessTokenUtil;
import com.allclear.socialhub.post.domain.StatisticType;
import com.allclear.socialhub.post.domain.StatisticValue;
import com.allclear.socialhub.post.dto.StatisticRequestParam;
import com.allclear.socialhub.post.dto.StatisticResponse;
import com.allclear.socialhub.post.service.StatisticService;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/posts/statistics")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "통계 API")
public class StatisticController {

	private final StatisticService statisticService;

	@Operation(summary = "통계 조회", description = "일자별, 시간별 통계를 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "통계 일자별, 시간별 데이터 조회됨",
					content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "type 이 date, hour 가 아닐 경우\n\n" +
					"value 가 count, view_count, like_count, share_count 가 아닐 경우\n\n" +
					"날짜 형식이 yyyy-MM-DD로 파싱 불가능한 경우\n\n" +
					"end 가 오늘보다 미래일 경우\n\n" +
					"start, end 가 올바른 날짜가 아닐 경우  ex. 23-02-31\n\n" +
					"start ~ end 최대 기간 넘을 경우\n\n" +
					"start가 end 보다 미래일경우\n\n",
					content = @Content)
	})
	@GetMapping
	public ResponseEntity<List<StatisticResponse>> getStatistics(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@Valid StatisticRequestParam statisticRequest
	) {

		// JWT 토큰 추출
		String username = userDetails.getUsername();
		// hashtag가 없으면 JWT 토큰에서 추출한 사용자 이름 설정
		if (statisticRequest.getHashtag() == null) {
			statisticRequest.setHashtag(username);
		}

		String hashtag = statisticRequest.getHashtag();
		StatisticType type = statisticRequest.getType();
		LocalDate start = statisticRequest.getStart();
		LocalDate end = statisticRequest.getEnd();
		StatisticValue value = statisticRequest.getValue();

		log.info("hashtag : " + hashtag);
		log.info("type : " + type);
		log.info(start + "~" + end);
		log.info("value : " + value);
		return ResponseEntity.ok(statisticService.getStatistics(hashtag, type, start, end, value));
	}

}
