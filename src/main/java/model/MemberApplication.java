package model;

import java.time.LocalDateTime;

// Represents a membership application submitted by a user.
// so like "User wants to become a member / register formally".

public class MemberApplication {

    public enum Type { COMMERCIAL, NON_COMMERCIAL }

    private int id;
    private Type type;
    private String email;
    private ApplicationStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;

    public MemberApplication() {}

    public MemberApplication(Type type, String email) {
        this.type = type;
        this.email = email;
        this.status = ApplicationStatus.PENDING;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
