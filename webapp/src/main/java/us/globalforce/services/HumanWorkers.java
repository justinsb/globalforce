//package us.globalforce.services;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//
//import javax.inject.Inject;
//import javax.persistence.EntityManager;
//
//import org.hibernate.cfg.Configuration;
//
//import us.globalforce.model.HumanWorker;
//
//import com.google.inject.persist.Transactional;
//
//public class HumanWorkers {
//    @Inject
//    private EntityManager em;
//
//    @Transactional
//    public HumanWorker find(int id) {
//        return em.find(HumanWorker.class, id);
//    }
//
//    // @Transactional
//    // public void save(MyEntity entity) {
//    // em.persist(entity);
//    // }
//
//    public Configuration getConfig() throws URISyntaxException {
//        URI dbUri = new URI(System.getenv("DATABASE_URL"));
//
//        String username = dbUri.getUserInfo().split(":")[0];
//        String password = dbUri.getUserInfo().split(":")[1];
//        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
//
//        // <property name="hibernate.ejb.naming_strategy">org.hibernate.cfg.ImprovedNamingStrategy</property>
//        // <property name="connection.pool_size">1</property>
//        // <property name="show_sql">true</property>
//        // <property name="hibernate.use_outer_join">true</property>
//        // <property name="current_session_context_class">thread</property>
//
//        Configuration config = new Configuration();
//        config.setProperty("hibernate.connection.url", dbUrl);
//        config.setProperty("hibernate.connection.username", username);
//        config.setProperty("hibernate.connection.password", password);
//
//        config.setProperty("hibernate.id.new_generator_mappings", "true");
//
//        config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
//        config.setProperty("connection.driver_class", "org.postgresql.Driver");
//
//        config.setProperty("hibernate.connection.charSet", "UTF-8");
//        config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
//
//        config.setProperty("hibernate.hbm2ddl.auto", "update");
//
//        return config;
//    }
// }
