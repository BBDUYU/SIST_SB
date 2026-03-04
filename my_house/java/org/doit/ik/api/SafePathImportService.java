package org.doit.ik.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class SafePathImportService {

    private final SafeReturnPathRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void runImport() {
        try {
            // 1. 리소스 폴더에서 파일 읽기
            ClassPathResource resource = new ClassPathResource("data/seoul_safe_path.json");
            InputStream inputStream = resource.getInputStream();
            JsonNode root = objectMapper.readTree(inputStream);
            
            JsonNode features = root.path("features");
            log.info(">>>> [안심귀갓길] 데이터 임포트 시작... 총 {}건", features.size());

            if (features.isArray()) {
                for (JsonNode feature : features) {
                    saveFeature(feature);
                }
            }
            log.info(">>>> [안심귀갓길] 데이터 임포트 완료!");
        } catch (Exception e) {
            log.error(">>>> [안심귀갓길] 임포트 중 에러: {}", e.getMessage());
        }
    }

    private void saveFeature(JsonNode feature) {
        JsonNode props = feature.path("properties");
        JsonNode geom = feature.path("geometry");

        SafeReturnPath path = new SafeReturnPath();
        path.setDetailLocation(props.path("세부위치").asText());
        path.setSigungu(props.path("시군구명").asText());
        path.setBjdName(props.path("읍면동명").asText());
        path.setBellCount(props.path("안심벨").asInt());
        path.setCctvCount(props.path("CCTV").asInt());
        path.setLampCount(props.path("보안등").asInt());
        
        path.setPathCoordinates(geom.path("coordinates").toString());
        path.setUpdatedAt(LocalDateTime.now());

        repository.save(path);
    }
}