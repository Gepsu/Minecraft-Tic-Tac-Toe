package its.geppy.tictactoe.Listeners;

import its.geppy.tictactoe.TicTacToe;
import its.geppy.tictactoe.Utilities.GameData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class onPlayerQuitEvent implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {

        TicTacToe.activeGames.stream().filter(game -> {
            if (game.getChallenger().equals(e.getPlayer()))
                return true;

            if (game.getOpponent().equals(e.getPlayer()))
                return true;

            return false;
        }).findFirst().ifPresent(GameData::endGame);

    }

}
