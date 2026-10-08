# Loan Application - Authentication Module

Spring Boot 3.5.6 authentication module for the Loan Application.

## Main features
- Customer registration
- 40% monthly-investment validation
- Email OTP verification and resend
- JWT access and refresh tokens
- Logout / refresh-token revocation
- Google OAuth2 login
- TOTP 2FA
- Customer profile
- ModelMapper
- MySQL with `ddl-auto=validate`

## Package structure
```text
com.example.loanapplication
├── config/auth
├── controller/auth
├── dto/auth
├── entity/auth
├── repository/auth
├── security/auth
├── service/auth
└── serviceImpl/auth
```

## Local email / Google setup
Create `src/main/resources/application-local.properties` from
`application-local.properties.example` and enter your own local Gmail App Password
and Google OAuth credentials. This local file is ignored by Git.

Run with the `local` profile from IntelliJ or with:
`--spring.profiles.active=local`

Do not commit Gmail App Passwords or Google client secrets.

## API base paths
Auth endpoints are available under both `/api/auth` and `/api/v1/auth`.

Example:
`POST http://localhost:8080/api/auth/resend-otp?email=your@email.com`

For a customer email that does not exist, the API returns `404 Customer not found`.
