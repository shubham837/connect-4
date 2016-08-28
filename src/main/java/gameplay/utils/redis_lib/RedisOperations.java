package gameplay.utils.redis_lib;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public class RedisOperations {

    @Autowired
    private RedisTemplate< String, Object > redisTemplate;

    public Object getValue( final String key ) {
        return redisTemplate.opsForValue().get( key );
    }

    public void setValue( final String key, final String value, final long  expireTime) {
        redisTemplate.opsForValue().set( key, value );
        if(expireTime != 0) {
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        }
    }

    public void setObjectMap( final String key, final Map properties) {
        /*final String key = String.format( "user:%s", user.getId() );
        final Map< String, Object > properties = new HashMap<>();

        properties.put( "id", user.getId() );
        properties.put( "name", user.getName() );
        properties.put( "email", user.getEmail() );*/

        redisTemplate.opsForHash().putAll( key, properties);
    }

    public Object getObjectMap( final String key, final String hashKey) {
        /* final String key = String.format( "user:%s", id );

        final String name = ( String )template.opsForHash().get( key, "name" );
        final String email = ( String )template.opsForHash().get( key, "email" );*/

        return redisTemplate.opsForHash().get( key, hashKey);
    }
}
