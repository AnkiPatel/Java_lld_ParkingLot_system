package parkinglot.payment;

import parkinglot.enums.PaymentMode;

/**
 * Concrete Strategy: Credit/Debit card payment at the exit kiosk.
 *
 * In a real system this would integrate with a payment gateway (Stripe,
 * Razorpay, etc.). Here it simulates a successful card transaction.
 */
public class CardPayment implements PaymentStrategy {

    private double amount;
    private String last4Digits;  // Last 4 digits of the card for receipt display
    private boolean paid;

    public CardPayment(String last4Digits) {
        this.last4Digits = last4Digits;
        this.paid = false;
        this.amount = 0.0;
    }

    @Override
    public boolean processPayment(double amount) {
        this.amount = amount;
        this.paid = true;
        System.out.println("  [PAYMENT] Card payment of $" + String.format("%.2f", amount)
                + " accepted. Card ending ****" + last4Digits);
        return true;
    }

    @Override
    public PaymentMode getPaymentMode() {
        return PaymentMode.CARD;
    }

    public double getAmount() {
        return amount;
    }

    public String getLast4Digits() {
        return last4Digits;
    }

    public boolean isPaid() {
        return paid;
    }
}
