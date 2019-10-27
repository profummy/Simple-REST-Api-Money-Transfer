package org.profummy;

import io.javalin.http.Context;
import org.profummy.domain.Account;
import org.profummy.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private static AccountService accountService;

    public AccountController(AccountService accountService) {
        AccountController.accountService = accountService;
    }

    public static void createAccount(Context ctx) {

        Account account = accountService.createAccount();

        ctx.status(201);

        ctx.json("New Account created! Id: " + account.getId() + " Balance: " + account.getBalance());

    }

    public static void getAllAccounts(Context ctx) {

        Collection<Account> accounts = new HashSet<>(accountService.findAllAccounts());

        if (accounts.isEmpty()) {

            ctx.json("There are no accounts available!");

        } else {

            ctx.json(accounts);
        }
    }

    public static void getAccount(Context ctx) {

        long accountId = Long.parseLong(ctx.pathParam("id"));

        if (accountService.findAccount(accountId).isEmpty()) {

            ctx.json("There are no accounts with id: " + accountId);

        } else {

            Account account = accountService.findAccount(accountId).get();

            ctx.json("Here is balance for account id " + account.getId() + ": " + account.getBalance() + " Past Transactions: " + account.getMoneyTransactions());
        }
    }

    public static void increaseFunds(Context ctx) {

        long accountId = Long.parseLong(ctx.pathParam("id"));
        BigDecimal amount = new BigDecimal(ctx.pathParam("amount"));

        if (accountService.findAccount(accountId).isEmpty()) {

            ctx.json("There are no accounts with id: " + accountId);

        } else if (amount.compareTo(BigDecimal.valueOf(0.01)) < 0 || amount.compareTo(BigDecimal.valueOf(1000000.00)) > 0) {

            ctx.json(amount + " is not a valid amount. Must be between 0.01 and 1000000.00");

        } else {

            accountService.increaseFunds(accountId, amount);

            ctx.json("Account with id: " + accountId + " credited with " + amount);

        }

    }

    public static void transferFunds(Context ctx) {

        long sendingAccountId = Long.parseLong(ctx.pathParam("sendingid"));

        long receivingAccountId = Long.parseLong(ctx.pathParam("receivingid"));

        BigDecimal amount = new BigDecimal(ctx.pathParam("amount"));

        Optional<Account> sendingAccount = accountService.findAccount(sendingAccountId);

        if (accountService.findAccount(sendingAccountId).isEmpty()) {

            ctx.json("There are no accounts with id: " + sendingAccountId);

        } else if (sendingAccount.get().checkSufficientFunds(sendingAccount.get().getBalance(), amount)) {

            BigDecimal sendingAccountBalance = sendingAccount.get().getBalance();

            ctx.json("Insufficient funds! Sending account balance: " + sendingAccountBalance + " Amount to send: " + amount);

            logger.info("Insufficient funds! Sending account balance: {} . Amount to send: {}", sendingAccountBalance, amount);

        } else if (accountService.findAccount(receivingAccountId).isEmpty()) {

            ctx.json("There are no accounts with id: " + receivingAccountId);

        } else if (amount.compareTo(BigDecimal.valueOf(0.01)) < 0 || amount.compareTo(BigDecimal.valueOf(1000000.00)) > 0) {


            ctx.json(amount + " is not a valid amount. Must be between 0.01 and 1000000.00");
        } else {

            accountService.transferFunds(sendingAccountId, receivingAccountId, amount);

            ctx.json("Account with id: " + sendingAccountId + " credited account with id: " + receivingAccountId + " with " + amount);

        }

    }

}
