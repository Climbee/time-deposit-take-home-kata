package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

// The foreign key to time_deposits is enforced by the schema; a plain id column avoids an unneeded JPA association.
@Entity
@Table(name = "withdrawals")
class WithdrawalEntity {

    @Id
    private Integer id;

    @Column(name = "time_deposit_id", nullable = false)
    private int timeDepositId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    protected WithdrawalEntity() {
    }

    Integer getId() {
        return id;
    }

    int getTimeDepositId() {
        return timeDepositId;
    }

    BigDecimal getAmount() {
        return amount;
    }

    LocalDate getDate() {
        return date;
    }
}
