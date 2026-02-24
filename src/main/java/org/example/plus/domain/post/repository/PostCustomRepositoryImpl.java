package org.example.plus.domain.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.plus.domain.post.model.dto.PostSummaryDto;

import java.util.List;

import static org.example.plus.common.entity.QComment.comment;
import static org.example.plus.common.entity.QPost.post;
import static org.example.plus.common.entity.QUser.user;

@RequiredArgsConstructor
public class PostCustomRepositoryImpl implements PostCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PostSummaryDto> findPostSummary(String username) {
        return queryFactory
                .select(Projections.constructor(
                        PostSummaryDto.class,
                        post.content,
                        comment.countDistinct().intValue()
                ))
                .from(post)
                .leftJoin(user).on(post.userId.eq(user.id))
                .leftJoin(comment).on(comment.postId.eq(comment.postId))
                .where(user.username.eq(username))
                .groupBy(post.id)
                .fetch();
    }
}