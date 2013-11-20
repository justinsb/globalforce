package us.globalforce.services;

import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.model.Credential;
import us.globalforce.model.Task;

import com.fathomdb.jdbc.JdbcConnection;
import com.fathomdb.jdbc.JdbcTransaction;
import com.fathomdb.jpa.Query;
import com.fathomdb.jpa.QueryFactory;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Singleton
public class JdbcRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcRepository.class);

    @Inject
    Provider<JdbcConnection> connectionProvider;

    @Inject
    QueryFactory queryFactory;

    final Random random = new Random();

    static interface Queries {
        // @Query("SELECT * FROM human_worker")
        // List<HumanWorker> listHumanWorkers();

        @Query("SELECT * FROM task WHERE organization=? AND problem=? AND objectid=?")
        List<Task> listTasks(String organization, String problem, String objectId);

        @Query("SELECT * FROM task WHERE organization=? AND problem=? AND objectid=? AND sequence=?")
        List<Task> listTasks(String organizaztion, String problem, String objectId, int sequence);

        @Query("SELECT * FROM task WHERE worker is null AND organization=?")
        List<Task> listAllOpenTasks(String organizationId);

        @Query("SELECT * FROM task WHERE organization=? AND worker is null and id <= ? LIMIT 1")
        List<Task> firstOpenTaskLTE(String organizationId, long pivot);

        @Query("SELECT * FROM task WHERE organization=? AND worker is null and id > ? LIMIT 1")
        List<Task> firstOpenTaskGT(String organizationId, long pivot);

        @Query(Query.AUTOMATIC_INSERT)
        void insertTask(Task task);

        @Query(Query.AUTOMATIC_UPDATE)
        void updateTask(Task task);

        @Query(Query.AUTOMATIC_INSERT)
        void insert(Credential credential);

        @Query("SELECT * FROM credential WHERE organization=? ORDER BY created DESC LIMIT 1")
        List<Credential> findNewestCredentials(String organization);

        @Query("SELECT DISTINCT organization FROM credential")
        List<String> findAllCredentialOrganizations();
    }

    // @JdbcTransaction
    // public List<HumanWorker> listHumanWorkers() throws SQLException {
    // Queries queries = queryFactory.get(Queries.class);
    // return queries.listHumanWorkers();
    // }

    @JdbcTransaction
    public List<Task> listTasks(String organizationId, String problemKey, String objectId) {
        Queries queries = queryFactory.get(Queries.class);
        return queries.listTasks(organizationId, problemKey, objectId);
    }

    @JdbcTransaction
    public List<Task> listTasks(String organizationId, ProblemType problem, String objectId) {
        return listTasks(organizationId, problem.getKey(), objectId);
    }

    @JdbcTransaction
    public long addTask(String organizationId, ProblemType problem, String sfClass, String objectId, int sequence,
            String sentence) {
        Queries queries = queryFactory.get(Queries.class);
        Task task = new Task();
        task.organization = organizationId;
        task.id = generateRandomInt64();
        task.problem = problem.getKey();
        task.sfClass = sfClass;
        task.objectId = objectId;
        task.sequence = sequence;
        task.input = sentence;
        queries.insertTask(task);

        return task.id;
    }

    @JdbcTransaction
    public Credential insertCredential(Credential credential) {
        Queries queries = queryFactory.get(Queries.class);
        credential.createdAt = System.currentTimeMillis() / 1000L;
        credential.id = generateRandomInt64();
        queries.insert(credential);

        return credential;
    }

    @JdbcTransaction
    public Credential findCredential(String organization) {
        Queries queries = queryFactory.get(Queries.class);
        List<Credential> credentials = queries.findNewestCredentials(organization);
        if (credentials.isEmpty()) {
            return null;
        }
        return credentials.get(0);
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
    public List<Task> listAllOpenTasks(String organizationId) {
        Queries queries = queryFactory.get(Queries.class);
        return queries.listAllOpenTasks(organizationId);
    }

    @JdbcTransaction
    public Task assignTask(String organizationId) {
        Queries queries = queryFactory.get(Queries.class);

        return assignTask(queries, organizationId);
    }

    private Task assignTask(Queries queries, String organizationId) {
        long pivot = generateRandomInt64();
        List<Task> tasks;

        tasks = queries.firstOpenTaskGT(organizationId, pivot);
        if (!tasks.isEmpty()) {
            return Iterables.getFirst(tasks, null);
        }

        tasks = queries.firstOpenTaskLTE(organizationId, pivot);
        if (!tasks.isEmpty()) {
            return Iterables.getFirst(tasks, null);
        }

        return null;
    }

    @JdbcTransaction
    public Task addTaskDecision(Task decision) {
        Queries queries = queryFactory.get(Queries.class);

        List<Task> existing = queries.listTasks(decision.organization, decision.problem, decision.objectId,
                decision.sequence);

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

    @JdbcTransaction
    public List<String> findAllCredentialOrganizations() {
        Queries queries = queryFactory.get(Queries.class);
        return queries.findAllCredentialOrganizations();

    }

    @JdbcTransaction
    public List<Task> assignTasks(String organizationId, int n, List<Long> veto) {
        Queries queries = queryFactory.get(Queries.class);

        List<Task> tasks = Lists.newArrayList();

        Set<Long> ids = Sets.newHashSet();

        for (Long v : veto) {
            ids.add(v);
        }

        for (int i = 0; i < n * 2; i++) {
            Task task = assignTask(queries, organizationId);
            if (task == null) {
                break;
            }

            if (ids.contains(task.id)) {
                continue;
            }

            tasks.add(task);
            ids.add(task.id);

            if (tasks.size() >= n) {
                break;
            }
        }

        return tasks;
    }

}
