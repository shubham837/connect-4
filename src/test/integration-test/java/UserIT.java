import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import gameplay.Application;
import gameplay.dao.UserDao;
import gameplay.models.User;
import gameplay.responses.UserDetailResponse;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by shubham.singhal on 27/08/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
//@ContextConfiguration(classes={RedisConfig.class, CassandraConfig.class}, loader=AnnotationConfigContextLoader.class)
public class UserIT {

    private static final Logger log = LoggerFactory.getLogger(UserIT.class);

    @Autowired
    private UserDao userDao;

    @Value("${local.server.port}")
    private int serverPort;

    @Autowired
    @Qualifier(value = "RedisCacheManager")
    CacheManager redisCacheManager;

    private final static UUID USER1_ID = UUID.randomUUID();
    private final static UUID USER2_ID = UUID.randomUUID();
    private final static String USER_RESOURCE = "/user";
    private final static String USER_DETAIL_RESOURCE = "/user/{user_id}";
    private final static String AUTH_KEY1 = "TEST_AUTH_KEY1";
    private final static String ACCESS_TOKEN1 = "TEST_ACCESS_TOKEN1";
    private final static String AUTH_KEY2 = "TEST_AUTH_KEY2";
    private final static String ACCESS_TOKEN2 = "TEST_ACCESS_TOKEN2";
    private final static String ADMIN_AUTH_KEY = "TEST_ADMIN_AUTH_KEY";
    private final static String ADMIN_ACCESS_TOKEN = "TEST_ADMIN_ACCESS_TOKEN";
    private final static UUID ADMIN_UUID = UUID.fromString("c40bfc4e-377c-47f7-861e-3338b217105f");

    private User getUser1(){
        User user = new User();
        user.setId(USER1_ID);
        user.setName("TestFirst1 TestLast1");
        user.setEmail("testemail1@example.com");
        return user;
    }

    private User getUser2(){
        User user = new User();
        user.setId(USER2_ID);
        user.setName("TestFirst2 TestLast2");
        user.setEmail("testemail2@example.com");
        return user;
    }

    private void saveToCassandra() {
        userDao.save(getUser1());
        userDao.save(getUser2());
    }

    private void saveToRedis() {
        RedisCache cache = (RedisCache) redisCacheManager.getCache("Authorization");
        cache.putIfAbsent(AUTH_KEY1 + ':' + ACCESS_TOKEN1, USER1_ID);
        cache.putIfAbsent(AUTH_KEY2 + ':' + ACCESS_TOKEN2, USER2_ID);
        cache.putIfAbsent(ADMIN_AUTH_KEY + ':' + ADMIN_ACCESS_TOKEN, ADMIN_UUID);
    }

    @Before
    public void setUp() {
        saveToCassandra();
        saveToRedis();
        RestAssured.port = serverPort;
    }

    @After
    public void tearDown(){
        userDao.deleteAll();
        Cache cache = redisCacheManager.getCache("Authorization");
        cache.clear();
    }

    @Test
    public void testGetUserListAPI() {
        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", ADMIN_AUTH_KEY);
        headers.put("ACCESS_TOKEN", ADMIN_ACCESS_TOKEN);
        Response response =  given()
                                .contentType("application/json")
                                .headers(headers)
                            .when()
                                .get(USER_RESOURCE)
                            .then()
                                .statusCode(HttpStatus.SC_OK)
                                .contentType(ContentType.JSON)
                                .body("errors", nullValue())
                                .body("users", notNullValue(),
                                      "users.id", containsInAnyOrder(USER1_ID.toString(), USER2_ID.toString()))
                            .extract().response();
    }

    @Test
    public void testPostUserAPI() {
        User user = new User();
        user.setEmail("postUser@example.com");
        user.setName("TESTNAME");

        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", ADMIN_AUTH_KEY);
        headers.put("ACCESS_TOKEN", ADMIN_ACCESS_TOKEN);

        Response response =  given()
                                .contentType("application/json")
                                .headers(headers)
                                .body(user)
                            .when()
                                .post(USER_RESOURCE)
                            .then()
                                .statusCode(HttpStatus.SC_CREATED)
                                .contentType(ContentType.JSON)
                                .body("errors", emptyCollectionOf(List.class))
                                .body("user", notNullValue())
                            .extract().response();
    }

    @Test
    public void testGetUserDetailAPI() {
        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", ADMIN_AUTH_KEY);
        headers.put("ACCESS_TOKEN", ADMIN_ACCESS_TOKEN);

        UserDetailResponse userDetailResponse =   given()
                                                    .pathParam("user_id", USER1_ID)
                                                    .headers(headers)
                                                 .when()
                                                    .get(USER_DETAIL_RESOURCE)
                                                 .as(UserDetailResponse.class);

        Assert.assertNotNull(userDetailResponse);
        User userDetail = userDetailResponse.getUser();
        Assert.assertEquals(userDetail.getId(), USER1_ID);
        Assert.assertEquals(userDetail.getEmail(), "testemail1@example.com");
    }

    @Test
    public void testPatchUserDetailAPI() {
        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", ADMIN_AUTH_KEY);
        headers.put("ACCESS_TOKEN", ADMIN_ACCESS_TOKEN);

        Map<String,String> patchUser = new HashMap<>();
        patchUser.put("email", "alice.bob@example.com");
        UserDetailResponse userDetailPatchResponse = given()
                                                        .contentType("application/json")
                                                        .pathParam("user_id", USER2_ID)
                                                        .body(patchUser)
                                                        .headers(headers)
                                                     .when()
                                                        .patch(USER_DETAIL_RESOURCE)
                                                     .as(UserDetailResponse.class);

        Assert.assertNotNull(userDetailPatchResponse);
        User userDetail = userDetailPatchResponse.getUser();
        Assert.assertEquals(userDetail.getId(), USER2_ID);
        Assert.assertEquals(userDetail.getEmail(), "alice.bob@example.com");
    }
}