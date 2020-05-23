package io.zentae.accounthandler.eventlisteners;

import io.zentae.accounthandler.AccountHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class EventListener implements Listener {

    private final AccountHandler accountHandler;

    public EventListener(AccountHandler accountHandler) {
        this.accountHandler = accountHandler;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerProxyConnect(PostLoginEvent e) {
        ProxiedPlayer player = e.getPlayer();

        if(accountHandler.isAccountExists(player)) {
            accountHandler.redisStoreAccount(player);
        } else {
            accountHandler.accountProvider(player);
            accountHandler.redisStoreAccount(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerProxyDisconnect(PlayerDisconnectEvent e) {
        ProxiedPlayer player = e.getPlayer();

        accountHandler.accountUpdate(player);
        accountHandler.redisUnstoreAccount(player);
    }

    @EventHandler
    public void onPlayerServerConnect(ServerConnectEvent e) {
        ServerInfo eventServer = e.getTarget();
        accountHandler.updatePlayerCount(eventServer.getPlayers().size() + 1, eventServer.getName());
    }

    @EventHandler
    public void onPlayerServerDisconnect(ServerDisconnectEvent e) {
        ServerInfo eventServer = e.getTarget();
        accountHandler.updatePlayerCount(eventServer.getPlayers().size(), eventServer.getName());
    }
}
