package io.zentae.hammer.utils;

import com.massivecraft.factions.*;
import io.zentae.hammer.H;
import io.zentae.hammer.utils.enums.Direction;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HUtils {

    public static void registerCraft() {
        ShapedRecipe hammer = new ShapedRecipe(hammer(1));
        hammer.shape("EEE", "ESE", " S ");
        hammer.setIngredient('E', Material.DRAGON_EGG);
        hammer.setIngredient('S', Material.STICK);

        H.getInstance().getServer().addRecipe(hammer);
    }

    public static void removeDurability(Player player, ItemStack magichoe) {

        int durability = H.getInstance().getConfiguration().getInt("hammer_usages");
        if(durability != -1) {
            List<String> nLore = new ArrayList<>();

            int lineNumber = 0;
            for(String line : H.getInstance().getConfiguration().getStringList("hammer_lore")) {
                if(line.contains("%hammer_usages%")) {
                    int usages = getUniqueNumerals(magichoe.getItemMeta().getLore().get(lineNumber));
                    if(usages > durability ||usages == -1) { usages = durability; }

                    int i = usages - 1;
                    if(i <= 0) {
                        player.getInventory().remove(magichoe);
                        player.updateInventory();

                        return;
                    }

                    line = line.replace("%hammer_usages%", String.valueOf(i));
                }

                nLore.add(ChatColor.translateAlternateColorCodes('&', line));
                lineNumber++;
            }

            ItemMeta itemMeta = magichoe.getItemMeta();
            itemMeta.setLore(nLore);

            magichoe.setItemMeta(itemMeta);

            double toRemoveDurability = magichoe.getType().getMaxDurability() / (double)durability;
            magichoe.setDurability((short) (magichoe.getDurability() + toRemoveDurability));

            player.updateInventory();
        }

    }

    public static int getUniqueNumerals(String string) {
        string = ChatColor.stripColor(string);

        String[] chars = string.split("");
        StringBuilder sb = new StringBuilder();

        int index = 0;
        for(String c : chars) {
            if(NumberUtils.isDigits(c)) {
                if(chars[index - 1] != null) {
                    if(!chars[index - 1].equalsIgnoreCase("&")) {
                        sb.append(chars[index]);
                    }
                }
            }
            index++;
        }
        if(sb.toString().isEmpty()) return -1;
        return Integer.parseInt(sb.toString());
    }

    public static void processBlocks(Block[] blocks, Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        boolean hasFullyBroke = true;
        for(Block b : blocks) {
            boolean isBreakable = true;
            if(b.getType() == Material.BEDROCK || b.getType() == Material.BARRIER || b.getType() == Material.MOB_SPAWNER) continue;

            for(Faction f : Factions.getInstance().getAllFactions()) {
                if(fPlayer.hasFaction()) {
                    if(fPlayer.getFaction().getTag().equalsIgnoreCase(f.getTag())) { break; }
                }
                for(FLocation l : f.getAllClaims()) { if(inChunk(l.getChunk(), b)) { isBreakable = false; hasFullyBroke = false; break; } }
                if(!isBreakable) break;
            }

            if(isBreakable) b.breakNaturally();
        }

        if(!hasFullyBroke) {
            player.sendMessage(getPrefix() + " §cLe hammer est inutilisable dans des claims de factions §c§oennemies§c…");
            player.sendMessage(getPrefix() + " §cCertains blocks n'ont pas été cassés.");
        }
    }

    private static boolean inChunk(Chunk c, Block b) {
        Chunk bc = b.getChunk();
        return c.getX() == bc.getX() && c.getZ() == bc.getZ();
    }

    public static Direction getCardinalDirection(Player player) {
        double rotation = player.getLocation().getYaw() - 180;
        if (rotation < 0) { rotation += 360.0; }

        double pitch = player.getLocation().getPitch();
        if(pitch >= 19.0) { return Direction.DOWN; }
        if(pitch <= -16.0) { return Direction.UP; }

        if (0 <= rotation && rotation < 22.5) { return Direction.NORTH; }

        if (22.5 <= rotation && rotation < 67.5) { return Direction.EAST; }
        if (67.5 <= rotation && rotation < 112.5) { return Direction.EAST; }

        if (112.5 <= rotation && rotation < 157.5) { return Direction.SOUTH; }
        if (157.5 <= rotation && rotation < 202.5) { return Direction.SOUTH; }

        if (202.5 <= rotation && rotation < 247.5) { return Direction.WEST; }
        if (247.5 <= rotation && rotation < 292.5) { return Direction.WEST; }

        if (292.5 <= rotation && rotation < 337.5) { return Direction.NORTH; }
        if (337.5 <= rotation && rotation <= 360) { return Direction.NORTH; }

        return null;
    }

    public static String getPrefix() { return ChatColor.translateAlternateColorCodes('&', H.getInstance().getConfiguration().getString("hammer_prefix")); }
    public static String getPermission() { return H.getInstance().getConfiguration().getString("hammer_permission"); }
    public static String getHammerName() { return ChatColor.translateAlternateColorCodes('&', H.getInstance().getConfiguration().getString("hammer_name")); }
    private static List<String> getHammerLore() {
        List<String> ll = new ArrayList<>();
        int usages = H.getInstance().getConfiguration().getInt("hammer_usages");

        for(String line : H.getInstance().getConfiguration().getStringList("hammer_lore")) {
            if(usages != -1) {
                if(line.contains("%hammer_usages%")) line = line.replace("%hammer_usages%", String.valueOf(usages));
            } else {
                if(line.contains("%hammer_usages%")) line = line.replace("%hammer_usages%", "∞");
            }
            ll.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        return ll;
    }

    public static ItemStack hammer(int quantity) {
        ItemStack it = new ItemStack(Material.DIAMOND_PICKAXE, quantity);
        ItemMeta im = it.getItemMeta();

        im.setDisplayName(getHammerName());
        im.setLore(getHammerLore());

        if(H.getInstance().getConfiguration().getBoolean("hammer_isenchanted")) {
            im.addEnchant(Enchantment.DURABILITY, 1, true);
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        it.setItemMeta(im);
        return it;
    }
}
