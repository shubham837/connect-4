package gameplay.responses;

import gameplay.models.User;

import java.util.List;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public class UserListResponse extends ServiceResponse{
    private List<User> users;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
