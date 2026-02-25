package org.doit.ik.api;

import java.time.LocalDateTime;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
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
        
        this.restTemplate = new RestTemplate();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE); 
        this.restTemplate.setUriTemplateHandler(factory);
    }
    
    @Value("${api.key}")
    private String serviceKey;

    @Transactional
    public void collectLhNotices() {
        String[] targetCodes = {"05", "06"};

        for (String code : targetCodes) {
            int page = 1;
            boolean hasMore = true;
            String typeName = code.equals("05") ? "분양주택" : "임대주택";

            log.info(">>>> LH {} 모든 공고 수집 시작", typeName);

            while (hasMore) {
                try {
                    // 페이지당 50개씩, 페이지 번호를 1씩 올려가며 호출
                    String listUrl = "https://apis.data.go.kr/B552555/lhLeaseNoticeInfo1/lhLeaseNoticeInfo1"
                            + "?serviceKey=" + serviceKey 
                            + "&PG_SZ=50"
                            + "&PAGE=" + page
                            + "&_type=json"
                            + "&UPP_AIS_TP_CD=" + code;
                    
                    log.info("{} 수집 중... (페이지: {})", typeName, page);
                    
                    String response = restTemplate.getForObject(listUrl, String.class);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(response);
                    
                    // 1. 응답 데이터 검증
                    if (root == null || root.size() < 2 || !root.get(1).has("dsList")) {
                        log.info("{} 수집 완료 (더 이상 데이터 없음)", typeName);
                        hasMore = false;
                        continue;
                    }

                    JsonNode dsList = root.get(1).get("dsList");
                    if (dsList.size() == 0) {
                        hasMore = false;
                        continue;
                    }

                    // 2. 목록 순회 및 상세 정보 수집
                    for (JsonNode item : dsList) {
                        processNoticeItem(item, typeName);
                    }

                    // 3. 페이지 증가
                    page++;
                    
                    // API 과부하 방지를 위한 짧은 휴식 (선택 사항)
                    Thread.sleep(200); 

                } catch (Exception e) {
                    log.error("{} {}페이지 수집 중 오류: {}", typeName, page, e.getMessage());
                    hasMore = false; // 에러 발생 시 중단
                }
            }
        }
        log.info(">>>> 모든 LH 공고 수집 프로세스 종료");
    }

    private void processNoticeItem(JsonNode item, String typeName) {
        String panId = item.path("PAN_ID").asText("");
        if (panId.isEmpty()) return;

        String address = fetchDetailAddress(item);

        if (address == null || address.isEmpty() || address.equals("단지주소")) {
            String region = item.path("CNP_CD_NM").asText("");
            String title = item.path("PAN_NM").asText("");
            address = region + " " + title;
            log.info(">>>> 상세주소 부재로 대체주소 생성: {}", address);
        }

        log.info("[검사] 공고명: {}, 최종 주소: {}", item.path("PAN_NM").asText(), address);

        LhNotice notice = lhNoticeRepository.findById(panId).orElse(new LhNotice());
        notice.setPanId(panId);
        notice.setPanNm(item.path("PAN_NM").asText("제목 없음"));
        notice.setAisTpCdNm(item.path("AIS_TP_CD_NM").asText("-"));
        notice.setPanSs(item.path("PAN_SS").asText("-"));
        notice.setDtlUrl(item.path("DTL_URL").asText(""));
        notice.setClsgDt(item.path("CLSG_DT").asText(""));
        notice.setFullAddress(address);
        notice.setUpdatedAt(LocalDateTime.now());

        // 카카오 좌표 변환
        Double[] coords = kakaoAddressService.getCoordinate(address);
        if (coords != null) {
            notice.setLatitude(coords[0]);
            notice.setLongitude(coords[1]);
        }
        
        lhNoticeRepository.saveAndFlush(notice); 
        log.info(">>>> DB 저장 완료: {}", notice.getPanNm());
    }
    
    private String fetchDetailAddress(JsonNode item) {
        try {
            String panId = item.path("PAN_ID").asText("");
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

            String response = restTemplate.getForObject(detailUrl, String.class);
            JsonNode root = new ObjectMapper().readTree(response);
            
            if (root != null && root.isArray() && root.size() > 1) {
                JsonNode dataNode = root.get(1);
                
                // dsSbd(단지), dsLbd(토지) 등 모든 노드를 순회하며 주소 필드 탐색
                Iterator<String> fieldNames = dataNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode listNode = dataNode.get(fieldName);
                    
                    if (listNode.isArray() && listNode.size() > 0) {
                        JsonNode firstItem = listNode.get(0);
                        // 주소 필드(LCT_ARA_ADR)가 있는지 확인
                        if (firstItem.has("LCT_ARA_ADR")) {
                            return firstItem.get("LCT_ARA_ADR").asText();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("상세 주소 파싱 에러 (PAN_ID: {}): {}", item.path("PAN_ID").asText(), e.getMessage());
        }
        return null;
    }
}