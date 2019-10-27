package org.profummy.service;

import org.profummy.dao.IDao;
import org.profummy.domain.Account;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public class AccountService {

    private IDao<Account> iDao;

    public AccountService(IDao<Account> iDao) {
        this.iDao = iDao;
    }

    public Account createAccount() {
        return iDao.create();
    }

    public Collection<Account> findAllAccounts() {
        return iDao.getAll();
    }

    public Optional<Account> findAccount(long id) {
        return iDao.getById(id);
    }

    public void increaseFunds(long receivingAccountId, BigDecimal amount) {
        iDao.update(receivingAccountId, amount);
    }

    public void transferFunds(long sendingAccountId, long receivingAccountId, BigDecimal amount) {
        iDao.update(sendingAccountId, receivingAccountId, amount);
    }

}
