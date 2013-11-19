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
import com.google.common.collect.Iterables;

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

        @Query("SELECT * FROM task WHERE problem=? AND objectid=? AND sequence=?")
        List<Task> listTasks(String problem, String objectId, int sequence);

        @Query("SELECT * FROM task WHERE worker is null")
        List<Task> listAllOpenTasks();

        @Query("SELECT * FROM task WHERE worker is null and id < ? LIMIT 1")
        List<Task> firstOpenTaskLT(long pivot);

        @Query("SELECT * FROM task WHERE worker is null and id > ? LIMIT 1")
        List<Task> firstOpenTaskGT(long pivot);

        @Query(Query.AUTOMATIC_INSERT)
        void insertTask(Task task);

        @Query(Query.AUTOMATIC_UPDATE)
        void updateTask(Task task);
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

    @JdbcTransaction
    public List<Task> listAllOpenTasks() {
        Queries queries = queryFactory.get(Queries.class);
        return queries.listAllOpenTasks();
    }

    @JdbcTransaction
    public Task assignTask() {
        Queries queries = queryFactory.get(Queries.class);

        long pivot = generateRandomInt64();
        List<Task> tasks;

        tasks = queries.firstOpenTaskGT(pivot);
        if (!tasks.isEmpty()) {
            return Iterables.getFirst(tasks, null);
        }

        tasks = queries.firstOpenTaskLT(pivot);
        if (!tasks.isEmpty()) {
            return Iterables.getFirst(tasks, null);
        }

        return null;
    }

    @JdbcTransaction
    public Task addTaskDecision(Task decision) {
        Queries queries = queryFactory.get(Queries.class);

        List<Task> existing = queries.listTasks(decision.problem, decision.objectId, decision.sequence);

        if (existing.isEmpty()) {
            decision.id = generateRandomInt64();
            queries.insertTask(decision);
            return decision;
        }

        Task matching = null;
        for (Task task : existing) {
            if (task.worker != null && task.worker.equals(decision.worker)) {
                matching = task;
                break;
            }
            if (matching == null && task.worker == null) {
                matching = task;
            }
        }

        if (matching == null) {
            decision.id = generateRandomInt64();
            queries.insertTask(decision);
        } else {
            decision.id = matching.id;
            queries.updateTask(decision);
        }

        return decision;
    }
}
