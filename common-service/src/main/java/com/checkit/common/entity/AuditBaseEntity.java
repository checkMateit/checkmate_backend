package com.checkit.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@MappedSuperclass
public abstract class AuditBaseEntity {

    @Column(name = "created_at" ,updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by",columnDefinition = "uuid", updatable = false)
    private UUID createdBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", columnDefinition = "uuid")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by", columnDefinition = "uuid")
    private UUID deletedBy;

    /* ===================== lifecycle ===================== */

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    /* ===================== helpers ===================== */

    public void setCreator(UUID actor) {
        this.createdBy = actor;
    }

    public void setUpdater(UUID actor) {
        this.updatedBy = actor;
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void softDelete(UUID actor) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.deletedAt = now;
        this.deletedBy = actor;
        this.updatedAt = now;
        this.updatedBy = actor;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}