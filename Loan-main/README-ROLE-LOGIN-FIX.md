# Loan Application Backend — Role-Based Login Response Fix

## Scope
The supplied Spring Boot project structure, controllers, existing endpoints, closure modules, and database configuration are preserved. The only authentication response change is the addition of a `role` field to `AuthResponse`, populated from the user's existing database `Roles` relationship when tokens are issued.

- `User` maps to customer in the frontend.
- `Loan Officer` maps to officer in the frontend.
- Registration and existing OAuth/2FA endpoints are unchanged.
- No schema change or SQL migration is required.

## Build
From this project root on a machine with Java 21 and Maven wrapper download access:
```powershell
.\mvnw.cmd clean compile
```

## Security/configuration note
Do not commit real database, OAuth, or mail credentials to a public repository. Keep local secrets out of Git and rotate any credentials that may have been shared.
