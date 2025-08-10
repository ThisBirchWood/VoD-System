package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.security.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomOAuth2User oAuth2user) {
            return oAuth2user.getUser();
        }
        return null;
    }
}
