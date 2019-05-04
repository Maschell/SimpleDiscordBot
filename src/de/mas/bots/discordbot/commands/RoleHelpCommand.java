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

package de.mas.bots.discordbot.commands;

import java.util.ArrayList;
import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.CommandHandler.SimpleCommand;
import de.mas.bots.discordbot.Settings;

public class RoleHelpCommand implements CommandExecutor {

    private final CommandHandler commandHandler;

    public RoleHelpCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Command(aliases = { Settings.CMD_HELP_ALIAS }, description = Settings.CMD_HELP_DESCRIPTION)
    public String onHelpCommand() {
        StringBuilder builder = new StringBuilder();
        builder.append("```"); // a xml code block looks fancy
        List<SimpleCommand> mutableCopy = new ArrayList<SimpleCommand>(commandHandler.getCommands());

        mutableCopy.removeIf(command -> !(command.getExecutor() instanceof RoleCommand));
        mutableCopy.sort((o1, o2) -> ((RoleCommand) o1.getExecutor()).getRole().getName().compareTo(((RoleCommand) o2.getExecutor()).getRole().getName()));

        for (CommandHandler.SimpleCommand simpleCommand : mutableCopy) {
            if (!simpleCommand.getCommandAnnotation().showInHelpPage()) {
                continue; // skip command
            }
            builder.append("\n");
            if (!simpleCommand.getCommandAnnotation().requiresMention()) {
                // the default prefix only works if the command does not require a mention
                builder.append(commandHandler.getDefaultPrefix());
            }
            String usage = simpleCommand.getCommandAnnotation().usage();
            if (usage.isEmpty()) { // no usage provided, using the first alias
                usage = simpleCommand.getCommandAnnotation().aliases()[0];
            }
            builder.append(usage);
            String description = simpleCommand.getCommandAnnotation().description();
            if (!description.equals("none")) {
                builder.append(" | ").append(description);
            }
        }
        builder.append("\n```"); // end of xml code block
        return builder.toString();
    }

}