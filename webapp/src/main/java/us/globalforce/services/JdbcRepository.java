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
import us.globalforce.model.Task;

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

        @Query("SELECT * FROM task WHERE problem=? AND objectid=?")
        List<Task> listTasks(String problem, String objectId);

        @Query(Query.AUTOMATIC_INSERT)
        void insertTask(Task task);
    }

    @JdbcTransaction
    public List<HumanWorker> listHumanWorkers() throws SQLException {
        Queries queries = queryFactory.get(Queries.class);
        return queries.listHumanWorkers();
    }

    @JdbcTransaction
    public List<Task> listTasks(ProblemType problem, String objectId) {
        Queries queries = queryFactory.get(Queries.class);
        return queries.listTasks(problem.getKey(), objectId);
    }

    @JdbcTransaction
    public long addTask(ProblemType problem, String objectId, int sequence, String sentence) {
        Queries queries = queryFactory.get(Queries.class);
        Task task = new Task();
        task.id = generateRandomInt64();
        task.problem = problem.getKey();
        task.objectId = objectId;
        task.sequence = sequence;
        task.input = sentence;
        queries.insertTask(task);

        return task.id;
    }

    private long generateRandomInt64() {
        long v;
        synchronized (random) {
            v = random.nextLong();
        }
        if (v == 0) {
            return generateRandomInt64();
        }
        if (v < 0) {
            v = -v;
        }
        return v;
    }
}
