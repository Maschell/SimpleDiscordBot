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

package de.mas.bots.discordbot.commands.admin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;

import com.athaydes.javanna.Javanna;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.CommandHandler.SimpleCommand;
import de.mas.bots.discordbot.RoleCommandInfo;
import de.mas.bots.discordbot.Settings;
import de.mas.bots.discordbot.commands.RoleCommand;
import de.mas.bots.discordbot.persistence.RolePersistence;

public class RoleCommandsCommand extends RestrictedCommandExecutor {

    private final CommandHandler commandHandlerUser;
    private final RolePersistence persistence;

    public RoleCommandsCommand(TextChannel channel, CommandHandler handlerUser, RolePersistence persistence, Server server) {
        super(channel);
        this.commandHandlerUser = handlerUser;
        this.persistence = persistence;

        persistence.getAll().forEach(roleinfo -> {
            try {
                addRoleCommand(roleinfo, handlerUser, server, roleinfo.getID());
            } catch (IllegalArgumentException e) {
                channel.sendMessage(e.toString());
            }
            roleCMDID = roleinfo.getID() + 1; // TODO: This is ugly.
        });
    }

    private int roleCMDID = 0;
    Map<Integer, SimpleCommand> roleCommands = new HashMap<>();

    @Command(aliases = {
            Settings.CMD_ADMIN_ADD_ROLE_CMD_ALIAS }, description = Settings.CMD_ADMIN_ADD_ROLE_CMD_DESCRIPTION, usage = Settings.CMD_ADMIN_ADD_ROLE_CMD_USAGE, requiredPermissions = Settings.ADMIN_ROLE_NAME)
    public String onAddCommand(Server server, TextChannel channel, Message m) {
        if (!checkTextChannel(channel) || m == null) return null;
        String[] splittedMsg = m.getContent().split(";");
        if (splittedMsg.length != 2) {
            return "ERROR: Expecting 2 parameter.\n" + "Example: " + Settings.CMD_ADMIN_ADD_ROLE_CMD_ALIAS + " !splatoon2;338783758138343434";
        }

        long roleID = 0;
        try {
            roleID = Long.parseLong(splittedMsg[1]);
        } catch (Exception e) {
            return "ERROR: Role ID needs to be a number.";
        }

        Optional<Role> roleOPT = server.getRoleById(roleID);
        if (!roleOPT.isPresent()) {
            return "ERROR: Role with ID " + roleID + " not found";
        }
        Role role = roleOPT.get();

        for (Entry<Integer, SimpleCommand> e : roleCommands.entrySet()) {
            CommandExecutor executor = e.getValue().getExecutor();
            if (executor instanceof RoleCommand && ((RoleCommand) executor).getRole().equals(role)) {
                return "ERROR: There already exist a role command for role \"" + role.getName() + "\"\n";
            }
        }

        List<String> aliases = new ArrayList<>();
        for (String alias : splittedMsg[0].substring(Settings.CMD_ADMIN_ADD_ROLE_CMD_ALIAS.length() + 1).split(",")) {
            aliases.add(alias);
        }

        RoleCommandInfo cmdInfo = new RoleCommandInfo(aliases.get(0), roleID);

        int newID = roleCMDID++;
        cmdInfo.setID(newID);

        try {
            addRoleCommand(cmdInfo, commandHandlerUser, server, newID);
        } catch (IllegalArgumentException e) {
            return e.toString();
        }

        persistence.add(cmdInfo);

        return "Creating role commands was successful.\n" + "\"" + aliases.get(0) + "\" Description: \"" + splittedMsg[1] + "\" for role: " + role;
    }

    @Command(aliases = {
            Settings.CMD_ADMIN_LIST_ROLE_CMD_ALIAS }, description = Settings.CMD_ADMIN_LIST_ROLE_CMD_DESCRIPTION, requiredPermissions = Settings.ADMIN_ROLE_NAME)
    public String onListCommand(TextChannel channel) {
        if (!checkTextChannel(channel)) return null;
        StringBuilder builder = new StringBuilder();
        builder.append("```xml\n"); // a xml code block looks fancy
        if (roleCommands.isEmpty()) {
            builder.append("No role commands exist");
        } else {
            roleCommands.forEach((cmdid, command) -> builder.append("ID:" + cmdid + " Command: " + command.getCommandAnnotation().aliases()[0] + "\n"));
        }
        builder.append("\n```"); // end of xml code block
        return builder.toString();
    }

    @Command(aliases = {
            Settings.CMD_ADMIN_REMOVE_ROLE_CMD_ALIAS }, description = Settings.CMD_ADMIN_REMOVE_ROLE_CMD_DESCRIPTION, usage = Settings.CMD_ADMIN_REMOVE_ROLE_CMD_USAGE, requiredPermissions = Settings.ADMIN_ROLE_NAME)
    public String onRemoveCommand(TextChannel channel, Message m) {
        if (!checkTextChannel(channel)) return null;
        String[] splittedMsg = m.getContent().split(" ");
        if (splittedMsg.length != 2) {
            return "ERROR: Expecting 1 parameter.\n" + "Example: " + Settings.CMD_ADMIN_REMOVE_ROLE_CMD_ALIAS + " 1";
        }

        int cmdID = 0;
        try {
            cmdID = Integer.parseInt(splittedMsg[1]);
        } catch (Exception e) {
            return "ERROR: Command ID needs to be a number.";
        }

        if (!roleCommands.containsKey(cmdID)) {
            return "ERROR: No role command with the ID \"" + cmdID + "\" exists.";
        }
        SimpleCommand cmd = roleCommands.get(cmdID);
        persistence.remove(cmdID);
        commandHandlerUser.removeCommand(cmd);
        roleCommands.remove(cmdID);

        return "Successfully removed the role command " + cmdID;
    }

    private SimpleCommand addRoleCommand(RoleCommandInfo info, CommandHandler handlerUser, Server server, int ID) throws IllegalArgumentException {
        String[] aliases = new String[] { info.getAlias() };

        Optional<Role> roleOPT = server.getRoleById(info.getRoleID());
        if (!roleOPT.isPresent()) {
            throw new IllegalArgumentException("The given role id doesn't exists. " + info.getRoleID());
        }
        Role role = roleOPT.get();

        for (PermissionType permission : role.getAllowedPermissions()) { // Make sure not to be an idiot.
            if (permission.isSet(PermissionType.ADMINISTRATOR.getValue()) || permission.isSet(PermissionType.BAN_MEMBERS.getValue())
                    || permission.isSet(PermissionType.MANAGE_CHANNELS.getValue()) || permission.isSet(PermissionType.MANAGE_SERVER.getValue())
                    || permission.isSet(PermissionType.KICK_MEMBERS.getValue()) || permission.isSet(PermissionType.MANAGE_WEBHOOKS.getValue())
                    || permission.isSet(PermissionType.VIEW_AUDIT_LOG.getValue()) || permission.isSet(PermissionType.MANAGE_ROLES.getValue())) {
                throw new IllegalArgumentException("The given role id has too much power " + Integer.toHexString(permission.getValue()));
            }
        }

        String desc = "Gives the user the role: \"" + role.getName() + "\"";
        Command c = Javanna.createAnnotation(Command.class, new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("aliases", aliases);
                put("description", desc);
            }
        });

        RoleCommand command = new RoleCommand(role);
        Method method = null;
        for (Method m : command.getClass().getMethods()) {
            if (m.getName().equals("switchRoleCommand")) {
                method = m;
                break;
            }
        }

        SimpleCommand cmd = handlerUser.registerCommand(c, method, command);

        roleCommands.put(ID, cmd);

        return cmd;
    }

}
