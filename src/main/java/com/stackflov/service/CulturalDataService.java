/*
package com.stackflov.service;

import com.stackflov.domain.Location;
import com.stackflov.domain.LocationCategory;
import com.stackflov.dto.CulturalInfoResponse;
import com.stackflov.dto.Row;
import com.stackflov.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class CulturalDataService {

    private final LocationRepository locationRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${public-data.seoul-api.key}")
    private String seoulApiKey;

    private static final String CULTURAL_SPACE_API_URL = "http://openapi.seoul.go.kr:8088";

    @Transactional
    public void fetchAndSaveCulturalData() {
        WebClient webClient = webClientBuilder.baseUrl(CULTURAL_SPACE_API_URL).build();

        CulturalInfoResponse response = webClient.get()
                .uri("/{key}/json/culturalSpaceInfo/1/1000/"
                        .replace("{key}", seoulApiKey))
                .retrieve()
                .bodyToMono(CulturalInfoResponse.class)
                .block();

        if (response == null || response.getCulturalSpaceInfo() == null) {
            return;
        }

        for (Row row : response.getCulturalSpaceInfo().getRows()) {
            if (row.getAddress() == null || row.getLongitude() == null || row.getLatitude() == null ||
                    row.getLongitude().isEmpty() || row.getLatitude().isEmpty()) {
                continue;
            }

            Location location = Location.builder()
                    .name(row.getName())
                    .address(row.getAddress())
                    .latitude(Double.parseDouble(row.getLatitude()))
                    .longitude(Double.parseDouble(row.getLongitude()))
                    .category(LocationCategory.CULTURE)
                    .theme(row.getTheme())
                    .phoneNumber(row.getPhoneNumber())
                    .homepageUrl(row.getHomepageUrl())
                    .imageUrl(row.getImageUrl())
                    .description(row.getDescription())
                    .build();

            if (!locationRepository.existsByName(location.getName())) {
                locationRepository.save(location);
            }
        }
    }
}
 */