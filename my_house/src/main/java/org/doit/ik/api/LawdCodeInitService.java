package org.doit.ik.api;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LawdCodeInitService {

    private final LawdCodeRepository lawdCodeRepository;

    @Transactional
    public void importLawdCodes() {
        try {
            // 1. 파일 읽기 (resources/lawd_codes.txt)
            InputStream inputStream = new ClassPathResource("lawd_codes.txt").getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "EUC-KR"));

            List<LawdCode> saveList = new ArrayList<>();
            String line;
            
            // 첫 줄 헤더 건너뛰기 (법정동코드 법정동명 폐지여부)
            reader.readLine(); 

            while ((line = reader.readLine()) != null) {
                // 탭(\t)으로 데이터 구분
                String[] data = line.split("\t");
                if (data.length < 3) continue;

                String fullCode = data[0].trim();   // 1111000000
                String fullName = data[1].trim();   // 서울특별시 종로구
                String isDeleted = data[2].trim();  // 존재

                // 핵심 필터링 로직 (팀장님 컨펌 필요!)
                // 1. '존재'하는 코드일 것
                // 2. 뒤 5자리가 '00000'일 것 (시군구 단위)
                // 3. 이름에 공백이 1개만 있을 것 (예: '서울특별시 종로구' -> OK / '서울특별시' -> NO / '종로구 청운동' -> NO)
                if ("존재".equals(isDeleted) && fullCode.endsWith("00000")) {
                    String[] nameParts = fullName.split(" ");
                    
                    if (nameParts.length == 2) {
                        LawdCode entity = new LawdCode();
                        entity.setLawdCd(fullCode.substring(0, 5)); // 앞 5자리만 추출 (11110)
                        entity.setCityNm(nameParts[0]);             // 서울특별시
                        entity.setSigunguNm(nameParts[1]);          // 종로구
                        entity.setActive(true);
                        
                        saveList.add(entity);
                    }
                }
            }

            // 2. 한 번에 저장 (Batch Insert)
            lawdCodeRepository.saveAll(saveList);
            log.info(">>>> 전국 시군구 코드 {}건 저장 완료!", saveList.size());

        } catch (Exception e) {
            log.error("파일 읽기 중 에러 발생: ", e);
        }
    }
}
