package service.promotions;

import dao.promotions.CampaignCounterDAO;
import dao.promotions.CampaignItemDAO;
import model.CampaignItem;
import service.sales.CartService.CartItem;

import java.util.List;

public class CampaignCounterService {

    private final CampaignCounterDAO counterDAO;
    private final CampaignItemDAO campaignItemDAO;

    public CampaignCounterService(CampaignCounterDAO counterDAO, CampaignItemDAO campaignItemDAO) {
        this.counterDAO = counterDAO;
        this.campaignItemDAO = campaignItemDAO;
    }

    /** Called when user clicks the Promotions link — increments campaign_hits for all items in each active campaign. */
    public void recordHit(int campaignId, List<Integer> itemIds) {
        for (int itemId : itemIds) {
            counterDAO.incrementCampaignHits(campaignId, itemId);
        }
    }

    /** Called when a campaign item is added to cart — increments item_hits_qty. */
    public void recordItemAdded(int campaignId, int itemId, int qty) {
        counterDAO.incrementItemHitsQty(campaignId, itemId, qty);
    }

    /** Called after successful payment — increments item_purchased_qty for campaign items. */
    public void recordPurchase(List<CartItem> cartItems) {
        for (CartItem ci : cartItems) {
            List<CampaignItem> campaignItems = campaignItemDAO.findActiveByItemId(ci.getProduct().getItemId());
            for (CampaignItem campaignItem : campaignItems) {
                counterDAO.incrementItemPurchasedQty(campaignItem.getCampaignId(), ci.getProduct().getItemId(), ci.getQty());
            }
        }
    }
}
