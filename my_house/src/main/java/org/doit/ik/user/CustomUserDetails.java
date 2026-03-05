package org.doit.ik.user;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

    private final Long uid;
    private final String email;
    private final String password;
    private final String role;
    private final UserStatus status;

    public CustomUserDetails(User user) {
        this.uid = user.getUid();
        this.email = user.getEmail();
        this.password = user.getPassword(); // BCrypt 해시여야 함
        this.role = user.getRole();         // "ROLE_USER" 형태
        this.status = user.getStatus();     // ACTIVE / DELETED 등
    }

    public Long getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public UserStatus getStatus() {
        return status;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String userRole = (this.role == null) ? "ROLE_USER" : this.role;
        return Collections.singletonList(new SimpleGrantedAuthority(userRole));
    }

    @Override
    public String getPassword() {
        return password; // LOCAL만 값 있음, 소셜은 null일 수 있음
    }

    @Override
    public String getUsername() {
        return email; // ✅ formLogin의 usernameParameter("email")과 매칭
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 탈퇴 상태면 잠금 처리
        return status != UserStatus.DELETED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

 // CustomUserDetails.java 내부
    @Override
    public boolean isEnabled() {
        // ✅ 기존: return status == null || status == UserStatus.ACTIVE;
        // ✅ 변경: status가 오직 ACTIVE일 때만 로그인을 허용함 (BANNED 차단)
        return this.status == UserStatus.ACTIVE;
    }
}