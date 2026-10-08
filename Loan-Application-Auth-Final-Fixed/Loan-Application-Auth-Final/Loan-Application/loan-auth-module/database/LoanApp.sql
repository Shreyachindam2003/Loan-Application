DROP DATABASE IF EXISTS LoanApp;
CREATE DATABASE LoanApp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE LoanApp;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE Roles (
    RoleId INT NOT NULL AUTO_INCREMENT,
    RoleName VARCHAR(100) NOT NULL,
    PRIMARY KEY (RoleId),
    UNIQUE KEY UX_Roles_RoleName (RoleName)
) ENGINE=InnoDB;

CREATE TABLE Customers (
    CustomerId INT NOT NULL AUTO_INCREMENT,
    FirstName VARCHAR(100) NOT NULL,
    LastName VARCHAR(100) NULL,
    Age INT NOT NULL,
    Email VARCHAR(255) NOT NULL,
    Password VARCHAR(255) NULL,
    MobileNo VARCHAR(20) NULL,
    PanNo VARCHAR(20) NOT NULL,
    AadhaarNo VARCHAR(30) NOT NULL,
    EmploymentType VARCHAR(100) NOT NULL,
    MonthlyIncome DECIMAL(18,2) NOT NULL,
    MonthlyInvestment DECIMAL(18,2) NOT NULL,
    IsEmailVerified BOOLEAN NOT NULL DEFAULT FALSE,
    VerificationOtpHash VARCHAR(255) NULL,
    VerificationOtpExpiry DATETIME(6) NULL,
    CreatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (CustomerId),
    UNIQUE KEY UX_Customers_Email (Email),
    UNIQUE KEY UX_Customers_PanNo (PanNo),
    UNIQUE KEY UX_Customers_AadhaarNo (AadhaarNo)
) ENGINE=InnoDB;

CREATE TABLE Users (
    UserId INT NOT NULL AUTO_INCREMENT,
    RoleId INT NOT NULL,
    CustomerId INT NULL,
    FirstName VARCHAR(100) NULL,
    LastName VARCHAR(100) NULL,
    Email VARCHAR(255) NOT NULL,
    Mobile VARCHAR(20) NULL,
    Password VARCHAR(255) NULL,
    OAuthProvider VARCHAR(50) NULL,
    OAuthProviderId VARCHAR(255) NULL,
    TwoFactorEnabled BOOLEAN NOT NULL DEFAULT FALSE,
    TwoFactorSecret VARCHAR(255) NULL,
    PRIMARY KEY (UserId),
    UNIQUE KEY UX_Users_CustomerId (CustomerId),
    UNIQUE KEY UX_Users_Email (Email),
    UNIQUE KEY UX_Users_OAuthProviderId (OAuthProviderId),
    KEY IX_Users_RoleId (RoleId),
    CONSTRAINT FK_Users_Customers
        FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId),
    CONSTRAINT FK_Users_Roles
        FOREIGN KEY (RoleId) REFERENCES Roles(RoleId)
) ENGINE=InnoDB;

CREATE TABLE RefreshTokens (
    RefreshTokenId BIGINT NOT NULL AUTO_INCREMENT,
    UserId INT NOT NULL,
    TokenHash VARCHAR(255) NOT NULL,
    ExpiresAt DATETIME(6) NOT NULL,
    Revoked BOOLEAN NOT NULL DEFAULT FALSE,
    CreatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (RefreshTokenId),
    UNIQUE KEY UX_RefreshTokens_TokenHash (TokenHash),
    KEY IX_RefreshTokens_UserId (UserId),
    CONSTRAINT FK_RefreshTokens_Users
        FOREIGN KEY (UserId) REFERENCES Users(UserId)
) ENGINE=InnoDB;

CREATE TABLE Notifications (
    NotificationId INT NOT NULL AUTO_INCREMENT,
    CustomerId INT NOT NULL,
    Message VARCHAR(1000) NOT NULL,
    NotificationType VARCHAR(50) NULL,
    ReferenceId INT NULL,
    IsRead BOOLEAN NOT NULL DEFAULT FALSE,
    CreatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (NotificationId),
    KEY IX_Notifications_CustomerId (CustomerId),
    CONSTRAINT FK_Notifications_Customers
        FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId)
) ENGINE=InnoDB;

INSERT INTO Roles (RoleName) VALUES ('User'), ('Loan Officer');

SET FOREIGN_KEY_CHECKS = 1;
