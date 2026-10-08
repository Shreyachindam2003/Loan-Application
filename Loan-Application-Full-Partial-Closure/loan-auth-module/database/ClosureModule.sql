USE LoanApp;

CREATE TABLE IF NOT EXISTS LoanDeals (
    DealId INT NOT NULL AUTO_INCREMENT,
    CustomerId INT NOT NULL,
    LoanAmount DECIMAL(18,2) NOT NULL,
    InterestRate DECIMAL(5,2) NOT NULL,
    TenureMonths INT NOT NULL,
    DealStatus VARCHAR(50) NOT NULL,
    CreatedAt DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (DealId),
    FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS LoanAccounts (
    LoanAccountId INT NOT NULL AUTO_INCREMENT,
    CustomerId INT NOT NULL,
    DealId INT NOT NULL,
    LoanAccountNo VARCHAR(100) NOT NULL,
    LoanAmount DECIMAL(18,2) NOT NULL,
    OutstandingPrincipal DECIMAL(18,2) NOT NULL,
    LoanStatus VARCHAR(50) NOT NULL,
    InterestRate DECIMAL(5,2) NOT NULL,
    TenureMonths INT NOT NULL,
    EmiAmount DECIMAL(18,2) NOT NULL,
    DisbursementDate DATETIME(6),
    TotalPaidAmount DECIMAL(18,2) DEFAULT 0,
    PRIMARY KEY (LoanAccountId),
    UNIQUE KEY UX_LoanAccounts_LoanAccountNo (LoanAccountNo),
    FOREIGN KEY (CustomerId) REFERENCES Customers(CustomerId),
    FOREIGN KEY (DealId) REFERENCES LoanDeals(DealId)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS EmiSchedules (
    EmiScheduleId INT NOT NULL AUTO_INCREMENT,
    LoanAccountId INT NOT NULL,
    InstallmentNo INT NOT NULL,
    DueDate DATE NOT NULL,
    PrincipalAmount DECIMAL(18,2) NOT NULL,
    InterestAmount DECIMAL(18,2) NOT NULL,
    OpeningBalance DECIMAL(18,2) NOT NULL,
    ClosingBalance DECIMAL(18,2) NOT NULL,
    Emi DECIMAL(18,2) NOT NULL,
    PaymentStatus VARCHAR(50) NOT NULL,
    PaidDate DATE,
    CancellationReason VARCHAR(500),
    PRIMARY KEY (EmiScheduleId),
    FOREIGN KEY (LoanAccountId) REFERENCES LoanAccounts(LoanAccountId)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ForeClosureRequests (
    RequestId INT NOT NULL AUTO_INCREMENT,
    LoanAccountId INT NOT NULL,
    ForeClosureType VARCHAR(50) NOT NULL,
    ForeClosureAmount DECIMAL(18,2),
    PartialAmount DECIMAL(18,2),
    PartialTenureMonths INT,
    RevisedEmiAmount DECIMAL(18,2),
    RemainingPrincipal DECIMAL(18,2),
    RequestedDate DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    ExpectedClosureDate DATETIME(6),
    Reason VARCHAR(1000),
    Status VARCHAR(50) DEFAULT 'PENDING',
    IsPaid BOOLEAN DEFAULT FALSE,
    PaidDate DATETIME(6),
    ClosedBy INT,
    PRIMARY KEY (RequestId),
    FOREIGN KEY (LoanAccountId) REFERENCES LoanAccounts(LoanAccountId),
    FOREIGN KEY (ClosedBy) REFERENCES Users(UserId)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS LoanPayments (
    PaymentId INT NOT NULL AUTO_INCREMENT,
    LoanAccountId INT NOT NULL,
    PaymentAmount DECIMAL(18,2) NOT NULL,
    PaymentDate DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    PaymentStatus VARCHAR(50) NOT NULL,
    PaymentName VARCHAR(100),
    PRIMARY KEY (PaymentId),
    FOREIGN KEY (LoanAccountId) REFERENCES LoanAccounts(LoanAccountId)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS LoanClosures (
    ClosureId INT NOT NULL AUTO_INCREMENT,
    LoanAccountId INT NOT NULL,
    ClosureType VARCHAR(50) NOT NULL,
    FinalSettlementAmount DECIMAL(18,2) NOT NULL,
    ClosureDate DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    ClosedBy INT,
    Remarks VARCHAR(1000),
    ClosureStatus VARCHAR(50) NOT NULL,
    PRIMARY KEY (ClosureId),
    FOREIGN KEY (LoanAccountId) REFERENCES LoanAccounts(LoanAccountId),
    FOREIGN KEY (ClosedBy) REFERENCES Users(UserId)
) ENGINE=InnoDB;
