package gameplay.dao;

import gameplay.models.Connect4Game;
import gameplay.models.User;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public interface UserDao extends CassandraRepository<User> {
    @Query("SELECT * FROM gg_user LIMIT 20")
    List<User> getList();

    @Query("SELECT * FROM gg_user WHERE id=?0")
    User getDetail(UUID id);
}
