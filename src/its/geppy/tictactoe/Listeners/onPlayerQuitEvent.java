package its.geppy.tictactoe.Listeners;

import its.geppy.tictactoe.TicTacToe;
import its.geppy.tictactoe.Utilities.GameData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class onPlayerQuitEvent implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {

        GameData game = TicTacToe.activeGames.stream().filter(g -> {
            if (g.getChallenger().equals(e.getPlayer()))
                return true;

            if (g.getOpponent().equals(e.getPlayer()))
                return true;

            return false;
        }).findFirst().orElse(null);

        if (game == null)
            return;

        if (game.getOpponent() instanceof Player) {
            GameData.Winner winner = e.getPlayer() == game.getOpponent() ? GameData.Winner.CHALLENGER : GameData.Winner.OPPONENT;
            game.queueGameEnd(game.getTaskID(), winner);
            return;
        }

        game.endGame();

    }

}
