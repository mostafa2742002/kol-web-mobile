package com.nano.soft.kol.user.entity;

import java.util.Date;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.data.annotation.LastModifiedBy;
import lombok.Data;

@Data
public abstract class AuditableBase {

    @CreatedBy
    @Schema(hidden = true)
    private String createdBy;

    @CreatedDate
    @Schema(hidden = true)
    private Date createdDate;

    @LastModifiedBy
    @Schema(hidden = true)
    private String lastModifiedBy;

    @LastModifiedDate
    @Schema(hidden = true)
    private Date lastModifiedDate;

}
