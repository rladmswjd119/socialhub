package com.allclear.socialhub.post.controller;

import com.allclear.socialhub.common.provider.JwtTokenProvider;
import com.allclear.socialhub.post.domain.StatisticType;
import com.allclear.socialhub.post.domain.StatisticValue;
import com.allclear.socialhub.post.dto.StatisticRequestParam;
import com.allclear.socialhub.post.dto.StatisticResponse;
import com.allclear.socialhub.post.service.StatisticService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/posts/statistics")
@Slf4j
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<List<StatisticResponse>> getStatistics(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid StatisticRequestParam statisticRequest
    ) {

        // JWT 토큰 추출
        String token = authorizationHeader.substring(7); // "Bearer " 부분 제거
        String username = jwtTokenProvider.extractAccountFromToken(token);
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
