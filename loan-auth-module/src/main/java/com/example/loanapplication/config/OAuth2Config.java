package com.example.loanapplication.config;

import com.example.loanapplication.entity.Customer;
import com.example.loanapplication.entity.Role;
import com.example.loanapplication.entity.User;
import com.example.loanapplication.repository.CustomerRepository;
import com.example.loanapplication.repository.RoleRepository;
import com.example.loanapplication.repository.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Configuration
public class OAuth2Config {
    @Bean
    OAuth2UserService<OAuth2UserRequest, OAuth2User> googleUserService(
            CustomerRepository customerRepository,
            UserRepository userRepository,
            RoleRepository roleRepository) {

        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        return request -> {
            OAuth2User oauthUser = delegate.loadUser(request);
            String email = oauthUser.getAttribute("email");
            String name = oauthUser.getAttribute("name");

            if (email != null && !userRepository.existsByEmail(email)) {
                String firstName = name == null ? "Google" : name;
                Role role = roleRepository.findByRoleName("User").orElseThrow();

                Customer customer = new Customer();
                customer.setFirstName(firstName);
                customer.setEmail(email);
                customer.setEmailVerified(true);
                customerRepository.save(customer);

                User user = new User();
                user.setFirstName(firstName);
                user.setEmail(email);
                user.setRole(role);
                user.setCustomer(customer);
                user.setTwoFactorEnabled(false);
                userRepository.save(user);
            }

            return oauthUser;
        };
    }
}
