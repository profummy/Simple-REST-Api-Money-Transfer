package org.profummy.dao;

import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.SQLGrammarException;
import org.profummy.TransactionType;
import org.profummy.domain.Account;
import org.profummy.domain.MoneyTransaction;
import org.profummy.factories.EmFactoryMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

public class AccountDAOImpl implements IDao<Account> {

    private final Logger logger = LoggerFactory.getLogger(AccountDAOImpl.class);

    @Override
    public Account create() {

        EntityManager entityManager = EmFactoryMaker.createFactory().createEntityManager();

        EntityTransaction entityTransaction = entityManager.getTransaction();

        try {
            entityTransaction.begin();

            Account account = new Account();

            entityManager.persist(account);

            entityManager.getTransaction().commit();

            logger.info("New Account created with id: {}", account.getId());

            return account;
        } catch (EntityExistsException | IllegalArgumentException | TransactionRequiredException e) {
            try {
                entityTransaction.rollback();
            } catch (PersistenceException ex) {
                throw e;
            } finally {
                entityManager.close();
            }
            throw e;
        }
    }

    @Override
    public Optional<Account> getById(long id) {

        EntityManager entityManager = EmFactoryMaker.createFactory().createEntityManager();

        try {
            Query q = entityManager.createQuery("select a from Account a left join fetch a.moneyTransactions where a.id = :id");

            q.setParameter("id", id);

            Account account = (Account) q.getSingleResult();

            logger.info("Account with id: {} found with balance {} and transactions {}", id, account.getBalance(), account.getMoneyTransactions());

            return Optional.of(account);
        } catch (IllegalArgumentException | NoResultException | NoSuchElementException e) {
            logger.info("Account with id: {} not found", id);
            return Optional.empty();
        } finally {
            entityManager.close();
        }


    }

    @Override
    public Collection<Account> getAll() {

        EntityManager entityManager = EmFactoryMaker.createFactory().createEntityManager();

        try {
            Query q = entityManager.createQuery("select a from Account a left join fetch a.moneyTransactions", Account.class);

            logger.info("All accounts found");

            return q.getResultList();
        } catch (IllegalArgumentException | QueryTimeoutException | SQLGrammarException e) {
            throw e;
        } finally {
            entityManager.close();
        }

    }

    //To transfer funds between accounts
    @Override
    public void update(long sendingAccountId, long receivingAccountId, BigDecimal amount) {

        EntityManager entityManager = EmFactoryMaker.createFactory().createEntityManager();

        EntityTransaction entityTransaction = entityManager.getTransaction();

        try {
            entityTransaction.begin();

            Account sendingAccount = getById(sendingAccountId).get();
            Account receivingAccount = getById(receivingAccountId).get();

            BigDecimal sendingAccountOldBalance = sendingAccount.getBalance();
            BigDecimal receivingAccountOldBalance = receivingAccount.getBalance();

            MoneyTransaction moneyTransactionDebit = new MoneyTransaction(amount, sendingAccountId, receivingAccountId, TransactionType.DEBIT);
            MoneyTransaction moneyTransactionCredit = new MoneyTransaction(amount, sendingAccountId, receivingAccountId, TransactionType.CREDIT);

            sendingAccount.transferFunds(receivingAccount, moneyTransactionDebit, moneyTransactionCredit);

            entityManager.persist(moneyTransactionDebit);
            entityManager.persist(moneyTransactionCredit);

            entityManager.merge(sendingAccount);
            entityManager.merge(receivingAccount);

            entityTransaction.commit();

            logger.info("Account with Id {} debited with amount {}. New balance is {}. Old balance was {}", sendingAccount, amount, sendingAccount.getBalance(), sendingAccountOldBalance);
            logger.info("Account with Id {} credited with amount {}. New balance is {}. Old balance was {}", receivingAccount, amount, receivingAccount.getBalance(), receivingAccountOldBalance);

        } catch (IllegalStateException | TransactionRequiredException | OptimisticLockException| StaleObjectStateException |RollbackException e) {
            try {
                entityTransaction.rollback();
            } catch (PersistenceException ex) {
                throw e;
            } finally {
                entityManager.close();
            }
            throw e;
        }

    }

    //To increase funds in one account
    @Override
    public void update(long receivingAccountId, BigDecimal amount) {

        EntityManager entityManager = EmFactoryMaker.createFactory().createEntityManager();

        EntityTransaction entityTransaction = entityManager.getTransaction();

        try {
            entityTransaction.begin();

            Account receivingAccount = getById(receivingAccountId).get();

            BigDecimal oldBalance = receivingAccount.getBalance();

            MoneyTransaction moneyTransaction = new MoneyTransaction(amount, receivingAccountId);

            receivingAccount.increaseFunds(moneyTransaction);

            entityManager.persist(moneyTransaction);

            entityManager.merge(receivingAccount);

            entityTransaction.commit();

            logger.info("Account with Id {} credited with amount {}. New balance is {}. Old balance was {} ", receivingAccountId, amount, receivingAccount.getBalance(), oldBalance);

        } catch (IllegalStateException | TransactionRequiredException | RollbackException | NoSuchElementException e) {
            try {
                entityTransaction.rollback();
            } catch (PersistenceException ex) {
                throw e;
            } finally {
                entityManager.close();
            }
            throw e;

        }


    }
}

