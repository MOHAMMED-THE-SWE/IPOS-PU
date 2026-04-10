package interfaces;

import model.CommercialApplication;

/**
 * ICommercialApplicationService — PU depends on this to forward
 * commercial membership applications to IPOS-SA for processing.
 */
public interface ICommercialApplicationService {

    /**
     * Submits a commercial application to SA.
     *
     * @param app the commercial application to submit (must not be null)
     * @return a non-null confirmation/reference ID string
     * @throws IllegalArgumentException if app is null or contains invalid data
     */
    String submitCommercialApplication(CommercialApplication app);
}
