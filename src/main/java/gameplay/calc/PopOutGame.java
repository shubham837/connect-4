package gameplay.calc;

import gameplay.errors.InvalidBoardStateException;
import gameplay.errors.MultipleWinnerException;
import gameplay.errors.NoUserExistException;
import gameplay.models.Connect4Game;
import gameplay.models.Board;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashSet;
import java.util.UUID;
import java.util.List;
import java.util.Set;


/**
 * Created by shubham.singhal on 26/08/16.
 */
public class PopOutGame implements Iconnect4Game {
    private static final Logger log = LoggerFactory.getLogger(PopOutGame.class);
    // TODO: Remove Admin uuid to config
    private final static UUID ADMIN_UUID = UUID.fromString("c40bfc4e-377c-47f7-861e-3338b217105f");

    public UUID getNextTurnUser(final Connect4Game connect4Game, final Board board) throws NoUserExistException{

        List<UUID> playersList = connect4Game.getUsers();

        if(playersList == null || playersList.size() ==0 ){
            throw new NoUserExistException("No User Exist");
        }

        if(board.getNextMoveUserId() != null) {
            for(int i=0; i< playersList.size(); ++i) {
                if(playersList.get(i).equals(board.getNextMoveUserId())){
                    board.setNextMoveUserId(playersList.get((i+1) % playersList.size()));
                }
            }
        } else {
            board.setNextMoveUserId(playersList.get(0));
        }
        return null;
    }

    public boolean validateBoardMove(final Board existingBoard,
                                     final Board newBoard,
                                     final UUID nextMove) throws InvalidBoardStateException{
        List<List<UUID>> existingBoardState = existingBoard.getBoardState();
        List<List<UUID>> newBoardState = newBoard.getBoardState();
        if(newBoardState == null ||
                existingBoardState.size() != newBoardState.size() ||
                existingBoardState.get(0).size() != newBoardState.get(0).size()) {
            throw new InvalidBoardStateException("Invalid Board passed");
        }
        int numOfMoveDiffers = 0;
        for (int row=0; row < existingBoardState.size(); row++) {
            for (int col = 0; col < existingBoardState.get(0).size(); col++) {
                if(!existingBoardState.get(row).get(col).equals(newBoardState.get(row).get(col))) {
                    if(!newBoardState.get(row).get(col).equals(nextMove)){
                        log.error("Board new State not matching with next move user id");
                        return false;
                    }
                    ++numOfMoveDiffers;
                }
            }
        }
        if(numOfMoveDiffers != 1) {
            log.info("Number of move differs exceed the allowed number 1" + numOfMoveDiffers);
            return false;
        }
        return true;
    }

    public UUID checkWin(final Board connect4GameBoard) throws MultipleWinnerException, InvalidBoardStateException{
        List<List<UUID>> boardGrid = connect4GameBoard.getBoardState();
        if(boardGrid == null || boardGrid.size() <=0 || boardGrid.get(0).size() <=0) {
            throw new InvalidBoardStateException("Empty Board passed");
        }
        Set<UUID> horizontalWinners = CheckHorizontalWin(boardGrid, boardGrid.size(), boardGrid.get(0).size());
        Set<UUID> verticalWinners = CheckVerticalWin(boardGrid, boardGrid.size(), boardGrid.get(0).size());
        Set<UUID> diagonalWinners = CheckDiagonalWin(boardGrid, boardGrid.size(), boardGrid.get(0).size());
        Set<UUID> allWinners = new HashSet<>();
        allWinners.addAll(horizontalWinners);
        allWinners.addAll(verticalWinners);
        allWinners.addAll(diagonalWinners);
        if(allWinners.size() > 1) {
            throw new MultipleWinnerException("Multiple Winners for Board: " + connect4GameBoard.getId(), allWinners);
        }
        UUID winnerId = null;
        for(UUID winner: allWinners) {
            winnerId = winner;
        }
        return winnerId;
    }

    private Set<UUID> CheckHorizontalWin(List<List<UUID>> grid, int rowCount, int colCount){
        Set<UUID> winners = new HashSet<>();
        for (int row=0; row < rowCount; row++) {
            for (int col=0; col < colCount - 3; col++) {
                if (!grid.get(row).get(col).equals(ADMIN_UUID) &&
                        grid.get(row).get(col).equals(grid.get(row).get(col + 1)) &&
                        grid.get(row).get(col).equals(grid.get(row).get(col + 2)) &&
                        grid.get(row).get(col).equals(grid.get(row).get(col + 3))) {
                    winners.add(grid.get(row).get(col));
                }
            }
        }
        return winners;
    }

    private Set<UUID> CheckVerticalWin(List<List<UUID>> grid, int rowCount, int colCount){
        Set<UUID> winners = new HashSet<>();
        for (int row=0; row < rowCount - 3; row++) {
            for (int col=0; col < colCount; col++) {
                if (!grid.get(row).get(col).equals(ADMIN_UUID) &&
                        grid.get(row).get(col).equals(grid.get(row + 1).get(col)) &&
                        grid.get(row).get(col).equals(grid.get(row + 2).get(col)) &&
                        grid.get(row).get(col).equals(grid.get(row + 3).get(col))) {
                    winners.add(grid.get(row).get(col));
                }
            }
        }
        return winners;
    }

    private Set<UUID> CheckDiagonalWin(List<List<UUID>> grid, int rowCount, int colCount){
        Set<UUID> winners = new HashSet<>();
        // check for a diagonal win (positive slope)
        for (int row=0; row < rowCount - 3; row++) {
            for (int col=0; col < colCount - 3; col++) {
                if (!grid.get(row).get(col).equals(ADMIN_UUID) &&
                        grid.get(row).get(col).equals(grid.get(row + 1).get(col + 1)) &&
                        grid.get(row).get(col).equals(grid.get(row + 2).get(col + 2)) &&
                        grid.get(row).get(col).equals(grid.get(row + 3).get(col + 3))) {
                    winners.add(grid.get(row).get(col));
                }
            }
        }
        // check for a diagonal win (negative slope)
        for (int row=0; row<rowCount - 3; row++) {
            for (int col=3; col<colCount; col++) {
                if (!grid.get(row).get(col).equals(ADMIN_UUID) &&
                        grid.get(row).get(col).equals(grid.get(row + 1).get(col - 1)) &&
                        grid.get(row).get(col).equals(grid.get(row + 2).get(col - 2)) &&
                        grid.get(row).get(col).equals(grid.get(row + 3).get(col - 3))) {
                    winners.add(grid.get(row).get(col));
                }
            }
        }
        return winners;
    }
}
