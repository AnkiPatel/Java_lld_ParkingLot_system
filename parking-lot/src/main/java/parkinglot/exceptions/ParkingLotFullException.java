package parkinglot.exceptions;

/**
 * Thrown when a vehicle attempts to enter and the parking lot
 * has reached its maximum capacity (totalOccupied >= maxCapacity).
 *
 * Entrance.acceptVehicle() catches this and displays the FULL message.
 */
public class ParkingLotFullException extends RuntimeException {

    public ParkingLotFullException(String message) {
        super(message);
    }
}
