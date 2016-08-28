import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import gameplay.Application;
import gameplay.dao.CoreDao;
import gameplay.dao.UserDao;
import gameplay.models.Connect4Game;
import gameplay.models.User;
import gameplay.models.enums.GameType;
import gameplay.responses.GameDetailResponse;
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

import java.util.*;

/**
 * Created by shubham.singhal on 28/08/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
//@ContextConfiguration(classes={RedisConfig.class, CassandraConfig.class}, loader=AnnotationConfigContextLoader.class)
public class GameApiIT {

    private static final Logger log = LoggerFactory.getLogger(UserIT.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private CoreDao coreDao;

    @Autowired
    @Qualifier(value = "RedisCacheManager")
    CacheManager redisCacheManager;

    @Value("${local.server.port}")
    private int serverPort;

    private final static UUID USER1_ID = UUID.randomUUID();
    private final static UUID USER2_ID = UUID.randomUUID();
    private final static UUID USER3_ID = UUID.randomUUID();
    private final static UUID GAME1_ID = UUID.randomUUID();
    private final static UUID GAME2_ID = UUID.randomUUID();
    private final static String GAME_RESOURCE = "/connect4";
    private final static String GAME_DETAIL_RESOURCE = "/connect4/{game_id}";
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
        user.setName("TestName");
        user.setEmail("testemail1@example.com");
        return user;
    }

    private User getUser2(){
        User user = new User();
        user.setId(USER2_ID);
        user.setName("Test Name2");
        user.setEmail("testemail2@example.com");
        return user;
    }

    private User getUser3(){
        User user = new User();
        user.setId(USER3_ID);
        user.setName("TestName3");
        user.setEmail("testemail3@example.com");
        return user;
    }

    private Connect4Game getGame1(){

        Connect4Game game = new Connect4Game();
        game.setId(GAME1_ID);
        game.setGameType(GameType.POP_OUT.toString());
        game.setNumberOfPlayers(2);
        List<UUID> users = new ArrayList<>();
        users.add(USER1_ID);
        users.add(USER2_ID);
        game.setUsers(users);
        return game;
    }

    private Connect4Game getGame2(){
        Connect4Game game = new Connect4Game();
        game.setId(GAME2_ID);
        game.setGameType(GameType.POP_OUT.toString());
        game.setNumberOfPlayers(3);
        return game;
    }

    private void saveToCassandra() {
        userDao.save(getUser1());
        userDao.save(getUser2());
        userDao.save(getUser3());
        coreDao.save(getGame1());
        coreDao.save(getGame2());
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
        coreDao.deleteAll();
        Cache cache = redisCacheManager.getCache("Authorization");
        cache.clear();
    }

    @Test
    public void testGetGameListAPI() {
        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", ADMIN_AUTH_KEY);
        headers.put("ACCESS_TOKEN", ADMIN_ACCESS_TOKEN);

        Response response =  given()
                                .contentType("application/json")
                                .headers(headers)
                            .when()
                                .get(GAME_RESOURCE)
                            .then()
                                .statusCode(HttpStatus.SC_OK)
                                .contentType(ContentType.JSON)
                                .body("errors", emptyCollectionOf(List.class))
                                .body("connect4Games", notNullValue(),
                                "connect4Games.id", containsInAnyOrder(GAME1_ID.toString(), GAME2_ID.toString()))
                            .extract().response();
    }

    @Test
    public void testPostGameAPI() {
        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", ADMIN_AUTH_KEY);
        headers.put("ACCESS_TOKEN", ADMIN_ACCESS_TOKEN);

        Connect4Game connect4Game = new Connect4Game();
        connect4Game.setGameType(GameType.POP_OUT.toString());
        connect4Game.setNumberOfPlayers(5);
        Response response =  given()
                                .contentType("application/json")
                                .body(connect4Game)
                                .headers(headers)
                            .when()
                                .post(GAME_RESOURCE)
                            .then()
                                .statusCode(HttpStatus.SC_CREATED)
                                .contentType(ContentType.JSON)
                                .body("errors", emptyCollectionOf(List.class))
                                .body("connect4Game", notNullValue())
                            .extract().response();
    }

    @Test
    public void testGetGameDetailAPI() {
        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", ADMIN_AUTH_KEY);
        headers.put("ACCESS_TOKEN", ADMIN_ACCESS_TOKEN);

        GameDetailResponse gameDetailResponse = given()
                                                    .pathParam("game_id", GAME1_ID)
                                                    .headers(headers)
                                                .when()
                                                    .get(GAME_DETAIL_RESOURCE)
                                                .as(GameDetailResponse.class);

        Assert.assertNotNull(gameDetailResponse);
        Connect4Game gameDetail = gameDetailResponse.getConnect4Game();
        Assert.assertEquals(gameDetail.getId(), GAME1_ID);
        Assert.assertEquals(gameDetail.getGameType(), GameType.POP_OUT.toString());
    }

    @Test
    public void testPutGameDetailAPI() {
        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", ADMIN_AUTH_KEY);
        headers.put("ACCESS_TOKEN", ADMIN_ACCESS_TOKEN);

        Map<String,String> gameDetail = new HashMap<>();
        gameDetail.put("gameType", "POP_10");
        GameDetailResponse gameDetailResponse = given()
                                                    .contentType("application/json")
                                                    .pathParam("game_id", GAME2_ID)
                                                    .body(gameDetail)
                                                    .headers(headers)
                                                .when()
                                                    .put(GAME_DETAIL_RESOURCE)
                                                .as(GameDetailResponse.class);

        Assert.assertNotNull(gameDetailResponse);
        Connect4Game connect4Game = gameDetailResponse.getConnect4Game();
        Assert.assertEquals(connect4Game.getId(), GAME2_ID);
        Assert.assertEquals(connect4Game.getGameType(), "POP_10");
    }
}