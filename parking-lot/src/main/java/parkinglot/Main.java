package parkinglot;

import parkinglot.accounts.Admin;
import parkinglot.enums.PaymentMode;
import parkinglot.enums.SpotType;
import parkinglot.enums.VehicleType;
import parkinglot.infrastructure.DisplayBoard;
import parkinglot.infrastructure.Entrance;
import parkinglot.infrastructure.Exit;
import parkinglot.infrastructure.ParkingFloor;
import parkinglot.models.ParkingSpot;
import parkinglot.models.RateCard;
import parkinglot.models.Ticket;
import parkinglot.models.Vehicle;
import parkinglot.strategy.FirstAvailableStrategy;
import parkinglot.system.ParkingLot;

/**
 * Demo runner for the Parking Lot LLD system.
 *
 * Demonstrates the complete system flow:
 *
 * SETUP:
 *   - Admin initializes the lot with floors, spots, and rate cards
 *
 * SCENARIO 1 - Car parks and exits with card payment (1h 30m = $16)
 *   - CAR enters → spot assigned → ticket issued
 *   - 90 minutes simulated
 *   - Car exits → fee calculated → card payment → spot released
 *
 * SCENARIO 2 - Motorcycle parks and exits with cash payment (45m = $3.75)
 *   - MOTORCYCLE enters → motorcycle spot assigned
 *   - 45 minutes simulated
 *   - Exits with cash payment
 *
 * SCENARIO 3 - Truck parks (LARGE spot), demonstrating vehicle type routing
 *
 * SCENARIO 4 - Admin adds a spot at runtime
 *
 * SCENARIO 5 - Accessible vehicle gets ACCESSIBLE spot
 *
 * SCENARIO 6 - Lot full simulation (small lot with 2 spots)
 *
 * SCENARIO 7 - Invalid ticket rejection at exit
 */
public class Main {

    public static void main(String[] args) {

        printBanner("PARKING LOT LLD — SYSTEM DEMO");

        // =====================================================================
        // SETUP: Initialize the parking lot
        // =====================================================================

        printSection("SETUP: Initializing Parking Lot System");

        // Singleton creation — first call sets name and capacity
        ParkingLot lot = ParkingLot.getInstance("City Center Parking", 40000);

        // --- Floor 1 ---
        DisplayBoard floor1Board = new DisplayBoard("BOARD-F1");
        ParkingFloor floor1 = new ParkingFloor(1, floor1Board);

        floor1.addSpot(new ParkingSpot("F1-A-001", SpotType.COMPACT,    1, "A"));
        floor1.addSpot(new ParkingSpot("F1-A-002", SpotType.COMPACT,    1, "A"));
        floor1.addSpot(new ParkingSpot("F1-B-001", SpotType.LARGE,      1, "B"));
        floor1.addSpot(new ParkingSpot("F1-B-002", SpotType.LARGE,      1, "B"));
        floor1.addSpot(new ParkingSpot("F1-C-001", SpotType.MOTORCYCLE, 1, "C"));
        floor1.addSpot(new ParkingSpot("F1-D-001", SpotType.ACCESSIBLE, 1, "D"));

        // --- Floor 2 ---
        DisplayBoard floor2Board = new DisplayBoard("BOARD-F2");
        ParkingFloor floor2 = new ParkingFloor(2, floor2Board);

        floor2.addSpot(new ParkingSpot("F2-A-001", SpotType.COMPACT, 2, "A"));
        floor2.addSpot(new ParkingSpot("F2-A-002", SpotType.COMPACT, 2, "A"));
        floor2.addSpot(new ParkingSpot("F2-B-001", SpotType.LARGE,   2, "B"));

        lot.addFloor(floor1);
        lot.addFloor(floor2);

        // --- Assignment Strategy ---
        lot.setAssignmentStrategy(new FirstAvailableStrategy());

        // --- Entrance and Exit ---
        DisplayBoard entranceBoard = new DisplayBoard("BOARD-ENTRANCE-1");
        Entrance entrance1 = new Entrance("ENTRANCE-1", entranceBoard, lot);
        lot.addEntrance(entrance1);

        DisplayBoard exitBoard = new DisplayBoard("BOARD-EXIT-1");
        Exit exit1 = new Exit("EXIT-1", exitBoard, lot);
        lot.addExit(exit1);

        // --- Rate Cards (specific cards BEFORE catch-all) ---
        // CAR + COMPACT: $10 first hour, $8 subsequent
        RateCard carCompactRate = new RateCard(VehicleType.CAR, SpotType.COMPACT, 10.0, 8.0);
        // CAR + LARGE: $12 first hour, $10 subsequent
        RateCard carLargeRate   = new RateCard(VehicleType.CAR, SpotType.LARGE, 12.0, 10.0);
        // MOTORCYCLE: $3 first hour, $2 subsequent
        RateCard motoRate       = new RateCard(VehicleType.MOTORCYCLE, SpotType.MOTORCYCLE, 3.0, 2.0);
        // TRUCK: $15 first hour, $12 subsequent
        RateCard truckRate      = new RateCard(VehicleType.TRUCK, SpotType.LARGE, 15.0, 12.0);
        // Default catch-all: $5 first hour, $3 subsequent
        RateCard defaultRate    = new RateCard(null, null, 5.0, 3.0);

        lot.addRateCard(carCompactRate);
        lot.addRateCard(carLargeRate);
        lot.addRateCard(motoRate);
        lot.addRateCard(truckRate);
        lot.addRateCard(defaultRate);

        System.out.println("  Parking lot initialized successfully.");
        lot.printStatus();

        // =====================================================================
        // SCENARIO 1: Car parks for 90 minutes, pays by card
        // Expected fee: firstHour=$10 + partialHour=$8*0.75=$6 => TOTAL: $16.00
        // =====================================================================

        printSection("SCENARIO 1: Car parks 90 minutes — Card payment");

        Vehicle car1 = new Vehicle("KA-01-AB-1234", VehicleType.CAR, false);
        Ticket ticket1 = entrance1.acceptVehicle(car1);

        // Simulate 90 minutes of parking
        ticket1.setEntryTime(System.currentTimeMillis() - (90 * 60 * 1000));

        exit1.processExit(ticket1, PaymentMode.CARD, "4242");
        lot.printStatus();

        // =====================================================================
        // SCENARIO 2: Motorcycle parks for 45 minutes, pays by cash
        // Expected fee: < 1 hour => $3 * 0.75 = $2.25
        // =====================================================================

        printSection("SCENARIO 2: Motorcycle parks 45 minutes — Cash payment");

        Vehicle moto1 = new Vehicle("KA-02-CD-5678", VehicleType.MOTORCYCLE, false);
        Ticket ticket2 = entrance1.acceptVehicle(moto1);

        // Simulate 45 minutes of parking
        ticket2.setEntryTime(System.currentTimeMillis() - (45 * 60 * 1000));

        exit1.processExit(ticket2, PaymentMode.CASH, null);
        lot.printStatus();

        // =====================================================================
        // SCENARIO 3: Truck parks for 2 hours 15 min, pays by card
        // Expected fee: $15 + $12 + $12*0.75=$9 => TOTAL: $36.00
        // =====================================================================

        printSection("SCENARIO 3: Truck parks 2h 15min — Card payment");

        Vehicle truck1 = new Vehicle("MH-04-EF-9999", VehicleType.TRUCK, false);
        Ticket ticket3 = entrance1.acceptVehicle(truck1);

        // Simulate 2 hours 15 minutes
        ticket3.setEntryTime(System.currentTimeMillis() - (135 * 60 * 1000));

        exit1.processExit(ticket3, PaymentMode.CARD, "1234");
        lot.printStatus();

        // =====================================================================
        // SCENARIO 4: Admin adds a new spot at runtime
        // =====================================================================

        printSection("SCENARIO 4: Admin adds a new COMPACT spot at runtime");

        Admin admin = new Admin("ADM-001", "Ravi Kumar", "ravi@citycenter.com", "hashed_pwd_123");
        admin.viewAccount();
        admin.login("hashed_pwd_123");

        ParkingSpot newSpot = new ParkingSpot("F1-A-003", SpotType.COMPACT, 1, "A");
        admin.addSpot(lot, 1, newSpot);

        lot.printStatus();

        // =====================================================================
        // SCENARIO 5: Accessible vehicle gets ACCESSIBLE spot
        // =====================================================================

        printSection("SCENARIO 5: Accessible car — ACCESSIBLE spot assigned");

        Vehicle accessibleCar = new Vehicle("KA-03-GH-1111", VehicleType.CAR, true);
        Ticket ticket5 = entrance1.acceptVehicle(accessibleCar);

        System.out.println("  Spot type assigned: " + ticket5.getSpot().getSpotType());

        // Simulate 30 minutes — partial first hour
        // Expected fee: $10 * 0.75 = $7.50 (uses default rate since no
        // specific card for CAR+ACCESSIBLE — falls back to defaultRate $5*0.75=$3.75)
        ticket5.setEntryTime(System.currentTimeMillis() - (30 * 60 * 1000));
        exit1.processExit(ticket5, PaymentMode.CASH, null);

        // =====================================================================
        // SCENARIO 6: Lot FULL simulation (using a tiny 2-spot lot)
        // =====================================================================

        printSection("SCENARIO 6: Parking lot FULL simulation");

        // Reset singleton so we can create a tiny test lot
        ParkingLot.resetInstance();
        ParkingLot tinyLot = ParkingLot.getInstance("Tiny Test Lot", 2);

        DisplayBoard tinyBoard    = new DisplayBoard("BOARD-TINY");
        ParkingFloor tinyFloor    = new ParkingFloor(1, tinyBoard);
        tinyFloor.addSpot(new ParkingSpot("T-001", SpotType.COMPACT, 1, "A"));
        tinyFloor.addSpot(new ParkingSpot("T-002", SpotType.COMPACT, 1, "A"));
        tinyLot.addFloor(tinyFloor);
        tinyLot.setAssignmentStrategy(new FirstAvailableStrategy());

        DisplayBoard tinyEntBoard = new DisplayBoard("BOARD-TINY-ENTRANCE");
        Entrance tinyEntrance     = new Entrance("TINY-ENT", tinyEntBoard, tinyLot);
        tinyLot.addEntrance(tinyEntrance);
        tinyLot.addRateCard(new RateCard(null, null, 5.0, 3.0));

        // Park 2 vehicles to fill up
        Vehicle v1 = new Vehicle("KA-10-AA-0001", VehicleType.CAR, false);
        Vehicle v2 = new Vehicle("KA-10-AA-0002", VehicleType.CAR, false);
        tinyEntrance.acceptVehicle(v1);
        tinyEntrance.acceptVehicle(v2);

        tinyLot.printStatus();

        // Try to park a 3rd vehicle — should be rejected
        System.out.println("  Attempting to park a 3rd vehicle in a full lot...");
        try {
            Vehicle v3 = new Vehicle("KA-10-AA-0003", VehicleType.CAR, false);
            tinyEntrance.acceptVehicle(v3);
        } catch (parkinglot.exceptions.ParkingLotFullException e) {
            System.out.println("  [CAUGHT] ParkingLotFullException: " + e.getMessage());
        }

        // =====================================================================
        // SCENARIO 7: Invalid ticket rejected at exit
        // =====================================================================

        printSection("SCENARIO 7: Invalid (already PAID) ticket rejected at exit");

        // Re-initialize main lot for this scenario
        ParkingLot.resetInstance();
        ParkingLot lot7 = ParkingLot.getInstance("City Center Parking", 40000);

        DisplayBoard f1b7   = new DisplayBoard("BOARD-F1-S7");
        ParkingFloor floor7 = new ParkingFloor(1, f1b7);
        floor7.addSpot(new ParkingSpot("S7-001", SpotType.COMPACT, 1, "A"));
        lot7.addFloor(floor7);
        lot7.setAssignmentStrategy(new FirstAvailableStrategy());

        DisplayBoard entBoard7 = new DisplayBoard("BOARD-ENT-S7");
        Entrance ent7          = new Entrance("ENT-S7", entBoard7, lot7);
        lot7.addEntrance(ent7);

        DisplayBoard exitBoard7 = new DisplayBoard("BOARD-EXIT-S7");
        Exit exit7              = new Exit("EXIT-S7", exitBoard7, lot7);
        lot7.addExit(exit7);

        lot7.addRateCard(new RateCard(null, null, 5.0, 3.0));

        Vehicle v7     = new Vehicle("KA-99-ZZ-9999", VehicleType.CAR, false);
        Ticket ticket7 = ent7.acceptVehicle(v7);
        ticket7.setEntryTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 hour

        // First exit — valid
        exit7.processExit(ticket7, PaymentMode.CASH, null);

        // Second exit attempt with same ticket — should throw
        System.out.println("\n  Attempting second exit with the same ticket...");
        try {
            exit7.processExit(ticket7, PaymentMode.CASH, null);
        } catch (parkinglot.exceptions.InvalidTicketException e) {
            System.out.println("  [CAUGHT] InvalidTicketException: " + e.getMessage());
        }

        printBanner("ALL SCENARIOS COMPLETE");
    }

    // -------------------------------------------------------------------------
    // Formatting helpers
    // -------------------------------------------------------------------------

    private static void printBanner(String title) {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║  " + padRight(title, 52) + "║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    private static void printSection(String title) {
        System.out.println("\n--- " + title + " ---");
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
