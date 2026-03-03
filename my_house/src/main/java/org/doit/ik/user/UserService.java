package org.doit.ik.user;

import org.springframework.security.core.Authentication;

public interface UserService {
    Long registerEmail(UserSignupForm form);
    User registerOrLoadSocial(Provider provider, String providerId, String email, String name);
    void withdrawCurrentUser(Authentication authentication);
    void withdraw(String email);
    void updatePhone(String email, String phone);
}