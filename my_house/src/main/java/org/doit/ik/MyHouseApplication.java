package org.doit.ik;

import org.doit.ik.api.LawdCodeInitService;
import org.doit.ik.complex.ComplexDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing 
public class MyHouseApplication {
	public static void main(String[] args) {
		SpringApplication.run(MyHouseApplication.class, args);
	}
	/*
	@Bean
	CommandLineRunner init(LawdCodeInitService service) {
		return args -> {
			service.importLawdCodes();
			System.out.println(">>>> 전국 법정동 코드 초기화 프로세스 완료!");
		};
	}
	
	
	@Bean
	CommandLineRunner testApi(ComplexDataService service) {
	    return args -> {
	        // 별도의 스레드에서 실행하거나, 초기화 완료 후 잠시 대기했다가 실행
	        Thread.sleep(2000); // 컨텍스트가 완전히 안정화될 때까지 대기
	        System.out.println(">>>> 데이터 수집 테스트 시작!");
	        service.collectAllTypes("202407");
	        System.out.println(">>>> 데이터 수집 테스트 완료!");
	    };
	}
	*/
	/*
	@Bean
    CommandLineRunner importCctv(org.doit.ik.api.CctvImportService service) {
        return args -> {
            String filePath = "C:\\Class\\cctv_data.csv"; // 💡 1번 방식 (절대 경로)
            System.out.println(">>>> CCTV 데이터 임포트 시작: " + filePath);
            
            service.importCctvCsv(filePath);
            
            System.out.println(">>>> CCTV 데이터 임포트 완료!");
        };
    }
	*/
	/*
	@Bean
    CommandLineRunner collectLh(org.doit.ik.api.LhNoticeService service) {
        return args -> {
            System.out.println(">>>> LH 공고 데이터 수집 및 좌표 매칭 시작!");
            
            // 아까 만든 수집 메서드 호출
            service.collectLhNotices();
            
            System.out.println(">>>> LH 공고 데이터 수집 완료! 이제 지도에서 매칭된 핀을 확인할 수 있습니다.");
        };
    }
    */
	/*
	@Bean
    CommandLineRunner importSafePath(org.doit.ik.api.SafePathImportService service) {
        return args -> {
            System.out.println(">>>> [데이터 초기화] 서울특별시 안심귀갓길 경로 데이터 임포트 시작!");
            
            // SafePathImportService에서 만든 실행 메서드 호출
            service.runImport();
            
            System.out.println(">>>> [데이터 초기화] 안심귀갓길 데이터 임포트 완료!");
        };
    }
    */
}
