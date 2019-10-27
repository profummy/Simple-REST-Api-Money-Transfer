import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.profummy.TransactionType;
import org.profummy.dao.IDao;
import org.profummy.domain.Account;
import org.profummy.domain.MoneyTransaction;
import org.profummy.service.AccountService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

class AccountServiceTests {

    @Mock
    IDao iDao;

    @InjectMocks
    AccountService accountService;

    @BeforeEach
    void initMock() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createAccountShouldCreateAnAccount() {

        Account account = new Account();

        when(iDao.create()).thenReturn(new Account());

        assertEquals(account, accountService.createAccount());
    }

    @Test
    void findAllAccountsShouldReturnCurrentCollectionOfAccounts() {
        Account account = new Account();

        Collection<Account> list = new ArrayList<>();

        list.add(account);

        Collection<Account> insertedList = new ArrayList<>();

        insertedList.add(account);

        when(iDao.getAll()).thenReturn(insertedList);

        assertEquals(list, accountService.findAllAccounts());

        assertEquals(list.size(), accountService.findAllAccounts().size());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, Long.MAX_VALUE})
    void getByIdShouldFindAccountWithCorrectIdIfExists(long id) {

        Account account = new Account();

        Optional<Account> optionalAccount = Optional.of(account);

        when(iDao.getById(id)).thenReturn(optionalAccount);

        assertEquals(Optional.of(account), accountService.findAccount(id));
    }

    @ParameterizedTest
    @ValueSource(longs = {55, Long.MAX_VALUE})
    void findAccountShouldReturnEmptyOptionalIfIdIfDoesNotExist(long id) {

        Optional<Account> optionalAccount = Optional.empty();

        when(iDao.getById(id)).thenReturn(Optional.empty());

        assertEquals(optionalAccount, accountService.findAccount(id));
    }

    @Test
    void increaseFundsShouldIncreaseBalanceOfReceivingAccount() {

        Account receivingAccount = new Account();

        BigDecimal amount = new BigDecimal("100.00");

        doAnswer(invocation -> {
            Long accountId = invocation.getArgument(0);
            BigDecimal amountSent = invocation.getArgument(1);

            MoneyTransaction moneyTransaction = new MoneyTransaction(amountSent, accountId);

            assertEquals((Long) receivingAccount.getId(), invocation.getArgument(0));
            assertEquals(receivingAccount.getBalance(), invocation.getArgument(1));

            receivingAccount.increaseFunds(moneyTransaction);

            return null;
        }).when(iDao).update(anyLong(), any(BigDecimal.class));

        accountService.increaseFunds(receivingAccount.getId(), amount);

        BigDecimal amountAfterIncrease = new BigDecimal("200.00");

        assertEquals(amountAfterIncrease, receivingAccount.getBalance());

    }

    @Test
    void transferFundsShouldIncreaseBalanceOfReceivingAccountAndDecreaseBalanceOfSendingAccount() {

        Account receivingAccount = new Account();
        Account sendingAccount = new Account();

        BigDecimal amount = new BigDecimal("100.00");

        doAnswer(invocation -> {
            Long sendingAccountId = invocation.getArgument(0);
            Long receivingAccountId = invocation.getArgument(1);
            BigDecimal amountSent = invocation.getArgument(2);

            MoneyTransaction moneyTransactionDebit = new MoneyTransaction(amountSent, sendingAccountId, receivingAccountId, TransactionType.DEBIT);
            MoneyTransaction moneyTransactionCredit = new MoneyTransaction(amountSent, sendingAccountId, receivingAccountId, TransactionType.CREDIT);

            assertEquals((Long) sendingAccount.getId(), invocation.getArgument(0));
            assertEquals((Long) receivingAccount.getId(), invocation.getArgument(1));
            assertEquals(moneyTransactionCredit.getAmount(), invocation.getArgument(2));

            sendingAccount.transferFunds(receivingAccount, moneyTransactionDebit, moneyTransactionCredit);

            return null;
        }).when(iDao).update(anyLong(), anyLong(), any(BigDecimal.class));

        accountService.transferFunds(sendingAccount.getId(), receivingAccount.getId(), amount);


        BigDecimal amountAfterIncrease = new BigDecimal("200.00");
        BigDecimal amountAfterDecrease = new BigDecimal("00.00");


        assertEquals(amountAfterIncrease, receivingAccount.getBalance());
        assertEquals(amountAfterDecrease, sendingAccount.getBalance());


    }

}
