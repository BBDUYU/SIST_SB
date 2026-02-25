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
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_provider_id"),
                    "ProviderId를 가져올 수 없습니다.");
        }

        String email = userInfo.getEmail();
        // 네 엔티티 email NOT NULL + UNIQUE라서, 이메일 없으면 가입 불가로 처리(정석)
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"),
                    "이메일 제공 동의가 필요합니다. (카카오는 account_email scope/동의항목 확인)");
        }

        String nickname = Optional.ofNullable(userInfo.getNickname()).orElse("사용자");
        String name = Optional.ofNullable(userInfo.getName()).orElse(nickname);

        // 1) provider + providerId로 우선 조회
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    // 2) email로 기존 계정이 있으면 정책 결정이 필요
                    //    - 여기서는 "email이 이미 있으면 그 계정에 provider/providerId를 붙여서 연동"하는 쪽으로 처리
                    Optional<User> byEmail = userRepository.findByEmail(email);
                    if (byEmail.isPresent()) {
                        User existing = byEmail.get();
                        existing.setProvider(provider);
                        existing.setProviderId(providerId);
                        // 닉네임이 비어있으면 업데이트
                        if (existing.getNickname() == null || existing.getNickname().isBlank()) {
                            existing.setNickname(nickname);
                        }
                        return existing;
                    }

                    // 3) 신규 생성
                    User created = new User();
                    created.setEmail(email);
                    created.setNickname(nickname);

                    // ⚠️ 현재 엔티티가 NOT NULL이라 임시값 세팅
                    created.setName(name);
                    created.setPhone("000-0000-0000"); // TODO: 추후 추가입력 플로우로 교체 권장

                    created.setProvider(provider);
                    created.setProviderId(providerId);

                    created.setPassword(null); // 소셜은 null
                    created.setRole("ROLE_USER");

                    // status/createdAt은 엔티티 기본값 사용
                    return created;
                });

        userRepository.save(user);

        // SecurityContext에 들어갈 OAuth2User 반환
        // nameAttributeKey는 provider별로 다를 수 있는데, 여기서는 providerId로 통일해도 무방
        Map<String, Object> attrs = oauth2User.getAttributes();

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
                attrs,
                userRequest.getClientRegistration().getProviderDetails()
                        .getUserInfoEndpoint().getUserNameAttributeName()
        );
    }

    private Provider mapProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "kakao" -> Provider.KAKAO;
            case "naver" -> Provider.NAVER;
            case "google" -> Provider.GOOGLE;
            default -> throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
                    "지원하지 않는 provider: " + registrationId);
        };
    }

    private OAuth2UserInfo toUserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleUserInfo(attributes);
            case "naver" -> new NaverUserInfo(attributes);
            case "kakao" -> new KakaoUserInfo(attributes);
            default -> throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
                    "지원하지 않는 provider: " + registrationId);
        };
    }
}