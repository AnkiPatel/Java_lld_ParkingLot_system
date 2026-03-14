package parkinglot.strategy;

import parkinglot.enums.SpotType;
import parkinglot.enums.VehicleType;
import parkinglot.exceptions.NoSpotAvailableException;
import parkinglot.infrastructure.ParkingFloor;
import parkinglot.models.ParkingSpot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Concrete Strategy: Assigns the first available compatible spot.
 *
 * Algorithm:
 * 1. Determine which SpotTypes are compatible with the incoming vehicle.
 * 2. Iterate floors in order (Floor 1, Floor 2, ...).
 * 3. On each floor, iterate all spots.
 * 4. Return the first spot that is both AVAILABLE and in the compatible list.
 * 5. If no spot found after full scan, throw NoSpotAvailableException.
 *
 * Time complexity: O(N) worst case, where N = total spots in the lot.
 * Average case is much better since most entries find a spot on lower floors.
 *
 * Vehicle-to-Spot Compatibility Matrix:
 * ------------------------------------------------------------------
 * | Vehicle Type | ACCESSIBLE | COMPACT | LARGE  | MOTORCYCLE |
 * ------------------------------------------------------------------
 * | CAR          |  if permit |   YES   |  YES   |    NO      |
 * | TRUCK        |    NO      |   NO    |  YES   |    NO      |
 * | VAN          |    NO      |   NO    |  YES   |    NO      |
 * | MOTORCYCLE   |    NO      |   NO    |  NO    |    YES     |
 * ------------------------------------------------------------------
 */
public class FirstAvailableStrategy implements SpotAssignmentStrategy {

    @Override
    public ParkingSpot assignSpot(VehicleType vehicleType, boolean isAccessible,
                                  List<ParkingFloor> floors) {

        List<SpotType> compatibleTypes = getCompatibleSpotTypes(vehicleType, isAccessible);

        for (ParkingFloor floor : floors) {
            Collection<ParkingSpot> spots = floor.getSpots().values();

            for (ParkingSpot spot : spots) {
                if (spot.isAvailable() && compatibleTypes.contains(spot.getSpotType())) {
                    // Mark spot as occupied on the floor (updates counts + display board)
                    floor.occupySpot(spot.getSpotId());
                    return spot;
                }
            }
        }

        throw new NoSpotAvailableException(
                "No compatible spot available for vehicle type: " + vehicleType);
    }

    /**
     * Returns the list of SpotTypes that the given vehicle can park in.
     *
     * Key business rules:
     * - Accessible spots are only for vehicles with the accessibility permit.
     * - CAR without permit: COMPACT or LARGE
     * - CAR with permit:    ACCESSIBLE only
     * - TRUCK / VAN:        LARGE only (too big for COMPACT)
     * - MOTORCYCLE:         MOTORCYCLE only
     */
    private List<SpotType> getCompatibleSpotTypes(VehicleType vehicleType,
                                                   boolean isAccessible) {
        List<SpotType> compatible = new ArrayList<SpotType>();

        if (vehicleType == VehicleType.MOTORCYCLE) {
            compatible.add(SpotType.MOTORCYCLE);

        } else if (vehicleType == VehicleType.CAR) {
            if (isAccessible) {
                compatible.add(SpotType.ACCESSIBLE);
            } else {
                compatible.add(SpotType.COMPACT);
                compatible.add(SpotType.LARGE);
            }

        } else if (vehicleType == VehicleType.TRUCK) {
            compatible.add(SpotType.LARGE);

        } else if (vehicleType == VehicleType.VAN) {
            compatible.add(SpotType.LARGE);
        }

        return compatible;
    }
}
