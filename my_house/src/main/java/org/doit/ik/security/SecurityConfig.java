package org.doit.ik.security;

import org.doit.ik.admin.CustomAccessDeniedHandler;
import org.doit.ik.security.oauth.CustomOAuth2UserService;
import org.doit.ik.user.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(customUserDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {

        http
            .authenticationProvider(daoAuthenticationProvider)
            .authorizeHttpRequests(auth -> auth
            		.requestMatchers(new AntPathRequestMatcher("/admin/**")).hasAuthority("ROLE_ADMIN")
                .requestMatchers(
                		
                    // 1. 누구나 접근 가능한 페이지 (Public)
                    new AntPathRequestMatcher("/"),
                    new AntPathRequestMatcher("/main"),
                    new AntPathRequestMatcher("/error"),
                    new AntPathRequestMatcher("/mypage/**"),                    
                    new AntPathRequestMatcher("/user/login"),
                    new AntPathRequestMatcher("/user/signup"),
                    new AntPathRequestMatcher("/user/find-id**"),
                    new AntPathRequestMatcher("/user/find-password**"),
                    new AntPathRequestMatcher("/api/auth/**"),
                    new AntPathRequestMatcher("/api/**"), 
                    new AntPathRequestMatcher("/css/**"),
                    new AntPathRequestMatcher("/js/**"),
                    new AntPathRequestMatcher("/image/**"),
                    new AntPathRequestMatcher("/images/**"),
                    new AntPathRequestMatcher("/oauth2/**"),
                    new AntPathRequestMatcher("/login/**"),
                    new AntPathRequestMatcher("/api/**"),                    
                    new AntPathRequestMatcher("/listing/**"),                    
                    new AntPathRequestMatcher("/h2-console/**")
                ).permitAll()
                
	             // 게시판 읽기(목록/상세)
	             .requestMatchers(HttpMethod.GET, "/board", "/board/*").permitAll()
	
	             // 게시판 쓰기/수정/댓글/삭제는 로그인
	             .requestMatchers(
	                "/board/write",
	                "/board/*/edit",
	                "/board/*/reply**",
	                "/board/*/delete**"
	             ).authenticated()

             
                .requestMatchers(
                    new AntPathRequestMatcher("/user/setup-phone"),
                    new AntPathRequestMatcher("/user/verify-phone"),
                    new AntPathRequestMatcher("/user/api/send-sms"),
                    new AntPathRequestMatcher("/user/api/verify-phone")
                ).permitAll()

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/user/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .failureUrl("/user/login?error=true")
                .defaultSuccessUrl("/main", true)
                .permitAll()
            )

            .oauth2Login(oauth2 -> oauth2
                .loginPage("/user/login")
                .defaultSuccessUrl("/main", false)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                    
                )
             // -----------------------------------------------------------
                // ✅ 이 부분 추가: OAuth2 로그인 실패 시 로그를 남깁니다.
                .failureHandler((request, response, exception) -> {
                    // 에러 메시지 추출
                    String errorMessage = exception.getMessage(); 
                    
                    // 세션에 에러 메시지 저장 (한글 깨짐 방지 위해 인코딩하거나 세션 활용)
                    request.getSession().setAttribute("LOGIN_ERROR_MSG", errorMessage);
                    
                    System.out.println("!!!!! OAuth2 Login Failed !!!!! : " + errorMessage);
                    
                    // 에러 파라미터를 붙여서 리다이렉트
                    response.sendRedirect("/user/login?error=true");
                })
                // -----------------------------------------------------------
            )

            .logout(logout -> logout
            	    .logoutUrl("/user/logout")
            	    .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout", "GET"))
            	    // ✅ 복잡한 핸들러 대신, 로그아웃 성공 시 바로 /main으로 이동
            	    .logoutSuccessUrl("/main") 
            	    .invalidateHttpSession(true) // 우리 서버 세션 삭제
            	    .clearAuthentication(true)   // 인증 정보 삭제
            	    .deleteCookies("JSESSIONID") // 쿠키 삭제
            	    .permitAll()
            	);

        http.exceptionHandling(exception -> exception
                .accessDeniedHandler(accessDeniedHandler)
            );
        
        // CSRF 설정
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers(
                new AntPathRequestMatcher("/user/find-id**"),
                new AntPathRequestMatcher("/user/find-password**"),
                new AntPathRequestMatcher("/api/auth/**"),
                new AntPathRequestMatcher("/h2-console/**"),
                // 번호 인증 API (fetch 통신 시 토스트/헤더 처리가 번거로우면 여기에 추가)
                new AntPathRequestMatcher("/user/api/**"),
                new AntPathRequestMatcher("/api/**")
            )
        );

        // H2 콘솔 사용 시 프레임 허용
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}