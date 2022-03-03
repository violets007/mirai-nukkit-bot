package top.zixuan007.bot.utils;

import cn.nukkit.Nukkit;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.Level;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.permission.Permission;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.api.bot.MiraiGroup;
import top.zixuan007.bot.BotPlugin;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * @author zixuan007
 * @version 1.0
 * @description: 快速执行一些命令
 * @date 2022/2/28 10:12 PM
 */
public class CommandUtils {


    private static final String UPTIME_FORMAT = TextFormat.RED + "%d" + TextFormat.GOLD + " days " +
            TextFormat.RED + "%d" + TextFormat.GOLD + " hours " +
            TextFormat.RED + "%d" + TextFormat.GOLD + " minutes " +
            TextFormat.RED + "%d" + TextFormat.GOLD + " seconds";


    public static boolean executeStatus(String playerName, MiraiGroup miraiGroup) {

        if (!Permission.DEFAULT_OP.equals(BotPlugin.getInstance().getPermissionByName(playerName))) {
            miraiGroup.sendMessageMirai("你没权权限执行此操作");
            return true;
        }

        CommandSender sender = BotPlugin.getInstance().getServer().getConsoleSender();
        Server server = sender.getServer();
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(TextFormat.GREEN + "---- " + TextFormat.WHITE + "Server status" + TextFormat.GREEN + " ----\n");

        long time = System.currentTimeMillis() - Nukkit.START_TIME;

        stringBuilder.append(TextFormat.GOLD + "Uptime: " + formatUptime(time));

        TextFormat tpsColor = TextFormat.GREEN;
        float tps = server.getTicksPerSecond();
        if (tps < 17) {
            tpsColor = TextFormat.GOLD;
        } else if (tps < 12) {
            tpsColor = TextFormat.RED;
        }

        stringBuilder.append(TextFormat.GOLD + "Current TPS: " + tpsColor + NukkitMath.round(tps, 2) + "\n");

        stringBuilder.append(TextFormat.GOLD + "Load: " + tpsColor + server.getTickUsage() + "%\n");

        stringBuilder.append(TextFormat.GOLD + "Network upload: " + TextFormat.GREEN + NukkitMath.round((server.getNetwork().getUpload() / 1024 * 1000), 2) + " kB/s\n");

        stringBuilder.append(TextFormat.GOLD + "Network download: " + TextFormat.GREEN + NukkitMath.round((server.getNetwork().getDownload() / 1024 * 1000), 2) + " kB/s\n");

        stringBuilder.append(TextFormat.GOLD + "Thread count: " + TextFormat.GREEN + Thread.getAllStackTraces().size() + "\n");


        Runtime runtime = Runtime.getRuntime();
        double totalMB = NukkitMath.round(((double) runtime.totalMemory()) / 1024 / 1024, 2);
        double usedMB = NukkitMath.round((double) (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024, 2);
        double maxMB = NukkitMath.round(((double) runtime.maxMemory()) / 1024 / 1024, 2);
        double usage = usedMB / maxMB * 100;
        TextFormat usageColor = TextFormat.GREEN;

        if (usage > 85) {
            usageColor = TextFormat.GOLD;
        }

        stringBuilder.append(TextFormat.GOLD + "Used memory: " + usageColor + usedMB + " MB. (" + NukkitMath.round(usage, 2) + "%)\n");

        stringBuilder.append(TextFormat.GOLD + "Total memory: " + TextFormat.RED + totalMB + " MB.\n");

        stringBuilder.append(TextFormat.GOLD + "Maximum VM memory: " + TextFormat.RED + maxMB + " MB.\n");

        stringBuilder.append(TextFormat.GOLD + "Available processors: " + TextFormat.GREEN + runtime.availableProcessors() + "\n");


        TextFormat playerColor = TextFormat.GREEN;
        if (((float) server.getOnlinePlayers().size() / (float) server.getMaxPlayers()) > 0.85) {
            playerColor = TextFormat.GOLD;
        }

        stringBuilder.append(TextFormat.GOLD + "Players: " + playerColor + server.getOnlinePlayers().size() + TextFormat.GREEN + " online, " +
                TextFormat.RED + server.getMaxPlayers() + TextFormat.GREEN + " max. \n");

        for (Level level : server.getLevels().values()) {
            stringBuilder.append(
                    TextFormat.GOLD + "\nWorld \"" + level.getFolderName() + "\"" + (!Objects.equals(level.getFolderName(), level.getName()) ? " (" + level.getName() + ")" : "") + ": " +
                            TextFormat.RED + level.getChunks().size() + TextFormat.GREEN + " chunks, " +
                            TextFormat.RED + level.getEntities().length + TextFormat.GREEN + " entities, " +
                            TextFormat.RED + level.getBlockEntities().size() + TextFormat.GREEN + " blockEntities." +
                            " Time " + ((level.getTickRate() > 1 || level.getTickRateTime() > 40) ? TextFormat.RED : TextFormat.YELLOW) + NukkitMath.round(level.getTickRateTime(), 2) + "ms" +
                            (level.getTickRate() > 1 ? " (tick rate " + level.getTickRate() + ")" : "")
            );
        }

        String message = stringBuilder.toString();
        miraiGroup.sendMessageMirai(filterColor(message));
        return true;
    }

    public static void executePluginList(String name, MiraiGroup miraiGroup) {

        if (!Permission.DEFAULT_OP.equals(BotPlugin.getInstance().getPermissionByName(name))) {
            miraiGroup.sendMessageMirai("你没权权限执行此操作");
            return;
        }

        CommandSender sender = BotPlugin.getInstance().getServer().getConsoleSender();
        StringBuilder list = new StringBuilder();
        Map<String, Plugin> plugins = sender.getServer().getPluginManager().getPlugins();
        for (Plugin plugin : plugins.values()) {
            if (list.length() > 0) {
                list.append(TextFormat.WHITE + ", ");
            }
            list.append(plugin.isEnabled() ? TextFormat.GREEN : TextFormat.RED);
            list.append(plugin.getDescription().getFullName());
        }
        TranslationContainer translationContainer = new TranslationContainer("nukkit.command.plugins.success", String.valueOf(plugins.size()), list.toString());
        String message = BotPlugin.getInstance().getServer().getLanguage().translate(translationContainer);
        miraiGroup.sendMessageMirai(filterColor(message));
    }

    public static boolean executeStop(String name, MiraiGroup miraiGroup) {

        if (!Permission.DEFAULT_OP.equals(BotPlugin.getInstance().getPermissionByName(name))) {
            miraiGroup.sendMessageMirai("你没权权限执行此操作");
            return true;
        }

        CommandSender sender = BotPlugin.getInstance().getServer().getConsoleSender();
        Command.broadcastCommandMessage(sender, new TranslationContainer("commands.stop.start"));
        sender.getServer().shutdown();

        return true;
    }

    public static boolean executeList(MiraiGroup miraiGroup) {
        CommandSender sender = BotPlugin.getInstance().getServer().getConsoleSender();
        StringBuilder online = new StringBuilder();
        StringBuilder message = new StringBuilder();
        int onlineCount = 0;
        for (Player player : sender.getServer().getOnlinePlayers().values()) {
            if (player.isOnline() && (!(sender instanceof Player) || ((Player) sender).canSee(player))) {
                online.append(player.getDisplayName()).append(", ");
                ++onlineCount;
            }
        }

        if (online.length() > 0) {
            online = new StringBuilder(online.substring(0, online.length() - 2));
        }

        TranslationContainer translationContainer = new TranslationContainer("commands.players.list",
                String.valueOf(onlineCount), String.valueOf(sender.getServer().getMaxPlayers()));

        message.append(BotPlugin.getInstance().getServer().getLanguage().translate(translationContainer) + "\n");
        message.append(online.toString() + "\n");

        miraiGroup.sendMessageMirai(message.toString());
        return true;
    }

    public static boolean executeHelp(CommandSender sender, String[] args, MiraiGroup miraiGroup) {
        StringBuilder command = new StringBuilder();
        StringBuilder stringBuilder = new StringBuilder();
        int pageNumber = 1;
        int pageHeight = 10;
        if (args.length != 0) {
            try {
                pageNumber = Integer.parseInt(args[args.length - 1]);
                if (pageNumber <= 0) {
                    pageNumber = 1;
                }

                String[] newargs = new String[args.length - 1];
                System.arraycopy(args, 0, newargs, 0, newargs.length);
                args = newargs;
                for (String arg : args) {
                    if (!command.toString().equals("")) {
                        command.append(" ");
                    }
                    command.append(arg);
                }
            } catch (NumberFormatException e) {
                pageNumber = 1;
                for (String arg : args) {
                    if (!command.toString().equals("")) {
                        command.append(" ");
                    }
                    command.append(arg);
                }
            }
        }

        /*if (sender instanceof ConsoleCommandSender) {
            pageHeight = Integer.MAX_VALUE;
        }*/

        if (command.toString().equals("")) {
            Map<String, Command> commands = new TreeMap<>();
            for (Command cmd : sender.getServer().getCommandMap().getCommands().values()) {
                if (cmd.testPermissionSilent(sender)) {
                    commands.put(cmd.getName(), cmd);
                }
            }
            int totalPage = commands.size() % pageHeight == 0 ? commands.size() / pageHeight : commands.size() / pageHeight + 1;
            pageNumber = Math.min(pageNumber, totalPage);
            if (pageNumber < 1) {
                pageNumber = 1;
            }


            String title = BotPlugin.getInstance().getServer().getLanguage().translate(new TranslationContainer("commands.help.header", String.valueOf(pageNumber), String.valueOf(totalPage)));
            stringBuilder.append(title + "\n");

            int i = 1;
            for (Command command1 : commands.values()) {
                if (i >= (pageNumber - 1) * pageHeight + 1 && i <= Math.min(commands.size(), pageNumber * pageHeight)) {
                    String line = TextFormat.DARK_GREEN + "/" + command1.getName() + ": " + TextFormat.WHITE + command1.getDescription();

                    line = BotPlugin.getInstance().getServer().getLanguage().translateString(line);
                    stringBuilder.append(line + "\n");
                }
                i++;
            }

            miraiGroup.sendMessageMirai(filterColor(stringBuilder.toString()));

            return true;
        } else {
            Command cmd = sender.getServer().getCommandMap().getCommand(command.toString().toLowerCase());
            if (cmd != null) {
                if (cmd.testPermissionSilent(sender)) {
                    String message = TextFormat.YELLOW + "--------- " + TextFormat.WHITE + " Help: /" + cmd.getName() + TextFormat.YELLOW + " ---------\n";
                    message += TextFormat.GOLD + "Description: " + TextFormat.WHITE + cmd.getDescription() + "\n";
                    StringBuilder usage = new StringBuilder();
                    String[] usages = cmd.getUsage().split("\n");
                    for (String u : usages) {
                        if (!usage.toString().equals("")) {
                            usage.append("\n" + TextFormat.WHITE);
                        }
                        usage.append(u);
                    }
                    message += TextFormat.GOLD + "Usage: " + TextFormat.WHITE + usage + "\n";
                    stringBuilder.append(message);
                    miraiGroup.sendMessageMirai(stringBuilder.toString());
                    return true;
                }
            }

            stringBuilder.append(TextFormat.RED + "No help for " + command.toString().toLowerCase());
            miraiGroup.sendMessageMirai(stringBuilder.toString());
            return true;
        }
    }


    public static String filterColor(String message) {
        return message.replaceAll("§\\d", "").replaceAll("§[a-z]", "");
    }

    private static String formatUptime(long uptime) {
        long days = TimeUnit.MILLISECONDS.toDays(uptime);
        uptime -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(uptime);
        uptime -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime);
        uptime -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptime);
        return String.format(UPTIME_FORMAT, days, hours, minutes, seconds);
    }

}
