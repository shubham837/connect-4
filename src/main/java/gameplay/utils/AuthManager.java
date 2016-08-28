package gameplay.utils;

import gameplay.calc.Connect4GameFactory;
import gameplay.interceptors.RequestInterceptor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheElement;

import java.security.MessageDigest;
import java.util.Date;
import java.util.UUID;

/**
 * Created by shubham.singhal on 28/08/16.
 */
public class AuthManager {

    private static final Logger log = Logger.getLogger(AuthManager.class);
    private static final String AUTHORIZATION_CACHE = "Authorization";
    private static final String SALT = "RANDOM_SALT_SHOULD_BE_DEFINED_IN_ENV_VARIABLE";
    private static AuthManager instance;

    @Autowired
    @Qualifier(value = "RedisCacheManager")
    private CacheManager redisCacheManager;

    public AuthManager(){}

    public static AuthManager getInstance()
    {
        if (instance == null)
        {
            synchronized(AuthManager.class)
            {
                if (instance == null)
                {
                    log.info("Initializing AuthManager");
                    instance = new AuthManager();
                }
            }
        }
        return instance;
    }

    public byte[] digestAuthKeyAndToken(String userId) {
        Date d = new Date();
        MessageDigest md = null;
        try {
            MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            return userId.getBytes();
        }
        String key = userId + SALT + d.getTime();
        return md.digest(key.getBytes());
    }

    public String generateAuthKey(String userId) {
        byte[] authKeyBytes= digestAuthKeyAndToken(userId);
        return authKeyBytes.toString();
    }

    public String generateAccessToken(String userId) {
        byte[] accessTokenBytes= digestAuthKeyAndToken(userId);
        return accessTokenBytes.toString();
    }

    public boolean saveAuthentication(String authKey, String accessToken, String userId) {
        Cache cache = redisCacheManager.getCache(AUTHORIZATION_CACHE);
        cache.putIfAbsent(authKey+ ':' + accessToken, userId);
        return true;
    }

    public UUID checkAuthorization(String clientType, String authKey, String accessToken) {

        Cache cache = redisCacheManager.getCache(AUTHORIZATION_CACHE);

        if(cache == null) {
            log.error("No Cache exist with name Authorization");
            return null;
        }
        RedisCacheElement redisCacheElement  =  (RedisCacheElement)  cache.get(authKey+ ':' + accessToken);

        if( redisCacheElement == null) {
            log.info("User not authorized for Auth Key: " + authKey + " AccesToken: "+ accessToken);
            return null;
        }
        return UUID.fromString((String)redisCacheElement.get());
    }
}
