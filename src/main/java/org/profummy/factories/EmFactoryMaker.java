package org.profummy.factories;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EmFactoryMaker {

    private static final String PERSISTENCE_UNIT_NAME = "bank";
    private static EntityManagerFactory factory;

    public static EntityManagerFactory createFactory() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return factory;
    }

}
