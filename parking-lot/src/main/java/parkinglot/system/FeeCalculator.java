package parkinglot.system;

import parkinglot.models.RateCard;
import parkinglot.models.Ticket;

import java.util.List;

/**
 * Calculates the parking fee for a completed parking session.
 *
 * Design decisions:
 * 1. FeeCalculator is stateless — it holds no fields.
 *    It is a pure computation class: given a Ticket and a list of RatCards,
 *    it returns a double. This makes it trivially testable in isolation.
 *
 * 2. FeeCalculator is separate from Ticket (SRP).
 *    Ticket is a data container. Fee calculation is a business rule that can
 *    change independently (e.g., new pricing tiers, peak-hour surcharges).
 *    If they were combined, every pricing change would touch the Ticket class.
 *
 * Pricing Algorithm:
 * ------------------------------------------------------------------
 * Duration         | Charge
 * ------------------------------------------------------------------
 * < 1 hour         | firstHourRate * partialHourFactor (0.75)
 * Exactly 1 hour   | firstHourRate
 * 1h + partial     | firstHourRate + subsequentHourRate * 0.75
 * N full hours     | firstHourRate + (N-1) * subsequentHourRate
 * N hours+partial  | firstHourRate + (N-1) * subsequentHourRate
 *                  |   + subsequentHourRate * 0.75
 * ------------------------------------------------------------------
 *
 * Rate card selection:
 * The most specific matching RateCard is used (first match wins).
 * Cards should be added to ParkingLot in specificity order: specific first,
 * catch-all (null/null) last. If no card matches, a hardcoded fallback is used.
 */
public class FeeCalculator {

    // Fallback rates used when no RateCard matches (should rarely happen
    // if the admin has configured a default null/null catch-all card)
    private static final double DEFAULT_FIRST_HOUR_RATE = 5.0;
    private static final double DEFAULT_SUBSEQUENT_RATE = 3.0;
    private static final double DEFAULT_PARTIAL_FACTOR  = 0.75;

    /**
     * Calculate and return the total parking fee.
     *
     * @param ticket    the ticket with entryTime and exitTime set
     * @param rateCards the configured list of rate cards (most specific first)
     * @return          total fee rounded to 2 decimal places
     */
    public double calculateFee(Ticket ticket, List<RateCard> rateCards) {

        // Step 1: Find the applicable rate card
        RateCard applicableCard = findMatchingRateCard(ticket, rateCards);

        double firstHourRate      = applicableCard.getFirstHourRate();
        double subsequentHourRate = applicableCard.getSubsequentHourRate();
        double partialHourFactor  = applicableCard.getPartialHourFactor();

        // Step 2: Calculate duration
        long durationMillis = ticket.getExitTime() - ticket.getEntryTime();

        // Ensure at least 1 minute is charged (prevents zero-fee edge case)
        long totalMinutes = durationMillis / 60000;
        if (totalMinutes < 1) {
            totalMinutes = 1;
        }

        int fullHours        = (int) (totalMinutes / 60);
        int remainingMinutes = (int) (totalMinutes % 60);

        System.out.println("  [FEE] Duration: " + fullHours + "h " + remainingMinutes + "m"
                + " | Rate card: " + applicableCard);

        // Step 3: Calculate fee based on duration tiers
        double fee = 0.0;

        if (fullHours == 0) {
            // Under one full hour — partial first-hour charge
            fee = firstHourRate * partialHourFactor;

        } else if (fullHours == 1) {
            // Exactly one full hour, plus optional partial second hour
            fee = firstHourRate;
            if (remainingMinutes > 0) {
                fee += subsequentHourRate * partialHourFactor;
            }

        } else {
            // Multiple full hours, plus optional partial final hour
            fee = firstHourRate + (double)(fullHours - 1) * subsequentHourRate;
            if (remainingMinutes > 0) {
                fee += subsequentHourRate * partialHourFactor;
            }
        }

        // Step 4: Round to 2 decimal places
        fee = Math.round(fee * 100.0) / 100.0;

        return fee;
    }

    /**
     * Find the first RateCard that matches the vehicle type and spot type
     * of the given ticket. Returns a fallback default card if none match.
     *
     * Why first-match?
     * Admins are instructed to add specific cards before the catch-all (null/null)
     * card. This gives specific rates priority over general rates.
     */
    private RateCard findMatchingRateCard(Ticket ticket, List<RateCard> rateCards) {
        for (RateCard card : rateCards) {
            if (card.matches(ticket.getVehicle().getVehicleType(),
                             ticket.getSpot().getSpotType())) {
                return card;
            }
        }

        // No match found — return a hardcoded default so the system never
        // fails to produce a fee, and log a warning for the admin to investigate.
        System.out.println("  [WARNING] No matching rate card found for "
                + ticket.getVehicle().getVehicleType()
                + " / " + ticket.getSpot().getSpotType()
                + ". Using system default rates.");

        return new RateCard(null, null, DEFAULT_FIRST_HOUR_RATE,
                DEFAULT_SUBSEQUENT_RATE);
    }
}
