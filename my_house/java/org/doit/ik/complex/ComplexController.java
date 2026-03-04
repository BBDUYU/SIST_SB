package org.doit.ik.complex;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ComplexController {

    private final ComplexRepository complexRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final SafetyService safetyService;

    @GetMapping("/listing/{cid}/panel")
    public String getDetailPanel(@PathVariable("cid") Long cid, Model model) {
        // 1. 데이터 조회
        Complex complex = complexRepository.findById(cid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ID: " + cid));
        List<RoomType> roomTypes = roomTypeRepository.findByComplex(complex);

        int safetyScore = safetyService.calculateSafetyScore(
            complex.getLatitude(), complex.getLongitude(), complex.getAddress()
        );
        int cctvCount = safetyService.getCctvCount(complex.getLatitude(), complex.getLongitude());
    
        // 2. 기본 정보 매핑
        Map<String, Object> propertyDto = new HashMap<>();
        propertyDto.put("name", complex.getTitle());
        propertyDto.put("address", complex.getAddress());
        propertyDto.put("type", formatType(complex.getType()));
        propertyDto.put("lat", complex.getLatitude());
        propertyDto.put("lng", complex.getLongitude());
        
        // 3. 가격(price) 결정 - formatMoney 적용! ✅
        String priceText = "상세 문의";
        String areaText = "정보 없음";
        if (!roomTypes.isEmpty()) {
            RoomType first = roomTypes.get(0);
            
            // 보증금/전세금을 "억/만" 단위로 변환
            String formattedDeposit = formatMoney(first.getDeposit());
            
            if ("전세".equals(first.getRentType())) {
                priceText = "전세 " + formattedDeposit;
            } else {
                // 월세인 경우 (예: 월세 1,000 / 45)
            	priceText = "보증금 " + formattedDeposit;
                if (first.getMonthlyRent() != null && first.getMonthlyRent() > 0) {
                    priceText += " / 월세 " + String.format("%,d", first.getMonthlyRent());
                }
            }
            areaText = first.getArea();
        }
        propertyDto.put("price", priceText);
        propertyDto.put("area", areaText);

        // 4. 기타 정보 주입
        propertyDto.put("safetyScore", safetyScore); 
        propertyDto.put("cctvCount", cctvCount);
        int safeDist = 0;
        String safeDistText = "정보 없음";

        if (complex.getAddress().contains("서울")) {
            safeDist = safetyService.getSafePathDistance(
                complex.getLatitude(), 
                complex.getLongitude(), 
                complex.getAddress()
            );
            
            // 거리값에 따른 텍스트 분기 처리
            if (safeDist == -1 || safeDist == -2) {
                safeDistText = "1km 이내 없음";
            } else {
                safeDistText = safeDist + "m";
            }
        } else {
            safeDistText = "서울 지역 한정 서비스";
        }

        propertyDto.put("safeRoadDistance", safeDistText);

        model.addAttribute("property", propertyDto); 
        model.addAttribute("roomList", roomTypes);

        return "listing/detail_panel :: panel";
    }

    private String formatMoney(Integer amount) {
        if (amount == null || amount == 0) return "0";
        
        // 10,000 단위가 1억이므로
        int eok = amount / 10000;
        int man = amount % 10000;
        
        StringBuilder sb = new StringBuilder();
        if (eok > 0) {
            sb.append(eok).append("억 ");
        }
        if (man > 0) {
            // 천 단위 콤마 추가 (예: 6,460)
            sb.append(String.format("%,d", man));
        }
        
        if (man > 0 || eok > 0) {
            sb.append("만");
        }
        
        return sb.toString().trim();
    }
    private String formatType(String typeCode) {
        if (typeCode == null) return "정보 없음";
        
        return switch (typeCode.toUpperCase()) {
            case "APT"  -> "아파트";
            case "OFFI" -> "오피스텔";
            case "SH"   -> "주택";
            default     -> typeCode; // 정의되지 않은 코드는 그대로 노출
        };
    }
}

