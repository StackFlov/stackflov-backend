package com.stackflov.dto;

import com.stackflov.domain.Comment; // Comment 임포트 추가
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor // @Builder를 사용하지 않고 AllArgsConstructor만 있다면 필요할 수 있음
@AllArgsConstructor // @Builder와 함께 사용될 때 모든 필드를 포함하는 생성자를 만듦
public class CommentResponseDto {
    private Long id;
    private String title;
    private String content;
    private String authorEmail; // 댓글 작성자 이메일
    private Boolean active; // ✅ active 필드 추가
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Comment 엔티티를 받아서 DTO로 변환하는 생성자 추가
    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.title = comment.getTitle();
        this.content = comment.getContent();
        this.authorEmail = comment.getUser().getEmail(); // User 엔티티에서 이메일 가져옴
        this.active = comment.getActive(); // ✅ active 값 설정
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
}