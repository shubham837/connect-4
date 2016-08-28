package gameplay.controller;

import gameplay.calc.Connect4GameFactory;
import gameplay.calc.Iconnect4Game;
import gameplay.dao.BoardDao;
import gameplay.dao.CoreDao;
import gameplay.errors.InvalidBoardStateException;
import gameplay.errors.MultipleWinnerException;
import gameplay.errors.NoUserExistException;
import gameplay.errors.ServiceError;
import gameplay.models.Board;
import gameplay.models.Connect4Game;
import gameplay.models.enums.GameType;
import gameplay.responses.BoardDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class BoardController {
    private static final Logger log = LoggerFactory.getLogger(BoardController.class);

    @Autowired
    CoreDao coreDao;

    @Autowired
    BoardDao boardDao;

    @RequestMapping(value = "/connect4/{gameId}/board", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<BoardDetailResponse> postAction(@RequestAttribute UUID userId, @PathVariable String gameId, @RequestBody Board board) {
        BoardDetailResponse boardDetailResponse = new BoardDetailResponse();
        List<ServiceError> serviceErrors = new ArrayList<>();
        boardDetailResponse.setErrors(serviceErrors);

        Connect4Game connect4Game = coreDao.getDetail(UUID.fromString(gameId));

        if(connect4Game == null) {
            log.error("No Game Exist for Game Id: " + gameId);
            serviceErrors.add(new ServiceError(0, "No valid game exist for requested game id"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        } else if(connect4Game.getBoardId() != null || connect4Game.getWinnerId() != null) {
            log.error("Connect4Game Board already exist for Game Id: " + gameId);
            serviceErrors.add(new ServiceError(1, "Board already exist for gameId"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        }

        // Setting Board Info
        UUID boardId = UUID.randomUUID();
        board.setId(boardId);
        board.setCreatedTs(new Date());
        board.setUpdatedTs(new Date());

        // Setting Connect4Game Info
        GameType gameType = GameType.valueOf(connect4Game.getGameType());
        Connect4GameFactory connect4GameFactory = Connect4GameFactory.getInstance();
        Iconnect4Game connect4GameCalc= connect4GameFactory.getConnect4Game(gameType);

        UUID boardWinner;
        try {
            boardWinner = connect4GameCalc.checkWin(board);
        }catch (MultipleWinnerException e) {
            log.error("Multiple Winner Exception in checking winner for Game Id: " + gameId + " Exception: " + e.getMessage());
            serviceErrors.add(new ServiceError(0, "Multiple Winners exist in Board"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        } catch (InvalidBoardStateException e){
            log.error("Invalid Board State Exception in checking winner for Game Id: " + gameId + " Exception: " + e.getMessage());
            serviceErrors.add(new ServiceError(0, "Invalid Board State"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        } catch (Exception e) {
            log.error("Unknown Exception in checking winner for Game Id: " + gameId + " Exception: " + e.getMessage());
            serviceErrors.add(new ServiceError(0, "Unknown exception in checking winner"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        }

        UUID nextUserTurn;
        try {
            nextUserTurn = connect4GameCalc.getNextTurnUser(connect4Game, board);
        }catch (NoUserExistException e) {
            log.error("No User Exist for Game Id: " + gameId);
            serviceErrors.add(new ServiceError(0, "No User Exist for Game Id"));
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        }

        if(boardWinner != null){
            connect4Game.setWinnerId(boardWinner);
            boardDetailResponse.setWinnerId(boardWinner);
            boardDetailResponse.setMessage("Yah!! You won!!");
        }

        board.setNextMoveUserId(nextUserTurn);
        connect4Game.setBoardId(boardId);
        connect4Game.setUpdatedTs(new Date());
        connect4Game.setLastModifiedBy(userId); // TODO
        boardDao.save(board);
        coreDao.save(connect4Game);

        boardDetailResponse.setBoard(board);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
    }

    @RequestMapping(value = "/connect4/{gameId}/board/{boardId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<BoardDetailResponse> putAction(@RequestAttribute UUID userId, @PathVariable String gameId, @PathVariable String boardId, @RequestBody Board board) {
        BoardDetailResponse boardDetailResponse = new BoardDetailResponse();
        List<ServiceError> serviceErrors = new ArrayList<>();
        boardDetailResponse.setErrors(serviceErrors);

        Connect4Game connect4Game = coreDao.getDetail(UUID.fromString(gameId));

        if(connect4Game == null || connect4Game.getBoardId() == null ||
                !boardId.equals(connect4Game.getBoardId().toString())) {
            log.error("No Board Exist for Game Id: " + gameId + " Board Id: " + boardId);
            serviceErrors.add(new ServiceError(0, "No valid board exist for requested game id and board id"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        }

        GameType gameType = GameType.valueOf(connect4Game.getGameType());
        Connect4GameFactory connect4GameFactory = Connect4GameFactory.getInstance();
        Iconnect4Game connect4GameCalc= connect4GameFactory.getConnect4Game(gameType);

        Board existingBoard = boardDao.getDetail(UUID.fromString(boardId));
        UUID currentTurnUserId = existingBoard.getNextMoveUserId();
        if(!userId.equals(currentTurnUserId)) {
            log.error("User Not authorized to change the state of board for Game Id: " + gameId + "  Board Id: " + boardId);
            serviceErrors.add(new ServiceError(0, "User not allowed to change state during out of turn"));
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        }

        boolean isValidMove;
        try {
            isValidMove = connect4GameCalc.validateBoardMove(existingBoard, board, currentTurnUserId);
        } catch (InvalidBoardStateException e) {
            log.error("Invalid Board State Passed for Game Id: " + gameId + " Board Id: " + boardId);
            serviceErrors.add(new ServiceError(0, "Invalid Board State"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        }

        if(isValidMove==false) {
            log.error("Invalid move in Board for Game Id: " + gameId + " Board Id: " + boardId);
            serviceErrors.add(new ServiceError(0, "Invalid move in Board"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        }

        UUID nextTurnUserId;
        try {
            nextTurnUserId = connect4GameCalc.getNextTurnUser(connect4Game, board);
        }catch (NoUserExistException e) {
            log.error("No User Exist for Game Id: " + gameId + "  Board Id: " + boardId);
            serviceErrors.add(new ServiceError(0, "No User Exist for Game Id"));
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        }

        UUID boardWinnerId;
        try {
            boardWinnerId = connect4GameCalc.checkWin(board);
        }catch (MultipleWinnerException e) {
            log.error("Multiple Winner Exception in checking winner for Game Id: " + gameId + " Exception: " + e.getMessage());
            serviceErrors.add(new ServiceError(0, "Multiple Winners exist in Board"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        } catch (InvalidBoardStateException e){
            log.error("Invalid Board State Exception in checking winner for Game Id: " + gameId + " Exception: " + e.getMessage());
            serviceErrors.add(new ServiceError(0, "Invalid Board State"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        } catch (Exception e) {
            log.error("Unknown Exception in checking winner for Game Id: " + gameId + " Exception: " + e.getMessage());
            serviceErrors.add(new ServiceError(0, "Unknown exception "));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
        }
        if(boardWinnerId != null) {
            connect4Game.setWinnerId(boardWinnerId);
            connect4Game.setUpdatedTs(new Date());
            connect4Game.setLastModifiedBy(userId);
            boardDetailResponse.setWinnerId(boardWinnerId);
            boardDetailResponse.setMessage("Yah!! You won!!");
        }

        existingBoard.setBoardState(board.getBoardState());
        existingBoard.setNextMoveUserId(nextTurnUserId);
        existingBoard.setLastModifiedBy(userId);
        existingBoard.setUpdatedTs(new Date());

        boardDao.save(existingBoard);
        coreDao.save(connect4Game);

        boardDetailResponse.setBoard(existingBoard);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
    }

    @RequestMapping(value = "/connect4/{gameId}/board/{boardId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<BoardDetailResponse>  getBoardDetail(@RequestAttribute UUID userId, @PathVariable String gameId, @PathVariable String boardId) {
        BoardDetailResponse boardDetailResponse = new BoardDetailResponse();
        List<ServiceError> serviceErrors = new ArrayList<>();
        boardDetailResponse.setErrors(serviceErrors);

        Connect4Game connect4Game = coreDao.getDetail(UUID.fromString(gameId));

        if(connect4Game == null || connect4Game.getBoardId() == null) {
            log.error("No Board Exist for Game Id: " + gameId + " Board Id: " + boardId);
            serviceErrors.add(new ServiceError(0, "No valid board exist for requested game id and board id"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);

        }

        Board board = boardDao.getDetail(UUID.fromString(boardId));

        boardDetailResponse.setBoard(board);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(boardDetailResponse);
    }

}
