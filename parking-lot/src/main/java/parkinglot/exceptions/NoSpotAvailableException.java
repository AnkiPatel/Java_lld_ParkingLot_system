package parkinglot.exceptions;

/**
 * Thrown when the SpotAssignmentStrategy cannot find any compatible,
 * available parking spot for the incoming vehicle type.
 *
 * This is a RuntimeException (unchecked) because the caller (Entrance)
 * is expected to handle it at a higher level and present a user-facing
 * message, not recover silently.
 */
public class NoSpotAvailableException extends RuntimeException {

    public NoSpotAvailableException(String message) {
        super(message);
    }
}
