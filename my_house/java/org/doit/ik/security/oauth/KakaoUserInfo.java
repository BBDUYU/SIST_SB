package org.doit.ik.security.oauth;

import java.util.Map;

@SuppressWarnings("unchecked")
public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.profile = kakaoAccount == null ? null : (Map<String, Object>) kakaoAccount.get("profile");
    }

    @Override public String getProviderId() {
        Object id = attributes.get("id");
        return id == null ? null : String.valueOf(id);
    }

    @Override public String getEmail() {
        // scope에 account_email 없으면 null일 수 있음
        return kakaoAccount == null ? null : (String) kakaoAccount.get("email");
    }

    @Override public String getName() {
        // 카카오는 보통 name 제공 안 함 (동의항목에 따라 다름)
        return getNickname();
    }

    @Override public String getNickname() {
        return profile == null ? null : (String) profile.get("nickname");
    }

    @Override public Map<String, Object> getAttributes() { return attributes; }
}