package parkinglot.enums;

/**
 * Supported payment modes at exit panels.
 * Drives which PaymentStrategy implementation is created by PaymentFactory.
 */
public enum PaymentMode {
    CASH,   // Physical currency payment at exit kiosk
    CARD    // Credit or debit card payment
}
