package gameplay.errors;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public class ServiceError {
    private int errorCode;
    private String errorMessage;

    public ServiceError(int errorCode,String errorMessage){
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
