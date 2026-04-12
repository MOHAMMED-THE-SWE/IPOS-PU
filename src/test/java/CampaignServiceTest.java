
import dao.promotions.CampaignCounterDAO;
import dao.promotions.CampaignDAO;
import dao.promotions.CampaignItemDAO;
import model.Campaign;
import model.CampaignItem;
import model.CampaignStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.promotions.CampaignService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignDAO campaignDAO;

    @Mock
    private CampaignItemDAO campaignItemDAO;

    @Mock
    private CampaignCounterDAO campaignCounterDAO;

    private CampaignService service;

    @BeforeEach
    void setUp() {
        service = new CampaignService(campaignDAO, campaignItemDAO, campaignCounterDAO);
    }

    @Test
    void createCampaign_validCampaignAndItems_savesAll() {
        Campaign campaign = new Campaign("Summer Sale", LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        campaign.setCampaignId(1);

        CampaignItem item1 = new CampaignItem(0, 101, 10.0);
        CampaignItem item2 = new CampaignItem(0, 202, 20.0);
        List<CampaignItem> items = List.of(item1, item2);

        when(campaignDAO.save(any(Campaign.class))).thenReturn(campaign);
        doNothing().when(campaignItemDAO).save(any(CampaignItem.class));
        doNothing().when(campaignCounterDAO).ensureCounter(anyInt(), anyInt());

        Campaign result = service.createCampaign(campaign, items);

        assertSame(campaign, result, "Should return the same campaign object");
        verify(campaignDAO, times(1)).save(campaign);
        verify(campaignItemDAO, times(2)).save(any(CampaignItem.class));
        verify(campaignCounterDAO, times(2)).ensureCounter(anyInt(), anyInt());
        assertEquals(1, item1.getCampaignId(), "Campaign ID should be set on item1");
        assertEquals(1, item2.getCampaignId(), "Campaign ID should be set on item2");
    }

    @Test
    void createCampaign_emptyItemsList_savesCampaignOnly() {
        Campaign campaign = new Campaign("Winter Sale", LocalDateTime.now(), LocalDateTime.now().plusDays(14));
        campaign.setCampaignId(2);

        when(campaignDAO.save(any(Campaign.class))).thenReturn(campaign);

        Campaign result = service.createCampaign(campaign, Collections.emptyList());

        assertSame(campaign, result);
        verify(campaignDAO, times(1)).save(campaign);
        verifyNoInteractions(campaignItemDAO);
        verifyNoInteractions(campaignCounterDAO);
    }

    @Test
    void getActiveCampaigns_returnsListFromDAO() {
        Campaign c1 = new Campaign("Spring Deal", LocalDateTime.now(), LocalDateTime.now().plusDays(3));
        Campaign c2 = new Campaign("Flash Sale", LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        when(campaignDAO.findActive()).thenReturn(List.of(c1, c2));

        List<Campaign> result = service.getActiveCampaigns();

        assertEquals(2, result.size(), "Should return 2 active campaigns");
        assertSame(c1, result.get(0));
        assertSame(c2, result.get(1));
        verify(campaignDAO, times(1)).findActive();
    }

    @Test
    void getAllCampaigns_returnsListFromDAO() {
        Campaign c1 = new Campaign("Old Campaign", LocalDateTime.now().minusDays(30), LocalDateTime.now().minusDays(1));
        c1.setStatus(CampaignStatus.ENDED);
        Campaign c2 = new Campaign("Current Campaign", LocalDateTime.now(), LocalDateTime.now().plusDays(5));
        when(campaignDAO.findAll()).thenReturn(List.of(c1, c2));

        List<Campaign> result = service.getAllCampaigns();

        assertEquals(2, result.size(), "Should return all campaigns including ended ones");
        verify(campaignDAO, times(1)).findAll();
    }

    @Test
    void cancelCampaign_callsUpdateStatusWithCancelled() {
        doNothing().when(campaignDAO).updateStatus(anyInt(), any(CampaignStatus.class));

        service.cancelCampaign(42);

        verify(campaignDAO, times(1)).updateStatus(42, CampaignStatus.CANCELLED);
    }

    @Test
    void endCampaign_callsUpdateStatusWithEnded() {
        doNothing().when(campaignDAO).updateStatus(anyInt(), any(CampaignStatus.class));

        service.endCampaign(99);

        verify(campaignDAO, times(1)).updateStatus(99, CampaignStatus.ENDED);
    }

    @Test
    void hasActiveCampaigns_whenActiveExist_returnsTrue() {
        Campaign active = new Campaign("Active Deal", LocalDateTime.now(), LocalDateTime.now().plusDays(2));
        when(campaignDAO.findActive()).thenReturn(List.of(active));

        assertTrue(service.hasActiveCampaigns(), "Should return true when there are active campaigns");
        verify(campaignDAO, times(1)).findActive();
    }

    @Test
    void hasActiveCampaigns_whenNoneActive_returnsFalse() {
        when(campaignDAO.findActive()).thenReturn(Collections.emptyList());

        assertFalse(service.hasActiveCampaigns(), "Should return false when no active campaigns exist");
        verify(campaignDAO, times(1)).findActive();
    }
}