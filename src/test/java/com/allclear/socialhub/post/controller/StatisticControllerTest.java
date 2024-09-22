package com.allclear.socialhub.post.controller;

import com.allclear.socialhub.auth.util.AccessTokenUtil;
import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.common.exception.ErrorCode;
import com.allclear.socialhub.common.exception.handler.GlobalExceptionHandler;
import com.allclear.socialhub.post.domain.StatisticType;
import com.allclear.socialhub.post.domain.StatisticValue;
import com.allclear.socialhub.post.dto.StatisticResponse;
import com.allclear.socialhub.post.service.StatisticService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatisticController.class)
@Slf4j
@DisplayName("StatisticController 테스트")
@WithMockUser(username = "test")
class StatisticControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StatisticController statisticController;

    @MockBean
    private StatisticService statisticService;

    @MockBean
    private AccessTokenUtil accessTokenUtil;

    @Mock
    private Claims mockClaims;

    private String jwt;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders.standaloneSetup(statisticController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        jwt = "mockJwtToken"; // 실제 JWT 생성 로직을 대체하는 Mock 값
        when(accessTokenUtil.getClaims(anyString())).thenReturn(mockClaims);
        when(accessTokenUtil.extractUsername(any(Claims.class))).thenReturn("test");
    }

    @Nested
    @DisplayName("통계 데이터를 요청할 때")
    class WhenRequestingStatistics {

        @Test
        @DisplayName("유효한 요청 시 (200)")
        void GivenValidRequest_ThenReturnStatisticsSuccessfully() throws Exception {

            // given
            List<StatisticResponse> mockDailyStatistics = Arrays.asList(
                    new StatisticResponse("2024-08-23", 5L),
                    new StatisticResponse("2024-08-24", 0L),
                    new StatisticResponse("2024-08-25", 3L)
            );

            StatisticType type = StatisticType.DATE;
            StatisticValue value = StatisticValue.COUNT;
            LocalDate start = LocalDate.of(2024, 8, 23);
            LocalDate end = LocalDate.of(2024, 8, 25);

            when(statisticService.getStatistics(anyString(), eq(type), eq(start), eq(end), eq(value)))
                    .thenReturn(mockDailyStatistics);

            // when, then
            mockMvc.perform(get("/api/posts/statistics")
                            .param("type", "DATE")
                            .param("start", "2024-08-23")
                            .param("end", "2024-08-25")
                            .param("value", "COUNT")
                            .header("Authorization", "Bearer " + jwt)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].time").value("2024-08-23"))
                    .andExpect(jsonPath("$[0].value").value(5))
                    .andExpect(jsonPath("$[1].time").value("2024-08-24"))
                    .andExpect(jsonPath("$[1].value").value(0))
                    .andExpect(jsonPath("$[2].time").value("2024-08-25"))
                    .andExpect(jsonPath("$[2].value").value(3));
        }

        @Test
        @DisplayName("start가 end보다 미래일 경우 (400)")
        void GivenStartDateAfterEndDate_ThenThrowsBadRequest() throws Exception {

            // given
            StatisticType type = StatisticType.DATE;
            StatisticValue value = StatisticValue.COUNT;
            LocalDate start = LocalDate.of(2024, 8, 25);
            LocalDate end = LocalDate.of(2024, 8, 23);

            log.info(start.toString() + " ~ " + end.toString());
            when(statisticService.getStatistics(anyString(), eq(type), eq(start), eq(end), eq(value)))
                    .thenThrow(new CustomException(ErrorCode.STATISTICS_INVALID_DATE_RANGE_START_AFTER_END));

            // when, then
            mockMvc.perform(get("/api/posts/statistics")
                            .param("type", "DATE")
                            .param("start", start.toString())
                            .param("end", end.toString())
                            .param("value", "COUNT")
                            .header("Authorization", "Bearer " + jwt)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("start 날짜는 end 날짜보다 이전이거나 같아야 합니다."));
        }

        @Test
        @DisplayName("DATE 최대 기간을 초과할 경우 (400)")
        void GivenExceedingMaxDateRange_ThenThrowsBadRequest() throws Exception {

            // given
            StatisticType type = StatisticType.DATE;
            StatisticValue value = StatisticValue.COUNT;
            LocalDate start = LocalDate.of(2024, 5, 25);
            LocalDate end = LocalDate.of(2024, 8, 23);

            log.info(start.toString() + " ~ " + end.toString());
            when(statisticService.getStatistics(anyString(), eq(type), eq(start), eq(end), eq(value)))
                    .thenThrow(new CustomException(ErrorCode.STATISTICS_INVALID_DATE_RANGE_TOO_LONG_DATE));

            // when, then
            mockMvc.perform(get("/api/posts/statistics")
                            .param("type", "DATE")
                            .param("start", start.toString())
                            .param("end", end.toString())
                            .param("value", "COUNT")
                            .header("Authorization", "Bearer " + jwt)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("최대 30일까지만 조회할 수 있습니다."));
        }

        @Test
        @DisplayName("HOUR 최대 기간을 초과할 경우 (400)")
        void GivenExceedingMaxHourRange_ThenThrowsBadRequest() throws Exception {

            // given
            StatisticType type = StatisticType.HOUR;
            StatisticValue value = StatisticValue.COUNT;
            LocalDate start = LocalDate.of(2024, 8, 15);
            LocalDate end = LocalDate.of(2024, 8, 23);

            log.info(start.toString() + " ~ " + end.toString());
            when(statisticService.getStatistics(anyString(), eq(type), eq(start), eq(end), eq(value)))
                    .thenThrow(new CustomException(ErrorCode.STATISTICS_INVALID_DATE_RANGE_TOO_LONG_HOUR));

            // when, then
            mockMvc.perform(get("/api/posts/statistics")
                            .param("type", "HOUR")
                            .param("start", start.toString())
                            .param("end", end.toString())
                            .param("value", "COUNT")
                            .header("Authorization", "Bearer " + jwt)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("최대 7일까지만 조회할 수 있습니다."));
        }

        @Test
        @DisplayName("end가 오늘보다 미래일 경우 (400)")
        void GivenEndDateAfterToday_ThenThrowsBadRequest() throws Exception {

            // given
            StatisticType type = StatisticType.DATE;
            StatisticValue value = StatisticValue.COUNT;
            LocalDate start = LocalDate.of(2024, 8, 25);
            LocalDate end = LocalDate.of(2024, 9, 23);

            // when, then
            mockMvc.perform(get("/api/posts/statistics")
                            .param("type", "HOUR")
                            .param("start", start.toString())
                            .param("end", end.toString())
                            .param("value", "COUNT")
                            .header("Authorization", "Bearer " + jwt)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("유효하지 않은 날짜입니다. expected: 'yyyy-MM-DD'"));
        }

        @Test
        @DisplayName("날짜 형식이 잘못된 경우 (400)")
        void GivenInvalidDateFormat_ThenThrowsBadRequest() throws Exception {

            // given
            StatisticType type = StatisticType.DATE;
            StatisticValue value = StatisticValue.COUNT;
            LocalDate start = LocalDate.of(2024, 8, 25);
            LocalDate end = LocalDate.of(2024, 9, 23);

            // when, then
            mockMvc.perform(get("/api/posts/statistics")
                            .param("type", "HOUR")
                            .param("start", "2024.08.23")
                            .param("end", "2024.08.24")
                            .param("value", "COUNT")
                            .header("Authorization", "Bearer " + jwt)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("유효하지 않은 날짜입니다. expected: 'yyyy-MM-DD'"));
        }

    }

}
