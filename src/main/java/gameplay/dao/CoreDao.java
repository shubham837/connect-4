package gameplay.dao;

import gameplay.models.Connect4Game;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public interface CoreDao extends CassandraRepository<Connect4Game> {
    @Query("SELECT * FROM gg_connect_four LIMIT 20")
    List<Connect4Game> getList();

    @Query("SELECT * FROM gg_connect_four WHERE id=?0")
    Connect4Game getDetail(UUID id);
}
