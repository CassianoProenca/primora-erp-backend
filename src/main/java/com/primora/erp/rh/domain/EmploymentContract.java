package com.primora.erp.rh.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employment_contracts")
public class EmploymentContract {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "monthly_salary", nullable = false)
    private BigDecimal monthlySalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EmploymentContract() {
    }

    public EmploymentContract(UUID id, UUID companyId, UUID employeeId, String title,
                              LocalDate startDate, LocalDate endDate, BigDecimal monthlySalary,
                              ContractStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.companyId = companyId;
        this.employeeId = employeeId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlySalary = monthlySalary;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(String title, LocalDate startDate, LocalDate endDate, BigDecimal monthlySalary,
                       ContractStatus status, Instant now) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlySalary = monthlySalary;
        this.status = status;
        this.updatedAt = now;
    }
}
