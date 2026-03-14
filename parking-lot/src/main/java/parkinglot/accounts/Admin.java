package parkinglot.accounts;

import parkinglot.models.ParkingSpot;
import parkinglot.models.RateCard;
import parkinglot.system.ParkingLot;

/**
 * Represents an Admin account with full management privileges.
 *
 * Design decision: Admin methods delegate to ParkingLot.
 * Admin does NOT directly manipulate floors, spots, or rate card lists.
 * All mutations go through ParkingLot, which is the single source of truth.
 * This means if ParkingLot adds validation, logging, or notifications to
 * these operations, Admin automatically benefits without any code change.
 *
 * Why delegate rather than embed logic here?
 * Imagine a future AgentAccount that also needs to add spots. If the logic
 * lived in Admin, we'd duplicate it in Agent. By placing it in ParkingLot,
 * all actors share the same implementation.
 */
public class Admin extends Account {

    public Admin(String accountId, String name, String email, String passwordHash) {
        super(accountId, name, email, passwordHash);
    }

    @Override
    public void viewAccount() {
        System.out.println("  +----------------------------------+");
        System.out.println("  | ADMIN ACCOUNT                    |");
        System.out.println("  | ID     : " + getAccountId());
        System.out.println("  | Name   : " + getName());
        System.out.println("  | Email  : " + getEmail());
        System.out.println("  | Status : " + getStatus());
        System.out.println("  | Role   : System Administrator    |");
        System.out.println("  +----------------------------------+");
    }

    /**
     * Add a new parking spot to a floor.
     *
     * @param lot         the parking lot system
     * @param floorNumber the floor to add the spot to
     * @param spot        the new spot to add
     */
    public void addSpot(ParkingLot lot, int floorNumber, ParkingSpot spot) {
        System.out.println("\n[ADMIN:" + getName() + "] Adding spot to Floor " + floorNumber);
        lot.addSpot(floorNumber, spot);
    }

    /**
     * Mark a spot as unavailable (e.g., for maintenance or permanent removal).
     *
     * @param lot         the parking lot system
     * @param floorNumber the floor the spot is on
     * @param spotId      the ID of the spot to remove
     */
    public void removeSpot(ParkingLot lot, int floorNumber, String spotId) {
        System.out.println("\n[ADMIN:" + getName() + "] Removing spot " + spotId
                + " from Floor " + floorNumber);
        lot.removeSpot(floorNumber, spotId);
    }

    /**
     * Add a new rate card to the pricing configuration.
     *
     * @param lot  the parking lot system
     * @param card the rate card to add
     */
    public void addRateCard(ParkingLot lot, RateCard card) {
        System.out.println("\n[ADMIN:" + getName() + "] Adding rate card: " + card);
        lot.addRateCard(card);
    }

    /**
     * Replace an existing rate card with an updated one.
     *
     * @param lot     the parking lot system
     * @param oldCard the rate card to replace (matched by reference)
     * @param newCard the new rate card to use
     */
    public void updateRateCard(ParkingLot lot, RateCard oldCard, RateCard newCard) {
        System.out.println("\n[ADMIN:" + getName() + "] Updating rate card.");
        lot.updateRateCard(oldCard, newCard);
    }
}
