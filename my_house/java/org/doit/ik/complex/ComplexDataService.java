package org.doit.ik.complex;

import java.time.LocalDateTime;
import java.util.List;

import org.doit.ik.api.AptApiService;
import org.doit.ik.api.HouseApiDto;
import org.doit.ik.api.KakaoAddressService;
import org.doit.ik.api.LawdCode;
import org.doit.ik.api.LawdCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplexDataService {

    private final AptApiService aptApiService;
    private final ComplexRepository complexRepository;
    private final RoomTypeRepository roomTypeRepository; 
    private final KakaoAddressService kakaoAddressService; 
    private final LawdCodeRepository lawdCodeRepository;

    @Transactional
    public void collectAndSaveComplexData(String lawdCd, String dealYmd, String type) {
        String cleanLawdCd = lawdCd.trim();
        LawdCode region = lawdCodeRepository.findById(cleanLawdCd)
                .orElseThrow(() -> new RuntimeException("등록되지 않은 지역코드입니다: [" + cleanLawdCd + "]"));

        String regionPrefix = region.getCityNm() + " " + region.getSigunguNm();
        List<HouseApiDto> dtoList = aptApiService.fetchHousingData(cleanLawdCd, dealYmd, type);

        for (HouseApiDto dto : dtoList) {
            String rawTitle = dto.getTitle();
            String apiAddress = dto.getAddress();
            String fullAddress = regionPrefix + " " + apiAddress; 

            // 1. Complex 조회 및 생성
            Complex complex = complexRepository.findByTitleAndAddressAndType(rawTitle, fullAddress, type)
                    .orElseGet(() -> {
                        Complex newComplex = new Complex();
                        newComplex.setTitle(rawTitle);
                        newComplex.setAddress(fullAddress);
                        newComplex.setType(type);
                        newComplex.setCreatedAt(LocalDateTime.now());
                        
                        // 💡 [좌표 추출 로직 시작]
                        // 1단계: 주소 + 건물명 (괄호 제거)
                        String cleanTitle = rawTitle.replaceAll("\\(.*\\)", "").trim();
                        String searchKeyword = (type.equals("SH")) ? fullAddress : fullAddress + " " + cleanTitle;
                        
                        Double[] coords = kakaoAddressService.getCoordinate(searchKeyword);

                        // 2단계: 실패 시 주소(지번)로만 재시도 (아파트/오피스텔 이름 불일치 대비)
                        if (coords == null && !type.equals("SH")) {
                            log.info(">>>> 1차 검색 실패, 주소로 재시도: {}", fullAddress);
                            coords = kakaoAddressService.getCoordinate(fullAddress);
                        }

                        if (coords != null) {
                            newComplex.setLatitude(coords[0]);
                            newComplex.setLongitude(coords[1]);
                        } else {
                            log.warn("!!!! 좌표 추출 최종 실패: {}", fullAddress + " " + rawTitle);
                        }
                        // 💡 [좌표 추출 로직 끝]

                        return complexRepository.save(newComplex);
                    });

            // 2. 데이터 가공
            Integer deposit = parseAmount(dto.getDeposit());
            Integer monthly = parseAmount(dto.getMonthly());
            String area = dto.getArea();

            // 💡 3. 중복 체크 후 RoomType 저장 (Idempotency 보장)
            if (!roomTypeRepository.existsByComplexAndAreaAndDepositAndMonthlyRent(complex, area, deposit, monthly)) {
                RoomType roomType = new RoomType();
                roomType.setComplex(complex); 
                roomType.setDeposit(deposit);
                roomType.setMonthlyRent(monthly);
                roomType.setRentType(monthly > 0 ? "월세" : "전세");
                roomType.setTypeName(area + "㎡");
                roomType.setArea(area);
                roomType.setStatus(RoomStatus.ACTIVE);

                roomTypeRepository.save(roomType);
            } else {
                // 이미 존재할 경우 로그만 찍고 넘어감 (DB 비대화 방지)
                log.debug("중복 거래 데이터 스킵: {} - {}㎡", complex.getTitle(), area);
            }
        }
        log.info("[{}] {}건의 데이터 처리 완료", type, dtoList.size());
    }

    public void collectAllTypes(String dealYmd) {
        String[] types = {"APT", "OFFI", "SH"};
        for (String type : types) {
            collectAllActiveRegions(dealYmd, type);
        }
    }

    public void collectAllActiveRegions(String dealYmd, String type) {
        List<LawdCode> activeRegions = lawdCodeRepository.findByIsActiveTrue();
        for (LawdCode region : activeRegions) {
            try {
                collectAndSaveComplexData(region.getLawdCd(), dealYmd, type);
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("!!!! {} 지역 수집 에러: {}", region.getSigunguNm(), e.getMessage());
            }
        }
    }

    private Integer parseAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(amount.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}