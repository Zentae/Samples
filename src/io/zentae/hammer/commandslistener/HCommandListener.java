package io.zentae.hammer.commandslistener;

import io.zentae.hammer.utils.HUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HCommandListener implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player)sender;

            if(!player.hasPermission(HUtils.getPermission())) {
                player.sendMessage(HUtils.getPrefix() + " §cVous n'avez pas la permission");
                return true;
            }

            if(args.length > 2) {
                if(args[0].equalsIgnoreCase("give")) {
                    if(!NumberUtils.isNumber(args[1])) {
                        player.sendMessage(HUtils.getPrefix() + " §cVeuillez saisir un nombre entier");
                        return true;
                    }

                    if(Bukkit.getPlayer(args[2]) == null) {
                        player.sendMessage(HUtils.getPrefix() + " §cCe joueur est hors-ligne ou n'existe pas !");
                        return true;
                    }

                    int quantity = Integer.parseInt(args[1]);
                    Player receiver = Bukkit.getPlayer(args[2]);

                    player.sendMessage(HUtils.getPrefix() + " §fVous avez donné §ax" + quantity + " " + HUtils.getHammerName() + " §fà §b" + receiver.getName());

                    receiver.getInventory().addItem(HUtils.hammer(quantity));
                    receiver.sendMessage(HUtils.getPrefix() + " §fVous avez reçu §ax" + quantity + " " + HUtils.getHammerName() + " §f!");
                    receiver.playSound(receiver.getLocation(), Sound.NOTE_PLING, 2L, 2L);

                    return true;
                }
            }

            displayHelp(player);
        } else {
            if(args.length > 2) {
                if(args[0].equalsIgnoreCase("give")) {
                    if(!NumberUtils.isNumber(args[1])) {
                        sender.sendMessage("Veuillez saisir un nombre entier");
                        return true;
                    }

                    if(Bukkit.getPlayer(args[2]) == null) {
                        sender.sendMessage("Ce joueur est hors-ligne ou n'existe pas !");
                        return true;
                    }

                    int quantity = Integer.parseInt(args[1]);
                    Player receiver = Bukkit.getPlayer(args[2]);

                    sender.sendMessage("Vous avez donné x" + quantity + " HAMMER à " + receiver.getName());

                    receiver.getInventory().addItem(HUtils.hammer(quantity));
                    receiver.sendMessage(HUtils.getPrefix() + " §fVous avez reçu §ax" + quantity + " " + HUtils.getHammerName() + " §f!");
                    receiver.playSound(receiver.getLocation(), Sound.NOTE_PLING, 2L, 2L);

                    return true;
                }
            }

            sender.sendMessage("--> /hammer give [Quantité] [Joueur]");
        }
        return false;
    }

    private void displayHelp(Player player) {
        player.sendMessage("§6------------------------------");
        player.sendMessage("§e/hammer §f- Vous affiche cette page d'aide…");
        player.sendMessage("§e/hammer §bgive [Quantité] [Joueur] §f- Permet de donner des marteaux aux joueurs");
        player.sendMessage("§6------------------------------");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 2L, 2L);
    }
}
