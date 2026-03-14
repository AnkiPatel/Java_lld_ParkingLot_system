package parkinglot.payment;

import parkinglot.enums.PaymentMode;

/**
 * Strategy interface for payment processing.
 *
 * Design pattern: Strategy Pattern
 *
 * Why Strategy here?
 * The system supports multiple payment modes (cash, card) and may support
 * more in the future (UPI, NFC, etc.). Rather than using if-else blocks
 * in the Exit class, we define a common interface and let each payment
 * mode implement it independently.
 *
 * Benefit: Adding a new payment mode requires only creating a new class
 * that implements this interface. Exit, FeeCalculator, and all other
 * classes remain unchanged. This is the Open/Closed Principle.
 */
public interface PaymentStrategy {

    /**
     * Process the payment for the given amount.
     *
     * @param amount the amount to be charged
     * @return true if payment was successful, false otherwise
     */
    boolean processPayment(double amount);

    /**
     * Returns the payment mode this strategy handles.
     * Useful for logging, receipt generation, and audit trails.
     */
    PaymentMode getPaymentMode();
}
