package plugin.findPairs.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import plugin.findPairs.mapper.data.PlayerScore;

public interface PlayerScoreMapper {

  @Select("select * from find_pairs_score")
  List<PlayerScore> selectList();

  @Insert("insert find_pairs_score(player_name, score, difficulty, registered_at) values (#{playerName}, #{score}, #{difficulty}, now())")
  int insert(PlayerScore playerScore);
}
