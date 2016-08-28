package gameplay.models;

import gameplay.models.enums.GameType;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by shubham.singhal on 26/08/16.
 */
@Table(value = "gg_connect_four")
public class Connect4Game {
    @PrimaryKeyColumn(name="id",ordinal = 0,type = PrimaryKeyType.PARTITIONED)
    private UUID id;

    @Column(value = "board_id")
    private UUID boardId;

    @Column(value = "row_count")
    private int rowCount;

    @Column(value = "col_count")
    private int colCount;

    @Column(value = "player_count")
    private int numberOfPlayers;

    @Column(value = "user_ids")
    private List<UUID> users;

    @Column(value = "game_type")
    private String gameType;

    @Column(value = "created_ts")
    private Date createdTs;

    @Column(value = "updated_ts")
    private Date updatedTs;

    @Column(value = "winner_id")
    private UUID winnerId;

    @Column(value = "last_modified_by")
    private UUID lastModifiedBy;

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColCount() {
        return colCount;
    }

    public void setColCount(int colCount) {
        this.colCount = colCount;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(UUID winnerId) {
        this.winnerId = winnerId;
    }

    public Date getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Date createdTs) {
        this.createdTs = createdTs;
    }

    public Date getUpdatedTs() {
        return updatedTs;
    }

    public void setUpdatedTs(Date updatedTs) {
        this.updatedTs = updatedTs;
    }

    public UUID getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(UUID lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }


    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public List<UUID> getUsers() {
        return users;
    }

    public void setUsers(List<UUID> users) {
        this.users = users;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public void setBoardId(UUID boardId) {
        this.boardId = boardId;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
}
