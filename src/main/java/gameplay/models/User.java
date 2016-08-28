package gameplay.models;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;
import java.util.UUID;

/**
 * Created by shubham.singhal on 26/08/16.
 */
@Table(value = "gg_user")
public class User {
    @PrimaryKeyColumn(name="id",ordinal = 0,type = PrimaryKeyType.PARTITIONED)
    private UUID id;

    @Column(value = "name")
    private String name;

    @Column(value = "email")
    private String email;

    @Column(value = "created_ts")
    private Date createdTs;

    @Column(value = "updated_ts")
    private Date updatedTs;

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

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User patch(User newUser){
        if(newUser.getEmail() != null) {
            this.setEmail(newUser.getEmail());
        }
        if(newUser.getName() != null) {
            this.setName(newUser.getName());
        }
        this.setUpdatedTs(new Date());
        return this;
    }
}
