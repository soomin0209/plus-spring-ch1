package org.example.plus.domain.post.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.plus.domain.post.model.dto.PostDto;
import org.example.plus.domain.post.model.dto.PostSummaryDto;
import org.example.plus.domain.post.model.request.CreatePostRequest;
import org.example.plus.domain.post.model.request.UpdatePostRequest;
import org.example.plus.domain.post.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDto> createPost(@AuthenticationPrincipal User user, @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.creatPost(user.getUsername(), request.getContent()));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<PostDto>> getPostListByUsername(@PathVariable String username) {
        return ResponseEntity.ok(postService.getPostListByUsername(username));
    }


    //특정 사용자가 작성한 게시글에 달린 댓글의 갯수를 구하는 기능을 만들어주세요.
    @GetMapping("/user/{username}/detail")
    public ResponseEntity<List<PostSummaryDto>> getPostListDetailByUsername(@PathVariable String username) {
        return ResponseEntity.ok(postService.getPostSummaryListByUsername(username));
    }



//    // 1. postId 기준으로 post를 조회하는 API
//    // 캐시에 값이 있으면 바로 리턴, 없으면 DB 조회 후 캐시에 저장
//    @GetMapping("/{postId}")
//    public ResponseEntity<PostDto> getPostById(@PathVariable long postId) {
//        return ResponseEntity.ok(postService.getPostById(postId));
//    }
//
//    @PutMapping("/{postId}")
//    public ResponseEntity<PostDto> updatePostById(@PathVariable long postId, @RequestBody UpdatePostRequest request) {
//        return ResponseEntity.ok(postService.updatePostById(postId, request));
//    }
//
//    @DeleteMapping("/{postId}")
//    public ResponseEntity<Void> deletePostById(@PathVariable long postId) {
//        postService.deletePostById(postId);
//        return ResponseEntity.ok().build();
//    }




    // 게시글 Id 기반으로 조회 - 캐시가 있으면 사용, 없으면 DB 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(@PathVariable long postId) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    // 게시글 수정시 캐시도 삭제
    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePostById(@PathVariable long postId, @RequestBody UpdatePostRequest request) {
        return ResponseEntity.ok(postService.updatePostById(postId, request));
    }

    // 인기 게시글 조회
    @GetMapping("/popular")
    public ResponseEntity<List<PostDto>> getPopularPostList(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(postService.getTopPostList(limit));
    }
}
