package gameplay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Created by shubham.singhal on 26/08/16.
 */
@Configuration
public class RedisConfig {
    private String redisHost;
    private int redisPort;
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Autowired
    private Environment env;

    private boolean isRedisConfigProvided() {
        redisHost = env.getProperty("spring.redis.host");
        redisPort = Integer.parseInt(env.getProperty("spring.redis.port"));
        if (redisHost == null) {
            log.info("Redis Host not provided");
            return false;
        }
        log.info("Redis Host: " + redisHost + "Redis Port: " + redisPort);

        return true;
    }

    @Bean
    public RedisSerializer redisStringSerializer() {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        return stringRedisSerializer;
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        log.info("Redis Host: " + env.getProperty("spring.redis.host") + "Redis Port: " + Integer.parseInt(env.getProperty("spring.redis.port")));
        factory.setHostName(env.getProperty("spring.redis.host"));
        factory.setPort(Integer.parseInt(env.getProperty("spring.redis.port")));
        factory.setUsePool(true);
        return factory;
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<Object, Object>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setKeySerializer( redisStringSerializer() );
        redisTemplate.setHashValueSerializer( new GenericToStringSerializer<Object>( Object.class ) );
        redisTemplate.setValueSerializer( new GenericToStringSerializer< Object >( Object.class ) );
        return redisTemplate;
    }

    @Bean(name = "RedisCacheManager", autowire = Autowire.BY_NAME)
    public CacheManager redisCacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate());
        redisCacheManager.setDefaultExpiration(300000);
        return redisCacheManager;
    }
}
