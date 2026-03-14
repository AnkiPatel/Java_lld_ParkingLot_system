package parkinglot.infrastructure;

import parkinglot.enums.SpotStatus;
import parkinglot.enums.SpotType;
import parkinglot.models.ParkingSpot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents one physical floor of the parking lot.
 *
 * Responsibilities:
 * 1. Own and manage all ParkingSpots on this floor.
 * 2. Maintain real-time available-spot counts per SpotType (O(1) lookup).
 * 3. Notify its DisplayBoard whenever counts change.
 *
 * Design decision — why track counts here?
 * If we stored only a flat list of spots, every availability check would
 * require scanning all spots on the floor: O(N). By maintaining a
 * Map<SpotType, Integer> for available counts, we can answer
 * "how many COMPACT spots are free on Floor 2?" in O(1) at the cost
 * of keeping the map in sync on every status change.
 *
 * ParkingFloor is the single place responsible for changing spot status.
 * All other classes (strategy, ParkingLot) go through ParkingFloor methods
 * rather than calling spot.setStatus() directly, ensuring counts stay accurate.
 */
public class ParkingFloor {

    private int floorNumber;
    private Map<String, ParkingSpot> spots;             // spotId -> ParkingSpot
    private Map<SpotType, Integer> availableCountByType; // SpotType -> available count
    private DisplayBoard displayBoard;

    public ParkingFloor(int floorNumber, DisplayBoard displayBoard) {
        this.floorNumber = floorNumber;
        this.displayBoard = displayBoard;
        this.spots = new HashMap<String, ParkingSpot>();
        this.availableCountByType = new HashMap<SpotType, Integer>();

        // Initialize counts to 0 for every spot type
        for (SpotType type : SpotType.values()) {
            availableCountByType.put(type, 0);
        }
    }

    /**
     * Add a new spot to this floor.
     * Called by Admin.addSpot() -> ParkingLot.addSpot() during setup or at runtime.
     */
    public void addSpot(ParkingSpot spot) {
        spots.put(spot.getSpotId(), spot);

        // Only AVAILABLE spots contribute to the count
        if (spot.isAvailable()) {
            incrementCount(spot.getSpotType());
            syncDisplayBoard();
        }
    }

    /**
     * Mark a spot as UNAVAILABLE (maintenance / removal).
     * If it was previously AVAILABLE, decrement the count.
     * Called by Admin.removeSpot() -> ParkingLot.removeSpot().
     */
    public void removeSpot(String spotId) {
        ParkingSpot spot = spots.get(spotId);
        if (spot == null) {
            System.out.println("  [WARNING] Spot " + spotId + " not found on floor " + floorNumber);
            return;
        }
        if (spot.isAvailable()) {
            decrementCount(spot.getSpotType());
        }
        spot.setStatus(SpotStatus.UNAVAILABLE);
        syncDisplayBoard();
    }

    /**
     * Mark a spot as OCCUPIED when a vehicle is assigned to it.
     * Decrements the available count and notifies the display board.
     * Called by FirstAvailableStrategy after a compatible spot is found.
     */
    public void occupySpot(String spotId) {
        ParkingSpot spot = spots.get(spotId);
        if (spot == null) {
            System.out.println("  [WARNING] Spot " + spotId + " not found on floor " + floorNumber);
            return;
        }
        spot.setStatus(SpotStatus.OCCUPIED);
        decrementCount(spot.getSpotType());
        syncDisplayBoard();
    }

    /**
     * Mark a spot as AVAILABLE again when a vehicle exits.
     * Increments the available count and notifies the display board.
     * Called by ParkingLot.releaseSpot() during exit processing.
     */
    public void freeSpot(String spotId) {
        ParkingSpot spot = spots.get(spotId);
        if (spot == null) {
            System.out.println("  [WARNING] Spot " + spotId + " not found on floor " + floorNumber);
            return;
        }
        spot.setStatus(SpotStatus.AVAILABLE);
        incrementCount(spot.getSpotType());
        syncDisplayBoard();
    }

    /**
     * Returns an unmodifiable view of all spots on this floor.
     * Used by SpotAssignmentStrategy to scan for available spots.
     * Unmodifiable to prevent external classes from bypassing floor-level
     * count management by directly manipulating the map.
     */
    public Map<String, ParkingSpot> getSpots() {
        return Collections.unmodifiableMap(spots);
    }

    /**
     * Returns whether a given spot exists on this floor.
     * Used by ParkingLot.releaseSpot() to find the correct floor.
     */
    public boolean hasSpot(String spotId) {
        return spots.containsKey(spotId);
    }

    public int getAvailableCount(SpotType type) {
        Integer count = availableCountByType.get(type);
        return (count != null) ? count : 0;
    }

    /**
     * Sum of available spots across all types on this floor.
     * Used by ParkingLot.getTotalAvailableSpots().
     */
    public int getTotalAvailable() {
        int total = 0;
        for (int count : availableCountByType.values()) {
            total += count;
        }
        return total;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public DisplayBoard getDisplayBoard() {
        return displayBoard;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void incrementCount(SpotType type) {
        int current = availableCountByType.get(type);
        availableCountByType.put(type, current + 1);
    }

    private void decrementCount(SpotType type) {
        int current = availableCountByType.get(type);
        if (current > 0) {
            availableCountByType.put(type, current - 1);
        }
    }

    /**
     * Push current counts to the DisplayBoard.
     * Called after every spot status change on this floor.
     */
    private void syncDisplayBoard() {
        for (SpotType type : SpotType.values()) {
            displayBoard.updateSpotCount(type, availableCountByType.get(type));
        }
    }

    public void printFloorStatus() {
        System.out.println("  Floor " + floorNumber + ": "
                + getTotalAvailable() + " available spots | "
                + "ACCESSIBLE=" + getAvailableCount(SpotType.ACCESSIBLE)
                + " COMPACT=" + getAvailableCount(SpotType.COMPACT)
                + " LARGE=" + getAvailableCount(SpotType.LARGE)
                + " MOTORCYCLE=" + getAvailableCount(SpotType.MOTORCYCLE));
    }
}
