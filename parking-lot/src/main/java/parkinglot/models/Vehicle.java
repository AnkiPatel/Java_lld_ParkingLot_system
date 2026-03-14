package parkinglot.models;

import parkinglot.enums.VehicleType;

/**
 * Represents a vehicle entering the parking lot.
 *
 * Design decision: Vehicle is a pure data object (no behavior).
 * It holds identity (license plate), classification (vehicleType),
 * and an accessibility flag that determines spot eligibility.
 *
 * Behavior (spot assignment, fee calculation) lives in the system
 * classes, not in Vehicle. This follows the Single Responsibility Principle.
 */
public class Vehicle {

    private String licensePlate;
    private VehicleType vehicleType;
    private boolean isAccessible; // true if driver holds accessibility permit

    public Vehicle(String licensePlate, VehicleType vehicleType, boolean isAccessible) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.isAccessible = isAccessible;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    @Override
    public String toString() {
        return licensePlate + " (" + vehicleType + (isAccessible ? ", ACCESSIBLE" : "") + ")";
    }
}
