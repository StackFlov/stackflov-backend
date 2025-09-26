package com.stackflov.service;

import com.stackflov.domain.Notice;
import com.stackflov.domain.Role;
import com.stackflov.domain.User;
import com.stackflov.dto.NoticeCreateRequestDto;
import com.stackflov.dto.NoticeResponseDto;
import com.stackflov.repository.NoticeRepository;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    // 관리자: 공지사항 생성
    @Transactional
    public Long createNotice(NoticeCreateRequestDto dto, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));
        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("공지사항을 작성할 권한이 없습니다.");
        }

        Notice notice = Notice.builder()
                .author(admin)
                .title(dto.getTitle())
                .content(dto.getContent())
                .active(true)                 // ✅ 활성으로 생성
                .build();

        return noticeRepository.save(notice).getId();
    }


    // 관리자: 공지사항 수정
    @Transactional
    public void updateNotice(Long noticeId, NoticeCreateRequestDto dto) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
        notice.update(dto.getTitle(), dto.getContent());
    }

    // 관리자: 공지사항 삭제
    @Transactional
    public void deleteNotice(Long noticeId) {
        noticeRepository.findById(noticeId).ifPresent(n -> {
            if (n.isActive()) n.softDelete();   // ✅ active=false, deletedAt 세팅
        });
    }

    // 사용자/관리자: 공지사항 상세 조회
    @Transactional // readOnly 해제: viewCount 증가 위해
    public NoticeResponseDto getNotice(Long noticeId) {
        Notice notice = noticeRepository.findByIdAndActiveTrue(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 공지입니다."));
        notice.increaseViewCount();             // ✅ 활성 공지만 카운트 증가
        return new NoticeResponseDto(notice);
    }

    // 사용자/관리자: 공지사항 목록 조회
    @Transactional(readOnly = true)
    public Page<NoticeResponseDto> getAllNotices(Pageable pageable) {
        Page<Notice> notices = noticeRepository.findAllByOrderByCreatedAtDesc(pageable);
        return notices.map(n -> new NoticeResponseDto(n.getId(), n.getTitle(), n.getAuthor().getNickname(), n.getViewCount(), n.getCreatedAt()));
    }
}