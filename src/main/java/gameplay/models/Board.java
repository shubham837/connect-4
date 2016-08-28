package gameplay.models;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by shubham.singhal on 26/08/16.
 */
@Table(value = "gg_board")
public class Board {
    @PrimaryKeyColumn(name="id",ordinal = 0,type = PrimaryKeyType.PARTITIONED)
    private UUID id;

    @Column(value = "board_state")
    private List<List<UUID>> boardState;

    @Column(value = "next_move_user_id")
    private UUID nextMoveUserId;

    @Column(value = "created_ts")
    private Date createdTs;

    @Column(value = "updated_ts")
    private Date updatedTs;

    @Column(value = "last_modified_by")
    private UUID lastModifiedBy;

    public Date getUpdatedTs() {
        return updatedTs;
    }

    public void setUpdatedTs(Date updatedTs) {
        this.updatedTs = updatedTs;
    }

    public Date getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Date createdTs) {
        this.createdTs = createdTs;
    }

    public UUID getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(UUID lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public List<List<UUID>> getBoardState() {
        return boardState;
    }

    public void setBoardState(List<List<UUID>> boardState) {
        this.boardState = boardState;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getNextMoveUserId() {
        return nextMoveUserId;
    }

    public void setNextMoveUserId(UUID nextMoveUserId) {
        this.nextMoveUserId = nextMoveUserId;
    }


}
