package parkinglot.payment;

import parkinglot.enums.PaymentMode;

/**
 * Factory for creating PaymentStrategy instances.
 *
 * Design pattern: Factory Pattern
 *
 * Why Factory here?
 * The Exit panel needs a PaymentStrategy object, but it should not be
 * responsible for deciding which concrete class to instantiate. If Exit
 * directly called "new CashPayment()" or "new CardPayment()", it would
 * be tightly coupled to both concrete implementations.
 *
 * With PaymentFactory, Exit only knows about the PaymentStrategy interface
 * and calls PaymentFactory.createPayment(mode, ...) to get the right object.
 * Adding a new payment mode (e.g., UPI) means adding a new class + one
 * line here. Exit does not change at all.
 *
 * Note: All methods are static because PaymentFactory holds no state.
 * It is a utility class, not a stateful object.
 */
public class PaymentFactory {

    // Private constructor: prevents instantiation of a pure utility class
    private PaymentFactory() {}

    /**
     * Creates and returns the appropriate PaymentStrategy based on payment mode.
     *
     * @param mode       the customer's chosen payment mode
     * @param cardLast4  last 4 digits of card (only used for CARD mode, pass null for CASH)
     * @return           a ready-to-use PaymentStrategy implementation
     */
    public static PaymentStrategy createPayment(PaymentMode mode, String cardLast4) {
        if (mode == PaymentMode.CASH) {
            return new CashPayment();
        } else if (mode == PaymentMode.CARD) {
            return new CardPayment(cardLast4);
        } else {
            throw new IllegalArgumentException("Unknown payment mode: " + mode);
        }
    }
}
