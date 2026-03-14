package parkinglot;

import parkinglot.enums.SpotType;
import parkinglot.enums.VehicleType;
import parkinglot.models.ParkingSpot;
import parkinglot.models.RateCard;
import parkinglot.models.Ticket;
import parkinglot.models.Vehicle;
import parkinglot.system.FeeCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java test class for FeeCalculator — no JUnit, no external dependencies.
 *
 * Each test method returns true (pass) or false (fail).
 * Main runs all tests and exits with code 0 (all pass) or 1 (any failure).
 *
 * mvn test  will compile and run this via maven-surefire-plugin.
 */
public class FeeCalculatorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  FeeCalculatorTest — Running all tests");
        System.out.println("==============================================");

        runTest("underOneHour_chargesPartialFirstHour",    FeeCalculatorTest::underOneHour_chargesPartialFirstHour);
        runTest("exactlyOneHour_chargesFirstHourOnly",     FeeCalculatorTest::exactlyOneHour_chargesFirstHourOnly);
        runTest("oneHourPlusPartial_chargesFirstAndPartial", FeeCalculatorTest::oneHourPlusPartial_chargesFirstAndPartial);
        runTest("twoFullHours_chargesFirstPlusSubsequent", FeeCalculatorTest::twoFullHours_chargesFirstPlusSubsequent);
        runTest("multipleHoursWithPartial_correctTotal",   FeeCalculatorTest::multipleHoursWithPartial_correctTotal);
        runTest("noMatchingRateCard_usesSystemDefault",    FeeCalculatorTest::noMatchingRateCard_usesSystemDefault);
        runTest("exactRateCardMatch_specificCardUsed",     FeeCalculatorTest::exactRateCardMatch_specificCardUsed);
        runTest("oneMinute_minimumCharge",                 FeeCalculatorTest::oneMinute_minimumCharge);

        System.out.println("==============================================");
        System.out.println("  RESULTS: " + passed + " passed, " + failed + " failed");
        System.out.println("==============================================");

        if (failed > 0) {
            System.exit(1); // Non-zero exit signals Maven test failure
        }
    }

    // -------------------------------------------------------------------------
    // Test runner helper
    // -------------------------------------------------------------------------

    interface TestCase {
        boolean run();
    }

    private static void runTest(String name, TestCase tc) {
        try {
            boolean result = tc.run();
            if (result) {
                System.out.println("  [PASS] " + name);
                passed++;
            } else {
                System.out.println("  [FAIL] " + name);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("  [FAIL] " + name + " — Exception: " + e.getMessage());
            failed++;
        }
    }

    private static void assertEquals(String label, double expected, double actual) {
        if (Math.abs(expected - actual) > 0.001) {
            throw new AssertionError(label + ": expected=" + expected + " actual=" + actual);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Ticket makeTicket(VehicleType vehicleType, SpotType spotType,
                                     long durationMinutes) {
        Vehicle vehicle   = new Vehicle("TEST-001", vehicleType, false);
        ParkingSpot spot  = new ParkingSpot("T-001", spotType, 1, "A");
        Ticket ticket     = new Ticket("TKT-TEST", vehicle, spot);
        long entryTime    = System.currentTimeMillis() - (durationMinutes * 60 * 1000);
        ticket.setEntryTime(entryTime);
        ticket.setExitTime(System.currentTimeMillis());
        return ticket;
    }

    private static List<RateCard> defaultRateCards() {
        List<RateCard> cards = new ArrayList<RateCard>();
        // CAR + COMPACT: $10 first, $8 subsequent
        cards.add(new RateCard(VehicleType.CAR, SpotType.COMPACT, 10.0, 8.0));
        // MOTORCYCLE: $3 first, $2 subsequent
        cards.add(new RateCard(VehicleType.MOTORCYCLE, SpotType.MOTORCYCLE, 3.0, 2.0));
        // Catch-all: $5 first, $3 subsequent
        cards.add(new RateCard(null, null, 5.0, 3.0));
        return cards;
    }

    // -------------------------------------------------------------------------
    // Test cases
    // -------------------------------------------------------------------------

    /**
     * 45 minutes → under 1 hour → firstHourRate * 0.75
     * CAR+COMPACT: $10 * 0.75 = $7.50
     */
    private static boolean underOneHour_chargesPartialFirstHour() {
        FeeCalculator calc = new FeeCalculator();
        Ticket ticket = makeTicket(VehicleType.CAR, SpotType.COMPACT, 45);
        double fee = calc.calculateFee(ticket, defaultRateCards());
        assertEquals("45-min fee", 7.50, fee);
        return true;
    }

    /**
     * 60 minutes → exactly 1 hour → firstHourRate only
     * CAR+COMPACT: $10.00
     */
    private static boolean exactlyOneHour_chargesFirstHourOnly() {
        FeeCalculator calc = new FeeCalculator();
        Ticket ticket = makeTicket(VehicleType.CAR, SpotType.COMPACT, 60);
        double fee = calc.calculateFee(ticket, defaultRateCards());
        assertEquals("60-min fee", 10.00, fee);
        return true;
    }

    /**
     * 90 minutes → 1 full hour + 30 min partial
     * CAR+COMPACT: $10 + $8 * 0.75 = $10 + $6 = $16.00
     */
    private static boolean oneHourPlusPartial_chargesFirstAndPartial() {
        FeeCalculator calc = new FeeCalculator();
        Ticket ticket = makeTicket(VehicleType.CAR, SpotType.COMPACT, 90);
        double fee = calc.calculateFee(ticket, defaultRateCards());
        assertEquals("90-min fee", 16.00, fee);
        return true;
    }

    /**
     * 120 minutes → exactly 2 full hours, no partial
     * CAR+COMPACT: $10 + (2-1)*$8 = $10 + $8 = $18.00
     */
    private static boolean twoFullHours_chargesFirstPlusSubsequent() {
        FeeCalculator calc = new FeeCalculator();
        Ticket ticket = makeTicket(VehicleType.CAR, SpotType.COMPACT, 120);
        double fee = calc.calculateFee(ticket, defaultRateCards());
        assertEquals("120-min fee", 18.00, fee);
        return true;
    }

    /**
     * 135 minutes (2h 15min) → 2 full hours + 15 min partial
     * CAR+COMPACT: $10 + $8 + $8*0.75 = $10 + $8 + $6 = $24.00
     */
    private static boolean multipleHoursWithPartial_correctTotal() {
        FeeCalculator calc = new FeeCalculator();
        Ticket ticket = makeTicket(VehicleType.CAR, SpotType.COMPACT, 135);
        double fee = calc.calculateFee(ticket, defaultRateCards());
        assertEquals("135-min fee", 24.00, fee);
        return true;
    }

    /**
     * VAN + LARGE with no specific rate card → falls back to default ($5/$3)
     * 90 minutes: $5 + $3*0.75 = $5 + $2.25 = $7.25
     */
    private static boolean noMatchingRateCard_usesSystemDefault() {
        FeeCalculator calc = new FeeCalculator();
        Ticket ticket = makeTicket(VehicleType.VAN, SpotType.LARGE, 90);
        // Only has CAR+COMPACT and MOTORCYCLE specific cards
        List<RateCard> cards = new ArrayList<RateCard>();
        cards.add(new RateCard(VehicleType.CAR, SpotType.COMPACT, 10.0, 8.0));
        cards.add(new RateCard(null, null, 5.0, 3.0)); // catch-all
        double fee = calc.calculateFee(ticket, cards);
        assertEquals("VAN/LARGE default rate 90-min", 7.25, fee);
        return true;
    }

    /**
     * MOTORCYCLE + MOTORCYCLE → specific card matches ($3/$2)
     * 45 minutes: under 1 hour → $3 * 0.75 = $2.25
     */
    private static boolean exactRateCardMatch_specificCardUsed() {
        FeeCalculator calc = new FeeCalculator();
        Ticket ticket = makeTicket(VehicleType.MOTORCYCLE, SpotType.MOTORCYCLE, 45);
        double fee = calc.calculateFee(ticket, defaultRateCards());
        assertEquals("MOTORCYCLE 45-min fee", 2.25, fee);
        return true;
    }

    /**
     * 1 minute → minimum charge → under 1 hour → firstHourRate * 0.75
     * CAR+COMPACT: $10 * 0.75 = $7.50
     */
    private static boolean oneMinute_minimumCharge() {
        FeeCalculator calc = new FeeCalculator();
        Ticket ticket = makeTicket(VehicleType.CAR, SpotType.COMPACT, 1);
        double fee = calc.calculateFee(ticket, defaultRateCards());
        assertEquals("1-min fee", 7.50, fee);
        return true;
    }
}
