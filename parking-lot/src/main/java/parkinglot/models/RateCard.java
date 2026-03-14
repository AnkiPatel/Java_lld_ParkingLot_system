package parkinglot.models;

import parkinglot.enums.SpotType;
import parkinglot.enums.VehicleType;

/**
 * Represents a pricing configuration entry.
 *
 * Design decisions:
 * 1. RateCard is a pure data object. FeeCalculator reads it; it has no logic.
 *    This satisfies SRP: one class holds pricing data, another computes fees.
 *
 * 2. vehicleType and spotType are nullable:
 *    - null vehicleType = rate applies to ALL vehicle types
 *    - null spotType    = rate applies to ALL spot types
 *    This allows a "catch-all" default rate card (both null) and specific
 *    overrides (e.g., CAR + COMPACT gets a different rate).
 *
 * 3. FeeCalculator searches rate cards in order and uses the first match.
 *    More specific cards should be added before the default catch-all card.
 *
 * 4. partialHourFactor is fixed at 0.75 per requirements (partial hour
 *    is billed at 75% of the hourly rate).
 *
 * Pricing tiers:
 *   - firstHourRate      : charged for the first full or partial hour
 *   - subsequentHourRate : charged for each additional full or partial hour
 */
public class RateCard {

    private VehicleType vehicleType;       // null = matches any vehicle type
    private SpotType spotType;             // null = matches any spot type
    private double firstHourRate;          // rate for the 1st hour
    private double subsequentHourRate;     // rate for each hour after the 1st
    private double partialHourFactor;      // fixed at 0.75 per business rule

    public RateCard(VehicleType vehicleType, SpotType spotType,
                    double firstHourRate, double subsequentHourRate) {
        this.vehicleType = vehicleType;
        this.spotType = spotType;
        this.firstHourRate = firstHourRate;
        this.subsequentHourRate = subsequentHourRate;
        this.partialHourFactor = 0.75; // Business rule: partial hour = 75% of hourly rate
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public double getFirstHourRate() {
        return firstHourRate;
    }

    public double getSubsequentHourRate() {
        return subsequentHourRate;
    }

    public double getPartialHourFactor() {
        return partialHourFactor;
    }

    /**
     * Returns true if this rate card applies to the given vehicle type and spot type.
     *
     * Matching logic:
     *   - If this card's vehicleType is null, it matches any vehicle type.
     *   - If this card's spotType is null, it matches any spot type.
     *   - Both conditions must hold simultaneously.
     */
    public boolean matches(VehicleType vt, SpotType st) {
        boolean vehicleMatch = (this.vehicleType == null) || (this.vehicleType == vt);
        boolean spotMatch = (this.spotType == null) || (this.spotType == st);
        return vehicleMatch && spotMatch;
    }

    @Override
    public String toString() {
        return "RateCard{"
                + "vehicleType=" + (vehicleType != null ? vehicleType : "ALL")
                + ", spotType=" + (spotType != null ? spotType : "ALL")
                + ", firstHour=$" + firstHourRate
                + ", subsequent=$" + subsequentHourRate
                + ", partialFactor=" + partialHourFactor
                + "}";
    }
}
