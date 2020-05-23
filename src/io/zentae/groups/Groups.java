package io.zentae.groups;

import io.zentae.groups.commandlisteners.GroupsCommandListener;
import io.zentae.groups.eventlisteners.GroupsEventListener;
import io.zentae.groupsapi.GroupsAPI;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.logging.Logger;

public class Groups extends Plugin {

    private static Logger log = Logger.getLogger("Minecraft");

    private HashMap<ProxiedPlayer, ProxiedPlayer> groupsRequesting = new HashMap<>();

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, new GroupsEventListener(this));
        getProxy().getPluginManager().registerCommand(this, new GroupsCommandListener("group", this));
        log.info(String.format("[%s] Activated Successfully. Created by %s for PlayFull. (Ver: %s)", getDescription().getName(), getDescription().getAuthor(), getDescription().getVersion()));
    }

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Deactivated Successfully. Created by %s for PlayFull. (Ver: %s)", getDescription().getName(), getDescription().getAuthor(), getDescription().getVersion()));
        for(io.zentae.groupsapi.Groups group : GroupsAPI.getGroupsList()) group.disbandGroup();
    }

    public void notifyGroup(io.zentae.groupsapi.Groups group, String message) {
        for(String lPlayerName : group.getGroupMembers()) {
            ProxiedPlayer lPlayer = getProxy().getPlayer(lPlayerName);
            if(lPlayer != null && lPlayer.isConnected()) lPlayer.sendMessage(new TextComponent(message));
        }
    }

    public String getPrefix(String prefixType) {
        if(prefixType.equalsIgnoreCase("error")) return "§f| §6§lPlay§e§lFull §f| §c§lErreur §f-●- §c";
        if(prefixType.equalsIgnoreCase("info")) return "§f| §6§lPlay§e§lFull §f| §b§lInfo §f-●- §b";
        if(prefixType.equalsIgnoreCase("waiting")) return "§f| §6§lPlay§e§lFull §f| §e§lAttente §f-●- §e";
        if(prefixType.equalsIgnoreCase("success")) return "§f| §6§lPlay§e§lFull §f| §a§lSuccès §f-●- §a";
        return null;
    }

    public HashMap<ProxiedPlayer, ProxiedPlayer> getGroupsRequesting() { return groupsRequesting; }

}
