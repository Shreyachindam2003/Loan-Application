# Loan-main backend setup and test guide

This archive preserves the existing Java package/folder layout and the supplied `src/main/resources/application.properties` values for local setup, as requested. Do not commit this file with real credentials to a public repository. If the Google/Gmail values are real, rotate them after sharing the archive and replace them locally.

## Environment
- Java 21
- MySQL database: `LoanApp`
- Port: `8080`
- Hibernate schema mode: `validate` (the app will not create or alter tables)
- Google OAuth callback default: `http://localhost:8080/login/oauth2/code/google`
- Angular origin: `http://localhost:4200`

## Existing roles
- `RoleId=1`, `RoleName=User` (customer)
- `RoleId=2`, `RoleName=Loan Officer` (officer)

Officer accounts are not created by registration or Google OAuth. They must already exist in `Users`, have the `Loan Officer` role, and their password must be BCrypt-encoded to match the app's password encoder. The plain value `12345678` cannot be compared directly with a BCrypt hash. Do not put plaintext passwords in the `Users.Password` column.

## Authentication flow implemented in this update
1. Customer registration sends a six-digit email OTP and stores its SHA-256 hash and expiry.
2. Customer email verification checks hash and expiry.
3. Google OAuth only accepts an existing user with a verified Google email; it does not auto-create a user.
4. Google OAuth does not issue access/refresh tokens directly. It returns `twoFactorRequired=true`; for first-time setup, response includes `twoFactorSetupUri` for manually entering the secret in Google Authenticator.
5. Submit the email and current authenticator code to `POST /api/auth/oauth2/2fa/verify`. On successful TOTP validation, 2FA is enabled and access/refresh tokens are issued.
6. `POST /api/auth/refresh` rotates the refresh token. `POST /api/auth/logout` revokes the provided refresh token.
7. Closure request creation is customer-role protected; request listing, payment creation and closure completion are officer-role protected.

## Main auth endpoints
- `POST /api/auth/register`
- `POST /api/auth/verify-email`
- `POST /api/auth/resend-otp?email=customer@example.com`
- `POST /api/auth/login`
- `GET /oauth2/authorization/google`
- `POST /api/auth/oauth2/2fa/verify` with `{ "email": "registered@example.com", "code": "123456" }`
- `POST /api/auth/refresh` with `{ "refreshToken": "..." }`
- `POST /api/auth/logout` with `{ "refreshToken": "..." }`

## Important validation note
The current container could not download Maven, so this archive has not been verified by a successful Maven compile or runtime test. After extracting on your Windows machine, run from the project root:

```bat
mvnw.cmd clean compile
```

Then start the app and test auth endpoints with Postman before connecting Angular. Fix any compile error shown by your local Maven installation first.

## Closure module
The existing closure APIs and entity structure are retained. The database has `ForeClosureRequests`, `LoanPayments`, `LoanClosures`, `LoanAccounts` and `EmiSchedules`. Before production use, test the complete approval/payment/account-update sequence end to end: payment must not be treated as real just because a client sends a status, and full/partial closure should update the loan account and affected EMI schedules transactionally. The current schema is not altered by this archive.
