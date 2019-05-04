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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;

import de.btobastian.sdcf4j.handler.JavacordHandler;
import de.mas.bots.discordbot.commands.RoleHelpCommand;
import de.mas.bots.discordbot.commands.admin.AdminHelpCommand;
import de.mas.bots.discordbot.commands.admin.ListRolesCommand;
import de.mas.bots.discordbot.commands.admin.RoleCommandsCommand;
import de.mas.bots.discordbot.persistence.RolePersistence;
import lombok.val;

public class Starter {

    public static void logWrapper(String str) {
        System.out.println(str);
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        File tmp = new File(Settings.TEMP_FOLDER);
        if (!tmp.exists()) {
            tmp.mkdir();
        }

        final Settings settings = new Settings("properties");

        new DiscordApiBuilder().setToken(settings.getBottoken()).login().thenAccept(api -> {
            // for (Server s : api.getServers()) {
            // System.out.println(s);
            // s.getTextChannels().forEach(System.out::println);
            // s.getRoles().forEach(System.out::println);
            // }

            Server s = api.getServerById(settings.getServerID()).get();
            TextChannel serverLogChannel = api.getTextChannelById(settings.getServerLogChannelID()).get();
            TextChannel serverLeftChannel = api.getTextChannelById(settings.getServerLeftChannelID()).get();

            logWrapper("Bot is now Online");
            serverLogChannel.sendMessage("I'm online now!");
            JavacordHandler roleCMDHandler = new JavacordHandler(api);
            JavacordHandler roleAdminCMDHandler = new JavacordHandler(api);

            val rolesWithPermission = settings.getRolesWithPermissions();

            getUsersWithAdminPermissions(settings.getServerID(), api, rolesWithPermission)
                    .forEach(user -> roleAdminCMDHandler.addPermission(user, Settings.ADMIN_ROLE_NAME));

            api.addServerMemberLeaveListener(event -> {
                MessageBuilder mb = new MessageBuilder();
                User user = event.getUser();
                mb.append(user).append(" (" + user.getDisplayName(s) + ")");
                mb.append(" has left the server.");
                mb.append(" (" + new Date() + ")");
                logWrapper(mb.getStringBuilder().toString());
                serverLogChannel.sendMessage(mb.getStringBuilder().toString());
                serverLeftChannel.sendMessage(user.getDisplayName(s) + " hat uns verlassen =(");
            });
            api.addServerMemberJoinListener(event -> {
                MessageBuilder mb = new MessageBuilder();
                User user = event.getUser();
                mb.append(user).append(" (" + user.getDisplayName(s) + ")");
                mb.append(" has joined the server.");
                mb.append(" (" + new Date() + ")");
                logWrapper(mb.getStringBuilder().toString());
                serverLogChannel.sendMessage(mb.getStringBuilder().toString());
            });

            api.addMessageCreateListener(event -> {
                for (MessageAttachment ma : event.getMessageAttachments()) {
                    try (FileOutputStream fos = new FileOutputStream(Settings.TEMP_FOLDER + File.separator + ma.getIdAsString() + "_" + ma.getFileName())) {
                        fos.write(ma.downloadAsByteArray().get());
                        logWrapper("Saved attachment to disk.");
                    } catch (Exception e) {
                        logWrapper("Failed to save attachment to disk.");
                        serverLogChannel.sendMessage("Failed to download attachment for " + event.getMessage());
                    }
                }
            });

            api.addMessageDeleteListener(event -> {
                MessageBuilder mb = new MessageBuilder();
                Optional<MessageAuthor> ma = event.getMessageAuthor();
                String author = "unkwn";
                if (ma.isPresent()) {
                    author = ma.get().getDisplayName();
                }

                mb.append("Message from User ");
                if (ma.isPresent() && ma.get().asUser().isPresent()) {
                    mb.append(ma.get().asUser().get()).append(" (" + author + ")");
                } else {
                    mb.append(author);
                }

                mb.append(" has been removed in channel ");
                Optional<ServerTextChannel> channel = event.getServerTextChannel();
                if (channel.isPresent()) {
                    if (channel.get().getId() == serverLogChannel.getId()) {
                        logWrapper("Ignoring message delete from server log channel. " + new Date());
                        return;
                    }
                    mb.append(channel.get()).append(" (" + channel.get().getName() + ")");
                } else {
                    mb.append("UNKNOWN");
                }

                if (event.getServerTextChannel().isPresent()) mb.append(" (Deleted at " + new Date() + ")");

                event.getMessageAttachments().ifPresent(list -> list.forEach(m -> mb.appendNewLine()
                        .append("Had attachment: " + m.getFileName() + " (" + m.getIdAsString() + "). Created at " + m.getCreationTimestamp() + ".")));

                if (event.getReadableMessageContent().isPresent() && !event.getReadableMessageContent().get().isEmpty()) {
                    mb.appendNewLine().appendNewLine().append("Content:");
                    Optional<Message> m = event.getMessage();
                    if (m.isPresent()) {
                        mb.append(" (Created at " + m.get().getCreationTimestamp() + ").");
                    }
                    mb.appendCode("XML", event.getReadableMessageContent().get());
                }

                logWrapper(mb.getStringBuilder().toString());

                serverLogChannel.sendMessage(mb.getStringBuilder().toString());
            });

            val configChannel = api.getTextChannelById(settings.getConfigChannelID()).get();

            // register the command
            roleCMDHandler.registerCommand(new RoleHelpCommand(roleCMDHandler));
            roleAdminCMDHandler.registerCommand(new AdminHelpCommand(roleAdminCMDHandler, configChannel));
            roleAdminCMDHandler.registerCommand(new ListRolesCommand(configChannel));
            try {
                roleAdminCMDHandler.registerCommand(new RoleCommandsCommand(configChannel, roleCMDHandler,
                        RolePersistence.getInstance(settings.getDatabaseFilePath()), api.getServerById(settings.getServerID()).get()));
            } catch (Exception e) {
                System.exit(0);
            }
        }).exceptionally(ExceptionLogger.get());

    }

    private static List<User> getUsersWithAdminPermissions(long server_ID, DiscordApi api, List<Long> rolesWithPermission) {
        val serverOpt = api.getServerById(server_ID);
        val userWithPermission = new ArrayList<User>();

        if (serverOpt.isPresent()) {
            Server server = serverOpt.get();
            rolesWithPermission.forEach(roleID -> {

                Optional<Role> r = server.getRoleById(roleID);
                if (r.isPresent()) {
                    userWithPermission.addAll(r.get().getUsers());
                }
            });
        }
        return userWithPermission;
    }

}
