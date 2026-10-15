package com.example.Loan.main.config.auth;

import com.example.Loan.main.respository.auth.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Configuration
public class OAuth2Config {

    @Bean
    OAuth2UserService<OAuth2UserRequest, OAuth2User> googleUserService(
            UserRepository userRepository) {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User oauthUser = delegate.loadUser(request);
            String email = oauthUser.getAttribute("email");
            Boolean verified = oauthUser.getAttribute("email_verified");
            if (email == null || email.isBlank() || !Boolean.TRUE.equals(verified)) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_user_info"),
                        "Google did not provide a verified email address");
            }
            if (!userRepository.existsByEmail(email.trim().toLowerCase())) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("account_not_registered"),
                        "No registered LoanApp account exists for this Google email. Register first.");
            }
            return oauthUser;
        };
    }
}
