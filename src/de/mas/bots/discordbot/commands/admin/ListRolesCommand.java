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


import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;

import de.btobastian.sdcf4j.Command;
import de.mas.bots.discordbot.Settings;

public class ListRolesCommand extends RestrictedCommandExecutor {

    public ListRolesCommand(TextChannel channel) {
        super(channel);
    }

    @Command(aliases = { Settings.CMD_ADMIN_LIST_ROLES_ALIAS }, description = Settings.CMD_ADMIN_LIST_ROLES_DESCRIPTION, requiredPermissions = Settings.ADMIN_ROLE_NAME)
    public String onCommand(TextChannel channel, Server server) {
        if (!checkTextChannel(channel)) return null;
        StringBuilder builder = new StringBuilder();
        builder.append("```xml\n");
        server.getRoles().forEach(role -> builder.append(role).append("\n"));
        builder.append("\n```");
        return builder.toString();
    }

}
