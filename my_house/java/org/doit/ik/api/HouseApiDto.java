package org.doit.ik.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HouseApiDto {

    // 1. 이름 관련 (Complex.title로 변환될 후보들)
    @JsonProperty("offiNm")
    private String offiNm;       // 오피스텔명
    
    @JsonProperty("aptNm")
    private String aptNm;        // 아파트명
    
    @JsonProperty("houseType")
    private String houseType;    // 단독/다가구 유형

    // 2. 주소 관련 (Complex.address로 변환될 후보들)
    @JsonProperty("umdNm")
    private String umdNm;        // 법정동
    
    @JsonProperty("jibun")
    private String jibun;        // 지번

    // 3. 금액 및 면적 (RoomType 저장용)
    @JsonProperty("deposit")
    private String deposit;      // 보증금
    
    @JsonProperty("monthlyRent")
    private String monthly;      // 월세
    
    @JsonProperty("excluUseAr")
    private String excluUseAr;   // 전용면적 (아파트/오피스텔)
    
    @JsonProperty("totalFloorAr")
    private String totalFloorAr; // 연면적 (단독/다가구)

    // --- Complex 엔티티 필드에 맞추기 위한 가공 메서드 ---


    public String getTitle() {
        if (offiNm != null && !offiNm.isEmpty()) return offiNm.trim();
        if (aptNm != null && !aptNm.isEmpty()) return aptNm.trim();
        if (houseType != null && !houseType.isEmpty()) return houseType.trim();
        return "이름없음";
    }


    public String getAddress() {
        String addr = (umdNm != null) ? umdNm.trim() : "";
        if (jibun != null && !jibun.trim().equals("0") && !jibun.isEmpty()) {
            addr += " " + jibun.trim();
        }
        return addr;
    }

    public String getArea() {
        if (excluUseAr != null && !excluUseAr.isEmpty()) return excluUseAr;
        if (totalFloorAr != null && !totalFloorAr.isEmpty()) return totalFloorAr;
        return "0";
    }
}