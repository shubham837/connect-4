package gameplay.errors;

import java.util.Set;
import java.util.UUID;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public class MultipleWinnerException extends Exception {
    Set<UUID> winnerList;

    public MultipleWinnerException(String message, Set<UUID> winnerList) {
        super(message);
        this.winnerList = winnerList;
    }
}
