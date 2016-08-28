package gameplay.responses;

import gameplay.models.Connect4Game;

import java.io.Serializable;
import java.util.List;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public class GameListResponse extends ServiceResponse implements Serializable {
    private List<Connect4Game> connect4Games;

    public List<Connect4Game> getConnect4Games() {
        return connect4Games;
    }

    public void setConnect4Games(List<Connect4Game> connect4Games) {
        this.connect4Games = connect4Games;
    }
}
