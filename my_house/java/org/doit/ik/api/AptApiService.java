package org.doit.ik.api;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AptApiService {

    @Value("${api.key}")
    private String SERVICE_KEY;
    
    public List<HouseApiDto> fetchHousingData(String lawdCd, String dealYmd, String type) {
        String baseUrl;
        // 유형별 URL 설정
        if ("OFFI".equals(type)) {
            baseUrl = "http://apis.data.go.kr/1613000/RTMSDataSvcOffiRent/getRTMSDataSvcOffiRent";
        } else if ("SH".equals(type)) {
            baseUrl = "http://apis.data.go.kr/1613000/RTMSDataSvcSHRent/getRTMSDataSvcSHRent";
        } else {
        	baseUrl = "http://apis.data.go.kr/1613000/RTMSDataSvcAptRent/getRTMSDataSvcAptRent";
        }

        // 💡 URI 객체를 직접 생성하여 RestTemplate에 전달 (인코딩 안전)
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("serviceKey", SERVICE_KEY)
                .queryParam("LAWD_CD", lawdCd)
                .queryParam("DEAL_YMD", dealYmd)
                .queryParam("numOfRows", 9999)
                .build(true) // 이미 인코딩된 키라면 true, 아니면 false 시도
                .toUri();

        RestTemplate restTemplate = new RestTemplate();
        
        try {
            log.info("{} API 호출 시작: {}", type, uri);
            String xmlResponse = restTemplate.getForObject(uri, String.class);
            
            // 데이터가 없는 경우 처리
            if (xmlResponse == null || xmlResponse.contains("<items/>") || !xmlResponse.contains("<item>")) {
                log.warn("{} 데이터가 해당 월에 없습니다. (지역: {}, 날짜: {})", type, lawdCd, dealYmd);
                return new ArrayList<>();
            }

            return parseXmlToDto(xmlResponse);
        } catch (Exception e) {
            log.error("API 호출 중 오류 발생: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<HouseApiDto> parseXmlToDto(String xml) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            AptResponse response = xmlMapper.readValue(xml, AptResponse.class);
            
            if (response != null && response.getBody() != null && response.getBody().getItems() != null) {
                return response.getBody().getItems();
            }
        } catch (Exception e) {
            log.error("XML 파싱 실패: {}", e.getMessage());
            log.debug("실패한 XML 내용: {}", xml); // 디버깅용
        }
        return new ArrayList<>();
    }
}