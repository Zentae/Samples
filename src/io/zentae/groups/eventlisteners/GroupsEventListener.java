package io.zentae.groups.eventlisteners;

import io.zentae.groups.Groups;
import io.zentae.groupsapi.GroupsAPI;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Random;

public class GroupsEventListener implements Listener {

    private Groups groups;

    public GroupsEventListener(Groups groups) { this.groups = groups; }

    @EventHandler
    public void onLeaderDisconnect(PlayerDisconnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        io.zentae.groupsapi.Groups group = GroupsAPI.getGroupByLeader(player.getName());

        if(group == null) return;

        Random random = new Random();
        int randomInt = random.nextInt(group.getGroupSize());
        GroupsAPI.putPlayerLeader(group.getGroupLeader(), group.getGroupMembers().get(randomInt), group);
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if(!e.getMessage().startsWith("!")) return;
        ProxiedPlayer player = null;

        if(e.getSender() instanceof ProxiedPlayer) {
            player = (ProxiedPlayer)e.getSender();
        } else return;
        if(!GroupsAPI.isGroupMember(player.getName())) {
            player.sendMessage(new TextComponent(groups.getPrefix("error") + "Vous ne possédez pas de groupe !"));
            e.setCancelled(true);
            return;
        }
        groups.notifyGroup(GroupsAPI.getGroupByMember(player.getName()), "§6[Groupe] §b" + player.getName() + " > " + e.getMessage().substring(1, e.getMessage().length()));
        e.setCancelled(true);
    }
}
