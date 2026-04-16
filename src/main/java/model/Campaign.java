package model;

import java.time.LocalDateTime;

// One campaign can apply to many products (CampaignItem links).
public class Campaign {

    private int campaignId;
    private String description;
    private LocalDateTime startDt;
    private LocalDateTime endDt;
    private CampaignStatus status;

    public Campaign() {}

    public Campaign(String description, LocalDateTime startDt, LocalDateTime endDt) {
        this.description = description;
        this.startDt = startDt;
        this.endDt = endDt;
        this.status = CampaignStatus.ACTIVE;
    }

    public int getCampaignId() { return campaignId; }
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartDt() { return startDt; }
    public void setStartDt(LocalDateTime startDt) { this.startDt = startDt; }

    public LocalDateTime getEndDt() { return endDt; }
    public void setEndDt(LocalDateTime endDt) { this.endDt = endDt; }

    public CampaignStatus getStatus() { return status; }
    public void setStatus(CampaignStatus status) { this.status = status; }
}
