package org.doit.ik.security.oauth;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.doit.ik.user.Provider;
import org.doit.ik.user.User;
import org.doit.ik.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // kakao/naver/google
        Provider provider = mapProvider(registrationId);

        OAuth2UserInfo userInfo = toUserInfo(registrationId, oauth2User.getAttributes());

        String providerId = userInfo.getProviderId();
        if (providerId == null || providerId.isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_provider_id"),
                "ProviderId를 가져올 수 없습니다."
            );
        }

        // ✅ 이메일 필수 정책(현재 너 코드 정책 그대로)
        String emailRaw = userInfo.getEmail();
        if (emailRaw == null || emailRaw.isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("email_not_found"),
                "이메일 제공 동의가 필요합니다."
            );
        }
        final String email = emailRaw.trim().toLowerCase();

        String nickname = Optional.ofNullable(userInfo.getNickname()).orElse("사용자").trim();
        String name = Optional.ofNullable(userInfo.getName()).orElse(nickname).trim();

        // 1) provider + providerId로 우선 조회
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
            .orElseGet(() -> {

                // 2) ✅ 같은 이메일의 LOCAL 계정이 있으면 소셜 연동
                Optional<User> byEmail = userRepository.findByEmailAndDeletedAtIsNull(email);
                if (byEmail.isPresent()) {
                    User existing = byEmail.get();

                    // 이미 다른 소셜로 묶여있으면 차단(정책)
                    if (existing.getProvider() != null
                            && existing.getProvider() != Provider.LOCAL
                            && existing.getProvider() != provider) {
                        throw new OAuth2AuthenticationException(
                            new OAuth2Error("already_linked"),
                            "이미 다른 소셜로 가입된 이메일입니다. 기존 방식으로 로그인해주세요."
                        );
                    }

                    existing.setProvider(provider);
                    existing.setProviderId(providerId);

                    if (existing.getNickname() == null || existing.getNickname().isBlank()) {
                        existing.setNickname(nickname);
                    }
                    if (existing.getName() == null || existing.getName().isBlank()) {
                        existing.setName(name);
                    }

                    return existing;
                }

                // 3) 신규 생성(소셜 간편가입)
                User created = new User();
                created.setEmail(email);
                created.setNickname(nickname);
                created.setName(name);

                created.setProvider(provider);
                created.setProviderId(providerId);

                created.setPassword(null); // 소셜은 null
                created.setRole("ROLE_USER");

                // phone은 회원가입에서 안 받으니 null 유지
                return created;
            });

        userRepository.save(user);

        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
            oauth2User.getAttributes(),
            userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName()
        );
    }

    private Provider mapProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "kakao" -> Provider.KAKAO;
            case "naver" -> Provider.NAVER;
            case "google" -> Provider.GOOGLE;
            default -> throw new OAuth2AuthenticationException(
                new OAuth2Error("unsupported_provider"),
                "지원하지 않는 provider: " + registrationId
            );
        };
    }

    private OAuth2UserInfo toUserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleUserInfo(attributes);
            case "naver" -> new NaverUserInfo(attributes);
            case "kakao" -> new KakaoUserInfo(attributes);
            default -> throw new OAuth2AuthenticationException(
                new OAuth2Error("unsupported_provider"),
                "지원하지 않는 provider: " + registrationId
            );
        };
    }
}