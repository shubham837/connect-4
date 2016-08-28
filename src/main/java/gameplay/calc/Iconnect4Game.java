package gameplay.calc;

import gameplay.errors.InvalidBoardStateException;
import gameplay.errors.MultipleWinnerException;
import gameplay.errors.NoUserExistException;
import gameplay.models.User;
import gameplay.models.Connect4Game;
import gameplay.models.Board;

import java.util.UUID;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public interface Iconnect4Game {

    UUID checkWin(final Board connect4GameBoard) throws MultipleWinnerException, InvalidBoardStateException;

    boolean validateBoardMove(final Board existingBoard,
                              final Board newBoard, final UUID nextMove) throws InvalidBoardStateException;

    UUID getNextTurnUser(final Connect4Game connect4Game, final Board board) throws NoUserExistException;

}
