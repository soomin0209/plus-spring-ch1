package org.example.plus.domain.post.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plus.common.entity.Post;
import org.example.plus.common.entity.User;
import org.example.plus.domain.post.model.dto.PostDto;
import org.example.plus.domain.post.model.dto.PostSummaryDto;
import org.example.plus.domain.post.model.request.UpdatePostRequest;
import org.example.plus.domain.post.repository.PostRepository;
import org.example.plus.domain.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostCacheService postCacheService;

    public PostDto creatPost(String username, String content) {

        User user = userRepository.findUserByUsername(username).orElseThrow(
            ()-> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );

        Post post = postRepository.save(new Post(content, user.getId()));

        return PostDto.from(post, user.getUsername());

    }


    // 지연 로딩이구나
    // 실질적으로 사용할 때 불러오는 것이구나!

    // 즉시 로딩으로 한번 테스트를 진행해보겠습니다!
    // 유저를 조회 하자 마자 조회를 할때 연관된 모든 것들을 싸그리 싹싹 긁거서 가져올 것이다.

    public List<PostDto> getPostListByUsername(String username) {

//        User user = userRepository.findUserByUsername(username).orElseThrow(
//            () -> new IllegalArgumentException("등록된 사용자가 없습니다.")
//        );
//
//        List<Post> postList = user.getPosts();
//
//
//        // post List 를 postDto list로 변환 한것이다.
//        return postList.stream()
//            .map(PostDto::from)
//            .collect(Collectors.toList());
        return null;
    }

    public List<PostSummaryDto> getPostSummaryListByUsername(String username) {

        List<PostSummaryDto> result = postRepository.findPostSummary(username);
        return result;
    }



//    @Cacheable(value = "postCache", key = "'id:' + #postId")
//    public PostDto getPostById(long postId) {
//
//        // 1단계: postId 기준으로 캐시에 값이 있는지 없는지 확인 -> @Cacheable
//
//        // 2단계: 값이 있으면 바로 리턴 -> @Cacheable
//
//        // 3단계: 값이 없으면 직접 DB 조회
//        log.info("postId={} DB 직접 조회", postId);
//
//        // 4단계: 가져온 값을 캐시에 저장 -> @Cacheable
//
//        Post post = postRepository.findById(postId).orElseThrow(
//                () -> new IllegalArgumentException("등록된 포스트가 없습니다.")
//        );
//
//        // 이전 QueryDSL 실습에서 User와 연관관계를 끊음 -> N+1 문제 발생하긴 하는데 일단 그냥 진행
//        User user = userRepository.findById(post.getUserId()).orElseThrow(
//                ()-> new IllegalArgumentException("등록된 사용자가 없습니다.")
//        );
//
//        return PostDto.from(post, user.getUsername());
//    }
//
//    @CachePut(value = "postCache", key = "'id:' + #postId")
//    public PostDto updatePostById(long postId, UpdatePostRequest request) {
//
//        Post post = postRepository.findById(postId).orElseThrow(
//                () -> new IllegalArgumentException("등록된 포스트가 없습니다.")
//        );
//
//        // 이전 QueryDSL 실습에서 User와 연관관계를 끊음 -> N+1 문제 발생하긴 하는데 일단 그냥 진행
//        User user = userRepository.findById(post.getUserId()).orElseThrow(
//                ()-> new IllegalArgumentException("등록된 사용자가 없습니다.")
//        );
//
//        post.update(request);
//        postRepository.save(post);
//
//        return PostDto.from(post, user.getUsername());
//    }
//
//    @CacheEvict(value = "postCache", key = "'id:' + #postId")
//    public void deletePostById(long postId) {
//
//        postRepository.deleteById(postId);
//    }



    public PostDto getPostById(long postId) {

        // 1단계: 캐시 있는지 확인
        PostDto cached = postCacheService.getPostCache(postId);

        if (cached != null) {
            log.info("Redis Data Cache Hit");
            postCacheService.increaseViewCount(postId); // viewCount 증가
            return cached;
        }

        // 2단계: 캐시가 없으면 DB 직접 조회
        log.info("Redis Data Cache Miss - {}", postId);
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("등록된 포스트가 없습니다.")
        );

        User user = userRepository.findById(post.getUserId()).orElseThrow(
                ()-> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );

        // 3단계: 캐시에 저장
        PostDto postDto = PostDto.from(post, user.getUsername());
        postCacheService.savePostCache(postId, postDto);

        return postDto;
    }

    public PostDto updatePostById(long postId, UpdatePostRequest request) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("등록된 포스트가 없습니다.")
        );

        User user = userRepository.findById(post.getUserId()).orElseThrow(
                ()-> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );

        post.update(request);
        postRepository.save(post);

        // 캐시 삭제(무효화)
        postCacheService.deletePostCache(postId);

        return PostDto.from(post, user.getUsername());
    }

    public List<PostDto> getTopPostList(int limit) {

        List<Long> topPostIdList = postCacheService.getTopPostList(limit);
        List<PostDto> result = new ArrayList<>();

        // NPE 방지
        if (topPostIdList == null || topPostIdList.isEmpty()) {
            return Collections.emptyList();
        }

        for (Long postId : topPostIdList) {

            // DB 직접 조회 전에 캐시에 해당 게시글이 있는지 확인
            PostDto postDto = postCacheService.getPostCache(postId);

            // 캐시에 값이 없으면 DB 직접 조회
            if (postDto == null) {

                Post post = postRepository.findById(postId).orElseThrow(
                        () -> new IllegalArgumentException("등록된 포스트가 없습니다.")
                );

                User user = userRepository.findById(post.getUserId()).orElseThrow(
                        ()-> new IllegalArgumentException("등록된 사용자가 없습니다.")
                );

                postDto = PostDto.from(post, user.getUsername());
            }
            result.add(postDto);
        }
        return result;
    }
}


