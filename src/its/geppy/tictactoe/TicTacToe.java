package its.geppy.tictactoe;

import its.geppy.tictactoe.Commands.AllCommands;
import its.geppy.tictactoe.Listeners.cancelEvents;
import its.geppy.tictactoe.Listeners.onPlayerInteractEvent;
import its.geppy.tictactoe.Listeners.onPlayerQuitEvent;
import its.geppy.tictactoe.Utilities.GameData;
import net.venturekraft.VentureKraftAPI.Processes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TicTacToe extends JavaPlugin implements CommandExecutor {

    public static List<GameData> activeGames = new ArrayList<>();

    private static TicTacToe mainInstance;
    public static TicTacToe getMain() { return mainInstance; }

    public static int maxIdleTime;
    public static int maxDistanceFromBoard;

    public Scoreboard scoreboard;

    @Override
    public void onEnable() {

        mainInstance = this;

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        Objects.requireNonNull(getCommand("tictactoe")).setExecutor(new AllCommands());
        Objects.requireNonNull(getCommand("ttt")).setExecutor(new AllCommands());

        getServer().getPluginManager().registerEvents(new cancelEvents(), this);
        getServer().getPluginManager().registerEvents(new onPlayerInteractEvent(), this);
        getServer().getPluginManager().registerEvents(new onPlayerQuitEvent(), this);

        maxIdleTime = getMain().getConfig().getInt("maximum-idle-time-in-seconds") * 20;
        maxDistanceFromBoard = getMain().getConfig().getInt("maximum-distance-from-board");

        try {
            scoreboard = getServer().getScoreboardManager().getMainScoreboard();
            Team team = scoreboard.registerNewTeam("ttt_nocollision");
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team.setCanSeeFriendlyInvisibles(false);
        } catch (Exception ignored) {}


    }

    @Override
    public void onDisable() {

        for (int i = 0; i < activeGames.size(); i++) {
            try {
                activeGames.get(i).endGame();
            } catch (Exception ignored) { }
        }

    }

    public static ItemStack getChallengeItem() {
        return Processes.buildItem("&6&lStick&f&lTac&6&lToe", null, Material.STICK, 1);
    }

}
