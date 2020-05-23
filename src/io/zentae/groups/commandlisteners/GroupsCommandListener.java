package io.zentae.groups.commandlisteners;

import io.zentae.groups.Groups;
import io.zentae.groupsapi.GroupsAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import javax.xml.soap.Text;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GroupsCommandListener extends Command {

    private Groups main;

    public GroupsCommandListener(String name, Groups main) {
        super(name, "", "g", "groups");
        this.main = main;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;

            if(args.length == 0) displayHelp(player);
            if (args.length > 1) {
                if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("add")) {
                    ProxiedPlayer targetedPlayer = main.getProxy().getPlayer(args[1]);
                    if (targetedPlayer == null) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Le joueur " + args[1] + " n'est pas connecté !"));
                        return;
                    }

                    if (player.getName().equalsIgnoreCase(targetedPlayer.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous ne pouvez pas vous inviter dans votre propre groupe !"));
                        return;
                    }

                    if (GroupsAPI.isGroupMember(targetedPlayer.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Ce joueur est déjà dans un groupe !"));
                        return;
                    }

                    if(GroupsAPI.isGroupMember(player.getName()) && !GroupsAPI.isGroupLeader(player.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous n'êtes pas le leader du groupe !"));
                        return;
                    }

                    main.getGroupsRequesting().put(player, targetedPlayer);

                    player.sendMessage(new TextComponent(main.getPrefix("waiting") + "L'invitation à rejoindre votre groupe a été envoyé avec succès !"));

                    TextComponent acceptPart = new TextComponent("§a[§a§l✔§a] ");
                    acceptPart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§f/g §baccept").create()));
                    acceptPart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/g accept"));

                    TextComponent refusePart = new TextComponent("§c[§c§l✘§c]");
                    refusePart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§f/g §brefuse").create()));
                    refusePart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/g refuse"));

                    targetedPlayer.sendMessage(new TextComponent(main.getPrefix("info") + "Vous avez reçu une demande d'invitation au groupe de " + player.getName() + " "), acceptPart, refusePart);

                    main.getProxy().getScheduler().schedule(main, () -> {
                        if(main.getGroupsRequesting().containsKey(player) && main.getGroupsRequesting().containsValue(targetedPlayer)) {
                            main.getGroupsRequesting().remove(player, targetedPlayer);
                            player.sendMessage(new TextComponent(main.getPrefix("info") + "Votre demande d'invitation au groupe à expiré..."));
                        }

                    }, 120, TimeUnit.SECONDS);
                    return;
                }

                if (args[0].equalsIgnoreCase("lead")) {
                    ProxiedPlayer targetPlayer = main.getProxy().getPlayer(args[1]);

                    if (!GroupsAPI.isGroupMember(player.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous n'avez pas de groupe ? Faîtes /g invite <Pseudo> pour ajouter un joueur à votre groupe."));
                        return;
                    }

                    io.zentae.groupsapi.Groups group = GroupsAPI.getGroupByMember(player.getName());

                    if (!group.isLeader(player.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous n'êtes pas leader du groupe."));
                        return;
                    }

                    if (targetPlayer == null) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Le joueur doit être en ligne pour devenir leader du groupe..."));
                        return;
                    }

                    if (targetPlayer.getName().equalsIgnoreCase(player.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous êtes déjà le leader du groupe !"));
                        return;
                    }

                    if (!group.isMember(targetPlayer.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Ce joueur ne fait pas parti de votre groupe !"));
                        return;
                    }

                    main.notifyGroup(group, "§6[Groupe] §f" + targetPlayer.getName() + " a été défini leader (Par " + player.getName() + ").");
                    GroupsAPI.putPlayerLeader(player.getName(), targetPlayer.getName(), group);

                    return;
                }

                if (args[0].equalsIgnoreCase("kick")) {
                    ProxiedPlayer targetPlayer = main.getProxy().getPlayer(args[1]);

                    if (!GroupsAPI.isGroupMember(player.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous n'avez pas de groupe ? Faîtes /g invite <Pseudo> pour ajouter un joueur à votre groupe."));
                        return;
                    }

                    io.zentae.groupsapi.Groups group = GroupsAPI.getGroupByMember(player.getName());

                    if (!group.isLeader(player.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous n'êtes pas leader du groupe."));
                        return;
                    }

                    if (args[1].equalsIgnoreCase(player.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous ne pouvez pas vous kick. Envie de quitter le groupe ? Faîtes /g leave"));
                        return;
                    }

                    if (!group.isMember(targetPlayer.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Ce joueur ne fait pas parti de votre groupe !"));
                        return;
                    }

                    main.notifyGroup(group, "§6[Groupe] §f" + args[1] + " a été kick du groupe (Par " + player.getName() + ").");
                    GroupsAPI.leave(args[1], group);

                    if (group.getGroupSize() == 1) group.disbandGroup();
                }
            }

            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("accept")) {
                    if (!main.getGroupsRequesting().containsValue(player)) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous n'attendez l'invitation d'aucun groupe..."));
                        return;
                    }
                    ProxiedPlayer leader = null;

                    for (Map.Entry<ProxiedPlayer, ProxiedPlayer> entrySet : main.getGroupsRequesting().entrySet()) {
                        if (entrySet.getValue() == player) {
                            leader = entrySet.getKey();
                            main.getGroupsRequesting().remove(entrySet.getKey(), entrySet.getValue());
                            break;
                        }
                    }

                    /*if (GroupsAPI.getGroupByMember(leader.getName()) != null || GroupsAPI.getGroupByLeader(leader.getName()) == null) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Ce groupe n'existe plus..."));
                        return;
                    }*/
                    io.zentae.groupsapi.Groups group = GroupsAPI.getGroupByMember(leader.getName());

                    if (group == null) group = new io.zentae.groupsapi.Groups(leader.getName());

                    group.addMember(player);
                    main.notifyGroup(group, "§6[Groupe] §f" + player.getName() + " §aà rejoint §fle groupe (Invité par " + leader.getName() + ").");
                    GroupsAPI.storeRedisGroup(group);
                }

                if (args[0].equalsIgnoreCase("refuse")) {
                    if (!main.getGroupsRequesting().containsValue(player)) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous n'attendez l'invitation d'aucun groupe..."));
                        return;
                    }

                    for (Map.Entry<ProxiedPlayer, ProxiedPlayer> entrySet : main.getGroupsRequesting().entrySet()) {
                        if (entrySet.getValue() == player) {
                            player.sendMessage(new TextComponent(main.getPrefix("success") + "Vous avez refusé la demande d'invitation au groupe de " + entrySet.getKey().getName() + "..."));

                            if (entrySet.getKey().isConnected())
                                entrySet.getKey().sendMessage(new TextComponent(main.getPrefix("info") + entrySet.getValue().getName() + " a refusé votre demande d'invitation..."));
                            main.getGroupsRequesting().remove(entrySet.getKey(), entrySet.getValue());
                            break;
                        }
                    }
                }

                if (args[0].equalsIgnoreCase("leave")) {
                    if (!GroupsAPI.isGroupMember(player.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous n'avez pas de groupe ? Faîtes /g invite <Pseudo> pour ajouter un joueur à votre groupe."));
                        return;
                    }

                    io.zentae.groupsapi.Groups group = GroupsAPI.getGroupByMember(player.getName());

                    if (group.getGroupSize() < 2) {
                        GroupsAPI.leave(player.getName(), group);
                        group.disbandGroup();
                    } else if (group.isLeader(player.getName())) {
                        Random random = new Random();
                        int randomInt = random.nextInt(group.getGroupSize());

                        String forcedLeader = group.getGroupMembers().get(randomInt);
                        GroupsAPI.putPlayerLeader(player.getName(), forcedLeader, group);

                        main.notifyGroup(group, "§6[Groupe] §f" + player.getName() + " §ca quitté §fle groupe...");
                        GroupsAPI.leave(player.getName(), group);
                    } else {
                        main.notifyGroup(group, "§6[Groupe] §f" + player.getName() + " §ca quitté §fle groupe...");
                        GroupsAPI.leave(player.getName(), group);
                    }

                }
                if (args[0].equalsIgnoreCase("list")) {
                    if (!GroupsAPI.isGroupMember(player.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous n'avez pas de groupe ? Faîtes /g invite <Pseudo> pour ajouter un joueur à votre groupe."));
                        return;
                    }
                    player.sendMessage(new TextComponent("§6------------------------------"));
                    player.sendMessage(new TextComponent("§6Groupe"));

                    int i = 1;
                    io.zentae.groupsapi.Groups group = GroupsAPI.getGroupByMember(player.getName());

                    for(String lPlayer : group.getGroupMembers()) {
                        ProxiedPlayer gPlayer = main.getProxy().getPlayer(lPlayer);

                        if(gPlayer == null) player.sendMessage(new TextComponent(ChatColor.WHITE + String.valueOf(i) + ". §c● §b" + lPlayer));
                        if(gPlayer != null) {
                            if(group.isLeader(lPlayer)) {
                                player.sendMessage(new TextComponent(ChatColor.WHITE + String.valueOf(i) + ". §a● §b" + lPlayer + " §f<> " + gPlayer.getServer().getInfo().getName() + " §7[Leader]"));
                            } else {
                                player.sendMessage(new TextComponent(ChatColor.WHITE + String.valueOf(i) + ". §a● §b" + lPlayer + " §f<> " + gPlayer.getServer().getInfo().getName()));
                            }
                        }
                        i++;
                    }
                    player.sendMessage(new TextComponent("§6------------------------------"));
                }

                if (args[0].equalsIgnoreCase("disband")) {
                    if (!GroupsAPI.isGroupMember(player.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous n'avez pas de groupe ? Faîtes /g invite <Pseudo> pour ajouter un joueur à votre groupe."));
                        return;
                    }

                    io.zentae.groupsapi.Groups group = GroupsAPI.getGroupByMember(player.getName());

                    if (!group.isLeader(player.getName())) {
                        player.sendMessage(new TextComponent(main.getPrefix("error") + "Vous n'êtes pas leader du groupe."));
                        return;
                    }

                    main.notifyGroup(group, main.getPrefix("info") + "Le groupe a été dissout !");
                    group.disbandGroup();
                }
            }
        }
    }

    private void displayHelp(ProxiedPlayer player) {
        player.sendMessage(new TextComponent("§6------------------------------"));
        player.sendMessage(new TextComponent("§e/g §binvite <Pseudo> §f- Inviter un joueur"));
        player.sendMessage(new TextComponent("§e/g §bleave §f- Quitter le groupe"));
        player.sendMessage(new TextComponent("§e/g §blist §f- Liste les membres du groupe"));
        player.sendMessage(new TextComponent("§e/g §blead <Pseudo> §f- Définir le leader du groupe"));
        player.sendMessage(new TextComponent("§e/g §bkick <Pseudo> §f- Expulser un joueur du groupe"));
        player.sendMessage(new TextComponent("§e/g §bdisband §f Dissoudre le groupe"));
        player.sendMessage(new TextComponent("§6------------------------------"));
    }
}
