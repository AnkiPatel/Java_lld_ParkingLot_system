package parkinglot.infrastructure;

import parkinglot.enums.SpotType;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a display board mounted at entrances and on each floor.
 *
 * Responsibilities:
 * 1. Show real-time count of available spots by SpotType.
 * 2. Show a "PARKING LOT FULL" message when at capacity.
 *
 * Design pattern: Lightweight Observer (recipient side)
 *
 * DisplayBoard is purely a display sink — it holds no business logic.
 * It is updated by ParkingFloor (on spot status change) and by
 * ParkingLot (on full/clear-full transitions). This decouples the
 * display concern from the business logic concern.
 *
 * In a production system this class would push updates to physical
 * LED boards or a web dashboard via an event. Here it prints to stdout.
 */
public class DisplayBoard {

    private String boardId;
    private Map<SpotType, Integer> availableSpots;
    private boolean isFull;

    public DisplayBoard(String boardId) {
        this.boardId = boardId;
        this.isFull = false;

        // Initialize counts to 0 for every spot type
        this.availableSpots = new HashMap<SpotType, Integer>();
        availableSpots.put(SpotType.ACCESSIBLE, 0);
        availableSpots.put(SpotType.COMPACT, 0);
        availableSpots.put(SpotType.LARGE, 0);
        availableSpots.put(SpotType.MOTORCYCLE, 0);
    }

    /**
     * Update the available count for a specific spot type.
     * Called by ParkingFloor whenever a spot on this floor changes status.
     */
    public void updateSpotCount(SpotType type, int count) {
        availableSpots.put(type, count);
    }

    /**
     * Display the FULL message. Called by ParkingLot when capacity is reached.
     */
    public void showFull() {
        this.isFull = true;
        System.out.println("  [DISPLAY:" + boardId + "] *** PARKING LOT FULL — No entry permitted ***");
    }

    /**
     * Clear the FULL message. Called by ParkingLot when a spot becomes free
     * after the lot was at full capacity.
     */
    public void clearFull() {
        this.isFull = false;
        System.out.println("  [DISPLAY:" + boardId + "] Parking lot space available again.");
    }

    /**
     * Print the current state of this display board to stdout.
     */
    public void display() {
        System.out.println("  +-----------------------------------------+");
        System.out.println("  | Display Board: " + boardId);
        if (isFull) {
            System.out.println("  | STATUS: *** FULL ***");
        } else {
            System.out.println("  | STATUS: AVAILABLE");
        }
        System.out.println("  | Available Spots:");
        System.out.println("  |   ACCESSIBLE  : " + availableSpots.get(SpotType.ACCESSIBLE));
        System.out.println("  |   COMPACT     : " + availableSpots.get(SpotType.COMPACT));
        System.out.println("  |   LARGE       : " + availableSpots.get(SpotType.LARGE));
        System.out.println("  |   MOTORCYCLE  : " + availableSpots.get(SpotType.MOTORCYCLE));
        System.out.println("  +-----------------------------------------+");
    }

    public String getBoardId() {
        return boardId;
}

    public boolean isFull() {
        return isFull;
    }

    public int getAvailableCount(SpotType type) {
        Integer count = availableSpots.get(type);
        return (count != null) ? count : 0;
    }
}
