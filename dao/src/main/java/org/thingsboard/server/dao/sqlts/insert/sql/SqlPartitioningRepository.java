package org.thingsboard.server.dao.sqlts.insert.sql;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dao.timeseries.SqlPartition;
import org.thingsboard.server.dao.util.SqlTsDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
@Transactional
public class SqlPartitioningRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(SqlPartition partition) {
        entityManager.createNativeQuery(partition.getQuery()).executeUpdate();
    }

}
