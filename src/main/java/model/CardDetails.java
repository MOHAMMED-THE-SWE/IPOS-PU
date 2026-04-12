package model;

public class CardDetails {

    private String cardType;
    private String cardNumber;
    private String expiry;
    private String cvv;
    private String holderName;

    public CardDetails() {}

    public CardDetails(String cardType, String cardNumber, String expiry, String cvv, String holderName) {
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.expiry = expiry;
        this.cvv = cvv;
        this.holderName = holderName;
    }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    /** Returns first 4 digits of the card number for logging. */
    public String getFirst4() {
        if (cardNumber == null || cardNumber.length() < 4) return "????";
        return cardNumber.replaceAll("\\s", "").substring(0, 4);
    }

    /** Returns last 4 digits of the card number for logging. */
    public String getLast4() {
        String stripped = cardNumber == null ? "" : cardNumber.replaceAll("\\s", "");
        if (stripped.length() < 4) return "????";
        return stripped.substring(stripped.length() - 4);
    }
}
