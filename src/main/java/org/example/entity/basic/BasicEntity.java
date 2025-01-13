package org.example.entity.basic;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.Temporal;

import java.time.LocalDateTime;
import java.util.Date;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class BasicEntity {

    @CreatedBy
    @Column(name = "create_id", updatable = false)
    private Long createId;

    @LastModifiedBy
    @Column(name = "update_id")
    private Long updateId;

    @CreatedDate
    @Column(name = "create_time_", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time_")
    private LocalDateTime updateTime;


}
