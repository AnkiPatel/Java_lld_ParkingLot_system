package parkinglot.models;

import parkinglot.enums.SpotStatus;
import parkinglot.enums.SpotType;

/**
 * Represents a single physical parking spot in the lot.
 *
 * Design decisions:
 * 1. A spot knows its own type, location, and status.
 * 2. A spot does NOT hold a reference to the parked Vehicle.
 *    That relationship is tracked by the Ticket, which acts as the
 *    contract between the vehicle and the spot. This avoids tight
 *    coupling between ParkingSpot and Vehicle.
 * 3. Status transitions are controlled externally by ParkingFloor,
 *    which also keeps the floor-level availability counts in sync.
 */
public class ParkingSpot {

    private String spotId;       // Unique ID, e.g. "F1-A-001"
    private SpotType spotType;   // Type of spot (COMPACT, LARGE, etc.)
    private int floor;           // Floor number the spot is on
    private String section;      // Section label within the floor (A, B, C...)
    private SpotStatus status;   // Current status of the spot

    public ParkingSpot(String spotId, SpotType spotType, int floor, String section) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.floor = floor;
        this.section = section;
        this.status = SpotStatus.AVAILABLE; // All spots start as available
    }

    public String getSpotId() {
        return spotId;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public int getFloor() {
        return floor;
    }

    public String getSection() {
        return section;
    }

    public SpotStatus getStatus() {
        return status;
    }

    public void setStatus(SpotStatus status) {
        this.status = status;
    }

    /**
     * Convenience method used by SpotAssignmentStrategy to filter spots.
     */
    public boolean isAvailable() {
        return status == SpotStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return spotId + " [" + spotType + "] Floor:" + floor
                + " Section:" + section + " Status:" + status;
    }
}
