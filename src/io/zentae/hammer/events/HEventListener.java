package io.zentae.hammer.events;

import io.zentae.hammer.utils.HUtils;
import io.zentae.hammer.utils.enums.Direction;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HEventListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        ItemStack current = player.getItemInHand();
        Block block = e.getBlock();

        if(current == null) return;
        if(!current.hasItemMeta()) return;
        if(!current.getItemMeta().hasDisplayName()) return;

        ItemMeta im = current.getItemMeta();
        if(!im.getDisplayName().equalsIgnoreCase(HUtils.getHammerName())) return;

        if(block.getType() == Material.BEDROCK || block.getType() == Material.BARRIER || block.getType() == Material.MOB_SPAWNER) return;

        Direction direction = HUtils.getCardinalDirection(player);
        Block[] toProcess = new Block[8];

        if(direction == Direction.UP || direction == Direction.DOWN) {
            toProcess[0] = block.getRelative(BlockFace.NORTH);
            toProcess[1] = block.getRelative(BlockFace.NORTH_EAST);
            toProcess[2] = block.getRelative(BlockFace.EAST);
            toProcess[3] = block.getRelative(BlockFace.SOUTH_EAST);
            toProcess[4] = block.getRelative(BlockFace.SOUTH);
            toProcess[5] = block.getRelative(BlockFace.SOUTH_WEST);
            toProcess[6] = block.getRelative(BlockFace.WEST);
            toProcess[7] = block.getRelative(BlockFace.NORTH_WEST);

            HUtils.processBlocks(toProcess, player);
            HUtils.removeDurability(player, current);
        }

        if(direction == Direction.EAST || direction == Direction.WEST) {
            toProcess[0] = block.getRelative(BlockFace.UP);
            toProcess[1] = block.getRelative(BlockFace.DOWN);
            toProcess[2] = block.getRelative(BlockFace.NORTH);
            toProcess[3] = block.getRelative(BlockFace.SOUTH);
            toProcess[4] = block.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP);
            toProcess[5] = block.getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN);
            toProcess[6] = block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP);
            toProcess[7] = block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN);

            HUtils.processBlocks(toProcess, player);
            HUtils.removeDurability(player, current);
        }

        if(direction == Direction.NORTH || direction == Direction.SOUTH) {
            toProcess[0] = block.getRelative(BlockFace.UP);
            toProcess[1] = block.getRelative(BlockFace.DOWN);
            toProcess[2] = block.getRelative(BlockFace.EAST);
            toProcess[3] = block.getRelative(BlockFace.WEST);
            toProcess[4] = block.getRelative(BlockFace.EAST).getRelative(BlockFace.UP);
            toProcess[5] = block.getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN);
            toProcess[6] = block.getRelative(BlockFace.WEST).getRelative(BlockFace.UP);
            toProcess[7] = block.getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN);

            HUtils.processBlocks(toProcess, player);
            HUtils.removeDurability(player, current);
        }
    }
}
