package org.profummy.dao;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public interface IDao<T> {

    T create();

    Optional<T> getById(long id);

    Collection<T> getAll();

    void update(long sendingAccountId, long receivingAccountId, BigDecimal amount);

    void update(long receivingAccountId, BigDecimal amount);

}
