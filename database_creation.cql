CREATE KEYSPACE IF NOT EXISTS gluck_games WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '2'}  AND durable_writes = true;


CREATE TABLE IF NOT EXISTS gluck_games.gg_user (

    id uuid,

    name text,

    email text,

    created_ts timestamp,

    updated_ts timestamp,

    PRIMARY KEY(id, email)

)WITH CLUSTERING ORDER BY (email DESC);


CREATE TABLE IF NOT EXISTS gluck_games.gg_board (

    id uuid PRIMARY KEY,

    board_state list<frozen<list<uuid>>> ,

    next_move_user_id uuid,

    created_ts timestamp,

    updated_ts timestamp,

    last_modified_by uuid,

);

CREATE TABLE IF NOT EXISTS gluck_games.gg_connect_four (

    id uuid,

    board_id uuid,

    row_count int,

    col_count int,

    player_count int,

    user_ids list<uuid>,

    game_type varchar,

    created_ts timestamp,

    updated_ts timestamp,

    last_modified_by uuid,

    PRIMARY KEY(id)
);
