package parkinglot.infrastructure;

import parkinglot.enums.PaymentMode;
import parkinglot.enums.TicketStatus;
import parkinglot.exceptions.InvalidTicketException;
import parkinglot.models.Ticket;
import parkinglot.payment.PaymentFactory;
import parkinglot.payment.PaymentStrategy;
import parkinglot.system.ParkingLot;

/**
 * Represents a physical exit point from the parking lot.
 *
 * Responsibilities:
 * 1. Validate the ticket presented by the customer.
 * 2. Trigger fee calculation via ParkingLot.
 * 3. Collect payment via the appropriate PaymentStrategy.
 * 4. Release the parking spot via ParkingLot.
 * 5. Mark the ticket as PAID and allow the vehicle to exit.
 *
 * Design decisions:
 * - Exit does NOT calculate the fee. It delegates to ParkingLot.calculateFee(),
 *   which internally uses FeeCalculator. This keeps Exit focused on the
 *   exit flow, not pricing logic.
 * - Exit does NOT instantiate payment objects directly. It uses PaymentFactory,
 *   which decouples Exit from concrete payment classes (Factory Pattern).
 * - If payment fails, the spot is NOT released and the ticket stays ACTIVE.
 *   The customer must retry payment.
 */
public class Exit {

    private String exitId;
    private DisplayBoard displayBoard;
    private ParkingLot parkingLot;

    public Exit(String exitId, DisplayBoard displayBoard, ParkingLot parkingLot) {
        this.exitId = exitId;
        this.displayBoard = displayBoard;
        this.parkingLot = parkingLot;
    }

    /**
     * Process the exit of a vehicle.
     *
     * Flow:
     * 1. Validate ticket is ACTIVE.
     * 2. Calculate fee (also sets exitTime on ticket).
     * 3. Display fee to customer.
     * 4. Create payment strategy via factory.
     * 5. Process payment.
     * 6. If payment succeeds: release spot, mark ticket PAID, print receipt.
     * 7. If payment fails: print failure message; spot remains occupied.
     *
     * @param ticket       the ticket scanned by the customer at exit
     * @param paymentMode  CASH or CARD
     * @param cardLast4    last 4 digits of card (ignored for CASH, pass null)
     * @throws InvalidTicketException if the ticket is not in ACTIVE status
     */
    public void processExit(Ticket ticket, PaymentMode paymentMode, String cardLast4) {
        System.out.println("\n[EXIT:" + exitId + "] Processing exit for ticket: "
                + ticket.getTicketId() + " | Vehicle: " + ticket.getVehicle());

        // Step 1: Validate ticket
        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new InvalidTicketException(
                    "Ticket " + ticket.getTicketId() + " is not active. Status: "
                            + ticket.getStatus());
        }

        // Step 2: Calculate fee (ParkingLot also sets exitTime on ticket here)
        double fee = parkingLot.calculateFee(ticket);
        ticket.setAmountDue(fee);
        System.out.println("  [EXIT:" + exitId + "] Parking fee calculated: $"
                + String.format("%.2f", fee));

        // Step 3: Create payment strategy via factory and process payment
        PaymentStrategy payment = PaymentFactory.createPayment(paymentMode, cardLast4);
        boolean paymentSuccess = payment.processPayment(ticket.getAmountDue());

        // Step 4: Handle payment result
        if (paymentSuccess) {
            parkingLot.releaseSpot(ticket);
            ticket.setStatus(TicketStatus.PAID);
            printReceipt(ticket, payment);
        } else {
            System.out.println("  [EXIT:" + exitId + "] Payment FAILED for ticket: "
                    + ticket.getTicketId() + ". Please retry.");
        }
    }

    private void printReceipt(Ticket ticket, PaymentStrategy payment) {
        System.out.println("  ========================================");
        System.out.println("  PARKING RECEIPT");
        System.out.println("  ========================================");
        System.out.println("  Ticket ID   : " + ticket.getTicketId());
        System.out.println("  Vehicle     : " + ticket.getVehicle());
        System.out.println("  Spot        : " + ticket.getSpot().getSpotId());
        System.out.println("  Amount Paid : $" + String.format("%.2f", ticket.getAmountDue()));
        System.out.println("  Payment     : " + payment.getPaymentMode());
        System.out.println("  Status      : " + ticket.getStatus());
        System.out.println("  ========================================");
        System.out.println("  [EXIT:" + exitId + "] Vehicle " + ticket.getVehicle().getLicensePlate()
                + " has exited. Thank you!");
    }

    public String getExitId() {
        return exitId;
    }

    public DisplayBoard getDisplayBoard() {
        return displayBoard;
    }
}
