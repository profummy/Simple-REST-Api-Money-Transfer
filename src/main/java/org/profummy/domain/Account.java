package org.profummy.domain;

import org.profummy.TransactionType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Account {

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private BigDecimal balance;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable
    @OrderBy(value = "timeStamp")
    private Set<MoneyTransaction> moneyTransactions;

    @Version
    private long version;

    public Account() {
        this.balance = new BigDecimal("100.00");
        this.moneyTransactions = new HashSet<>();
    }

    //increase funds of one account
    public void increaseFunds(MoneyTransaction moneyTransaction) {
        moneyTransactions.add(moneyTransaction);
        this.balance = this.balance.add(moneyTransaction.getAmount());
        moneyTransaction.getAccounts().add(this);
    }

    //transfer between two accounts (increase in one, decrease in the other)
    public void transferFunds(Account receivingAccount, MoneyTransaction moneyTransactionDebit, MoneyTransaction moneyTransactionCredit) {

        moneyTransactionDebit.setTransactionType(TransactionType.DEBIT);

        this.balance = this.balance.subtract(moneyTransactionDebit.getAmount());

        moneyTransactions.add(moneyTransactionDebit);

        moneyTransactionDebit.getAccounts().add(this);

        moneyTransactionCredit.setTransactionType(TransactionType.CREDIT);

        receivingAccount.increaseFunds(moneyTransactionCredit);

        receivingAccount.getMoneyTransactions().add(moneyTransactionCredit);

        moneyTransactionCredit.getAccounts().add(receivingAccount);

    }

    public boolean checkSufficientFunds(BigDecimal balance, BigDecimal sendingAmount) {
        return sendingAmount.compareTo(balance) > 0;
    }

    public long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Set<MoneyTransaction> getMoneyTransactions() {
        return moneyTransactions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id == account.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "com.profummy.com.profummy.dao.dao.Account{" +
                "id=" + id +
                ", balance=" + balance +
                '}';
    }
}
