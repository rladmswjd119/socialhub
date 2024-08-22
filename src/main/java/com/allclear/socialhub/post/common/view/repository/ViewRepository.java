package com.allclear.socialhub.post.common.view.repository;

import com.allclear.socialhub.post.common.view.domain.PostView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewRepository extends JpaRepository<PostView, Long> {
}
