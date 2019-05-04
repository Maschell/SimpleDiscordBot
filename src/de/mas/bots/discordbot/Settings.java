/*******************************************************************************
 * Copyright (c) 2017-2019 Maschell
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/

package de.mas.bots.discordbot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import lombok.Cleanup;
import lombok.Data;

@Data
public class Settings {
    public static final String TEMP_FOLDER = "temp";
    public static final String ADMIN_ROLE_NAME = "RoleManager";

    public static final String CMD_ADMIN_ADD_ROLE_CMD_ALIAS = "!adminaddrolecmd";
    public static final String CMD_ADMIN_ADD_ROLE_CMD_DESCRIPTION = "Adds a new role command";
    public static final String CMD_ADMIN_ADD_ROLE_CMD_USAGE = Settings.CMD_ADMIN_ADD_ROLE_CMD_ALIAS + " command;roleID";

    public static final String CMD_ADMIN_LIST_ROLE_CMD_ALIAS = "!adminlistrolecmds";
    public static final String CMD_ADMIN_LIST_ROLE_CMD_DESCRIPTION = "Lists all role commands";

    public static final String CMD_ADMIN_REMOVE_ROLE_CMD_ALIAS = "!adminremoverolecmd";
    public static final String CMD_ADMIN_REMOVE_ROLE_CMD_DESCRIPTION = "Removes a role command";
    public static final String CMD_ADMIN_REMOVE_ROLE_CMD_USAGE = Settings.CMD_ADMIN_REMOVE_ROLE_CMD_ALIAS + " CMDID";

    public static final String CMD_ADMIN_HELP_ALIAS = "!adminhelp";
    public static final String CMD_ADMIN_HELP_DESCRIPTION = "Shows this";

    public static final String CMD_ADMIN_LIST_ROLES_ALIAS = "!adminlistroles";
    public static final String CMD_ADMIN_LIST_ROLES_DESCRIPTION = "Lists all possible roles";

    public static final String CMD_HELP_ALIAS = "!roles";
    public static final String CMD_HELP_DESCRIPTION = "Shows this page";

    private static final String SETTING_BOT_TOKEN = "bot_token";
    private static final String SETTING_BOT_TOKEN_DEFAULT = "<bot_token>";
    private static final String SETTING_SERVER_ID = "server_id";

    private static final long SETTING_SERVER_ID_DEFAULT = 0;
    private static final String SETTING_CONFIG_CHANNEL_ID = "config_channel_id";
    private static final String SETTING_SERVER_LOG_CHANNEL_ID = "server_log_channel_id";
    private static final String SETTING_SERVER_LEFT_CHANNEL_ID = "server_left_channel_id";
    private static final long SETTING_CONFIG_CHANNEL_ID_DEFAULT = 0;
    private static final long SETTING_SERVER_LOG_CHANNEL_ID_DEFAULT = 0;
    private static final long SETTING_SERVER_LEFT_CHANNEL_ID_DEFAULT = 0;
    private static final String SETTING_DATABASE_FILENAME = "role_database";
    private static final String SETTING_DATABASE_FILENAME_DEFAULT = "roledb.db";
    private static final String SETTING_ROLES_WITH_PERMISSION = "roles_with_permission";

    private String bottoken = SETTING_BOT_TOKEN_DEFAULT;
    private long serverID = SETTING_SERVER_ID_DEFAULT;
    private long configChannelID = SETTING_CONFIG_CHANNEL_ID_DEFAULT;
    private long serverLogChannelID = SETTING_SERVER_LOG_CHANNEL_ID_DEFAULT;
    private long serverLeftChannelID = SETTING_SERVER_LEFT_CHANNEL_ID_DEFAULT;
    private String databaseFilePath = SETTING_BOT_TOKEN_DEFAULT;
    private List<Long> rolesWithPermissions = new ArrayList<>();

    public Settings(String filename) throws FileNotFoundException, IOException {
        String appConfigPath = filename;
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(appConfigPath));
            loadSettings(appProps);
        } catch (FileNotFoundException e) {
            createSettingsFromInput();
            saveSettings(appConfigPath);
        }
    }

    public Settings(String filename, String password) throws FileNotFoundException, IOException {
        String appConfigPath = filename;
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(appConfigPath));
            loadSettings(appProps);
        } catch (FileNotFoundException e) {
            createSettingsFromInput();
            saveSettings(appConfigPath);
        }
    }

    private void createSettingsFromInput() {
        @Cleanup
        Scanner scanner = new Scanner(System.in);
        System.out.println("A new configuration file will be created.");
        System.out.println();
        System.out.print("Please enter the bot token: ");
        bottoken = scanner.nextLine();
        System.out.print("Please enter the server ID where bot is running: ");
        serverID = getLongValueFromScanner(scanner);
        System.out.print("Please enter the channel ID where the bot will be configurated: ");
        configChannelID = getLongValueFromScanner(scanner);
        System.out.print("Please enter the channel ID where the bot log actions: ");
        serverLogChannelID = getLongValueFromScanner(scanner);
        System.out.print("Please enter the channel ID posts left messages: ");
        serverLeftChannelID = getLongValueFromScanner(scanner);

        System.out.print("Please enter a list roles (ID) that are allowed to configure the bot. One role each line, end with an empty line.: ");
        String cur = "";
        while (!(cur = scanner.nextLine()).isEmpty()) {
            try {
                rolesWithPermissions.add(Long.parseLong(cur));
                System.out.println("added");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid role ID.");
            }
        }
        System.out.print("done.");
    }

    private long getLongValueFromScanner(Scanner scanner) {
        long result = 0;
        boolean okay = false;
        do {
            try {
                result = scanner.nextLong();
                okay = true;
            } catch (InputMismatchException e) {
                System.out.println("!Please enter a number!");
                scanner.nextLine();
            }
        } while (!okay);
        scanner.nextLine();
        return result;
    }

    private void loadSettings(Properties appProps) {
        this.bottoken = appProps.getProperty(SETTING_BOT_TOKEN);
        try {
            this.serverID = Long.parseLong(appProps.getProperty(SETTING_SERVER_ID));
            this.configChannelID = Long.parseLong(appProps.getProperty(SETTING_CONFIG_CHANNEL_ID));
            this.serverLogChannelID = Long.parseLong(appProps.getProperty(SETTING_SERVER_LOG_CHANNEL_ID));
            this.serverLeftChannelID = Long.parseLong(appProps.getProperty(SETTING_SERVER_LEFT_CHANNEL_ID));
        } catch (NumberFormatException e) {
        }
        this.databaseFilePath = appProps.getProperty(SETTING_DATABASE_FILENAME);
        String[] roles = appProps.getProperty(SETTING_ROLES_WITH_PERMISSION).split(",");
        for (String s : roles) {
            try {
                rolesWithPermissions.add(Long.parseLong(s));
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse role id");

            }
        }

    }

    private void saveSettings(String filepath) throws IOException {
        Properties prop = new Properties();
        prop.setProperty(SETTING_BOT_TOKEN, bottoken);
        prop.setProperty(SETTING_SERVER_ID, Long.toString(serverID));
        prop.setProperty(SETTING_CONFIG_CHANNEL_ID, Long.toString(configChannelID));
        prop.setProperty(SETTING_SERVER_LOG_CHANNEL_ID, Long.toString(serverLogChannelID));
        prop.setProperty(SETTING_SERVER_LEFT_CHANNEL_ID, Long.toString(serverLeftChannelID));
        prop.setProperty(SETTING_DATABASE_FILENAME, SETTING_DATABASE_FILENAME_DEFAULT);
        prop.setProperty(SETTING_ROLES_WITH_PERMISSION, StringUtils.join(rolesWithPermissions, ','));
        prop.store(new FileWriter(filepath), "");
    }

}
