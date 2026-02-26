package org.doit.ik.security.oauth;

import java.util.Map;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getName();
    String getNickname();
    Map<String, Object> getAttributes();
}