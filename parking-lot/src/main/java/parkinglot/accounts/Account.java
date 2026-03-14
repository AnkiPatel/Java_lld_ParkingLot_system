package parkinglot.accounts;

import parkinglot.enums.AccountStatus;

/**
 * Abstract base class for all user accounts in the parking lot system.
 *
 * Design decision: Why an abstract class and not an interface?
 * Admin (and future Agent) share common data fields: accountId, name,
 * email, passwordHash, and status. An interface cannot hold state.
 * An abstract class lets us define these shared fields once and avoid
 * code duplication across subclasses.
 *
 * The abstract method viewAccount() enforces that every subclass
 * provides its own display logic (e.g., Admin shows different fields
 * than a future Customer account might).
 *
 * Note: In a real system, password storage would use a proper hashing
 * algorithm (BCrypt, Argon2). Here we store a plain hash string to
 * demonstrate the concept without external dependencies.
 */
public abstract class Account {

    private String accountId;
    private String name;
    private String email;
    private String passwordHash;
    private AccountStatus status;

    public Account(String accountId, String name, String email, String passwordHash) {
        this.accountId = accountId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = AccountStatus.ACTIVE; // All accounts start ACTIVE
    }

    // -------------------------------------------------------------------------
    // Abstract method — each subclass must define its own view
    // -------------------------------------------------------------------------

    /**
     * Display the account details.
     * Subclasses override to show role-specific information.
     */
    public abstract void viewAccount();

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    /**
     * Simulate login: checks account is ACTIVE and passwordHash matches.
     *
     * @param inputPasswordHash the hash of the password entered
     * @return true if credentials are valid and account is active
     */
    public boolean login(String inputPasswordHash) {
        if (status != AccountStatus.ACTIVE) {
            System.out.println("  [AUTH] Login failed: account is " + status);
            return false;
        }
        if (!passwordHash.equals(inputPasswordHash)) {
            System.out.println("  [AUTH] Login failed: invalid credentials for " + email);
            return false;
        }
        System.out.println("  [AUTH] Login successful for: " + name + " (" + email + ")");
        return true;
    }

    /**
     * Simulate logout.
     */
    public void logout() {
        System.out.println("  [AUTH] " + name + " has logged out.");
    }
}
