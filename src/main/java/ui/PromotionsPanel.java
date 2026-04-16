package ui;

import model.Campaign;
import model.CampaignItem;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PromotionsPanel extends JPanel {

    private final MainFrame frame;
    private final JPanel campaignsPanel;

    public PromotionsPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Active Promotions", SwingConstants.CENTER);
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(UIConstants.BRAND_RED);
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 4, 0));
        add(title, BorderLayout.NORTH);

        campaignsPanel = new JPanel();
        campaignsPanel.setLayout(new BoxLayout(campaignsPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(campaignsPanel), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton shopBtn = UIConstants.primaryButton("Shop Now");
        south.add(shopBtn);
        add(south, BorderLayout.SOUTH);
        shopBtn.addActionListener(e -> frame.showPanel("CATALOGUE"));
    }

    public void refresh() {
        campaignsPanel.removeAll();

        List<Campaign> activeCampaigns = frame.getCampaignService().getActiveCampaigns();

        // Build a product description map once for all campaigns
        Map<Integer, String> descMap = frame.getCatalogueService().getAll().stream()
            .collect(Collectors.toMap(p -> p.getItemId(), p -> p.getDescription()));

        for (Campaign campaign : activeCampaigns) {
            // Record campaign hit
            List<CampaignItem> items = frame.getCampaignService().getCampaignItems(campaign.getCampaignId());
            List<Integer> itemIds = items.stream().map(CampaignItem::getItemId).toList();
            frame.getCampaignCounterService().recordHit(campaign.getCampaignId(), itemIds);

            JPanel campaignBox = new JPanel(new BorderLayout(5, 5));
            campaignBox.setBorder(BorderFactory.createTitledBorder(campaign.getDescription()));

            StringBuilder sb = new StringBuilder("<html><body style='width: 500px'>");
            sb.append("<b>Period:</b> ").append(campaign.getStartDt().toLocalDate())
              .append(" to ").append(campaign.getEndDt().toLocalDate()).append("<br><br>");
            sb.append("<b>Discounted Items:</b><br>");

            for (CampaignItem ci : items) {
                String name = descMap.getOrDefault(ci.getItemId(), "Item #" + ci.getItemId());
                sb.append("&nbsp;&nbsp;&bull; ").append(name)
                  .append(" — <b>").append(String.format("%.0f%%", ci.getDiscountPercent()))
                  .append(" OFF</b><br>");
            }
            sb.append("</body></html>");

            JLabel info = new JLabel(sb.toString());
            campaignBox.add(info, BorderLayout.CENTER);
            campaignsPanel.add(campaignBox);
            campaignsPanel.add(Box.createVerticalStrut(10));
        }

        if (activeCampaigns.isEmpty()) {
            campaignsPanel.add(new JLabel("No active promotions at this time."));
        }

        campaignsPanel.revalidate();
        campaignsPanel.repaint();
    }
}
