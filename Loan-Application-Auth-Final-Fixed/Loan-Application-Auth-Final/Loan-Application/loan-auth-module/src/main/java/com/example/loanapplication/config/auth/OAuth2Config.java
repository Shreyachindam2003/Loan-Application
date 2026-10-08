package com.example.loanapplication.config.auth;

import com.example.loanapplication.entity.auth.Role;
import com.example.loanapplication.entity.auth.User;
import com.example.loanapplication.repository.auth.RoleRepository;
import com.example.loanapplication.repository.auth.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Configuration
public class OAuth2Config {

    @Bean
    OAuth2UserService<OAuth2UserRequest, OAuth2User> googleUserService(
            UserRepository userRepository,
            RoleRepository roleRepository) {

        DefaultOAuth2UserService delegate =
                new DefaultOAuth2UserService();

        return request -> {

            OAuth2User oauthUser =
                    delegate.loadUser(request);

            String email =
                    oauthUser.getAttribute("email");

            String name =
                    oauthUser.getAttribute("name");

            String providerId =
                    oauthUser.getAttribute("sub");

            if (email != null
                    && !userRepository.existsByEmail(email)) {

                Role role =
                        roleRepository
                                .findByRoleName("User")
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "User role not found"));

                User user = new User();

                user.setFirstName(
                        name == null ? "Google" : name);
                user.setEmail(email);
                user.setRole(role);
                user.setOauthProvider("google");
                user.setOauthProviderId(providerId);
                user.setTwoFactorEnabled(false);

                userRepository.save(user);
            }

            return oauthUser;
        };
    }
}
