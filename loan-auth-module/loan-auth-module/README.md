# Loan Application - Module 1 Backend

## Scope
Only Module 1 is implemented here:
- Customer registration
- 40% monthly-investment rule
- Email OTP verification (development OTP printed in console)
- Login with JWT access + refresh token
- Logout / refresh token revocation
- Google OAuth2 login wiring
- TOTP 2FA using Google Authenticator
- GET /api/v1/customers/me

Angular is intentionally not included.

## Database
1. Create/use the existing `LoanApp` database.
2. Run `sql/module1_auth_customer_registration.sql`.
3. Make sure MySQL username/password in application.properties are correct.

## Important
`spring.jpa.hibernate.ddl-auto=validate` is used intentionally. Hibernate will validate the schema and will NOT create/update tables.

## Run
mvn spring-boot:run

## Registration
POST /api/v1/auth/register

Example:
{
  "firstName": "Rohit",
  "lastName": "Rakshe",
  "age": 25,
  "email": "rohit@example.com",
  "password": "Password@123",
  "mobileNumber": "9876543210",
  "panNo": "ABCDE1234F",
  "aadhaarNo": "123456789012",
  "employmentType": "Private",
  "salary": 50000,
  "monthlyInvestment": 15000
}

Rule:
monthlyInvestment <= salary * 40%

If investment is above 40%, registration is rejected.

## Email verification
POST /api/v1/auth/verify-email

{
  "email": "rohit@example.com",
  "otp": "123456"
}

For this learning/backend-first version the OTP is printed in the Spring Boot console. Later you can connect SMTP/SendGrid/AWS SES.

## Login
POST /api/v1/auth/login

{
  "email": "rohit@example.com",
  "password": "Password@123"
}

If 2FA is enabled, add:
"twoFactorCode": "123456"

## 2FA
After login, call:
POST /api/v1/auth/2fa/setup
Authorization: Bearer <accessToken>

The response contains an `otpauth://` URI. Add that URI to Google Authenticator.

Then:
POST /api/v1/auth/2fa/enable
Authorization: Bearer <accessToken>

{
  "code": "123456"
}

After enabling, login requires the 2FA code.

## JWT refresh
POST /api/v1/auth/refresh
{
  "refreshToken": "..."
}

## Logout
POST /api/v1/auth/logout
{
  "refreshToken": "..."
}

## Profile
GET /api/v1/customers/me
Authorization: Bearer <accessToken>

## Google OAuth2
Open:
http://localhost:8080/oauth2/authorization/google

Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET in the environment or application.properties.

Google redirect URI:
http://localhost:8080/login/oauth2/code/google

The success handler redirects to:
http://localhost:4200/oauth-success?email=...

Angular can later replace this simple redirect handling.

## Production notes
- Replace the sample JWT secret.
- Never log OTPs in production.
- Use an email provider/SMTP for real OTP delivery.
- Encrypt/protect Aadhaar/PAN data and do not expose sensitive fields from profile APIs.
- For production Google OAuth, issue a secure application token/session instead of passing identity in a query parameter.
