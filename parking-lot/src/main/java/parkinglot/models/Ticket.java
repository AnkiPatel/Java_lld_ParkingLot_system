package parkinglot.models;

import parkinglot.enums.TicketStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents the parking ticket issued to a customer at entry.
 *
 * Design decisions:
 * 1. Ticket is the central transactional object. It links a Vehicle
 *    to a ParkingSpot and records the time window of the parking session.
 * 2. entryTime is captured at construction time (immutable after creation).
 *    exitTime is set externally by ParkingLot.calculateFee() when the
 *    customer scans the ticket at exit.
 * 3. amountDue is computed by FeeCalculator and stored here so the
 *    Exit panel can display and collect the exact amount.
 * 4. Ticket does NOT calculate the fee itself. That responsibility
 *    belongs to FeeCalculator (Single Responsibility Principle).
 */
public class Ticket {

    private String ticketId;
    private Vehicle vehicle;
    private ParkingSpot spot;
    private long entryTime;   // milliseconds since epoch
    private long exitTime;    // set when ticket is scanned at exit
    private TicketStatus status;
    private double amountDue;

    public Ticket(String ticketId, Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
        this.exitTime = 0;
        this.amountDue = 0.0;
        this.status = TicketStatus.ACTIVE;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public long getEntryTime() {
        return entryTime;
    }

    public long getExitTime() {
        return exitTime;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setExitTime(long exitTime) {
        this.exitTime = exitTime;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    /**
     * Package-private setter used by FeeCalculator tests to simulate
     * a specific entry time (e.g., 90 minutes in the past).
     */
    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "Ticket{"
                + "id='" + ticketId + "'"
                + ", vehicle=" + vehicle.getLicensePlate()
                + ", spot=" + spot.getSpotId()
                + ", entryTime=" + sdf.format(new Date(entryTime))
                + ", status=" + status
                + "}";
    }
}
