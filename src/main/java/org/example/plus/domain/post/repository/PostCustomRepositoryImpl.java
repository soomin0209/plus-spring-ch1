package org.example.plus.domain.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.plus.domain.post.model.dto.PostSummaryDto;

import java.util.List;

import static org.example.plus.common.entity.QComment.comment;
import static org.example.plus.common.entity.QPost.post;

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
                .leftJoin(post.comments, comment)
                .where(post.user.username.eq(username))
                .groupBy(post.id)
                .fetch();
    }
}