package gameplay.responses;

import gameplay.errors.ServiceError;

import java.util.List;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public abstract class ServiceResponse {
        private List<ServiceError> errors;

    public List<ServiceError> getErrors() {
        return errors;
    }

    public void setErrors(List<ServiceError> errors) {
        this.errors = errors;
    }
}
