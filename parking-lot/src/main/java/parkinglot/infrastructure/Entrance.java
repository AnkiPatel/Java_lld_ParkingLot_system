package parkinglot.infrastructure;

import parkinglot.exceptions.ParkingLotFullException;
import parkinglot.models.ParkingSpot;
import parkinglot.models.Ticket;
import parkinglot.models.Vehicle;
import parkinglot.system.ParkingLot;

/**
 * Represents a physical entry point into the parking lot.
 *
 * Responsibilities:
 * 1. Check if the lot has capacity before allowing a vehicle in.
 * 2. Trigger spot assignment via ParkingLot (which delegates to strategy).
 * 3. Request a ticket from ParkingLot and return it to the customer.
 * 4. Show the current display board state at this entrance.
 *
 * Design decisions:
 * - Entrance does NOT decide which spot to assign. It delegates to
 *   ParkingLot.assignSpot(), which in turn uses SpotAssignmentStrategy.
 *   This respects SRP: Entrance handles the entry flow; strategy handles
 *   spot selection logic.
 * - Entrance holds a reference to ParkingLot (not to individual floors),
 *   keeping the entry flow simple and hiding floor-level complexity.
 * - Each Entrance has its own DisplayBoard showing current availability
 *   to drivers approaching from this entry point.
 */
public class Entrance {

    private String entranceId;
    private DisplayBoard displayBoard;
    private ParkingLot parkingLot;

    public Entrance(String entranceId, DisplayBoard displayBoard, ParkingLot parkingLot) {
        this.entranceId = entranceId;
        this.displayBoard = displayBoard;
        this.parkingLot = parkingLot;
    }

    /**
     * Process an incoming vehicle.
     *
     * Flow:
     * 1. Check lot capacity — reject if full.
     * 2. Assign a compatible spot via ParkingLot.
     * 3. Issue a ticket via ParkingLot.
     * 4. Print ticket details for the customer.
     * 5. Return the ticket (customer keeps it until exit).
     *
     * @param vehicle the incoming vehicle
     * @return the parking ticket issued to the customer
     * @throws ParkingLotFullException if the lot is at maximum capacity
     */
    public Ticket acceptVehicle(Vehicle vehicle) {
        System.out.println("\n[ENTRANCE:" + entranceId + "] Vehicle arriving: " + vehicle);

        if (parkingLot.isFull()) {
            displayBoard.showFull();
            throw new ParkingLotFullException(
                    "Parking lot is full. Cannot accept vehicle: " + vehicle.getLicensePlate());
        }

        // Step 1: Assign a spot (delegates to SpotAssignmentStrategy internally)
        ParkingSpot assignedSpot = parkingLot.assignSpot(vehicle);
        System.out.println("  [ENTRANCE:" + entranceId + "] Spot assigned: " + assignedSpot);

        // Step 2: Issue the ticket
        Ticket ticket = parkingLot.issueTicket(vehicle, assignedSpot);
        System.out.println("  [ENTRANCE:" + entranceId + "] Ticket issued: " + ticket);

        return ticket;
    }

    public String getEntranceId() {
        return entranceId;
    }

    public DisplayBoard getDisplayBoard() {
        return displayBoard;
    }
}
