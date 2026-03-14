package parkinglot.enums;

/**
 * Lifecycle states of an admin or agent account.
 */
public enum AccountStatus {
    ACTIVE,   // Account is active and can log in
    INACTIVE, // Account has been deactivated (e.g. employee left)
    BLOCKED   // Account has been blocked due to policy violation
}
