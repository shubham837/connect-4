package gameplay.responses;

import gameplay.models.User;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public class UserDetailResponse extends ServiceResponse{
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
