package model;

import java.time.LocalDateTime;

public class CommercialApplication {

    private int applicationId;
    private String companyName;
    private String companyRegNo;
    private String directors;
    private String businessType;
    private String address;
    private String email;
    private ApplicationStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;

    public CommercialApplication() {}

    public CommercialApplication(String companyName, String companyRegNo, String directors,
                                  String businessType, String address, String email) {
        this.companyName = companyName;
        this.companyRegNo = companyRegNo;
        this.directors = directors;
        this.businessType = businessType;
        this.address = address;
        this.email = email;
        this.status = ApplicationStatus.PENDING;
    }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyRegNo() { return companyRegNo; }
    public void setCompanyRegNo(String companyRegNo) { this.companyRegNo = companyRegNo; }

    public String getDirectors() { return directors; }
    public void setDirectors(String directors) { this.directors = directors; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
