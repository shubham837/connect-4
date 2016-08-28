import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import gameplay.Application;
import gameplay.dao.BoardDao;
import gameplay.dao.CoreDao;
import gameplay.dao.UserDao;
import gameplay.models.Board;
import gameplay.models.Connect4Game;
import gameplay.models.User;
import gameplay.models.enums.GameType;
import gameplay.responses.BoardDetailResponse;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by shubham.singhal on 28/08/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class BoardApiIT {

    private static final Logger log = LoggerFactory.getLogger(BoardApiIT.class);


    @Autowired
    private UserDao userDao;

    @Autowired
    private CoreDao coreDao;

    @Autowired
    private BoardDao boardDao;

    @Autowired
    @Qualifier(value = "RedisCacheManager")
    private CacheManager redisCacheManager;

    @Value("${local.server.port}")
    private int serverPort;

    private final static UUID USER1_ID = UUID.randomUUID();
    private final static UUID USER2_ID = UUID.randomUUID();
    private final static UUID GAME1_ID = UUID.randomUUID();
    private final static UUID GAME2_ID = UUID.randomUUID();
    private final static UUID GAME3_ID = UUID.randomUUID();
    private final static UUID BOARD1_ID = UUID.randomUUID();
    private final static UUID BOARD2_ID = UUID.randomUUID();
    private final static UUID ADMIN_UUID = UUID.fromString("c40bfc4e-377c-47f7-861e-3338b217105f");
    private final static String BOARD_RESOURCE = "/connect4/{game_id}/board";
    private final static String BOARD_DETAIL_RESOURCE = "/connect4/{game_id}/board/{board_id}";
    private final static String AUTH_KEY1 = "TEST_AUTH_KEY1";
    private final static String ACCESS_TOKEN1 = "TEST_ACCESS_TOKEN1";
    private final static String AUTH_KEY2 = "TEST_AUTH_KEY2";
    private final static String ACCESS_TOKEN2 = "TEST_ACCESS_TOKEN2";
    private final static String ADMIN_AUTH_KEY = "TEST_ADMIN_AUTH_KEY";
    private final static String ADMIN_ACCESS_TOKEN = "TEST_ADMIN_ACCESS_TOKEN";

    private User getUser1(){
        User user = new User();
        user.setId(USER1_ID);
        user.setName("TestName1");
        user.setEmail("testemail1@example.com");
        return user;
    }

    private User getUser2(){
        User user = new User();
        user.setId(USER2_ID);
        user.setName("TestName2");
        user.setEmail("testemail2@example.com");
        return user;
    }


    private Connect4Game getGame1(){
        Connect4Game game = new Connect4Game();
        game.setId(GAME1_ID);
        game.setGameType(GameType.POP_10.toString());
        game.setNumberOfPlayers(2);
        List<UUID> users = new ArrayList<>();
        users.add(USER2_ID);
        users.add(USER1_ID);
        game.setUsers(users);
        game.setBoardId(BOARD1_ID);
        return game;
    }

    private Connect4Game getGame2(){
        Connect4Game game = new Connect4Game();
        game.setId(GAME2_ID);
        game.setGameType(GameType.POP_OUT.toString());
        List<UUID> users = new ArrayList<>();
        users.add(USER2_ID);
        game.setUsers(users);
        game.setNumberOfPlayers(1);
        game.setBoardId(BOARD2_ID);
        return game;
    }

    private Connect4Game getGame3(){
        Connect4Game game = new Connect4Game();
        game.setId(GAME3_ID);
        game.setGameType(GameType.POP_OUT.toString());
        List<UUID> users = new ArrayList<>();
        users.add(USER1_ID);
        users.add(USER2_ID);
        game.setUsers(users);
        game.setNumberOfPlayers(2);
        return game;
    }

    private Board getBoard1() {
        Board board = new Board();
        List< List<UUID>> boardState = new ArrayList<>(Collections.nCopies(6, new ArrayList<>(Collections.nCopies(7, ADMIN_UUID))));
        board.setId(BOARD1_ID);
        board.setBoardState(boardState);
        return board;
    }

    private Board getBoard2() {
        Board board = new Board();
        List< List<UUID>> boardState = new ArrayList<>(Collections.nCopies(7, new ArrayList<>(Collections.nCopies(8, ADMIN_UUID))));
        board.setId(BOARD2_ID);
        board.setBoardState(boardState);
        board.setNextMoveUserId(USER2_ID);
        return board;
    }

    private void saveToCassandra() {
        userDao.save(getUser1());
        userDao.save(getUser2());
        boardDao.save(getBoard1());
        boardDao.save(getBoard2());
        coreDao.save(getGame1());
        coreDao.save(getGame2());
        coreDao.save(getGame3());
    }

    private void saveToRedis() {
        RedisCache cache = (RedisCache) redisCacheManager.getCache("Authorization");
        cache.put(AUTH_KEY1 + ':' + ACCESS_TOKEN1, USER1_ID);
        cache.put(AUTH_KEY2 + ':' + ACCESS_TOKEN2, USER2_ID);
        cache.put(ADMIN_AUTH_KEY + ':' + ADMIN_ACCESS_TOKEN, ADMIN_UUID);
    }

    @Before
    public void setUp() {
        saveToCassandra();
        saveToRedis();
        RestAssured.port = serverPort;
    }

    @After
    public void tearDown(){
        coreDao.deleteAll();
        userDao.deleteAll();
        boardDao.deleteAll();
        Cache cache = redisCacheManager.getCache("Authorization");
        cache.clear();
    }

    @Test
    public void testPostBoardAPI_BoardAlreadyExist() {
        Board board = new Board();
        List< List<UUID>> boardState = new ArrayList<>(Collections.nCopies(6, new ArrayList<>(Collections.nCopies(7, ADMIN_UUID))));
        board.setBoardState(boardState);
        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", AUTH_KEY2);
        headers.put("ACCESS_TOKEN", ACCESS_TOKEN2);

        Response response =  given()
                                .contentType("application/json")
                                .pathParam("game_id", GAME1_ID)
                                .body(board)
                                .headers(headers)
                            .when()
                                .post(BOARD_RESOURCE)
                            .then()
                                .statusCode(HttpStatus.SC_BAD_REQUEST)
                                .contentType(ContentType.JSON)
                                .body("errors", notNullValue())
                                .body("board", nullValue())
                            .extract().response();
    }

    @Test
    public void testPostBoardAPI_Success() {
        Board board = new Board();
        List< List<UUID>> boardState = new ArrayList<>(Collections.nCopies(6, new ArrayList<>(Collections.nCopies(7, ADMIN_UUID))));
        board.setBoardState(boardState);
        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", AUTH_KEY2);
        headers.put("ACCESS_TOKEN", ACCESS_TOKEN2);

        Response response =  given()
                                .contentType("application/json")
                                .pathParam("game_id", GAME3_ID)
                                .body(board)
                                .headers(headers)
                            .when()
                                .post(BOARD_RESOURCE)
                            .then()
                                .statusCode(HttpStatus.SC_CREATED)
                                .contentType(ContentType.JSON)
                                .body("errors", emptyCollectionOf(List.class))
                                .body("board", notNullValue())
                            .extract().response();
    }

    @Test
    public void testGetBoardDetailAPI() {
        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", AUTH_KEY2);
        headers.put("ACCESS_TOKEN", ACCESS_TOKEN2);

        BoardDetailResponse boardDetailResponse = given()
                                                    .pathParam("game_id", GAME1_ID)
                                                    .pathParam("board_id", BOARD1_ID)
                                                    .headers(headers)
                                                  .when()
                                                    .get(BOARD_DETAIL_RESOURCE)
                                                  .as(BoardDetailResponse.class);

        Assert.assertNotNull(boardDetailResponse);
        Board boardDetail = boardDetailResponse.getBoard();
        Assert.assertEquals(boardDetail.getId(), BOARD1_ID);
        boardDetail.getBoardState();
    }

    @Test
    public void testPatchBoardAPI() {
        Map<String, List<List<UUID>>> newBoardDetail = new HashMap<>();
        List<List<UUID>> newBoardState = Stream.generate(ArrayList<UUID>::new)
                                        .limit(7)
                                        .collect(Collectors.toList());
        for(List<UUID> rowList : newBoardState) {
            for(int i=0; i<8;++i){
                rowList.add(ADMIN_UUID);
            }
        }
        newBoardState.get(0).set(1,USER2_ID);
        newBoardDetail.put("boardState", newBoardState);

        Map<String, String> headers = new HashMap<>();
        headers.put("AUTH_KEY", AUTH_KEY2);
        headers.put("ACCESS_TOKEN", ACCESS_TOKEN2);
        BoardDetailResponse boardDetailResponse = given()
                                                    .contentType("application/json")
                                                    .pathParam("game_id", GAME2_ID)
                                                    .pathParam("board_id", BOARD2_ID)
                                                    .body(newBoardDetail)
                                                    .headers(headers)
                                                  .when()
                                                    .put(BOARD_DETAIL_RESOURCE)
                                                  .as(BoardDetailResponse.class);

        Assert.assertNotNull(boardDetailResponse);
        Board boardDetail = boardDetailResponse.getBoard();
        Assert.assertEquals(boardDetail.getId(), BOARD2_ID);
    }
}