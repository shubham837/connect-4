package gameplay.responses;

import gameplay.models.Board;

import java.util.UUID;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public class BoardDetailResponse extends ServiceResponse {
    private Board board;

    private UUID winnerId;

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(UUID winnerId) {
        this.winnerId = winnerId;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }
}
