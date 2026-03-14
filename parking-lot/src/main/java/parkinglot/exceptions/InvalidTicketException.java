package parkinglot.exceptions;

/**
 * Thrown when a ticket presented at exit is not in ACTIVE status.
 *
 * Common causes:
 *  - Ticket was already paid (double-scan at exit)
 *  - Ticket was cancelled by admin
 *  - Ticket ID does not exist in the active ticket registry
 */
public class InvalidTicketException extends RuntimeException {

    public InvalidTicketException(String message) {
        super(message);
    }
}
