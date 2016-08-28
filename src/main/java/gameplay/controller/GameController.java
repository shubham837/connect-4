package gameplay.controller;

import gameplay.dao.CoreDao;
import gameplay.errors.ServiceError;
import gameplay.models.Connect4Game;
import gameplay.models.User;
import gameplay.responses.BoardDetailResponse;
import gameplay.responses.GameDetailResponse;
import gameplay.responses.GameListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.cassandra.repository.MapId;
import org.springframework.data.cassandra.repository.support.BasicMapId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by shubham.singhal on 26/08/16.
 */
@RestController
public class GameController {
    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private CoreDao coreDao;

    @RequestMapping(value = "/connect4",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<GameListResponse> getConnect4Games(@RequestAttribute UUID userId) {
        GameListResponse gameListResponse = new GameListResponse();
        List<ServiceError> serviceErrors = new ArrayList<>();
        gameListResponse.setErrors(serviceErrors);

        List<Connect4Game> connect4Games = new ArrayList<>();
        try {
            coreDao.findAll().forEach(e -> connect4Games.add(e));
        } catch (Exception e) {
            log.error("Exception in fetching Game List, Exception: " + e.getMessage());
            serviceErrors.add(new ServiceError(0, "internal server exception"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(gameListResponse);
        }

        gameListResponse.setConnect4Games(connect4Games);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gameListResponse);
    }

    @RequestMapping(value = "/connect4/{game_id}",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<GameDetailResponse> getConnect4GameDetail(@RequestAttribute UUID userId, @PathVariable String game_id) {
        GameDetailResponse gameDetailResponse = new GameDetailResponse();
        List<ServiceError> serviceErrors = new ArrayList<>();
        gameDetailResponse.setErrors(serviceErrors);

        Connect4Game connect4Game = coreDao.getDetail(UUID.fromString(game_id));

        if(connect4Game == null) {
            log.error("Game does not exist for gameId: " + game_id);
        }

        gameDetailResponse.setConnect4Game(connect4Game);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gameDetailResponse);
    }

    @RequestMapping(value = "/connect4",method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<GameDetailResponse> postConnect4Game(@RequestAttribute UUID userId, @RequestBody Connect4Game connect4Game) {
        GameDetailResponse gameDetailResponse = new GameDetailResponse();
        List<ServiceError> serviceErrors = new ArrayList<>();
        gameDetailResponse.setErrors(serviceErrors);

        connect4Game.setCreatedTs(new Date());
        connect4Game.setUpdatedTs(new Date());
        connect4Game.setId(UUID.randomUUID());

        connect4Game.setLastModifiedBy(userId);
        coreDao.save(connect4Game);

        gameDetailResponse.setConnect4Game(connect4Game);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(gameDetailResponse);
    }

    @RequestMapping(value = "/connect4/{game_id}",method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<GameDetailResponse> putConnect4Game(@RequestAttribute UUID userId, @PathVariable String game_id, @RequestBody Connect4Game connect4Game) {
        GameDetailResponse gameDetailResponse = new GameDetailResponse();
        List<ServiceError> serviceErrors = new ArrayList<>();
        gameDetailResponse.setErrors(serviceErrors);

        connect4Game.setUpdatedTs(new Date());
        connect4Game.setId(UUID.fromString(game_id));
        coreDao.save(connect4Game);

        gameDetailResponse.setConnect4Game(connect4Game);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(gameDetailResponse);
    }

}
