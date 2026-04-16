package model;

// Represents a link between a campaign and a product item.
// from brief remember like "Campaign X applies to Product Y".
public class CampaignItem {

    private int campaignId;
    private int itemId;
    private double discountPercent;

    public CampaignItem() {}

    public CampaignItem(int campaignId, int itemId, double discountPercent) {
        this.campaignId = campaignId;
        this.itemId = itemId;
        this.discountPercent = discountPercent;
    }

    public int getCampaignId() { return campaignId; }
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }
}