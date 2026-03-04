package org.doit.ik.api;

import java.net.URI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KakaoAddressService {

    @Value("${kakao.api.key}") // 💡 프로퍼티 설정값 사용
    private String KAKAO_REST_API_KEY;

    private final String KAKAO_ADDRESS_URL = "https://dapi.kakao.com/v2/local/search/address.json";
    private final String KAKAO_KEYWORD_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";

    public Double[] getCoordinate(String query) {
        // 1단계: 주소 검색 시도 (가장 정확한 지번/도로명 중심)
        Double[] coords = callKakaoApi(KAKAO_ADDRESS_URL, query);
        
        // 2단계: 주소로 안 나오면 키워드 검색으로 재시도 (건물명 중심)
        if (coords == null) {
            log.info("주소 검색 결과 없음, 키워드로 재시도: {}", query);
            coords = callKakaoApi(KAKAO_KEYWORD_URL, query);
        }
        
        return coords;
    }

    private Double[] callKakaoApi(String url, String query) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + KAKAO_REST_API_KEY);

            URI uri = UriComponentsBuilder
                    .fromHttpUrl(url)
                    .queryParam("query", query)
                    .build()
                    .encode()
                    .toUri();

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            JSONObject jsonResponse = new JSONObject(response.getBody());
            JSONArray documents = jsonResponse.getJSONArray("documents");

            if (documents.length() > 0) {
                JSONObject first = documents.getJSONObject(0);
                // address.json과 keyword.json 모두 x, y 좌표 필드명은 동일합니다.
                return new Double[]{
                    Double.parseDouble(first.getString("y")), // 위도
                    Double.parseDouble(first.getString("x"))  // 경도
                };
            }
        } catch (Exception e) {
            log.error("API 호출 에러 [{}]: {}", query, e.getMessage());
        }
        return null;
    }
}