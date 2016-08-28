package gameplay.calc;

import gameplay.models.enums.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public class Connect4GameFactory {
    private static final Logger log = LoggerFactory.getLogger(Connect4GameFactory.class);

    private static Connect4GameFactory instance;
    private Connect4GameFactory(){}

    public static Connect4GameFactory getInstance()
    {
        if (instance == null)
        {
            synchronized(Connect4GameFactory.class)
            {
                if (instance == null)
                {
                    log.info("Initializing Connect4GameFactory");
                    instance = new Connect4GameFactory();
                }
            }
        }

        return instance;
    }

    public Iconnect4Game getConnect4Game(GameType gameType){
        Iconnect4Game connect4Game = null;
        if (gameType==GameType.POP_10) {
            connect4Game = null;
        } else if (gameType == GameType.POP_OUT) {
            connect4Game = new PopOutGame();
        }
        return connect4Game;
    }

}
