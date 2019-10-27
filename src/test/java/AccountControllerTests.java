import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.profummy.AccountController;
import org.profummy.domain.Account;
import org.profummy.service.AccountService;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class AccountControllerTests {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private Context context;

    @BeforeEach
    void initMock() {

        MockitoAnnotations.initMocks(this);

        context = mock(Context.class);

    }

    @Test
    void verifyCreateAccountReturns201AndJsonWithAccountDetails() {

        Account account = new Account();

        when(accountService.createAccount()).thenReturn(account);

        AccountController.createAccount(context);

        verify(context).status(201);

        verify(context).json("New Account created! Id: " + account.getId() + " Balance: " + account.getBalance());
    }

    @Test
    void verifyGetAllAccountsReturnsJsonMessageWhenNoAccountsExist() {

        Set<Account> accounts = new HashSet<>();

        when(accountService.findAllAccounts()).thenReturn(accounts);

        AccountController.getAllAccounts(context);

        verify(context).json("There are no accounts available!");

    }

    @Test
    void verifyGetAllAccountsReturnsJsonOfAllExistingAccounts() {

        Set<Account> accounts = new HashSet<>();

        Account account = new Account();
        Account account2 = new Account();

        when(accountService.findAllAccounts()).thenReturn(accounts);

        accounts.add(account);
        accounts.add(account2);

        AccountController.getAllAccounts(context);

        verify(context).json(accounts);

    }

    @Test
    void verifyGetAccountReturnsJsonMessageWhenNoAccountsExistWithId() {

        String stringId = "9898003";

        long id = 9898003L;

        when(context.pathParam(anyString())).thenReturn(stringId);

        when(accountService.findAccount(id)).thenReturn(Optional.empty());

        AccountController.getAccount(context);

        verify(context).json("There are no accounts with id: " + id);

    }

    @Test
    void verifyGetAccountReturnsJsonWithAccountInformationWhenAccountIsFound() {

        String stringId = "0";

        long id = 0;

        Account account = new Account();

        when(context.pathParam(anyString())).thenReturn(stringId);

        when(accountService.findAccount(id)).thenReturn(Optional.of(account));

        AccountController.getAccount(context);

        verify(context).json("Here is balance for account id " + account.getId() + ": " + account.getBalance() + " Past Transactions: " + account.getMoneyTransactions());

    }

    @Test
    void verifyIncreaseFundsReturnsJsonMessageWhenNoAccountsExistWithId() {

        String stringId = "9898003";

        long id = 9898003L;

        when(context.pathParam(anyString())).thenReturn(stringId);

        when(accountService.findAccount(id)).thenReturn(Optional.empty());

        AccountController.increaseFunds(context);

        verify(context).json("There are no accounts with id: " + id);

    }

    @Test
    void verifyIncreaseFundsReturnsJsonMessageWhenAmountIsNotValid() {

        String stringId = "9898003";

        long id = 9898003L;

        Account account = new Account();

        BigDecimal amount = new BigDecimal("2000000");

        when(context.pathParam(anyString())).thenReturn(stringId, amount.toString());

        when(accountService.findAccount(id)).thenReturn(Optional.of(account));

        AccountController.increaseFunds(context);

        verify(context, times(2)).pathParam(anyString());

        verify(context).json(amount.toString() + " is not a valid amount. Must be between 0.01 and 1000000.00");

    }

    @Test
    void verifyIncreaseFundsReturnsJsonMessageWithCreditDetailsWhenAmountIsValid() {

        String stringId = "9898003";

        long id = 9898003L;

        Account account = new Account();

        BigDecimal amount = new BigDecimal("2000");

        when(context.pathParam(anyString())).thenReturn(stringId, amount.toString());

        when(accountService.findAccount(id)).thenReturn(Optional.of(account));

        AccountController.increaseFunds(context);

        verify(context, times(2)).pathParam(anyString());

        verify(context).json("Account with id: " + id + " credited with " + amount);

    }

    @Test
    void verifyTransferFundsReturnsJsonMessageWhenNoAccountsExistWithId() {

        String stringId = "9898003";

        long id = 9898003L;

        Account account = new Account();

        when(context.pathParam(anyString())).thenReturn(stringId);

        when(accountService.findAccount(id)).thenReturn(Optional.of(account), Optional.empty());

        AccountController.transferFunds(context);

        verify(context).json("There are no accounts with id: " + id);

    }

    @Test
    void verifyTransferFundsReturnsJsonMessageWhenSendingAccountHasInsufficientFunds() {

        String stringSendingId = "9898003";

        long sendingId = 9898003L;

        String stringReceivingId = "1";

        Account account = new Account();

        BigDecimal amount = new BigDecimal("2000");

        when(context.pathParam(anyString())).thenReturn(stringSendingId, stringReceivingId, amount.toString());

        when(accountService.findAccount(sendingId)).thenReturn(Optional.of(account));

        AccountController.transferFunds(context);

        verify(context).json("Insufficient funds! Sending account balance: " + account.getBalance() + " Amount to send: " + amount);

    }

    @Test
    void verifyTransferFundsReturnsJsonMessageWhenReceivingAccountDoesNotExist() {

        String stringSendingId = "9898003";

        long sendingId = 9898003L;

        String stringReceivingId = "1";

        long receivingId = 1;

        Account account = new Account();

        BigDecimal amount = new BigDecimal("50");

        when(context.pathParam(anyString())).thenReturn(stringSendingId, stringReceivingId, amount.toString());

        when(accountService.findAccount(sendingId)).thenReturn(Optional.of(account));

        when(accountService.findAccount(receivingId)).thenReturn(Optional.empty());

        AccountController.transferFunds(context);

        verify(context).json("There are no accounts with id: " + receivingId);

    }

    @Test
    void verifyTransferFundsReturnsJsonMessageWhenSendingAmountIsNotValid() {

        String stringSendingId = "9898003";

        long sendingId = 9898003L;

        String stringReceivingId = "1";

        long receivingId = 1;

        Account account = new Account();

        BigDecimal amount = new BigDecimal("0.00");

        when(context.pathParam(anyString())).thenReturn(stringSendingId, stringReceivingId, amount.toString());

        when(accountService.findAccount(sendingId)).thenReturn(Optional.of(account));

        when(accountService.findAccount(receivingId)).thenReturn(Optional.of(account));

        AccountController.transferFunds(context);

        verify(context).json(amount + " is not a valid amount. Must be between 0.01 and 1000000.00");

    }

    @Test
    void verifyTransferFundsReturnsJsonMessageWithTransferDetailsWhenSuccessful() {

        String stringSendingId = "9898003";

        long sendingId = 9898003L;

        String stringReceivingId = "1";

        long receivingId = 1;

        Account account = new Account();

        BigDecimal amount = new BigDecimal("50");

        when(context.pathParam(anyString())).thenReturn(stringSendingId, stringReceivingId, amount.toString());

        when(accountService.findAccount(sendingId)).thenReturn(Optional.of(account));

        when(accountService.findAccount(receivingId)).thenReturn(Optional.of(account));

        AccountController.transferFunds(context);

        verify(context).json("Account with id: " + sendingId + " credited account with id: " + receivingId + " with " + amount);

    }

}
