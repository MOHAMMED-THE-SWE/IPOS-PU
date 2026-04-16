package model;

import java.time.LocalDateTime;

public class PaymentLog {

    private int id;
    private String payee;
    private String reference;
    private double amount;
    private LocalDateTime paidAt;
    private String cardType;
    private String cardFirst4;
    private String cardLast4;
    private String expiry;
    private PaymentStatus status;
    private PaymentProvider provider;

    public PaymentLog() {}

    public PaymentLog(String payee, String reference, double amount, String cardType,
                      String cardFirst4, String cardLast4, String expiry,
                      PaymentStatus status, PaymentProvider provider) {
        this.payee = payee;
        this.reference = reference;
        this.amount = amount;
        this.cardType = cardType;
        this.cardFirst4 = cardFirst4;
        this.cardLast4 = cardLast4;
        this.expiry = expiry;
        this.status = status;
        this.provider = provider;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPayee() { return payee; }
    public void setPayee(String payee) { this.payee = payee; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getCardFirst4() { return cardFirst4; }
    public void setCardFirst4(String cardFirst4) { this.cardFirst4 = cardFirst4; }

    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public PaymentProvider getProvider() { return provider; }
    public void setProvider(PaymentProvider provider) { this.provider = provider; }
}
