package com.allclear.socialhub.post.common.share.domain;

import com.allclear.socialhub.common.domain.Timestamped;
import com.allclear.socialhub.post.domain.Post;
import com.allclear.socialhub.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "post_share")
@Getter
public class PostShare extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
}
