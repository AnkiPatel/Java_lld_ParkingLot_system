package parkinglot;

import parkinglot.enums.SpotType;
import parkinglot.enums.VehicleType;
import parkinglot.exceptions.NoSpotAvailableException;
import parkinglot.infrastructure.DisplayBoard;
import parkinglot.infrastructure.ParkingFloor;
import parkinglot.models.ParkingSpot;
import parkinglot.models.Vehicle;
import parkinglot.strategy.FirstAvailableStrategy;
import parkinglot.strategy.SpotAssignmentStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java tests for FirstAvailableStrategy (spot assignment logic).
 * No JUnit — exits with 0 (pass) or 1 (fail) so mvn test works correctly.
 */
public class SpotAssignmentStrategyTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  SpotAssignmentStrategyTest — Running all tests");
        System.out.println("==============================================");

        runTest("car_assignedToCompactSpot",            SpotAssignmentStrategyTest::car_assignedToCompactSpot);
        runTest("truck_assignedToLargeSpot",            SpotAssignmentStrategyTest::truck_assignedToLargeSpot);
        runTest("van_assignedToLargeSpot",              SpotAssignmentStrategyTest::van_assignedToLargeSpot);
        runTest("motorcycle_assignedToMotorcycleSpot",  SpotAssignmentStrategyTest::motorcycle_assignedToMotorcycleSpot);
        runTest("accessibleCar_assignedToAccessible",   SpotAssignmentStrategyTest::accessibleCar_assignedToAccessible);
        runTest("noCompatibleSpot_throwsException",     SpotAssignmentStrategyTest::noCompatibleSpot_throwsException);
        runTest("spotMarkedOccupied_afterAssignment",   SpotAssignmentStrategyTest::spotMarkedOccupied_afterAssignment);
        runTest("multipleFloors_assignsFirstAvailable", SpotAssignmentStrategyTest::multipleFloors_assignsFirstAvailable);

        System.out.println("==============================================");
        System.out.println("  RESULTS: " + passed + " passed, " + failed + " failed");
        System.out.println("==============================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    // -------------------------------------------------------------------------
    // Test runner
    // -------------------------------------------------------------------------

    interface TestCase { boolean run(); }

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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static List<ParkingFloor> makeFloors(ParkingSpot... spots) {
        DisplayBoard board = new DisplayBoard("TEST-BOARD");
        ParkingFloor floor = new ParkingFloor(1, board);
        for (ParkingSpot spot : spots) {
            floor.addSpot(spot);
        }
        List<ParkingFloor> floors = new ArrayList<ParkingFloor>();
        floors.add(floor);
        return floors;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /** CAR (non-accessible) should be assigned to COMPACT spot */
    private static boolean car_assignedToCompactSpot() {
        SpotAssignmentStrategy strategy = new FirstAvailableStrategy();
        List<ParkingFloor> floors = makeFloors(
            new ParkingSpot("C-001", SpotType.COMPACT, 1, "A"),
            new ParkingSpot("L-001", SpotType.LARGE,   1, "B")
        );
        ParkingSpot assigned = strategy.assignSpot(VehicleType.CAR, false, floors);
        return assigned.getSpotType() == SpotType.COMPACT;
    }

    /** TRUCK should only be assigned to LARGE spot */
    private static boolean truck_assignedToLargeSpot() {
        SpotAssignmentStrategy strategy = new FirstAvailableStrategy();
        List<ParkingFloor> floors = makeFloors(
            new ParkingSpot("C-001", SpotType.COMPACT, 1, "A"),
            new ParkingSpot("L-001", SpotType.LARGE,   1, "B")
        );
        ParkingSpot assigned = strategy.assignSpot(VehicleType.TRUCK, false, floors);
        return assigned.getSpotType() == SpotType.LARGE;
    }

    /** VAN should only be assigned to LARGE spot */
    private static boolean van_assignedToLargeSpot() {
        SpotAssignmentStrategy strategy = new FirstAvailableStrategy();
        List<ParkingFloor> floors = makeFloors(
            new ParkingSpot("C-001", SpotType.COMPACT,    1, "A"),
            new ParkingSpot("L-001", SpotType.LARGE,      1, "B"),
            new ParkingSpot("M-001", SpotType.MOTORCYCLE, 1, "C")
        );
        ParkingSpot assigned = strategy.assignSpot(VehicleType.VAN, false, floors);
        return assigned.getSpotType() == SpotType.LARGE;
    }

    /** MOTORCYCLE should only be assigned to MOTORCYCLE spot */
    private static boolean motorcycle_assignedToMotorcycleSpot() {
        SpotAssignmentStrategy strategy = new FirstAvailableStrategy();
        List<ParkingFloor> floors = makeFloors(
            new ParkingSpot("C-001", SpotType.COMPACT,    1, "A"),
            new ParkingSpot("M-001", SpotType.MOTORCYCLE, 1, "B")
        );
        ParkingSpot assigned = strategy.assignSpot(VehicleType.MOTORCYCLE, false, floors);
        return assigned.getSpotType() == SpotType.MOTORCYCLE;
    }

    /** Accessible CAR should be assigned to ACCESSIBLE spot */
    private static boolean accessibleCar_assignedToAccessible() {
        SpotAssignmentStrategy strategy = new FirstAvailableStrategy();
        List<ParkingFloor> floors = makeFloors(
            new ParkingSpot("C-001", SpotType.COMPACT,    1, "A"),
            new ParkingSpot("A-001", SpotType.ACCESSIBLE, 1, "D")
        );
        ParkingSpot assigned = strategy.assignSpot(VehicleType.CAR, true, floors);
        return assigned.getSpotType() == SpotType.ACCESSIBLE;
    }

    /** No compatible spot → NoSpotAvailableException must be thrown */
    private static boolean noCompatibleSpot_throwsException() {
        SpotAssignmentStrategy strategy = new FirstAvailableStrategy();
        // Only COMPACT spots — TRUCK needs LARGE
        List<ParkingFloor> floors = makeFloors(
            new ParkingSpot("C-001", SpotType.COMPACT, 1, "A")
        );
        try {
            strategy.assignSpot(VehicleType.TRUCK, false, floors);
            return false; // Should have thrown
        } catch (NoSpotAvailableException e) {
            return true;  // Correct exception thrown
        }
    }

    /** After assignment, spot status must be OCCUPIED */
    private static boolean spotMarkedOccupied_afterAssignment() {
        SpotAssignmentStrategy strategy = new FirstAvailableStrategy();
        List<ParkingFloor> floors = makeFloors(
            new ParkingSpot("C-001", SpotType.COMPACT, 1, "A")
        );
        ParkingSpot assigned = strategy.assignSpot(VehicleType.CAR, false, floors);
        return !assigned.isAvailable(); // Must be OCCUPIED now
    }

    /** With two floors, first available spot on Floor 1 should be returned first */
    private static boolean multipleFloors_assignsFirstAvailable() {
        SpotAssignmentStrategy strategy = new FirstAvailableStrategy();

        DisplayBoard b1 = new DisplayBoard("BOARD-1");
        ParkingFloor floor1 = new ParkingFloor(1, b1);
        floor1.addSpot(new ParkingSpot("F1-C-001", SpotType.COMPACT, 1, "A"));

        DisplayBoard b2 = new DisplayBoard("BOARD-2");
        ParkingFloor floor2 = new ParkingFloor(2, b2);
        floor2.addSpot(new ParkingSpot("F2-C-001", SpotType.COMPACT, 2, "A"));

        List<ParkingFloor> floors = new ArrayList<ParkingFloor>();
        floors.add(floor1);
        floors.add(floor2);

        ParkingSpot assigned = strategy.assignSpot(VehicleType.CAR, false, floors);
        // Should get Floor 1 spot first
        return assigned.getSpotId().equals("F1-C-001");
    }
}
