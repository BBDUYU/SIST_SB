package org.doit.ik.security.oauth;

import java.util.Map;

@SuppressWarnings("unchecked")
public class NaverUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final Map<String, Object> response;

    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.response = (Map<String, Object>) attributes.get("response");
    }

    @Override public String getProviderId() { return response == null ? null : (String) response.get("id"); }
    @Override public String getEmail() { return response == null ? null : (String) response.get("email"); }
    @Override public String getName() { return response == null ? null : (String) response.get("name"); }
    @Override public String getNickname() { return response == null ? null : (String) response.get("nickname"); }

    @Override public Map<String, Object> getAttributes() { return attributes; }
}