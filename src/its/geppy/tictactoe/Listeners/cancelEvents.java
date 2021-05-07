package its.geppy.tictactoe.Listeners;

import its.geppy.tictactoe.TicTacToe;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class cancelEvents implements Listener {

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {

        ItemStack heldItem = e.getPlayer().getInventory().getItemInMainHand();

        if (heldItem.hasItemMeta())
            if (Objects.equals(heldItem.getItemMeta(), TicTacToe.getChallengeItem().getItemMeta()))
                e.setCancelled(true);


    }

}
