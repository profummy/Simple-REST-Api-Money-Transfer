package org.profummy.factories;

import org.profummy.AccountController;
import org.profummy.dao.AccountDAOImpl;
import org.profummy.service.AccountService;

public class AccountControllerFactory {

    public static void create() {
        new AccountController(new AccountService(new AccountDAOImpl()));
    }

}
