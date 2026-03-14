package parkinglot.enums;

/**
 * Lifecycle states of a parking ticket.
 *
 * State transitions:
 *   ACTIVE --> PAID       (customer pays and exits)
 *   ACTIVE --> CANCELLED  (admin cancels ticket, e.g. vehicle towed)
 */
public enum TicketStatus {
    ACTIVE,    // Vehicle is currently parked; payment not yet made
    PAID,      // Payment completed; vehicle has exited
    CANCELLED  // Ticket was voided by admin
}
