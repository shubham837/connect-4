package gameplay.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.core.CassandraTemplate;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Configuration
public class CassandraConfig {
    public static final String CASSANDRA_URL = "cassandra_url";

    private static final Pattern PATTERN = Pattern.compile("^([^:]+):(\\d+)/(\\w+)$");
    private static final Logger log = LoggerFactory.getLogger(CassandraConfig.class);

    @Autowired
    private Environment env;

    private String host;
    private int port;
    private String keyspace;

    private Session session;

    @Bean
    public CassandraTemplate cassandraTemplate() {
        if (!this.isCassandraConfigProvided()) {
            return null;
        }
        log.info("Cassandra Host: " + host + " Port: " + port + " Keyspace: " + keyspace);
        Cluster cluster = Cluster.builder().addContactPointsWithPorts(Collections.singletonList(new InetSocketAddress(host, port))).build();
        session = cluster.connect();

        session.execute("USE " + keyspace);

        CassandraTemplate cassandraTemplate = new CassandraTemplate(session);
        return cassandraTemplate;
    }

    @PreDestroy
    protected void destroy() {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    protected boolean parseConnectionString(String text) {
        Matcher matcher = PATTERN.matcher(text);
        if (matcher.find()) {
            host = matcher.group(1);
            port = Integer.parseInt(matcher.group(2));
            keyspace = matcher.group(3);

            return true;
        } else {
            log.error("Can't parse cassandra_url: " + text);
            return false;
        }
    }

    public boolean isCassandraConfigProvided() {
        String url = env.getProperty(CASSANDRA_URL);
        if (url == null || !this.parseConnectionString(url)) {
            return false;
        }

        return true;
    }
}