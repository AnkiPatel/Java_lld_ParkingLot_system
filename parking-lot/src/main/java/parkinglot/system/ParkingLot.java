package parkinglot.system;

import parkinglot.enums.SpotType;
import parkinglot.infrastructure.DisplayBoard;
import parkinglot.infrastructure.Entrance;
import parkinglot.infrastructure.Exit;
import parkinglot.infrastructure.ParkingFloor;
import parkinglot.models.ParkingSpot;
import parkinglot.models.RateCard;
import parkinglot.models.Ticket;
import parkinglot.models.Vehicle;
import parkinglot.strategy.SpotAssignmentStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The central orchestrator and single source of truth for the parking lot.
 *
 * Design pattern: Singleton
 *
 * Why Singleton?
 * There is exactly one parking lot in the system. If two parts of the code
 * held separate ParkingLot instances, they would have inconsistent views of
 * capacity, ticket counts, and spot availability. The Singleton pattern
 * guarantees that all Entrances, Exits, and Admin operations share the
 * exact same state.
 *
 * Why not static methods?
 * ParkingLot has rich state (floors, tickets, rate cards, counters).
 * Static methods cannot hold instance state cleanly, cannot implement
 * interfaces, and cannot be easily replaced with a mock in tests.
 * A Singleton object gives us all the benefits of a single shared instance
 * while still being a proper OOP object.
 *
 * Responsibilities:
 * 1. Manage all ParkingFloors, Entrances, Exits, and RateCards.
 * 2. Track total occupied vehicles against maxCapacity.
 * 3. Delegate spot assignment to SpotAssignmentStrategy.
 * 4. Issue and track Tickets.
 * 5. Release spots and update counts on exit.
 * 6. Delegate fee calculation to FeeCalculator.
 * 7. Notify all DisplayBoards when lot becomes full or has space again.
 * 8. Provide Admin operations: addSpot, removeSpot, updateRateCard.
 */
public class ParkingLot {

    // -------------------------------------------------------------------------
    // Singleton infrastructure
    // -------------------------------------------------------------------------

    private static ParkingLot instance;

    private ParkingLot(String name, int maxCapacity) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.totalOccupied = 0;
        this.ticketCounter = 0;
        this.floors = new ArrayList<ParkingFloor>();
        this.entrances = new ArrayList<Entrance>();
        this.exits = new ArrayList<Exit>();
        this.rateCards = new ArrayList<RateCard>();
        this.activeTickets = new HashMap<String, Ticket>();
        this.feeCalculator = new FeeCalculator();
    }

    /**
     * First-time initialization. Call this once during system startup.
     * Subsequent calls with any arguments return the existing instance.
     */
    public static ParkingLot getInstance(String name, int maxCapacity) {
        if (instance == null) {
            instance = new ParkingLot(name, maxCapacity);
        }
        return instance;
    }

    /**
     * Retrieve the existing instance. Throws if not yet initialized.
     * Use this everywhere except the initial setup call.
     */
    public static ParkingLot getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "ParkingLot has not been initialized. "
                    + "Call getInstance(name, maxCapacity) first.");
        }
        return instance;
    }

    /**
     * Reset the singleton — used for testing so each test starts fresh.
     * Not intended for production use.
     */
    public static void resetInstance() {
        instance = null;
    }

    // -------------------------------------------------------------------------
    // State fields
    // -------------------------------------------------------------------------

    private String name;
    private int maxCapacity;
    private int totalOccupied;
    private int ticketCounter;

    private List<ParkingFloor> floors;
    private List<Entrance> entrances;
    private List<Exit> exits;
    private List<RateCard> rateCards;
    private Map<String, Ticket> activeTickets;   // ticketId -> Ticket

    private SpotAssignmentStrategy assignmentStrategy;
    private FeeCalculator feeCalculator;

    // -------------------------------------------------------------------------
    // Setup / configuration methods (called during system initialization)
    // -------------------------------------------------------------------------

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    public void addEntrance(Entrance entrance) {
        entrances.add(entrance);
    }

    public void addExit(Exit exit) {
        exits.add(exit);
    }

    public void addRateCard(RateCard card) {
        rateCards.add(card);
    }

    public void setAssignmentStrategy(SpotAssignmentStrategy strategy) {
        this.assignmentStrategy = strategy;
    }

    // -------------------------------------------------------------------------
    // Core operations — called by Entrance and Exit during vehicle flow
    // -------------------------------------------------------------------------

    /**
     * Check if the lot has reached maximum capacity.
     * Called by Entrance before accepting a vehicle.
     */
    public boolean isFull() {
        return totalOccupied >= maxCapacity;
    }

    /**
     * Assign a compatible available spot to the incoming vehicle.
     * Delegates to the configured SpotAssignmentStrategy.
     * Increments the occupied count and triggers FULL notification if needed.
     *
     * @param vehicle the incoming vehicle
     * @return the assigned ParkingSpot
     */
    public ParkingSpot assignSpot(Vehicle vehicle) {
        if (assignmentStrategy == null) {
            throw new IllegalStateException(
                    "No SpotAssignmentStrategy configured. Call setAssignmentStrategy() first.");
        }

        ParkingSpot spot = assignmentStrategy.assignSpot(
                vehicle.getVehicleType(), vehicle.isAccessible(), floors);

        totalOccupied++;

        if (isFull()) {
            notifyAllDisplaysFull();
        }

        return spot;
    }

    /**
     * Create and register a ticket for a parked vehicle.
     *
     * @param vehicle the parked vehicle
     * @param spot    the spot the vehicle is parked in
     * @return the newly created Ticket
     */
    public Ticket issueTicket(Vehicle vehicle, ParkingSpot spot) {
        String ticketId = "TKT-" + (++ticketCounter);
        Ticket ticket = new Ticket(ticketId, vehicle, spot);
        activeTickets.put(ticketId, ticket);
        return ticket;
    }

    /**
     * Calculate the parking fee for a ticket.
     * Also sets the exitTime on the ticket (time of fee calculation = time of exit scan).
     *
     * @param ticket the ticket being settled
     * @return the total fee due
     */
    public double calculateFee(Ticket ticket) {
        ticket.setExitTime(System.currentTimeMillis());
        return feeCalculator.calculateFee(ticket, rateCards);
    }

    /**
     * Release the spot occupied by a ticket's vehicle.
     * Called by Exit after successful payment.
     * Decrements occupied count and clears FULL notification if needed.
     *
     * @param ticket the paid ticket whose spot should be freed
     */
    public void releaseSpot(Ticket ticket) {
        String spotId = ticket.getSpot().getSpotId();

        // Find which floor owns this spot and free it there
        ParkingFloor ownerFloor = getFloorBySpotId(spotId);
        if (ownerFloor != null) {
            boolean wasAtCapacity = isFull();
            ownerFloor.freeSpot(spotId);
            totalOccupied--;
            activeTickets.remove(ticket.getTicketId());

            // If lot was full and just freed up, clear the FULL message
            if (wasAtCapacity) {
                notifyAllDisplaysClearFull();
            }
        } else {
            System.out.println("  [WARNING] Could not find floor for spot: " + spotId);
        }
    }

    // -------------------------------------------------------------------------
    // Admin operations — called via Admin class
    // -------------------------------------------------------------------------

    /**
     * Add a new spot to a specific floor (Admin operation).
     */
    public void addSpot(int floorNumber, ParkingSpot spot) {
        ParkingFloor floor = getFloorByNumber(floorNumber);
        if (floor != null) {
            floor.addSpot(spot);
            System.out.println("  [ADMIN] Spot added: " + spot + " to Floor " + floorNumber);
        } else {
            System.out.println("  [ADMIN] Floor " + floorNumber + " not found.");
        }
    }

    /**
     * Mark a spot as unavailable (Admin operation — maintenance/removal).
     */
    public void removeSpot(int floorNumber, String spotId) {
        ParkingFloor floor = getFloorByNumber(floorNumber);
        if (floor != null) {
            floor.removeSpot(spotId);
            System.out.println("  [ADMIN] Spot " + spotId + " marked UNAVAILABLE on Floor "
                    + floorNumber);
        } else {
            System.out.println("  [ADMIN] Floor " + floorNumber + " not found.");
        }
    }

    /**
     * Replace an existing rate card with a new one (Admin operation).
     * The old card is removed by reference equality.
     */
    public void updateRateCard(RateCard oldCard, RateCard newCard) {
        rateCards.remove(oldCard);
        rateCards.add(newCard);
        System.out.println("  [ADMIN] Rate card updated. New card: " + newCard);
    }

    // -------------------------------------------------------------------------
    // Status / reporting methods
    // -------------------------------------------------------------------------

    /**
     * Print a summary of the lot's current state.
     */
    public void printStatus() {
        System.out.println("\n========================================");
        System.out.println("  Parking Lot: " + name);
        System.out.println("  Capacity   : " + totalOccupied + " / " + maxCapacity + " occupied");
        System.out.println("  Available  : " + getTotalAvailableSpots() + " spots");
        System.out.println("  Status     : " + (isFull() ? "FULL" : "OPEN"));
        System.out.println("  Floors     :");
        for (ParkingFloor floor : floors) {
            floor.printFloorStatus();
        }
        System.out.println("  Active Tickets: " + activeTickets.size());
        System.out.println("========================================\n");
    }

    /**
     * Total available spots across all floors and all spot types.
     */
    public int getTotalAvailableSpots() {
        int total = 0;
        for (ParkingFloor floor : floors) {
            total += floor.getTotalAvailable();
        }
        return total;
    }

    public int getTotalOccupied() {
        return totalOccupied;
    }

    public String getName() {
        return name;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public List<ParkingFloor> getFloors() {
        return floors;
    }

    public Map<String, Ticket> getActiveTickets() {
        return activeTickets;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Find a floor by its floor number. Returns null if not found.
     */
    private ParkingFloor getFloorByNumber(int floorNumber) {
        for (ParkingFloor floor : floors) {
            if (floor.getFloorNumber() == floorNumber) {
                return floor;
            }
        }
        return null;
    }

    /**
     * Find the floor that owns a given spot ID. Returns null if not found.
     */
    private ParkingFloor getFloorBySpotId(String spotId) {
        for (ParkingFloor floor : floors) {
            if (floor.hasSpot(spotId)) {
                return floor;
            }
        }
        return null;
    }

    /**
     * Notify all display boards (entrances + floors) that the lot is full.
     */
    private void notifyAllDisplaysFull() {
        for (Entrance entrance : entrances) {
            entrance.getDisplayBoard().showFull();
        }
        for (ParkingFloor floor : floors) {
            floor.getDisplayBoard().showFull();
        }
    }

    /**
     * Notify all display boards that the lot has available space again.
     */
    private void notifyAllDisplaysClearFull() {
        for (Entrance entrance : entrances) {
            entrance.getDisplayBoard().clearFull();
        }
        for (ParkingFloor floor : floors) {
            floor.getDisplayBoard().clearFull();
        }
    }
}
