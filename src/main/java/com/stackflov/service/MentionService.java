package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.repository.MentionRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MentionService {

    private final UserRepository userRepository;
    private final MentionRepository mentionRepository;
    private final NotificationService notificationService;

    // 정규표현식으로 @닉네임 패턴 찾기
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣_]+)");

    @Transactional
    public void processMentions(User mentioner, String content, Board board, Comment comment) {
        if (content == null || content.isBlank()) {
            return;
        }

        Set<String> mentionedNicknames = new HashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            mentionedNicknames.add(matcher.group(1));
        }

        for (String nickname : mentionedNicknames) {
            userRepository.findByNickname(nickname).ifPresent(mentionedUser -> {
                // 자기 자신을 멘션한 경우는 제외
                if (mentioner.getId().equals(mentionedUser.getId())) {
                    return;
                }

                // 멘션 정보 저장
                Mention mention = Mention.builder()
                        .mentioner(mentioner)
                        .mentioned(mentionedUser)
                        .board(board)
                        .comment(comment)
                        .build();
                mentionRepository.save(mention);

                // 멘션된 사용자에게 알림 보내기
                String message = mentioner.getNickname() + "님이 회원님을 언급했습니다.";
                String link = (board != null) ? "/boards/" + board.getId() : "/somewhere/else"; // 댓글 멘션 시 링크는 상황에 맞게 수정
                notificationService.notify(mentionedUser, NotificationType.MENTION, message, link);
            });
        }
    }
}