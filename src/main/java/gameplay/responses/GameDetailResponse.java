package gameplay.responses;

import gameplay.models.Connect4Game;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public class GameDetailResponse extends ServiceResponse {
    private Connect4Game connect4Game;

    public Connect4Game getConnect4Game() {
        return connect4Game;
    }

    public void setConnect4Game(Connect4Game connect4Game) {
        this.connect4Game = connect4Game;
    }
}
