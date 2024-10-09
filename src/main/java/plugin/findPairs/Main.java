package plugin.findPairs;

import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.findPairs.command.FindPairsCommand;

public final class Main extends JavaPlugin {

  @Override
  public void onEnable() {
    FindPairsCommand findPairsCommand = new FindPairsCommand(this);
    Bukkit.getPluginManager().registerEvents(findPairsCommand, this);
    getCommand("findPairs").setExecutor(findPairsCommand);
  }

}
