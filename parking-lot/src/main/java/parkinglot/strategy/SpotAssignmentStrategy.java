package parkinglot.strategy;

import parkinglot.enums.VehicleType;
import parkinglot.infrastructure.ParkingFloor;
import parkinglot.models.ParkingSpot;
import parkinglot.exceptions.NoSpotAvailableException;

import java.util.List;

/**
 * Strategy interface for parking spot assignment.
 *
 * Design pattern: Strategy Pattern
 *
 * Why Strategy here?
 * Spot assignment is a business rule that can change independently of the
 * rest of the system. Today we use "first available". Tomorrow the business
 * may want "nearest to entrance" or "prefer ground floor for accessibility".
 *
 * By defining this interface, we can swap assignment algorithms without
 * touching Entrance, ParkingLot, or any other class. Each algorithm lives
 * in its own class and is fully testable in isolation.
 */
public interface SpotAssignmentStrategy {

    /**
     * Find and return an available compatible spot for the given vehicle.
     *
     * The implementation is responsible for:
     * 1. Determining which spot types are compatible with the vehicle type.
     * 2. Searching across all floors for an available compatible spot.
     * 3. Marking the spot as OCCUPIED before returning it.
     *
     * @param vehicleType   type of the incoming vehicle
     * @param isAccessible  true if the driver has an accessibility permit
     * @param floors        all floors in the parking lot to search across
     * @return              the assigned ParkingSpot
     * @throws NoSpotAvailableException if no compatible spot is found
     */
    ParkingSpot assignSpot(VehicleType vehicleType, boolean isAccessible,
                           List<ParkingFloor> floors);
}
