package its.geppy.tictactoe.Listeners;

import its.geppy.tictactoe.TicTacToe;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class cancelEvents implements Listener {

    @EventHandler
    public void onVillagerInteract(InventoryOpenEvent e) {

        if (!(e.getInventory().getHolder() instanceof Villager))
            return;

        ItemStack heldItem = e.getPlayer().getInventory().getItemInMainHand();

        if (!heldItem.hasItemMeta())
            return;

        if (!Objects.equals(heldItem.getItemMeta(), TicTacToe.getChallengeItem().getItemMeta()))
            return;

        e.setCancelled(true);
    }

}
