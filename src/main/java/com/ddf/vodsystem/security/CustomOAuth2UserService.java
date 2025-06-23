package com.ddf.vodsystem.security;
import com.ddf.vodsystem.repositories.UserRepository;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService  {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

//        String email = oAuth2User.getAttribute("email");
//        String name = oAuth2User.getAttribute("name");
//        String googleId = oAuth2User.getAttribute("sub");
//
//        userRepository.findByGoogleId(googleId).orElseGet(() -> {
//            User user = new User();
//            user.setEmail(email);
//            user.setName(name);
//            user.setGoogleId(googleId);
//            return userRepository.save(user);
//        });

        return oAuth2User;
    }
}
