package us.globalforce.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.model.HumanWorker;

import com.fathomdb.jdbc.JdbcConnection;
import com.fathomdb.jdbc.JdbcTransaction;
import com.fathomdb.jpa.Query;
import com.fathomdb.jpa.QueryFactory;

@Singleton
public class JdbcRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcRepository.class);

    @Inject
    Provider<JdbcConnection> connectionProvider;

    @Inject
    QueryFactory queryFactory;

    final Random random = new Random();

    static interface Queries {
        @Query("SELECT * FROM human_worker")
        List<HumanWorker> listHumanWorkers();
    }

    @JdbcTransaction
    public List<HumanWorker> listHumanWorkers() throws SQLException {
        Queries queries = queryFactory.get(Queries.class);
        return queries.listHumanWorkers();
    }

}
