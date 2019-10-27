import org.junit.jupiter.api.Test;
import org.profummy.domain.Account;

import javax.persistence.RollbackException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AccountDAOImplTests extends EntityManagerSetup {


    @Test
    void createAccountCreatesOneAccount() {

        accountDAOImpl.create();

        Collection<Account> accounts = new HashSet<>(accountDAOImpl.getAll());

        assertNotNull(accounts);

        assertEquals(1, accounts.size());

    }

    @Test
    void createAccountCreatesDifferentAccountsWhenCalledAgain() {

        accountDAOImpl.create();
        accountDAOImpl.create();

        Collection<Account> accounts = new HashSet<>(accountDAOImpl.getAll());

        assertNotNull(accounts);

        assertEquals(2, accounts.size());

    }

    @Test
    void getByIdGetsExistingAccountWithCorrectId() {

        accountDAOImpl.create();

        Optional<Account> account = accountDAOImpl.getById(1);

        assertEquals(1, account.get().getId());

    }

    @Test
    void getByIdGetsEmptyOptionalObjectWhenIdDoesNotExist() {

        accountDAOImpl.create();

        Optional<Account> account = accountDAOImpl.getById(2);

        assertEquals(Optional.empty(), account);

    }

    @Test
    void getAllAccountsReturnsEmptyResultListWhenNoAccountsExist() {

        assertTrue(accountDAOImpl.getAll().isEmpty());

    }

    @Test
    void increaseFundsIncreasesBalanceOfAccountWithCorrectAmount() {

        accountDAOImpl.create();

        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal expectedAmount = new BigDecimal("200.00");

        accountDAOImpl.update(1, amount);

        Account account = accountDAOImpl.getById(1).get();

        assertEquals(expectedAmount, account.getBalance());

    }

    @Test
    void transferFundsIncreasesBalanceOfReceivingAccountAndDecreasesBalanceOfSendingAccountWithCorrectAmount() {

        accountDAOImpl.create();
        accountDAOImpl.create();

        BigDecimal sendingAmount = new BigDecimal("50.00");

        BigDecimal expectedSendingBalanceAfterTransfer = new BigDecimal("50.00");
        BigDecimal expectedReceivingBalanceAfterTransfer = new BigDecimal("150.00");

        accountDAOImpl.update(1, 2, sendingAmount);

        Account sendingAccount = accountDAOImpl.getById(1).get();
        Account receivingAccount = accountDAOImpl.getById(2).get();

        assertEquals(expectedSendingBalanceAfterTransfer, sendingAccount.getBalance());
        assertEquals(expectedReceivingBalanceAfterTransfer, receivingAccount.getBalance());

    }

    @Test
    void checkFundsAreSufficientToTransfer() {

        accountDAOImpl.create();

        BigDecimal sendingAmount = new BigDecimal("50.00");

        Account sendingAccount = accountDAOImpl.getById(1).get();

        sendingAccount.checkSufficientFunds(sendingAccount.getBalance(), sendingAmount);

        assertFalse(sendingAccount.checkSufficientFunds(sendingAccount.getBalance(), sendingAmount));

    }


    /*
    Optimistic locking test using versioning of Account
    RollbackException caused by OptimisticLockException caused by StaleObjectStateException
    RollbackException caused due to the implementation of method in AccountDAOImpl as protection
    Could not get test to pass consistently :)
     */
    @Test
    void checkMultipleTransfersOnSameAccountThrowRollbackException() {

        accountDAOImpl.create();
        accountDAOImpl.create();

        BigDecimal sendingAmount = new BigDecimal("50.00");

        Runnable runnable = () -> accountDAOImpl.update(1, 2, sendingAmount);

        Thread thread1 = new Thread(runnable, "Transfer Request 1");
        thread1.start();

        Runnable runnable2 = () ->

        {
            try {
                accountDAOImpl.update(1, 2, sendingAmount);
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread thread2 = new Thread(runnable2, "Transfer Request 2");
        thread2.start();


        assertThrows(RollbackException.class, () -> {
        accountDAOImpl.update(1, 2, sendingAmount);
        });


    }

}
