import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.profummy.dao.AccountDAOImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerSetup {

    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;
    static AccountDAOImpl accountDAOImpl;


    @BeforeEach
    public void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("bankTest");
        entityManager = entityManagerFactory.createEntityManager();
        accountDAOImpl = new AccountDAOImpl();

    }

    @AfterEach
    public void deInit(){
        accountDAOImpl = null;
        entityManager.clear();
        entityManager.close();
        entityManagerFactory.close();
    }

}
