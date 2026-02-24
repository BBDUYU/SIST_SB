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
}
