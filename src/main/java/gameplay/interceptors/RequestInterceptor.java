package gameplay.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gameplay.Application;
import gameplay.dao.UserDao;
import gameplay.utils.AuthManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheElement;
import org.springframework.data.redis.cache.RedisCacheKey;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.util.UUID;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public class RequestInterceptor extends HandlerInterceptorAdapter{

        private static final Logger log = Logger.getLogger(RequestInterceptor.class);

        @Autowired
        @Qualifier(value = "RedisCacheManager")
        private CacheManager redisCacheManager;


        //before the actual handler will be executed
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            log.info("Request Method: " + request.getMethod() + " URL: " + request.getRequestURI() + " Client Type: " + request.getHeader("CLIENT_TYPE")
                    + " Auth Key: " + request.getHeader("AUTH_KEY") + " Access Token: " + request.getHeader("ACCESS_TOKEN"));

            AuthManager authManager = new AuthManager();
            UUID userId = checkAuthorization(request.getHeader("CLIENT_TYPE"), request.getHeader("AUTH_KEY"),
                    request.getHeader("ACCESS_TOKEN"));

            if ("GET".equals(request.getMethod())) {
                checkETagHeader();
            }

            if(userId == null) {
                log.error("No User exist for given auth key and access token");
                return false;
            }

            long startTime = System.currentTimeMillis();
            request.setAttribute("startTime", startTime);
            request.setAttribute("userId", userId);

            return true;
        }

        //after the handler is executed
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                               ModelAndView modelAndView) throws Exception {

            long startTime = (Long)request.getAttribute("startTime");

            long endTime = System.currentTimeMillis();

            long executeTime = endTime - startTime;

            log.info("[" + handler + "] executeTime : " + executeTime + "ms" +
                    "Response Status: " + response.getStatus());
        }

        private UUID checkAuthorization(String clientType, String authKey, String accessToken) {

            Cache cache = redisCacheManager.getCache("Authorization");

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


        private boolean checkETagHeader(){
            return true;
        }

}
