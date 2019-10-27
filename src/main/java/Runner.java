import io.javalin.Javalin;
import org.hibernate.StaleObjectStateException;
import org.profummy.AccountController;
import org.profummy.factories.AccountControllerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Runner {

    public static void main(String[] args) {

        AccountControllerFactory.create();

        Javalin app = Javalin.create()
                .start(7000);

        app.routes(() -> {
            path("accounts", () -> {
                get(AccountController::getAllAccounts);
                path(":add", () -> {
                    post(AccountController::createAccount);
                });
                path(":id", () -> {
                    get(AccountController::getAccount);
                });
                path(":increasefunds", () -> {
                    path(":id", () -> {
                        path(":amount", () -> {
                            post(AccountController::increaseFunds);
                        });
                    });
                });
                path(":transfer", () -> {
                    path(":sendingid", () -> {
                        path(":receivingid", () -> {
                            path(":amount", () -> {
                                post(AccountController::transferFunds);
                            });
                        });
                    });
                });


            });
        });

        app.exception(NumberFormatException.class, ((exception, ctx) ->
                ctx.json("Invalid formatting for parameters")));

        app.exception(StaleObjectStateException.class, ((exception, ctx) ->
                ctx.json("Update already in progress.Try again later.")));

    }

}

