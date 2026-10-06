package com.rentivo.backend.audit;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * adminId/adminPhone are a snapshot taken at write time (no FK), matching the pattern used
 * elsewhere for records that must stay readable even if the referenced row changes later.
 */
@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminId;

    @Column(nullable = false, length = 15)
    private String adminPhone;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false, length = 30)
    private String targetType;

    private Long targetId;

    @Column(length = 500)
    private String detail;

    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getAdminPhone() { return adminPhone; }
    public void setAdminPhone(String adminPhone) { this.adminPhone = adminPhone; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
