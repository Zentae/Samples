package io.zentae.accounthandler;

import io.zentae.accountapi.Account;
import io.zentae.accounthandler.databasemanager.DbManager;
import io.zentae.accounthandler.eventlisteners.EventListener;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AccountHandler extends Plugin {

    private static final Logger log = Logger.getLogger("Minecraft");

    private final String JEDIS_HOST = "127.0.0.1";
    private final int JEDIS_PORT = 6379;
    //private String JEDIS_PASS = "GOTCHA";

    @Override
    public void onEnable() {
        DbManager.initAllDatabaseConnections();
        getProxy().getPluginManager().registerListener(this, new EventListener(this));

        launchDataUpdater();

        log.info(String.format("[%s] Activated successfully. Created by %s. Ver: (%s)", getDescription().getName(), getDescription().getAuthor(), getDescription().getVersion()));
    }

    public void onDisable() {
        for(ProxiedPlayer lPlayer : getProxy().getPlayers()) { accountUpdate(lPlayer) ;}
        DbManager.closeAllDatabaseConnections();

        log.info(String.format("[%s] Deactivated successfully. Created by %s. Ver: (%s)", getDescription().getName(), getDescription().getAuthor(), getDescription().getVersion()));
    }

    private void launchDataUpdater() {
        getProxy().getScheduler().schedule(this, () -> {
            for(ProxiedPlayer p : getProxy().getPlayers()) { if(isAccountExists(p)) accountUpdate(p); }
        }, 0, 10, TimeUnit.MINUTES);
    }

    public boolean isAccountExists(ProxiedPlayer player) {
        try {
            Connection connection = DbManager.PLAYFULL.getDbAcces().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PlayFull_Account WHERE username = ? ");

            preparedStatement.setString(1, player.getName());
            preparedStatement.executeQuery();

            ResultSet resultSet = preparedStatement.getResultSet();

            if(resultSet.next()) {
                connection.close();
                return true;
            } else {
                connection.close();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void updatePlayerCount(int playerCount, String serverName) {
        Jedis jedis = new Jedis(JEDIS_HOST, JEDIS_PORT);

        jedis.set("playerCount_" + serverName, String.valueOf(playerCount));
        jedis.close();
    }

    public void redisStoreAccount(ProxiedPlayer player) {
        Account account = getDatabaseUserAccount(player);

        String username = player.getName();
        String coins = String.valueOf(account.getCoins());
        String tokens = String.valueOf(account.getTokens());
        String options = account.getRawOptions();
        String friends = account.getRawFriends();
        String blocked_players = account.getRawBlockedPlayers();
        String ban_args = account.getRawBanArgs();
        String mute_args = account.getRawMuteArgs();

        Jedis jedis = new Jedis(JEDIS_HOST, JEDIS_PORT);

        Pipeline pipeline = jedis.pipelined();

        pipeline.set("username_" + username, username);
        pipeline.set("coins_" + username, coins);
        pipeline.set("tokens_" + username, tokens);
        pipeline.set("options_" + username, options);
        pipeline.set("friends_" + username, friends);
        pipeline.set("blocked_players_" + username, blocked_players);
        pipeline.set("ban_args_" + username, ban_args);
        pipeline.set("mute_args_" + username, mute_args);
        pipeline.sync();

        jedis.close();

    }

    public void redisUnstoreAccount(ProxiedPlayer player) {
        String username = player.getName();

        Jedis jedis = new Jedis(JEDIS_HOST, JEDIS_PORT);

        Pipeline pipeline = jedis.pipelined();

        pipeline.del("username_" + username);
        pipeline.del("coins_" + username);
        pipeline.del("tokens_" + username);
        pipeline.del("options_" + username);
        pipeline.del("friends_" + username);
        pipeline.del("blocked_players_" + username);
        pipeline.del("ban_args_" + username);
        pipeline.del("mute_args_" + username);
        pipeline.sync();

        jedis.close();
    }

    public Account getRedisUserAccount(ProxiedPlayer player) {
        String username = player.getName();

        Jedis jedis = new Jedis(JEDIS_HOST, JEDIS_PORT);

        if(jedis.exists("username_" + username)) {

            String a_uuid = player.getUniqueId().toString();
            String a_username = jedis.get("username_" + username);
            int a_coins = Integer.parseInt(jedis.get("coins_" + username));
            int a_tokens = Integer.parseInt(jedis.get("tokens_" + username));
            String a_options = jedis.get("options_" + username);
            String a_friends = jedis.get("friends_" + username);
            String a_blocked_players = jedis.get("blocked_players_" + username);
            String a_ban_args = jedis.get("ban_args_" + username);
            String a_mute_args = jedis.get("mute_args_" + username);

            jedis.close();
            return new Account(a_uuid, a_username, a_coins, a_tokens, a_options, a_friends, a_blocked_players, a_ban_args, a_mute_args);
        }

        jedis.close();
        return null;
    }

    public Account getDatabaseUserAccount(ProxiedPlayer player) {
        if(!isAccountExists(player)) return null;

        try {
            Connection connection = DbManager.PLAYFULL.getDbAcces().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PlayFull_Account WHERE username = ? ");

            preparedStatement.setString(1, player.getName());
            preparedStatement.executeQuery();

            ResultSet resultSet = preparedStatement.getResultSet();
            if(resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                String username = resultSet.getString("username");

                int coins = resultSet.getInt("coins");
                int tokens = resultSet.getInt("tokens");

                String friends = resultSet.getString("friends");
                String options = resultSet.getString("options");
                String blocked_players = resultSet.getString("blocked_players");

                String ban_args = resultSet.getString("ban_args");
                String mute_args = resultSet.getString("mute_args");

                connection.close();
                return new Account(uuid, username, coins, tokens, options, friends, blocked_players, ban_args, mute_args);
            } else {
                connection.close();
                return null;
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void accountUpdate(ProxiedPlayer player) {
        Account account = getRedisUserAccount(player);
        String username = player.getName();

        try {
            Connection connection = DbManager.PLAYFULL.getDbAcces().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE PlayFull_Account SET coins = ?, tokens = ?, options = ?, friends = ?, blocked_players = ?, ban_args = ?, mute_args = ? WHERE username = ? ");

            preparedStatement.setInt(1, account.getCoins());
            preparedStatement.setInt(2, account.getTokens());
            preparedStatement.setString(3, account.getRawOptions());
            preparedStatement.setString(4, account.getRawFriends());
            preparedStatement.setString(5, account.getRawBlockedPlayers());
            preparedStatement.setString(6, account.getRawBanArgs());
            preparedStatement.setString(7, account.getRawMuteArgs());
            preparedStatement.setString(8, username);
            preparedStatement.executeUpdate();

            connection.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void accountProvider(ProxiedPlayer player) {
        if(isAccountExists(player)) return;

        String username = player.getName();
        UUID uuid = player.getUniqueId();
        try {
            Connection connection = DbManager.PLAYFULL.getDbAcces().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO PlayFull_Account (uuid, username, coins, tokens, options, friends, blocked_players, ban_args, mute_args) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, username);
            preparedStatement.setInt(3, 0);
            preparedStatement.setInt(4, 0);
            preparedStatement.setString(5, "");
            preparedStatement.setString(6,"");
            preparedStatement.setString(7, "");
            preparedStatement.setString(8, "");
            preparedStatement.setString(9, "");
            preparedStatement.execute();

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
