package com.allclear.socialhub.post.common.like.repository;

import com.allclear.socialhub.post.common.like.domain.PostLike;
import com.allclear.socialhub.post.common.response.StatisticQueryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    @Query("SELECT DATE_FORMAT(pl.createdAt, :dateFormatPattern) AS time, count(*) AS value " +
            "FROM PostLike AS pl " +
            "WHERE pl.post.id in :postIds " +
            "AND DATE(pl.createdAt) BETWEEN :start AND :end " +
            "GROUP BY DATE_FORMAT(pl.createdAt, :dateFormatPattern) " +
            "ORDER BY time ASC")
    List<StatisticQueryResponse> findStatisticByPostIds(
            @Param("postIds") List<Long> postIds,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("dateFormatPattern") String dateFormatPattern);

    void deleteAllByPostId(Long postId);

}
