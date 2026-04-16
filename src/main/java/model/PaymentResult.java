package model;

import java.time.LocalDateTime;

public class PaymentResult {

    private PaymentStatus status;
    private String reference;
    private String message;
    private LocalDateTime timestamp;
    private PaymentProvider provider;

    public PaymentResult() {}

    public PaymentResult(PaymentStatus status, String reference, String message, PaymentProvider provider) {
        this.status = status;
        this.reference = reference;
        this.message = message;
        this.provider = provider;
        this.timestamp = LocalDateTime.now();
    }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public PaymentProvider getProvider() { return provider; }
    public void setProvider(PaymentProvider provider) { this.provider = provider; }

    public boolean isSuccess() { return status == PaymentStatus.SUCCESS; }
}
