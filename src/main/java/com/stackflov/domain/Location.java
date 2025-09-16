package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // Builder를 위해 모든 필드를 받는 생성자 추가
@Builder            // 빌더 패턴 자동 생성
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;

    // --- [추가된 필드] ---
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // --- [추가된 필드] ---
    @Enumerated(EnumType.STRING)
    private LocationCategory category;

    // --- [추가된 필드] ---
    private String theme;
    private String phoneNumber;
    private String homepageUrl;
    private String imageUrl;
    @Lob // 긴 텍스트를 저장하기 위한 어노테이션
    private String description;

    /*
     * @Builder와 @AllArgsConstructor를 클래스 레벨에 추가했기 때문에,
     * 기존에 있던 생성자는 삭제해도 됩니다. Lombok이 자동으로 생성해 줍니다.
     */
}