package parkinglot.payment;

import parkinglot.enums.PaymentMode;

/**
 * Concrete Strategy: Cash payment at the exit kiosk.
 *
 * In a real system this would interface with a cash acceptor hardware
 * module. Here it simulates acceptance and always returns true.
 */
public class CashPayment implements PaymentStrategy {

    private double amount;
    private boolean paid;

    public CashPayment() {
        this.paid = false;
        this.amount = 0.0;
    }

    @Override
    public boolean processPayment(double amount) {
        this.amount = amount;
        this.paid = true;
        System.out.println("  [PAYMENT] Cash payment of $" + String.format("%.2f", amount) + " accepted.");
        return true;
    }

    @Override
    public PaymentMode getPaymentMode() {
        return PaymentMode.CASH;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isPaid() {
        return paid;
    }
}
