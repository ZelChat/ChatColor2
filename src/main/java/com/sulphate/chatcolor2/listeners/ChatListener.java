package com.sulphate.chatcolor2.listeners;

import com.sulphate.chatcolor2.commands.Setting;
import com.sulphate.chatcolor2.data.PlayerDataStore;
import com.sulphate.chatcolor2.managers.ConfigsManager;
import com.sulphate.chatcolor2.utils.Config;
import com.sulphate.chatcolor2.utils.GeneralUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatListener implements Listener {

    private final ConfigsManager configsManager;
    private final GeneralUtils generalUtils;
    private final PlayerDataStore dataStore;

    private YamlConfiguration mainConfig;

    public ChatListener(ConfigsManager configsManager, GeneralUtils generalUtils, PlayerDataStore dataStore) {
        this.configsManager = configsManager;
        this.generalUtils = generalUtils;
        this.dataStore = dataStore;

        mainConfig = configsManager.getConfig(Config.MAIN_CONFIG);
    }

    public void onEvent(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();
        UUID uuid = player.getUniqueId();

        // Check default colour.
        if (mainConfig.getBoolean(Setting.DEFAULT_COLOR_ENABLED.getConfigPath())) {
            generalUtils.checkDefault(uuid);
        }

        message = checkColourCodes(message, player);

        // Check if they have a group colour, and if it should be enforced.
        String groupColour = generalUtils.getGroupColour(player);
        String colour = dataStore.getColour(uuid);

        if (groupColour != null) {
            // If it should be forced, set it so.
            if (mainConfig.getBoolean(Setting.FORCE_GROUP_COLORS.getConfigPath())) {
                colour = groupColour;
            }
        }

        e.setMessage(generalUtils.colouriseMessage(colour, message, true));
    }

    private String checkColourCodes(String message, Player player) {
        // If their message contains &, check they have permissions for it, or strip the colour.
        if (!player.hasPermission("chatcolor.use-color-codes")) {
            // A player reported using '&&a' for example, would bypass this. So, loop until it's not different.
            while (GeneralUtils.isDifferentWhenColourised(message)) {
                // Strip the colour from the message.
                message = org.bukkit.ChatColor.stripColor(GeneralUtils.colourise(message));
            }
        }

        if (!player.hasPermission("chatcolor.use-hex-codes") && GeneralUtils.containsHexColour(message)) {
            while (GeneralUtils.isDifferentWhenColourised(message)) {
                message = org.bukkit.ChatColor.stripColor(GeneralUtils.colourise(message));
            }
        }

        return message;
    }

}
