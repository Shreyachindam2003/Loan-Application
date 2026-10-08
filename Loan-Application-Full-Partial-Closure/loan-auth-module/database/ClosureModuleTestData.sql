USE LoanApp;

-- Create one test deal using the first customer in the database.
INSERT INTO LoanDeals
    (CustomerId, LoanAmount, InterestRate, TenureMonths, DealStatus)
SELECT
    MIN(CustomerId),
    500000.00,
    10.00,
    60,
    'ACTIVE'
FROM Customers;

-- Create one test loan account for the latest deal.
INSERT INTO LoanAccounts
    (CustomerId, DealId, LoanAccountNo, LoanAmount,
     OutstandingPrincipal, LoanStatus, InterestRate,
     TenureMonths, EmiAmount, DisbursementDate, TotalPaidAmount)
SELECT
    CustomerId,
    DealId,
    CONCAT('TEST-LA-', DealId),
    500000.00,
    400000.00,
    'ACTIVE',
    10.00,
    60,
    8491.00,
    NOW(),
    100000.00
FROM LoanDeals
ORDER BY DealId DESC
LIMIT 1;

-- Check the test loan account.
SELECT *
FROM LoanAccounts
ORDER BY LoanAccountId DESC
LIMIT 1;
