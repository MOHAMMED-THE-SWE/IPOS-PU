package service.promotions;

import dao.promotions.CampaignDAO;
import dao.promotions.CampaignCounterDAO;
import dao.promotions.CampaignItemDAO;
import model.Campaign;
import model.CampaignItem;
import model.CampaignStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages advertising campaigns for IPOS-PU-PRM (Promotions).
 *
 * <p>Provides CRUD operations for campaigns and their associated products,
 * and ensures campaign counters are initialised whenever new items are added.
 * Active campaigns drive discount pricing in {@link PricingService}.</p>
 *
 * @author Team C
 */
public class CampaignService {

    private final CampaignDAO campaignDAO;
    private final CampaignItemDAO campaignItemDAO;
    private final CampaignCounterDAO campaignCounterDAO;

    /**
     * Creates a new CampaignService.
     *
     * @param campaignDAO        DAO for campaign records
     * @param campaignItemDAO    DAO for campaign-product associations
     * @param campaignCounterDAO DAO for campaign hit and purchase counters
     */
    public CampaignService(CampaignDAO campaignDAO,
                           CampaignItemDAO campaignItemDAO,
                           CampaignCounterDAO campaignCounterDAO) {
        this.campaignDAO = campaignDAO;
        this.campaignItemDAO = campaignItemDAO;
        this.campaignCounterDAO = campaignCounterDAO;
    }

    /**
     * Persists a new campaign together with its product items and initialises counters.
     *
     * @param campaign the campaign to create
     * @param items    the list of products (with discount percentages) to associate with the campaign
     * @return the saved campaign with its generated ID populated
     */
    public Campaign createCampaign(Campaign campaign, List<CampaignItem> items) {
        campaignDAO.save(campaign);
        for (CampaignItem item : items) {
            item.setCampaignId(campaign.getCampaignId());
            campaignItemDAO.save(item);
            campaignCounterDAO.ensureCounter(campaign.getCampaignId(), item.getItemId());
        }
        return campaign;
    }

    /**
     * Returns all campaigns whose status is {@code ACTIVE} and whose date window
     * currently includes today.
     *
     * @return a list of active campaigns; never null, may be empty
     */
    public List<Campaign> getActiveCampaigns() {
        return campaignDAO.findActive();
    }

    /**
     * Returns all campaigns regardless of status.
     *
     * @return a list of all campaigns; never null, may be empty
     */
    public List<Campaign> getAllCampaigns() {
        return campaignDAO.findAll();
    }

    /**
     * Returns all product-discount associations for a given campaign.
     *
     * @param campaignId the campaign whose items to retrieve
     * @return a list of {@link model.CampaignItem} records; never null
     */
    public List<CampaignItem> getCampaignItems(int campaignId) {
        return campaignItemDAO.findByCampaignId(campaignId);
    }

    /**
     * Cancels a campaign by setting its status to {@code CANCELLED}.
     *
     * @param campaignId the ID of the campaign to cancel
     */
    public void cancelCampaign(int campaignId) {
        campaignDAO.updateStatus(campaignId, CampaignStatus.CANCELLED);
    }

    /**
     * Ends a campaign by setting its status to {@code ENDED}.
     *
     * @param campaignId the ID of the campaign to end
     */
    public void endCampaign(int campaignId) {
        campaignDAO.updateStatus(campaignId, CampaignStatus.ENDED);
    }

    /**
     * Updates the description and date range of an existing campaign.
     *
     * @param campaignId  the ID of the campaign to update
     * @param description the new description
     * @param startDt     the new start date/time
     * @param endDt       the new end date/time
     */
    public void updateCampaign(int campaignId, String description, LocalDateTime startDt, LocalDateTime endDt) {
        Campaign c = campaignDAO.findById(campaignId)
            .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + campaignId));
        c.setDescription(description);
        c.setStartDt(startDt);
        c.setEndDt(endDt);
        campaignDAO.update(c);
    }

    /**
     * Replaces the items (and discount rates) associated with an existing campaign.
     * Old items are removed and the new list is persisted. Counters are initialised
     * for any new item that did not have one already.
     *
     * @param campaignId the ID of the campaign to update
     * @param items      the new list of products with discount percentages
     */
    public void updateCampaignItems(int campaignId, List<CampaignItem> items) {
        campaignItemDAO.deleteByCampaignId(campaignId);
        for (CampaignItem item : items) {
            item.setCampaignId(campaignId);
            campaignItemDAO.save(item);
            campaignCounterDAO.ensureCounter(campaignId, item.getItemId());
        }
    }

    /**
     * Permanently deletes a campaign and all its associated items and counters.
     *
     * @param campaignId the ID of the campaign to delete
     */
    public void deleteCampaign(int campaignId) {
        campaignDAO.delete(campaignId);
    }

    /**
     * Returns any active campaigns that overlap in date range and share items with the given campaign.
     * Used to warn admins about conflicting promotions after campaign creation.
     *
     * @param newCampaignId the ID of the newly created campaign to check against
     * @param startDt       the start of the new campaign's date range
     * @param endDt         the end of the new campaign's date range
     * @return list of overlapping campaigns; empty if none
     */
    public List<Campaign> getOverlappingCampaigns(int newCampaignId, LocalDateTime startDt, LocalDateTime endDt) {
        return campaignDAO.findOverlapping(newCampaignId, startDt, endDt);
    }

    /**
     * Checks whether any campaigns are currently active.
     *
     * @return {@code true} if at least one active campaign exists
     */
    public boolean hasActiveCampaigns() {
        return !campaignDAO.findActive().isEmpty();
    }
}
