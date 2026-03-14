# Parking Lot LLD — Code Generation Specification

> **Purpose**: This file is a precise, unambiguous spec for generating the complete Java implementation of the Parking Lot LLD. Every class, field, method, and relationship is defined here. Use this file to regenerate identical code at any time.

---

## Meta Constraints

- Language: **Java** (no Spring, no Hibernate, no Jakarta EE, no Lombok)
- Advanced Java features to AVOID: Generics (beyond simple List/Map), Streams, Lambdas, Reflection, Annotations, Functional interfaces
- Each class in its own `.java` file
- Package: `parkinglot`
- OOP portable: code must be logically translatable to C++, Python, C#

---

## Project Structure

```
parkinglot/
├── enums/
│   ├── SpotType.java
│   ├── VehicleType.java
│   ├── SpotStatus.java
│   ├── TicketStatus.java
│   ├── PaymentMode.java
│   └── AccountStatus.java
├── models/
│   ├── Vehicle.java
│   ├── ParkingSpot.java
│   ├── Ticket.java
│   └── RateCard.java
├── accounts/
│   ├── Account.java          ← abstract
│   └── Admin.java
├── payment/
│   ├── PaymentStrategy.java  ← interface
│   ├── CashPayment.java
│   ├── CardPayment.java
│   └── PaymentFactory.java
├── strategy/
│   ├── SpotAssignmentStrategy.java  ← interface
│   └── FirstAvailableStrategy.java
├── infrastructure/
│   ├── DisplayBoard.java
│   ├── ParkingFloor.java
│   ├── Entrance.java
│   └── Exit.java
├── system/
│   ├── FeeCalculator.java
│   └── ParkingLot.java       ← Singleton
├── exceptions/
│   ├── NoSpotAvailableException.java
│   ├── InvalidTicketException.java
│   └── ParkingLotFullException.java
└── Main.java                 ← demo runner
```

---

## 1. Enums

### `SpotType.java`
```java
package parkinglot.enums;
public enum SpotType {
    ACCESSIBLE, COMPACT, LARGE, MOTORCYCLE
}
```

### `VehicleType.java`
```java
package parkinglot.enums;
public enum VehicleType {
    CAR, TRUCK, VAN, MOTORCYCLE
}
```

### `SpotStatus.java`
```java
package parkinglot.enums;
public enum SpotStatus {
    AVAILABLE, OCCUPIED, UNAVAILABLE
}
```

### `TicketStatus.java`
```java
package parkinglot.enums;
public enum TicketStatus {
    ACTIVE, PAID, CANCELLED
}
```

### `PaymentMode.java`
```java
package parkinglot.enums;
public enum PaymentMode {
    CASH, CARD
}
```

### `AccountStatus.java`
```java
package parkinglot.enums;
public enum AccountStatus {
    ACTIVE, INACTIVE, BLOCKED
}
```

---

## 2. Exceptions

### `NoSpotAvailableException.java`
```java
package parkinglot.exceptions;
public class NoSpotAvailableException extends RuntimeException {
    public NoSpotAvailableException(String message) { super(message); }
}
```

### `InvalidTicketException.java`
```java
package parkinglot.exceptions;
public class InvalidTicketException extends RuntimeException {
    public InvalidTicketException(String message) { super(message); }
}
```

### `ParkingLotFullException.java`
```java
package parkinglot.exceptions;
public class ParkingLotFullException extends RuntimeException {
    public ParkingLotFullException(String message) { super(message); }
}
```

---

## 3. Models

### `Vehicle.java`
- Fields: `String licensePlate`, `VehicleType vehicleType`, `boolean isAccessible`
- Constructor: all-arg
- Getters for all fields
- `toString()` returning `licensePlate + " (" + vehicleType + ")"`

### `ParkingSpot.java`
- Fields: `String spotId`, `SpotType spotType`, `int floor`, `String section`, `SpotStatus status`
- Constructor: `(spotId, spotType, floor, section)` → status defaults to `SpotStatus.AVAILABLE`
- Getters for all fields
- `setStatus(SpotStatus status)` setter
- `isAvailable()` → returns `status == SpotStatus.AVAILABLE`
- `toString()` returning `spotId + " [" + spotType + "] Floor:" + floor + " Status:" + status`

### `Ticket.java`
- Fields: `String ticketId`, `Vehicle vehicle`, `ParkingSpot spot`, `long entryTime`, `long exitTime`, `TicketStatus status`, `double amountDue`
- Constructor: `(ticketId, vehicle, spot)` → sets `entryTime = System.currentTimeMillis()`, status = `ACTIVE`, exitTime = 0, amountDue = 0
- Getters for all fields
- `setExitTime(long exitTime)` setter
- `setAmountDue(double amount)` setter
- `setStatus(TicketStatus status)` setter
- `toString()` showing ticketId, vehicle licensePlate, spot spotId, entryTime

### `RateCard.java`
- Fields: `VehicleType vehicleType` (nullable = applies to all), `SpotType spotType` (nullable = applies to all), `double firstHourRate`, `double subsequentHourRate`, `double partialHourFactor` (always 0.75)
- Constructor: `(vehicleType, spotType, firstHourRate, subsequentHourRate)` → partialHourFactor hardcoded to 0.75
- Getters for all fields
- `matches(VehicleType vt, SpotType st)` → returns true if (vehicleType == null || vehicleType == vt) AND (spotType == null || spotType == st)

---

## 4. Payment

### `PaymentStrategy.java` (interface)
- Method: `boolean processPayment(double amount)`
- Method: `PaymentMode getPaymentMode()`

### `CashPayment.java`
- Implements `PaymentStrategy`
- Field: `double amount`, `boolean paid`
- `processPayment(double amount)`: sets `this.amount = amount`, sets `paid = true`, prints "Cash payment of $X accepted", returns true
- `getPaymentMode()`: returns `PaymentMode.CASH`

### `CardPayment.java`
- Implements `PaymentStrategy`
- Fields: `double amount`, `String last4Digits`, `boolean paid`
- Constructor: `(String last4Digits)`
- `processPayment(double amount)`: sets `this.amount = amount`, sets `paid = true`, prints "Card payment of $X accepted for card ending ****XXXX", returns true
- `getPaymentMode()`: returns `PaymentMode.CARD`

### `PaymentFactory.java`
- Static method: `PaymentStrategy createPayment(PaymentMode mode, String cardLast4)`
  - If CASH: return new `CashPayment()`
  - If CARD: return new `CardPayment(cardLast4)`
  - Default: throw `IllegalArgumentException("Unknown payment mode")`

---

## 5. Spot Assignment Strategy

### `SpotAssignmentStrategy.java` (interface)
- Method: `ParkingSpot assignSpot(VehicleType vehicleType, boolean isAccessible, List<ParkingFloor> floors)`
  - Throws `NoSpotAvailableException`

### `FirstAvailableStrategy.java`
- Implements `SpotAssignmentStrategy`
- Has private method `List<SpotType> getCompatibleSpotTypes(VehicleType vehicleType, boolean isAccessible)`:
  - MOTORCYCLE → [MOTORCYCLE]
  - CAR, isAccessible=true → [ACCESSIBLE]
  - CAR, isAccessible=false → [COMPACT, LARGE]
  - TRUCK → [LARGE]
  - VAN → [LARGE]
- `assignSpot(vehicleType, isAccessible, floors)`:
  - Loop floors → loop spots on each floor (via `floor.getSpots().values()`)
  - Check: `spot.isAvailable()` AND `compatibleTypes.contains(spot.getSpotType())`
  - If match: call `floor.occupySpot(spot.getSpotId())` and return spot
  - After all loops: throw `NoSpotAvailableException("No compatible spot available for " + vehicleType)`

---

## 6. Infrastructure

### `DisplayBoard.java`
- Fields: `String boardId`, `Map<SpotType, Integer> availableSpots` (SpotType → count), `boolean isFull`
- Constructor: `(boardId)` → initializes map with 0 for each SpotType, isFull = false
- `updateSpotCount(SpotType type, int count)`: updates map entry
- `showFull()`: sets `isFull = true`, prints "PARKING LOT FULL"
- `clearFull()`: sets `isFull = false`
- `display()`: prints boardId, isFull status, and each SpotType → count

### `ParkingFloor.java`
- Fields: `int floorNumber`, `Map<String, ParkingSpot> spots` (spotId → ParkingSpot), `Map<SpotType, Integer> availableCountByType`, `DisplayBoard displayBoard`
- Constructor: `(floorNumber, displayBoard)` → initializes empty maps
- `addSpot(ParkingSpot spot)`: adds to map, increments `availableCountByType[spot.getSpotType()]`
- `removeSpot(String spotId)`: sets spot status to UNAVAILABLE, decrements count if was AVAILABLE
- `occupySpot(String spotId)`: sets status to OCCUPIED, decrements `availableCountByType`, updates displayBoard
- `freeSpot(String spotId)`: sets status to AVAILABLE, increments `availableCountByType`, updates displayBoard
- `getSpots()`: returns unmodifiable view of spots map
- `getAvailableCount(SpotType type)`: returns count from map (0 if not found)
- `getTotalAvailable()`: sum of all values in availableCountByType

### `Entrance.java`
- Fields: `String entranceId`, `DisplayBoard displayBoard`, `SpotAssignmentStrategy assignmentStrategy`, `ParkingLot parkingLot` (reference)
- Constructor: `(entranceId, displayBoard, assignmentStrategy, parkingLot)`
- `acceptVehicle(Vehicle vehicle)`:
  - Check `parkingLot.isFull()` → if true throw `ParkingLotFullException`
  - Call `parkingLot.assignSpot(vehicle)` → gets ParkingSpot
  - Call `parkingLot.issueTicket(vehicle, spot)` → gets Ticket
  - Print ticket details
  - Return Ticket

### `Exit.java`
- Fields: `String exitId`, `DisplayBoard displayBoard`, `ParkingLot parkingLot`
- Constructor: `(exitId, displayBoard, parkingLot)`
- `processExit(Ticket ticket, PaymentMode paymentMode, String cardLast4)`:
  - Validate ticket status == ACTIVE → else throw `InvalidTicketException`
  - Call `parkingLot.calculateFee(ticket)` → gets double fee
  - Ticket.setAmountDue(fee)
  - Print fee to customer
  - Create payment via `PaymentFactory.createPayment(paymentMode, cardLast4)`
  - Call `payment.processPayment(ticket.getAmountDue())`
  - If success: call `parkingLot.releaseSpot(ticket)`, ticket.setStatus(PAID)
  - Print exit confirmation

---

## 7. System

### `FeeCalculator.java`
- No fields (stateless)
- `calculateFee(Ticket ticket, List<RateCard> rateCards)`:
  - Find matching RateCard: loop rateCards, call `card.matches(vehicleType, spotType)`, take first match; if none found use default (firstHour=5.0, subsequent=3.0)
  - `durationMillis = ticket.getExitTime() - ticket.getEntryTime()`
  - `totalMinutes = (int)(durationMillis / 60000)` — use at least 1 minute minimum
  - `fullHours = totalMinutes / 60`
  - `remainingMinutes = totalMinutes % 60`
  - Fee calculation:
    ```
    if fullHours == 0:
        fee = rateCard.getFirstHourRate() * rateCard.getPartialHourFactor()   // under 1 hour
    else if fullHours == 1:
        fee = rateCard.getFirstHourRate()
        if remainingMinutes > 0: fee += rateCard.getSubsequentHourRate() * rateCard.getPartialHourFactor()
    else:
        fee = rateCard.getFirstHourRate() + (fullHours - 1) * rateCard.getSubsequentHourRate()
        if remainingMinutes > 0: fee += rateCard.getSubsequentHourRate() * rateCard.getPartialHourFactor()
    ```
  - Return fee (rounded to 2 decimal places using `Math.round(fee * 100.0) / 100.0`)

### `ParkingLot.java` (Singleton)
- Fields:
  - `private static ParkingLot instance`
  - `String name`
  - `int maxCapacity` (40000)
  - `int totalOccupied`
  - `List<ParkingFloor> floors`
  - `List<Entrance> entrances`
  - `List<Exit> exits`
  - `List<RateCard> rateCards`
  - `Map<String, Ticket> activeTickets` (ticketId → Ticket)
  - `SpotAssignmentStrategy assignmentStrategy`
  - `FeeCalculator feeCalculator`
  - `int ticketCounter` (for generating ticketId)
- Constructor: `private ParkingLot(String name, int maxCapacity)`
- `public static ParkingLot getInstance(String name, int maxCapacity)`:
  - If instance == null: instance = new ParkingLot(name, maxCapacity)
  - Return instance
- `public static ParkingLot getInstance()`: return instance (throw if null)
- `isFull()`: returns `totalOccupied >= maxCapacity`
- `addFloor(ParkingFloor floor)`: adds to floors list
- `addEntrance(Entrance entrance)`: adds to entrances list
- `addExit(Exit exit)`: adds to exits list
- `addRateCard(RateCard card)`: adds to rateCards list
- `setAssignmentStrategy(SpotAssignmentStrategy strategy)`: sets field
- `assignSpot(Vehicle vehicle)`:
  - Calls `assignmentStrategy.assignSpot(vehicle.getVehicleType(), vehicle.isAccessible(), floors)`
  - Increments `totalOccupied`
  - If `isFull()`: calls `notifyAllDisplaysFull()`
  - Returns ParkingSpot
- `issueTicket(Vehicle vehicle, ParkingSpot spot)`:
  - Creates ticketId = "TKT-" + (++ticketCounter)
  - Creates new Ticket(ticketId, vehicle, spot)
  - Puts in activeTickets map
  - Returns Ticket
- `releaseSpot(Ticket ticket)`:
  - Find the floor containing ticket.getSpot().getSpotId()
  - Call `floor.freeSpot(spotId)`
  - Decrements `totalOccupied`
  - Removes ticket from activeTickets
  - If `totalOccupied == maxCapacity - 1`: calls `notifyAllDisplaysClearFull()`
- `calculateFee(Ticket ticket)`:
  - Sets ticket.exitTime = System.currentTimeMillis()
  - Returns `feeCalculator.calculateFee(ticket, rateCards)`
- `addSpot(int floorNumber, ParkingSpot spot)`:
  - Find floor by floorNumber
  - Call `floor.addSpot(spot)`
- `removeSpot(int floorNumber, String spotId)`:
  - Find floor by floorNumber
  - Call `floor.removeSpot(spotId)`
- `updateRateCard(RateCard oldCard, RateCard newCard)`:
  - Remove old, add new
- `notifyAllDisplaysFull()`: private — loops all entrances and floors, calls `displayBoard.showFull()`
- `notifyAllDisplaysClearFull()`: private — loops all entrances and floors, calls `displayBoard.clearFull()`
- `getFloorByNumber(int floorNumber)`: private helper — loops floors, returns match or null
- `getTotalAvailableSpots()`: sums `floor.getTotalAvailable()` across all floors
- `printStatus()`: prints lot name, occupied/max, available by type across all floors

---

## 8. Accounts

### `Account.java` (abstract)
- Fields: `String accountId`, `String name`, `String email`, `String passwordHash`, `AccountStatus status`
- Constructor: `(accountId, name, email, passwordHash)` → status = ACTIVE
- Getters for all fields
- `setStatus(AccountStatus status)` setter
- `abstract void viewAccount()` — abstract method

### `Admin.java` (extends Account)
- Constructor: calls `super(...)`
- `viewAccount()`: prints all account details
- `addSpot(ParkingLot lot, int floorNumber, ParkingSpot spot)`: delegates to `lot.addSpot(floorNumber, spot)`
- `removeSpot(ParkingLot lot, int floorNumber, String spotId)`: delegates to `lot.removeSpot(floorNumber, spotId)`
- `addRateCard(ParkingLot lot, RateCard card)`: delegates to `lot.addRateCard(card)`
- `updateRateCard(ParkingLot lot, RateCard oldCard, RateCard newCard)`: delegates to `lot.updateRateCard(oldCard, newCard)`

---

## 9. Main.java — Demo Runner

The main method must demonstrate this complete flow:

### Setup
1. Create `ParkingLot.getInstance("City Center Parking", 40000)`
2. Create 2 floors, add spots to each:
   - Floor 1: 2 COMPACT spots, 2 LARGE spots, 1 MOTORCYCLE spot, 1 ACCESSIBLE spot
   - Floor 2: 2 COMPACT spots, 1 LARGE spot
3. Create `DisplayBoard` for each floor and for each entrance
4. Create `FirstAvailableStrategy`
5. Set assignment strategy on lot
6. Create `Entrance` and `Exit`, add to lot
7. Create `RateCard`: CAR/COMPACT → firstHour=10.0, subsequent=8.0
8. Create default `RateCard`: null/null → firstHour=5.0, subsequent=3.0
9. Add both rate cards to lot

### Demo Flow
1. Print initial lot status
2. Create Vehicle: `CAR, "KA-01-AB-1234", isAccessible=false`
3. Call `entrance.acceptVehicle(vehicle)` → prints ticket info
4. Simulate 90 minutes: set `ticket.entryTime = System.currentTimeMillis() - 90*60*1000`
5. Call `exit.processExit(ticket, PaymentMode.CARD, "4242")` → prints fee and payment
6. Print final lot status
7. Create Motorcycle, park it, demonstrate different rate

---

## Code Style Rules

- All print statements use `System.out.println()`
- No try-catch in Main — let exceptions propagate (shows the exception contract is understood)
- No stream operations — use traditional for loops
- Fields are `private`, methods are `public` unless noted
- No `var` keyword
- Imports: explicit (no wildcard `import java.util.*`)

---

## Regeneration Checklist

When regenerating code from this spec:
- [ ] All 6 enums generated
- [ ] All 3 exceptions generated  
- [ ] All 4 models generated (Vehicle, ParkingSpot, Ticket, RateCard)
- [ ] Payment: interface + 2 concrete + factory = 4 files
- [ ] Strategy: interface + 1 concrete = 2 files
- [ ] Infrastructure: DisplayBoard + ParkingFloor + Entrance + Exit = 4 files
- [ ] System: FeeCalculator + ParkingLot = 2 files
- [ ] Accounts: Account (abstract) + Admin = 2 files
- [ ] Main.java with full demo
- [ ] Total: **26 files**
