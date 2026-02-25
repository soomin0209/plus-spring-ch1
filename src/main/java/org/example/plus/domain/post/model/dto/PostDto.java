package org.example.plus.domain.post.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.plus.common.entity.Post;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    private long id;
    private String content;
    private String username;
    private LocalDate check;
    // Redis 시간을 나타내는 LocalDate, LocalDateTime을 사용하기 위해 설정 추가 -> RedisConfig

    public static PostDto from(Post post, String username) {
        return new PostDto(post.getId(), post.getContent(), username, LocalDate.now());
    }
}
