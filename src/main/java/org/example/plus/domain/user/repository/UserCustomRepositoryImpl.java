package org.example.plus.domain.user.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.plus.common.enums.UserRoleEnum;
import org.example.plus.domain.user.model.request.UserSearchRequest;
import org.example.plus.domain.user.model.response.UserSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.example.plus.common.entity.QUser.user;

@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserSearchResponse> searchUserByMultiCondition(UserSearchRequest request) {

        BooleanBuilder builder = new BooleanBuilder();
        if (request.getUsername() != null && request.getUsername().isBlank()) {
            builder.and(user.username.eq(request.getUsername()));
        }
        if (request.getEmail() != null && request.getEmail().isBlank()) {
            builder.and(user.email.endsWith(request.getEmail()));
        }
        if (request.getRole() != null) {
            builder.and(user.roleEnum.eq(request.getRole()));
        }

        return queryFactory
                .select(Projections.constructor(UserSearchResponse.class,
                        user.username, user.email, user.roleEnum))
                .from(user)
                .where(builder)
                .fetch();
    }

    @Override
    public List<UserSearchResponse> searchUserByMultiConditionV2(UserSearchRequest request) {
        return queryFactory
                .select(Projections.constructor(UserSearchResponse.class,
                        user.username, user.email, user.roleEnum))
                .from(user)
                .where(
                        usernameCondition(request.getUsername()),
                        emailCondition(request.getEmail()),
                        roleCondition(request.getRole())
                )
                .fetch();
    }

    @Override
    public Page<UserSearchResponse> searchUserByMultiConditionPage(UserSearchRequest request, Pageable pageable) {

        // 1단계 - 실제 데이터 값
        List<UserSearchResponse> result = queryFactory
                .select(Projections.constructor(UserSearchResponse.class,
                        user.username, user.email, user.roleEnum))
                .from(user)
                .where(
                        usernameCondition(request.getUsername()),
                        emailCondition(request.getEmail()),
                        roleCondition(request.getRole())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2단계 - 전체 데이터 갯수
        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        usernameCondition(request.getUsername()),
                        emailCondition(request.getEmail()),
                        roleCondition(request.getRole())
                )
                .fetchOne();

        // 3단계 - 전체 개수가 null인 경우 방지
        if (total == null) {
            total = 0L;
        }

        // 4단계 - Page 객체로 변환
        return new PageImpl<>(result, pageable, total);
    }

    private BooleanExpression usernameCondition(String username) {
        return username != null ? user.username.eq(username) : null;
    }

    private BooleanExpression emailCondition(String email) {
        return email != null ? user.email.endsWith(email) : null;
    }

    private BooleanExpression roleCondition(UserRoleEnum role) {
        return role != null ? user.roleEnum.eq(role) : null;
    }
}
