package gameplay.dao;

import gameplay.models.Board;
import gameplay.models.Connect4Game;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Created by shubham.singhal on 26/08/16.
 */
public interface BoardDao extends CassandraRepository<Board> {
    @Query("SELECT * FROM gg_board LIMIT 20")
    List<Board> getList();

    @Query("SELECT * FROM gg_board WHERE id=?0")
    Board getDetail(UUID id);
}
