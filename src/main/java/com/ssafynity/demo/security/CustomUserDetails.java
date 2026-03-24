package com.ssafynity.demo.security;

import com.ssafynity.demo.domain.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security 인증 객체.
 * Controller에서 @AuthenticationPrincipal CustomUserDetails 로 현재 사용자를 주입받는다.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String nickname;
    private final String role;

    public CustomUserDetails(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.password = member.getPassword();
        this.nickname = member.getNickname();
        this.role = member.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return true; }
}
