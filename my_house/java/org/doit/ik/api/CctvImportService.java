package org.doit.ik.api;


import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CctvImportService {

    private final CctvRepository cctvRepository;

    @Transactional
    public void importCctvCsv(String filePath) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "EUC-KR"))) {
            String[] line;
            reader.readNext(); // 헤더(첫 줄) 스킵

            List<Cctv> batchList = new ArrayList<>();
            int totalCount = 0;

            while ((line = reader.readNext()) != null) {
                try {
                    // 데이터가 너무 많으므로 최소한의 위경도 유효성 검사만 진행
                    if (line[12].isEmpty() || line[13].isEmpty()) continue;

                    Cctv cctv = new Cctv();
                    cctv.setAgency(line[2]);       // 관리기관명
                    cctv.setAddressRoad(line[3]);  // 도로명주소
                    cctv.setAddressJibun(line[4]); // 지번주소
                    cctv.setPurpose(CctvPurpose.fromString(line[5]));
                    cctv.setCount(Integer.parseInt(line[6].isEmpty() ? "1" : line[6])); // 카메라대수
                    cctv.setLatitude(Double.parseDouble(line[12]));  // 위도
                    cctv.setLongitude(Double.parseDouble(line[13])); // 경도
                    cctv.setDataDate(line[14]);    // 데이터기준일자

                    batchList.add(cctv);
                    totalCount++;

                    // 1000개 단위로 모아서 저장 후 리스트 비우기 (메모리 관리)
                    if (batchList.size() >= 1000) {
                        cctvRepository.saveAll(batchList);
                        batchList.clear();
                        log.info("{}개 데이터 저장 중...", totalCount);
                    }
                } catch (Exception e) {
                    // 한 줄 에러 나도 멈추지 않고 계속 진행
                    continue; 
                }
            }
            // 남은 데이터 저장
            if (!batchList.isEmpty()) {
                cctvRepository.saveAll(batchList);
            }
            log.info("CCTV 데이터 총 {}건 수집 완료!", totalCount);

        } catch (Exception e) {
            log.error("CSV 파일 로드 실패: {}", e.getMessage());
        }
    }
}