package org.example.plus;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.plus.common.entity.Comment;
import org.example.plus.common.entity.Post;
import org.example.plus.common.entity.User;
import org.example.plus.common.enums.UserRoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.example.plus.common.entity.QComment.comment;
import static org.example.plus.common.entity.QPost.post;
import static org.example.plus.common.entity.QUser.user;

@SpringBootTest
@Transactional
public class QuerydslSearchTest {

    @Autowired
    JPAQueryFactory queryFactory;

    @Test
    @DisplayName("NORMAL 사용자 중 gmail.com 이메일을 가진 사용자 조회")
    void test_case01() {
        List<User> result = queryFactory
                .selectFrom(user)
                .where(
                        user.roleEnum.eq(UserRoleEnum.NORMAL),
                        user.email.endsWith("gmail.com")
                )
                .fetch();

        // 앨리스 (alice@gmail.com)
        // 찰리 (charlie@gmail.com)

        System.out.println("검색 완료");
    }

    @Test
    @DisplayName("사용자 이름 오름차순, ID 내림차순 정렬 후 3명만 조회")
    void test_case02() {
        List<User> result = queryFactory
                .selectFrom(user)   // .select(user).from(user)
                .orderBy(user.username.asc(), user.id.desc())
                .limit(3)   // -> 관리자, 밥, 앨리스
                // .offset(2): 2번 인덱스부터 시작 -> 앨리스, 찰리
                .fetch();

        System.out.println("검색 완료");
    }

    @Test
    @DisplayName("“여행” 키워드가 포함된 게시글(Post) 조회")
    void test_case03() {
        List<Post> result = queryFactory
                .selectFrom(post)
                .where(post.content.contains("여행")) // contains = LIKE '%keyword%'
                .fetch();

        System.out.println("검색 완료");
    }

    @Test
    @DisplayName("ADMIN 사용자 또는 이름에 “밥”이 포함된 사용자 조회")
    void test_case04() {
        List<User> result = queryFactory
                .selectFrom(user)
                .where(
                        user.roleEnum.eq(UserRoleEnum.ADMIN)
                                .or(user.username.contains("밥"))
                )
                .fetch();

        System.out.println("검색 완료");
    }

    @Test
    @DisplayName("“앨리스”가 작성한 게시글(Post) 조회")
    void test_case05() {
        List<Post> result = queryFactory
                .selectFrom(post)
                .join(post.user, user)  // SQL의 Inner Join
                .where(user.username.eq("앨리스"))
                .fetch();

        System.out.println("검색 완료");
    }

    @Test
    @DisplayName("게시글과 작성자 정보를 한 번에 로딩")
    void test_case06() {
        List<Post> result = queryFactory
                .selectFrom(post)
                .join(post.user, user).fetchJoin()  // N+1 문제 방지
                .fetch();

        System.out.println("검색 완료");
    }

    @Test
    @DisplayName("“리제로 3기 감상평” 게시글의 모든 댓글 조회")
    void test_case07() {
        List<Comment> comments = queryFactory
                .selectFrom(comment)
                .join(comment.post, post)
                .where(post.content.eq("리제로 3기 감상평"))
                .fetch();

        System.out.println("검색 완료");
    }

    @Test
    @DisplayName("게시글 목록을 5개씩 조회 (2페이지: 6~10번 게시글)")
    void test_case08() {
        List<Post> page2 = queryFactory
                .selectFrom(post)
                .orderBy(post.id.asc())
                .offset(5)
                .limit(5)
                .fetch();

        System.out.println("검색 완료");
    }
}
