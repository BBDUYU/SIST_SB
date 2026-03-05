package org.doit.ik.security;

import org.doit.ik.admin.CustomAccessDeniedHandler;
import org.doit.ik.security.oauth.CustomOAuth2UserService;
import org.doit.ik.user.CustomUserDetailsService;
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
            		.requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")
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
                    System.out.println("!!!!! OAuth2 Login Failed !!!!!");
                    System.out.println("Error Message: " + exception.getMessage());
                    exception.printStackTrace(); // 전체 에러 스택트레이스 출력
                    response.sendRedirect("/user/login?error");
                })
                // -----------------------------------------------------------
            )

            .logout(logout -> logout
                .logoutUrl("/user/logout")
                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout", "GET"))
                .logoutSuccessUrl("/main")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
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