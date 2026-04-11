package service.sales;

import dao.sales.ProductDAO;
import interfaces.ICatalogueService;
import model.Product;

import java.util.List;

/**
 * Provides read access to the IPOS-PU product catalogue.
 *
 * <p>Implements {@link ICatalogueService}. Delegates all queries to
 * {@link dao.sales.ProductDAO}, supporting full catalogue retrieval and
 * keyword-based search for the customer-facing browse experience.</p>
 *
 * @author Team C
 */
public class CatalogueService implements ICatalogueService {

    private final ProductDAO productDAO;

    /**
     * Creates a new CatalogueService.
     *
     * @param productDAO the DAO used to query the {@code pu_products} table
     */
    public CatalogueService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Override
    public List<Product> getAll() {
        return productDAO.findAll();
    }

    @Override
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAll();
        return productDAO.findByKeyword(keyword.trim());
    }
}
