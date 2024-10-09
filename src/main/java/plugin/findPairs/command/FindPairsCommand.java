package plugin.findPairs.command;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import plugin.findPairs.PlayerScoreData;
import plugin.findPairs.Main;
import plugin.findPairs.mapper.data.PlayerScore;

/**
 * 神経衰弱ゲームを開始するコマンドです。コマンドを実行するとEntityペアが複数組出現します。
 * 制限時間内にペアを揃えることで得点を獲得し、高得点を目指すゲームです。
 * 結果はプレイヤー名、得点、難易度、日時などで保存されます。
 */
public class FindPairsCommand extends BaseCommand implements Listener {

  public static final int NUMBER_OF_PAIRS = 5;
  public static final int COUNT_DOWN = 5;
  public static final int GAME_TIME = 30;
  public static final int SCORE = 0;
  public static final String EASY = "easy";
  public static final String NORMAL = "normal";
  public static final String HARD = "hard";
  public static final String NONE = "none";
  private static final String LIST = "list";

  private final Main main;
  private final PlayerScoreData playerScoreData = new PlayerScoreData();
  private final Map<Integer, List<Entity>> entityPairsMap = new HashMap<>();
  private int countDown;
  private int gameTime;
  private int score;

  public FindPairsCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String s, String[] args) {
    //最初の引数が「list」だったらスコア一覧を表示して処理を終了する。
    if (args.length == 1 && LIST.equals(args[0])) {
      sendPlayerScoreList(player);
      return false;
    }
    if (getDifficulty(player, args).equals(NONE)) {
      return false;
    }
    countDown = COUNT_DOWN;
    Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
      if (countDown <= 0) {
        Runnable.cancel();
        player.sendTitle("ゲームスタート！", "", 0, 10, 0);

        gameTime = GAME_TIME;
        score = SCORE;
        setPlayerStatusOfHardMode(player, args);
        spawnEntityPairs(player, getDifficulty(player, args));
        gamePlay(player, args);
        return;
      }
      player.sendTitle(String.valueOf(countDown), "右クリックしてペアを見つけろ！", 0, 20, 0);
      countDown -= 1;
    }, 0, 20);
    return true;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String s,
      String[] args) {
    return false;
  }

  /**
   * 現在登録されているスコアの一覧をメッセージに送る。
   *
   * @param player 　プレイヤー
   */
  private void sendPlayerScoreList(Player player) {
    List<PlayerScore> playerScoreList = playerScoreData.selectList();
    for (PlayerScore playerScore : playerScoreList) {
      player.sendMessage(playerScore.getId() + " | "
          + playerScore.getPlayerName() + " | "
          + playerScore.getScore() + " | "
          + playerScore.getDifficulty() + " | "
          + playerScore.getRegisteredAt()
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
  }

  /**
   * コマンドを入力する際に、引数として難易度（easy,normal,hard）を入力する。
   *
   * @param player 　プレイヤー
   * @param args   　難易度
   * @return 難易度
   */
  String getDifficulty(Player player, String[] args) {
    if (args.length == 1 && (EASY.equals(args[0]) || NORMAL.equals(args[0]) || HARD.equals(
        args[0]))) {
      return args[0];
    }
    player.sendMessage(ChatColor.RED
        + "実行できません。コマンド引数の1つ目に難易度指定が必要です。[easy, normal, hard]");
    return NONE;
  }

  /**
   * 難易度がhard時にプレイヤーの初期設定を行います。
   * @param player　プレイヤー
   * @param args　難易度
   */
  private void setPlayerStatusOfHardMode(Player player, String[] args) {
    if (getDifficulty(player, args).equals(HARD)) {
      player.setHealth(20);
      player.setFoodLevel(20);
      PlayerInventory inventory = player.getInventory();
      inventory.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
      inventory.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
      inventory.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
      inventory.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
    }
  }

  /**
   * ペア登録されたEntityを出現させる。 出現したペアはentityPairsMapの値にListとして登録される。
   * Entityは難易度に応じて異なるEntityが出現する。
   *
   * @param player     　コマンドを実行したプレイヤー
   * @param difficulty 　　　 難易度
   */
  private void spawnEntityPairs(Player player, String difficulty) {
    for (int i = 0; i < FindPairsCommand.NUMBER_OF_PAIRS; i++) {
      Entity spawnFirstEntity = player.getWorld()
          .spawnEntity(getFirstEntitySpawnLocation(player), getEnemy(difficulty));
      spawnFirstEntity.setInvulnerable(true);
      Entity spawnSecondEntity = player.getWorld()
          .spawnEntity(getSecondEntitySpawnLocation(player), getEnemy(difficulty));
      spawnSecondEntity.setInvulnerable(true);
      entityPairsMap.put(i, List.of(spawnFirstEntity, spawnSecondEntity));
    }
  }

  /**
   * ペアの片方のEntityの出現場所を取得する。
   *
   * @param player 　プレイヤー
   * @return　出現場所
   */
  private Location getFirstEntitySpawnLocation(Player player) {
    Location playerLocation = player.getLocation();
    int randomX1 = new SplittableRandom().nextInt(5);
    int randomZ1 = new SplittableRandom().nextInt(5);

    return new Location(player.getWorld()
        , playerLocation.getX() + randomX1
        , playerLocation.getY()
        , playerLocation.getZ() + randomZ1);
  }

  /**
   * もう片方のペアのEntityの出現場所を取得する。
   *
   * @param player 　プレイヤー
   * @return　出現場所
   */
  private Location getSecondEntitySpawnLocation(Player player) {
    Location playerLocation = player.getLocation();
    int randomX2 = new SplittableRandom().nextInt(5);
    int randomZ2 = new SplittableRandom().nextInt(5);

    return new Location(player.getWorld()
        , playerLocation.getX() + randomX2
        , playerLocation.getY()
        , playerLocation.getZ() + randomZ2);
  }

  /**
   * 難易度に応じたEntityTypeを取得する。
   *
   * @param difficulty 　難易度
   * @return　EntityType
   */
  private EntityType getEnemy(String difficulty) {
    return switch (difficulty) {
      case "normal" -> EntityType.PIG;
      case "hard" -> EntityType.SKELETON;
      default -> EntityType.TURTLE;
    };
  }

  /**
   * FindPairsゲームを実行する。
   *
   * @param player 　プレイヤー
   */
  private void gamePlay(Player player, String[] args) {
    Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
      if (gameTime <= 0) {
        Runnable.cancel();
        player.sendTitle("ゲームが終了しました。", "合計：" + score + "点", 0, 60, 0);
        entityPairsMap.values().forEach(entityList -> entityList.forEach(Entity::remove));
        entityPairsMap.clear();

        playerScoreData.insert(
            new PlayerScore(player.getName(), score, getDifficulty(player, args)));
        return;
      }
      player.sendMessage("残り時間：" + gameTime + "秒");
      gameTime -= 5;
    }, 0, 5 * 20);
  }

  @EventHandler
  public void searchEntityPairs(PlayerInteractEntityEvent e) {
    // メインハンド以外のイベントの場合、処理を終了する。
    if (e.getHand() != EquipmentSlot.HAND) {
      return;
    }
    Entity entity = e.getRightClicked();
    entityPairsMap.forEach((key, value) -> {
      if (value.getFirst().equals(entity) || value.getLast().equals(entity)) {
        entity.setGlowing(true);
        e.getPlayer().sendMessage(key + "番");
      }
    });
    int countGlowing = countGlowingEntity();
    checkGlowingPairs(countGlowing);
  }

  /**
   * Glowing状態のEntityの数をカウントする。
   *
   * @return　Glowing状態のEntityの数
   */
  public int countGlowingEntity() {
    int count = 0;
    for (List<Entity> listOfPairsEntity : entityPairsMap.values()) {
      for (Entity entity : listOfPairsEntity) {
        if (entity.isGlowing()) {
          count++;
        }
      }
    }
    return count;
  }

  /**
   * 正しいEntityペアを選択できたらEntityを消滅させ、得点を加算する。別のEntityを選択したらGlowing状態をリセットする。
   *
   * @param countGlowing Glowing状態のEntityの数
   */
  private void checkGlowingPairs(int countGlowing) {
    if (countGlowing == 2) {
      entityPairsMap.entrySet().removeIf(entry -> {
        Entity firstEntity = entry.getValue().get(0);
        Entity secondEntity = entry.getValue().get(1);

        if (firstEntity.isGlowing() && secondEntity.isGlowing()) {
          switch (firstEntity.getType()) {
            case PIG -> score += 10;
            case SKELETON -> score += 15;
            default -> score += 5;
          }
          removeEffect(entry, firstEntity, secondEntity);
          firstEntity.remove();
          secondEntity.remove();
          return true;
        } else if (firstEntity.isGlowing() || secondEntity.isGlowing()) {
          entityPairsMap.values().stream().flatMap(Collection::stream)
              .forEach(entity -> entity.setGlowing(false));
        }
        return false;
      });
    }
  }

  /**
   * 消滅エフェクトを出現させる。
   *
   * @param entry        　　　　　消滅したペアマップ情報
   * @param firstEntity  　消滅したペアの一方のEntity
   * @param secondEntity 　消滅したペアのもう一方のEntity
   */
  private static void removeEffect(Entry<Integer, List<Entity>> entry, Entity firstEntity,
      Entity secondEntity) {
    firstEntity.getWorld().spawnParticle(Particle.CLOUD, entry.getValue().get(0).getLocation()
        , 50, 0.5, 0.5, 0.5, 1);
    secondEntity.getWorld().spawnParticle(Particle.CLOUD, entry.getValue().get(1).getLocation()
        , 50, 0.5, 0.5, 0.5, 1);
    firstEntity.getWorld()
        .playSound(entry.getValue().get(0).getLocation(), Sound.ENTITY_PLAYER_LEVELUP
            , 1.0f, 1.0f);
    secondEntity.getWorld()
        .playSound(entry.getValue().get(1).getLocation(), Sound.ENTITY_PLAYER_LEVELUP
            , 1.0f, 1.0f);
  }
}
