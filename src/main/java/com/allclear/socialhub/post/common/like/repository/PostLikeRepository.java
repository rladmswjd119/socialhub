package com.allclear.socialhub.post.common.like.repository;

import com.allclear.socialhub.post.common.like.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
}
