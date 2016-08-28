package gameplay.controller;

import gameplay.Application;
import gameplay.dao.CoreDao;
import gameplay.dao.UserDao;
import gameplay.errors.ServiceError;
import gameplay.models.Connect4Game;
import gameplay.models.User;
import gameplay.responses.UserDetailResponse;
import gameplay.responses.UserListResponse;
import gameplay.utils.AuthManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.cassandra.repository.MapId;
import org.springframework.data.cassandra.repository.support.BasicMapId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by shubham.singhal on 26/08/16.
 */
@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserDao userDao;

    @Autowired
    @Qualifier(value = "RedisCacheManager")
    private CacheManager redisCacheManager;

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<UserDetailResponse> postUser(@RequestAttribute UUID userId, @RequestBody User user) {
        UserDetailResponse userDetailResponse = new UserDetailResponse();
        List<ServiceError> serviceErrors = new ArrayList<>();
        userDetailResponse.setErrors(serviceErrors);

        if(!validateUser(user)) {
            log.error("User not Valid or already exist in system");
            serviceErrors.add(new ServiceError(0, "User Not Valid or already exist in system"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(userDetailResponse);
        }

        log.info("Creating User: " + user.toString());
        user.setId(UUID.randomUUID());
        user.setCreatedTs(new Date());
        user.setUpdatedTs(new Date());

        try {
            user = userDao.save(user);
        } catch (Exception e) {
            log.error("Exception in Creating new user in Database for Email: " + user.getEmail());
            serviceErrors.add(new ServiceError(0, "Internal Error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(userDetailResponse);
        }

        Cache cache = redisCacheManager.getCache("Authorization");
        cache.putIfAbsent(userId.toString() + ':' + userId.toString(), userId);

        userDetailResponse.setUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(userDetailResponse);
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<UserListResponse> getUserList(@RequestAttribute UUID userId) {
        UserListResponse userListResponse = new UserListResponse();

        List<User> userList = new ArrayList<>();
        userDao.findAll().forEach(e->userList.add(e));
        userListResponse.setUsers(userList);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(userListResponse);
    }

    @RequestMapping(value = "/user/{user_id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<UserDetailResponse> getUserDetail(@RequestAttribute UUID userId, @PathVariable String user_id) {
        UserDetailResponse userDetailResponse = new UserDetailResponse();
        List<ServiceError> serviceErrors = new ArrayList<>();
        userDetailResponse.setErrors(serviceErrors);

        log.info("Get User Detail for User Id: " + user_id);
        User user = userDao.getDetail(UUID.fromString(user_id));

        if(user == null) {
            log.error("User Does not exist for User Id: " + user_id);
            serviceErrors.add(new ServiceError(0, "User Not Exit"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(userDetailResponse);
        }
        userDetailResponse.setUser(user);

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(userDetailResponse);
    }

    @RequestMapping(value = "/user/{user_id}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<UserDetailResponse> patchUserDetail(@RequestAttribute UUID userId, @PathVariable String user_id, @RequestBody User user) {
        UserDetailResponse userDetailResponse = new UserDetailResponse();
        List<ServiceError> serviceErrors = new ArrayList<>();
        userDetailResponse.setErrors(serviceErrors);

        User existingUser = userDao.getDetail(UUID.fromString(user_id));
        if(existingUser == null) {
            log.error("User Does not exist for User Id: " + user_id);
            serviceErrors.add(new ServiceError(0, "User Not Exit"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(userDetailResponse);
        }

        existingUser.patch(user);
        userDao.save(existingUser);

        userDetailResponse.setUser(existingUser);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(userDetailResponse);
    }

    private boolean validateUser(User user) {
        // Check in Elastic Search if user already exist or not
        return true;
    }

}
