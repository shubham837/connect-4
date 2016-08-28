package gameplay.errors;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public class InvalidBoardStateException extends Exception {
    public InvalidBoardStateException(String message) {
        super(message);
    }
}
