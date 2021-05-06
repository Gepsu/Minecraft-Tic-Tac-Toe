package its.geppy.tictactoe.Listeners;

import its.geppy.tictactoe.TicTacToe;
import its.geppy.tictactoe.Utilities.BoardManager;
import its.geppy.tictactoe.Utilities.GameData;
import its.geppy.tictactoe.Utilities.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;

public class onPlayerInteractEvent implements Listener {

    @EventHandler
    public void onPlayerRightClickLivingEntity(PlayerInteractAtEntityEvent e) {

        if (!(e.getRightClicked() instanceof LivingEntity))
            return;

        if (!e.getHand().equals(EquipmentSlot.HAND))
            return;

        Player player = e.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (!player.hasPermission("ttt.play")) {
            player.sendMessage(ChatColor.RED + "You don't have a permission to play.");
            return;
        }

        if (!heldItem.hasItemMeta())
            return;

        if (!Objects.equals(heldItem.getItemMeta(), TicTacToe.getChallengeItem().getItemMeta()))
            return;

        double min = TicTacToe.getMain().getConfig().getDouble("minimum-distance-between-boards");
        if (player.getNearbyEntities(min, min, min).stream().anyMatch(ent -> Objects.equals(ent.getCustomName(), "tictactoe_stand")))
            return;

        LivingEntity opponent = (LivingEntity) e.getRightClicked();

        if (!(opponent instanceof Player)){
            List<String> allowedOpponents = TicTacToe.getMain().getConfig().getStringList("list-of-ai-opponents");
            String found = allowedOpponents.stream()
                    .filter(o -> opponent.getType().toString().equals(o))
                    .findFirst()
                    .orElse(null);
            if (found == null)
                return;
        } else {
            if (!opponent.hasPermission("ttt.play")) {
                player.sendMessage(ChatColor.RED + "They don't have a permission to play.");
                return;
            }
        }

        if (TicTacToe.activeGames.stream().anyMatch(game -> {
            if (game.getChallenger().equals(player))
                return true;

            if (game.getOpponent().equals(player))
                return true;

            if (game.getChallenger().equals(opponent))
                return true;

            if (game.getOpponent().equals(opponent))
                return true;

            return false;
        }))
            return;

        SoundManager.playSound(player, Sound.BLOCK_BELL_RESONATE);
        if (opponent instanceof Player)
            SoundManager.playSound((Player) opponent, Sound.BLOCK_BELL_RESONATE);

        BoardManager.buildBoard(player, opponent);

    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        ItemStack heldItem = e.getPlayer().getInventory().getItemInMainHand();

        if (!heldItem.hasItemMeta())
            return;

        if (!Objects.equals(heldItem.getItemMeta(), TicTacToe.getChallengeItem().getItemMeta()))
            return;

        if (!e.getHand().equals(EquipmentSlot.HAND))
            return;

        GameData game = TicTacToe.activeGames.stream()
                .filter(g -> g.getChallenger().equals(e.getPlayer()) || g.getOpponent().equals(e.getPlayer()))
                .findFirst()
                .orElse(null);

        if (game == null)
            return;

        if (game.isPreGame())
            return;

        if (!game.getAI()) {

            if (game.getTurn() == GameData.Turn.X && !game.getChallenger().equals(e.getPlayer()))
                return;

            if (game.getTurn() == GameData.Turn.O && !game.getOpponent().equals(e.getPlayer()))
                return;

        }

        if (e.getPlayer().equals(game.getChallenger()) || e.getPlayer().equals(game.getOpponent())) {
            Vector clickable = BoardManager.getClickable(e.getPlayer(), game);

            if (clickable != null) {
                SoundManager.playSound(e.getPlayer(), Sound.BLOCK_STONE_BUTTON_CLICK_ON);
                BoardManager.clicked(clickable, game);
            }
        }

    }


}
