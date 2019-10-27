package org.profummy.domain;

import org.profummy.TransactionType;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class MoneyTransaction {

    @Id
    @GeneratedValue
    private long id;

    @NotEmpty(message = "Amount cannot be empty ")
    @DecimalMin(message = " Amount must be at least 0.01", value = "0.01")
    @DecimalMax(message = "Amount must be at most 1,000,000.00", value = "1,000,000.00")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal amount;

    private long sendingAccountId;

    @NotNull(message = "Receiving account Id cannot be null")
    private long receivingAccountId;

    @Column(nullable = false)
    private LocalDateTime timeStamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @ManyToMany(mappedBy = "moneyTransactions")
    private Set<Account> accounts = new HashSet<>();

    @Version
    private long version;

    public MoneyTransaction() {
    }

    public MoneyTransaction(BigDecimal amount, long sendingAccountId, long receivingAccountId, TransactionType transactionType) {
        this.amount = amount;
        this.sendingAccountId = sendingAccountId;
        this.receivingAccountId = receivingAccountId;
        this.transactionType = transactionType;
        this.timeStamp = LocalDateTime.now();
    }

    //For creating a transaction to single account
    public MoneyTransaction(BigDecimal amount,long receivingAccountId) {
        this.amount = amount;
        this.receivingAccountId = receivingAccountId;
        this.transactionType = TransactionType.CREDIT;
        this.timeStamp = LocalDateTime.now();
    }

    void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    Set<Account> getAccounts() {
        return accounts;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoneyTransaction that = (MoneyTransaction) o;
        return id == that.id &&
                sendingAccountId == that.sendingAccountId &&
                receivingAccountId == that.receivingAccountId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sendingAccountId, receivingAccountId);
    }

    @Override
    public String toString() {
        return "com.profummy.com.profummy.dao.dao.MoneyTransaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", sendingAccountId=" + sendingAccountId +
                ", receivingAccountId=" + receivingAccountId +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
