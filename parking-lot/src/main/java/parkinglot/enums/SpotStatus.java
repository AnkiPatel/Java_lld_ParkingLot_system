package parkinglot.enums;

/**
 * Lifecycle states of a parking spot.
 *
 * State transitions:
 *   AVAILABLE  --> OCCUPIED    (vehicle parked)
 *   OCCUPIED   --> AVAILABLE   (vehicle exits)
 *   AVAILABLE  --> UNAVAILABLE (admin removes/maintenance)
 *   UNAVAILABLE --> AVAILABLE  (admin restores)
 */
public enum SpotStatus {
    AVAILABLE,    // Spot is empty and ready to accept a vehicle
    OCCUPIED,     // Spot currently has a vehicle parked
    UNAVAILABLE   // Spot is out of service (maintenance / removed)
}
