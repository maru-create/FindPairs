CREATE TABLE IF NOT EXISTS find_pairs_score (
    id int auto_increment, 
    player_name varchar(100), 
    score int, 
    difficulty varchar(30), 
    registered_at datetime, 
    primary key(id)
);