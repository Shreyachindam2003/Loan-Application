package com.example.loanapplication.entity.auth;

import jakarta.persistence.*;

@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RoleId", nullable = false)
    private Role role;

    @OneToOne
    @JoinColumn(
            name = "CustomerId",
            referencedColumnName = "CustomerId",
            unique = true
    )
    private Customer customer;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Mobile")
    private String mobile;

    @Column(name = "Password")
    private String password;

    @Column(name = "OAuthProvider")
    private String oauthProvider;

    @Column(name = "OAuthProviderId")
    private String oauthProviderId;

    @Column(name = "TwoFactorEnabled", nullable = false)
    private Boolean twoFactorEnabled = false;

    @Column(name = "TwoFactorSecret")
    private String twoFactorSecret;

    public Integer getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPassword() {
        return password;
    }

    public String getOauthProvider() {
        return oauthProvider;
    }

    public String getOauthProviderId() {
        return oauthProviderId;
    }

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public void setOauthProviderId(String oauthProviderId) {
        this.oauthProviderId = oauthProviderId;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }
}