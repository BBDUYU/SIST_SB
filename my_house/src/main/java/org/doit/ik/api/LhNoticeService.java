package org.doit.ik.api;

import java.time.LocalDateTime;
import java.util.Iterator;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 중요: Spring용 Transactional
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LhNoticeService {

    private final LhNoticeRepository lhNoticeRepository;
    private final KakaoAddressService kakaoAddressService;
    private final RestTemplate restTemplate;

    public LhNoticeService(LhNoticeRepository lhNoticeRepository, KakaoAddressService kakaoAddressService) {
        this.lhNoticeRepository = lhNoticeRepository;
        this.kakaoAddressService = kakaoAddressService;
        
        // 🚀 Apache HttpClient5 설정: 5초 지나면 강제로 연결 끊기
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(5))
                .setResponseTimeout(Timeout.ofSeconds(5))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // Factory 교체
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        
        this.restTemplate = new RestTemplate(factory);
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory();
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE); 
        this.restTemplate.setUriTemplateHandler(uriFactory);
    }
    
    @Value("${api.key}")
    private String serviceKey;

    // ❌ [수정] 메서드 전체 @Transactional 제거 (그래야 건별로 Commit 됨)
    public void collectLhNotices() {
        log.info(">>>> LH 공고 데이터 전체 재수집 시작!");
        String[] targetCodes = {"05", "06"};

        for (String code : targetCodes) {
            int page = 1;
            boolean hasMore = true;

            while (hasMore) {
                try {
                    log.info(">>>> [데이터 요청] 코드: {}, 현재페이지: {}", code, page);
                    Thread.sleep(1000);

                    String listUrl = "https://apis.data.go.kr/B552555/lhLeaseNoticeInfo1/lhLeaseNoticeInfo1"
                            + "?serviceKey=" + serviceKey 
                            + "&PG_SZ=50&PAGE=" + page + "&_type=json&UPP_AIS_TP_CD=" + code;
                    
                    String response = restTemplate.getForObject(listUrl, String.class);
                    JsonNode root = new ObjectMapper().readTree(response);
                    
                    // 🚩 수정 포인트: dsList가 없거나, 비어있으면(isEmpty) 종료!
                    if (root == null || root.size() < 2 || !root.get(1).has("dsList") || root.get(1).get("dsList").isEmpty()) {
                        log.info(">>>> [수집 완료] {} 코드의 모든 데이터를 수집했습니다. (마지막 페이지: {})", code, page);
                        hasMore = false;
                        continue;
                    }

                    JsonNode dsList = root.get(1).get("dsList");
                    
                    // 데이터가 실제로 있을 때만 루프 실행
                    for (JsonNode item : dsList) {
                        if (!"공고중".equals(item.path("PAN_SS").asText(""))) continue;
                        try {
                            processNoticeItem(item);
                        } catch (Exception e) {
                            log.error(">>>> [건너뜀] 개별 항목 처리 중 에러: {}", e.getMessage());
                        }
                    }
                    
                    page++; // 데이터 처리가 끝난 후 다음 페이지로
                } catch (Exception e) {
                    log.error(">>>> [중단] 페이지 수집 중 에러 발생: {}", e.getMessage());
                    hasMore = false;
                }
            }
        }
        log.info(">>>> 모든 데이터 수집 작업이 완료되었습니다!");
    }

    // ✨ [수정] 여기에 @Transactional 추가 (한 건마다 즉시 DB 반영)
    @Transactional
    public void processNoticeItem(JsonNode item) {
        String panId = item.path("PAN_ID").asText("");
        String address = fetchDetailAddress(item);
        
        // 1. 주소 데이터 보정 (기존 로직 동일)
        if (address == null || address.isEmpty() || address.equals("단지주소")) {
            String region = item.path("CNP_CD_NM").asText("");
            String title = item.path("PAN_NM").asText();
            String cleanTitle = title.replaceAll("\\(.*?\\)", "").replaceAll("\\[.*?\\]", "").trim();
            
            // 검색어 노이즈 제거
            String[] junkKeywords = {"입주자", "공고", "잔여", "일반", "추가", "매각", "블록", "권", "후", "선착순"};
            for (String key : junkKeywords) {
                if (cleanTitle.contains(key)) cleanTitle = cleanTitle.split(key)[0].trim();
            }
            address = (cleanTitle.length() < 2) ? region : region + " " + cleanTitle;
        }

        // 2. 카카오 API로 좌표 추출
        Double[] coords = kakaoAddressService.getCoordinate(address);
        
        // 3. 검색어 단축 재시도 로직
        if (coords == null && address.contains(" ")) {
            String[] parts = address.split(" ");
            for (int i = 1; i <= 2; i++) {
                if (parts.length - i < 2) break;
                String retryAddr = String.join(" ", java.util.Arrays.copyOfRange(parts, 0, parts.length - i));
                coords = kakaoAddressService.getCoordinate(retryAddr);
                if (coords != null) {
                    address = retryAddr;
                    break;
                }
            }
        }

        // 🚀 [핵심 수정] 좌표가 있을 때만 저장!
        if (coords != null) {
            LhNotice notice = lhNoticeRepository.findById(panId).orElse(new LhNotice());
            notice.setPanId(panId);
            notice.setPanNm(item.path("PAN_NM").asText());
            notice.setAisTpCdNm(item.path("AIS_TP_CD_NM").asText());
            notice.setPanSs(item.path("PAN_SS").asText());
            notice.setDtlUrl(item.path("DTL_URL").asText());
            notice.setClsgDt(item.path("CLSG_DT").asText());
            notice.setFullAddress(address);
            notice.setLatitude(coords[0]);
            notice.setLongitude(coords[1]);
            notice.setUpdatedAt(LocalDateTime.now());

            lhNoticeRepository.saveAndFlush(notice); 
            log.info(">>>> [저장 완료] {} (좌표: {}, {})", address, coords[0], coords[1]);
        } else {
            // 좌표를 못 찾으면 저장하지 않고 로그만 남기고 종료
            log.warn(">>>> [저장 건너뜀] 좌표를 찾을 수 없음: {}", address);
        }
    }

    private String fetchDetailAddress(JsonNode item) {
        String panId = item.path("PAN_ID").asText("");
        String panNm = item.path("PAN_NM").asText();
        
        // 🚩 [원인파악] 호출 시작 로그 추가
        log.info(">>>> [상세조회 시작] PAN_ID: {}, 공고명: {}", panId, panNm);

        try {
            String uppAisTpCd = item.path("UPP_AIS_TP_CD").asText("");
            String splInfTpCd = item.path("SPL_INF_TP_CD").asText("");
            String ccrCnntSysDsCd = item.path("CCR_CNNT_SYS_DS_CD").asText("");
            String aisTpCd = item.path("AIS_TP_CD").asText("");

            String detailUrl = "https://apis.data.go.kr/B552555/lhLeaseNoticeDtlInfo1/getLeaseNoticeDtlInfo1"
                             + "?serviceKey=" + serviceKey
                             + "&PAN_ID=" + panId
                             + "&UPP_AIS_TP_CD=" + uppAisTpCd
                             + "&SPL_INF_TP_CD=" + splInfTpCd
                             + "&CCR_CNNT_SYS_DS_CD=" + ccrCnntSysDsCd
                             + "&AIS_TP_CD=" + aisTpCd
                             + "&_type=json";

            // ⏱️ 타임아웃 발생 시 catch 블록으로 던져지도록 처리
            String response = restTemplate.getForObject(detailUrl, String.class);
            
            // 응답을 받았을 때만 로그 출력
            log.info(">>>> [상세조회 완료] PAN_ID: {}", panId);

            JsonNode root = new ObjectMapper().readTree(response);

            if (root != null && root.isArray() && root.size() > 1) {
                JsonNode dataNode = root.get(1);
                Iterator<String> fieldNames = dataNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    if (fieldName.endsWith("Nm")) continue;

                    JsonNode listNode = dataNode.get(fieldName);
                    if (listNode.isArray() && listNode.size() > 0) {
                        JsonNode firstItem = listNode.get(0);
                        if (firstItem.has("LCT_ARA_ADR")) {
                            String addr = firstItem.get("LCT_ARA_ADR").asText("").trim();
                            if (addr.isEmpty() || addr.equals("단지주소")) continue;
                            String dtlAddr = firstItem.path("LCT_ARA_DTL_ADR").asText("").trim();
                            if (dtlAddr.equals("단지상세주소")) dtlAddr = "";
                            return (addr + " " + dtlAddr).trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 🚨 타임아웃이나 에러 발생 시 로그를 찍고 null을 리턴하여 다음으로 넘어가게 함
            log.error(">>>> [상세조회 실패/타임아웃] PAN_ID: {}, 사유: {}", panId, e.getMessage());
        }
        return null; 
    }
}