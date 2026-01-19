/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.engine;

import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "CONTROL_BPM_ENGINE", indexes = {
        @Index(name = "IDX_BPM_ENGINE_BASE_URL_UNQ", columnList = "BASE_URL", unique = true)
})
@Entity
public class BpmEngine {
    public static final String SELECTED_ENGINE_ATTRIBUTE = "selectedEngine";

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "AUTH_ENABLED")
    private Boolean authEnabled = false;

    @Column(name = "BASE_URL", nullable = false)
    @NotNull
    private String baseUrl;

    @Column(name = "TYPE_", nullable = false)
    @NotNull
    private String type;

    @InstanceName
    @Column(name = "NAME", nullable = false, length = 50)
    @NotNull
    private String name;

    @Column(name = "ENVIRONMENT_TYPE")
    private String environmentType;

    @Column(name = "IS_DEFAULT")
    private Boolean isDefault = false;

    @Column(name = "AUTH_TYPE")
    private String authType;

    @Column(name = "BASIC_AUTH_USERNAME")
    private String basicAuthUsername;

    @Column(name = "BASIC_AUTH_PASSWORD")
    private String basicAuthPassword;

    @Column(name = "HTTP_HEADER_NAME")
    private String httpHeaderName;

    @Column(name = "HTTP_HEADER_VALUE", length = 500)
    private String httpHeaderValue;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private OffsetDateTime lastModifiedDate;

    @DeletedBy
    @Column(name = "DELETED_BY")
    private String deletedBy;

    @DeletedDate
    @Column(name = "DELETED_DATE")
    private OffsetDateTime deletedDate;

    public EnvironmentType getEnvironmentType() {
        return environmentType == null ? null : EnvironmentType.fromId(environmentType);
    }

    public void setEnvironmentType(EnvironmentType environmentType) {
        this.environmentType = environmentType == null ? null : environmentType.getId();
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getHttpHeaderValue() {
        return httpHeaderValue;
    }

    public void setHttpHeaderValue(String httpHeaderValue) {
        this.httpHeaderValue = httpHeaderValue;
    }

    public String getHttpHeaderName() {
        return httpHeaderName;
    }

    public void setHttpHeaderName(String httpHeaderName) {
        this.httpHeaderName = httpHeaderName;
    }

    public String getBasicAuthPassword() {
        return basicAuthPassword;
    }

    public void setBasicAuthPassword(String basicAuthPassword) {
        this.basicAuthPassword = basicAuthPassword;
    }

    public String getBasicAuthUsername() {
        return basicAuthUsername;
    }

    public void setBasicAuthUsername(String basicAuthUsername) {
        this.basicAuthUsername = basicAuthUsername;
    }

    public AuthType getAuthType() {
        return authType == null ? null : AuthType.fromId(authType);
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType == null ? null : authType.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EngineType getType() {
        return type == null ? null : EngineType.fromId(type);
    }

    public void setType(EngineType type) {
        this.type = type == null ? null : type.getId();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Boolean getAuthEnabled() {
        return authEnabled;
    }

    public void setAuthEnabled(Boolean authEnabled) {
        this.authEnabled = authEnabled;
    }

    public OffsetDateTime getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(OffsetDateTime deletedDate) {
        this.deletedDate = deletedDate;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public OffsetDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}