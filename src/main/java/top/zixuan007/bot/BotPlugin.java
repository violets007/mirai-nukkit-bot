package top.zixuan007.bot;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.api.bot.MiraiGroup;
import me.dreamvoid.miraimc.nukkit.event.MiraiBotOnlineEvent;
import me.dreamvoid.miraimc.nukkit.event.MiraiFriendMessageEvent;
import me.dreamvoid.miraimc.nukkit.event.MiraiGroupMessageEvent;
import tip.Main;
import top.zixuan007.bot.command.ConsoleCommandSenderC;
import top.zixuan007.bot.pojo.ServerInfo;
import top.zixuan007.bot.socket.BedrockSocket;
import top.zixuan007.bot.task.BlackBEQueryTask;
import top.zixuan007.bot.task.CheckPlayerPermissionsTask;
import top.zixuan007.bot.utils.CommandUtils;
import top.zixuan007.bot.utils.ItemIDSunName;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zixuan007
 * @version 1.0
 * @description: 速成版 灰常滴臃肿
 * @date 2022/2/28 8:50 PM
 */
public class BotPlugin extends PluginBase implements Listener {

    private HashMap<Long, Long> lastExecuteTime = new HashMap<Long, Long>();
    private static MiraiGroup miraiGroup;
    private static BotPlugin instance;
    private static ConsoleCommandSenderC consoleCommandSenderC;
    private Config config;
    private Config bindConfig;
    private MiraiBot bot;
    private tip.Main tips = null;
    // 用于记录sendMessage
    private StringBuilder stringBuilder = new StringBuilder();
    private long lastSendMessageTime = System.currentTimeMillis();
    private boolean showConsoleMessage = false;

    @Override
    public void onEnable() {
        if (instance == null) instance = this;
        if (consoleCommandSenderC == null) consoleCommandSenderC = new ConsoleCommandSenderC();
        saveResource("config.yml", false);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, consoleCommandSenderC);
        loadConfig();
        checkTipsPlugin();
        getServer().getScheduler().scheduleRepeatingTask(new CheckPlayerPermissionsTask(this), 10, false);
    }

    public void loadConfig() {
        this.config = new Config(getDataFolder() + File.separator + "config.yml", Config.YAML);
        this.bindConfig = new Config(getDataFolder() + File.separator + "bing.yml", Config.YAML);
    }

    @Override
    public void onDisable() {

    }

    public void checkTipsPlugin() {
        Plugin tips = getServer().getPluginManager().getPlugin("Tips");
        if ((tips instanceof tip.Main)) this.tips = (Main) tips;
        if (tips != null) {
            getLogger().info("检测到: " + tips.getDescription().getFullName() + " 插件存在,可配置QQ群聊天消息显示格式!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(">> 绑定QQ账号必须在游戏内进行");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(">> /bindqq [qq账号] >> 绑定QQ");
            return true;
        }

        if (!isNumericZidai(args[0])) {
            sender.sendMessage(">> 绑定的QQ账号输入的不是数字");
            return true;
        }

        HashMap<String, String> bindInfo = new HashMap<>();
        bindInfo.put("qq", args[0]);
        bindInfo.put("permissions", "user");
        if (sender.isOp()) bindInfo.put("permissions", "op");
        this.bindConfig.set(sender.getName(), bindInfo);
        this.bindConfig.save();

        sender.sendMessage(">> 成功绑定QQ账号: " + args[0]);

        return true;
    }


    @EventHandler
    public void onMiraiBotOnline(MiraiBotOnlineEvent event) {
        this.bot = MiraiBot.getBot(event.getID());
    }

    @EventHandler
    public void onGroupMessageReceive(MiraiGroupMessageEvent e) {
        String message = e.getMessage();
        String command = message.split(" ")[0];
        List<Long> groupIds = this.config.getLongList("receive-group");
        MiraiBot bot = MiraiBot.getBot(e.getBotID());
        ConsoleCommandSender consoleSender = getServer().getConsoleSender();
        Map<String, String> bindInfo = getBindInfoByQQ(e.getSenderID());
        String name = getPlayerNameByQQ(e.getSenderID());
        MiraiGroup group = bot.getGroup(e.getGroupID());

        if (!groupIds.contains(e.getGroupID())) return;
        if (this.lastExecuteTime.containsKey(e.getSenderID())) {
            if ((System.currentTimeMillis() - lastSendMessageTime < 3000)) {
                group.sendMessageMirai("[mirai:at:" + e.getSenderID() + "] ：" + "执行任务过快,请稍后");
                return;
            }
        }

        // 差个几十毫秒暂时先不管
        lastSendMessageTime = System.currentTimeMillis();
        this.lastExecuteTime.put(e.getSenderID(), System.currentTimeMillis());
        setMiraiGroup(group);
        stringBuilder.delete(0, stringBuilder.length());
        this.showConsoleMessage = true;
        switch (command) {
            case "#服务器状态":
                CommandUtils.executeStatus(name, group);
                break;
            case "#插件列表":
                CommandUtils.executePluginList(name, group);
                break;
            case "#关闭":
                CommandUtils.executeStop(name, group);
                break;
            case "#帮助命令":
                String[] commands = message.split(" ");
                String[] args = new String[0];

                if (commands.length >= 2) {
                    args = new String[]{commands[1]};
                }

                CommandUtils.executeHelp(consoleSender, args, group);
                break;
            case "#查云黑":
                commands = message.trim().split(" ");
                if (commands.length < 2) {
                    group.sendMessageMirai("命令执行有误,请使用 #查云黑 <playerName> [xuid]");
                    return;
                }

                String xuid = "";
                if (commands.length > 2) xuid = commands[2];

                getServer().getScheduler().scheduleAsyncTask(this, new BlackBEQueryTask(commands[1], xuid, miraiGroup));
                break;
            case "#查看背包":
                commands = message.trim().split(" ");
                if (commands.length < 2) {
                    group.sendMessageMirai("命令执行有误,请使用 #查看背包 [playerName]");
                    return;
                }

                CompoundTag offlinePlayerData = getServer().getOfflinePlayerData(commands[1]);
                if (offlinePlayerData == null) {
                    group.sendMessageMirai("无法找到玩家: " + commands[1]);
                    return;
                }
                StringBuilder stringBuilder = new StringBuilder();
                Player player = getServer().getPlayer(commands[1]);
                stringBuilder.append("=" + commands[1] + "背包物品=\n");

//                    getServer().getLanguage().translateString(name)
                if (player != null) {
                    Item[] armorContents = player.getInventory().getArmorContents();
                    Map<Integer, Item> contents = player.getInventory().getContents();
                    for (Map.Entry<Integer, Item> entry : contents.entrySet()) {
                        Integer index = entry.getKey();
                        Item item = entry.getValue();
                        if (item.getCount() > 0) {
                            name = ItemIDSunName.getIDByName(item);
                            name = TextFormat.clean(name);

                            stringBuilder.append("物品: " + name + "\t数量: " + item.getCount() + "\n");
                        }
                    }

                    stringBuilder.append("装备栏: \n");
                    for (Item armorContent : armorContents) {
                        name = ItemIDSunName.getIDByName(armorContent);
                        stringBuilder.append("装备: " + name + "\n");
                    }

                } else {
                    ListTag<? extends Tag> inventory = offlinePlayerData.getList("Inventory");


                    for (int i = 0; i < inventory.size(); i++) {
                        CompoundTag tag = (CompoundTag) inventory.get(i);
                        Item itemHelper = NBTIO.getItemHelper(tag);
                        if (itemHelper.getCount() > 0) {
                            name = ItemIDSunName.getIDByName(itemHelper);
                            name = TextFormat.clean(name);

                            if (i < 45) {
                                stringBuilder.append("物品: " + name + "\t数量: " + itemHelper.getCount() + "\n");
                            } else {
                                if (i == 45) {
                                    stringBuilder.append("装备栏: \n");
                                }

                                stringBuilder.append("装备: " + name + "\n");
                            }

                        }

                    }
                }

                group.sendMessageMirai(stringBuilder.toString());
                break;
            case "#执行命令":
                commands = message.trim().split(" ");
                if (commands.length < 2) {
                    group.sendMessageMirai("命令执行有误,示例:#执行命令 op vilotes");
                    return;
                }
                String executeCommand = message.replace(commands[0] + " ", "").trim();

                List<String> prohibitionExecutionList = this.config.getStringList("prohibition-execution");
                if (prohibitionExecutionList.contains(commands[1])) {
                    group.sendMessageMirai("此命令禁止在QQ群执行,请使用 #群帮助命令");
                    return;
                }

                bindInfo = getBindInfoByQQ(e.getSenderID());
                if (bindInfo == null) {
                    group.sendMessageMirai("当前QQ账号没有在游戏中进行绑定");
                    return;
                }

                String permissions = bindInfo.get("permissions");
                consoleCommandSenderC.setOp(permissions.equals("op") ? true : false);
                getServer().dispatchCommand(consoleCommandSenderC, executeCommand);
                break;
            case "#聊天显示":
                boolean enable = this.config.getBoolean("show-server-player-chat", false);
                if (enable) {
                    this.config.set("show-server-player-chat", false);
                    miraiGroup.sendMessageMirai("成功关闭聊天显示");
                } else {
                    this.config.set("show-server-player-chat", true);
                    miraiGroup.sendMessageMirai("成功开启聊天显示");
                }
                this.saveConfig();
                break;
            case "#在线玩家":
                CommandUtils.executeList(group);
                break;
            case "#mcpe":
                commands = message.split(" ");
                if (commands.length < 2) {
                    group.sendMessageMirai("命令执行有误,示例:#查服务器 ip port");
                    return;
                }

                int port = 19132;
                if (commands.length > 2) port = Integer.parseInt(commands[2]);

                ServerInfo serverInfo = BedrockSocket.fetchData(commands[1], port);
                if (serverInfo.getOnline() == null || serverInfo.getOnline().length() < 1) {
                    group.sendMessageMirai("当前服务器不在线");
                    return;
                }

                group.sendMessageMirai("[MCPE]\n" +
                        "Motd: " + serverInfo.getMotd() + "\n" +
                        "sub-Motd: " + serverInfo.getSubMotd() + "\n" +
                        "协议版本: " + serverInfo.getAgreement() + "\n" +
                        "游戏版本: " + serverInfo.getVersion() + "\n" +
                        "在线: " + serverInfo.getOnline() + "/" + serverInfo.getMax() + "\n" +
                        "游戏模式: " + serverInfo.getGameMode()
                );

                break;
            case "#禁止命令":
                commands = message.split(" ");
                if (commands.length < 2) {
                    group.sendMessageMirai("命令执行有误,示例:#禁用命令 kill");
                    return;
                }

                List<String> banCommandList = this.config.getStringList("prohibition-execution");
                banCommandList.add(commands[1]);
                this.config.set("prohibition-execution", banCommandList);
                this.config.save();

                group.sendMessageMirai("成功禁止命令: " + commands[1]);

                break;
            case "#发消息":
                commands = message.split(" ");

                if (bindInfo == null) {
                    group.sendMessageMirai("当前QQ账号还没有在游戏中绑定,请前往游戏绑定!");
                    return;
                }

                if (commands.length < 2) {
                    group.sendMessageMirai("命令执行有误,示例:#发消息 ABC");
                    return;
                }

                message = message.substring(message.indexOf(" ", 0), message.length());

                getServer().broadcastMessage("[group]: " + getPlayerNameByQQ(e.getSenderID()) + " >> " + message);
                break;
            case "#群帮助命令":
                sendHelpByQQGroup(group);
                break;
        }

        this.showConsoleMessage = false;
        if (this.stringBuilder.length() > 0)
            miraiGroup.sendMessageMirai(this.stringBuilder.toString());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(PlayerChatEvent event) {
        boolean enableShow = this.getConfig().getBoolean("show-server-player-chat", false);
        if (enableShow) {
            String name = event.getPlayer().getName();
            String msg = CommandUtils.filterColor(event.getMessage());
            List<Long> groupIds = this.config.getLongList("receive-group");

            HashMap<String, Object> groupMessageMap = (HashMap<String, Object>) this.getConfig().get("group-tips");
            boolean enable = (boolean) groupMessageMap.get("enable");

            if (enable) {
                String message = this.tips.getVarManager().toMessage(event.getPlayer(), groupMessageMap.get("format").toString());
                message = TextFormat.clean(message.replace("{msg}", msg));
                for (Long groupId : groupIds) {
                    bot.getGroup(groupId).sendMessageMirai(message);
                }
            } else {
                for (Long groupId : groupIds) {
                    bot.getGroup(groupId).sendMessageMirai("[MCPE]: " + name + " >> " + msg);
                }
            }

        }

    }

    /*
    调试代码
    @EventHandler
    public void onFriend(MiraiFriendMessageEvent event) {

        String message = event.getMessage();
        String[] commands = message.trim().split(" ");
        if (commands.length < 2) {
            event.getFriend().sendMessageMirai("命令执行有误,请使用 #查云黑 <playerName> [xuid]");
            return;
        }

        String xuid = "";
        if (commands.length > 2) xuid = commands[2];

        getServer().getScheduler().scheduleAsyncTask(this, new BlackBEQueryTask(commands[1], xuid, event.getFriend()));
    }*/


    public void sendHelpByQQGroup(MiraiGroup miraiGroup) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("==群帮助命令==\n");
        stringBuilder.append("#关闭\n");
        stringBuilder.append("#插件列表\n");
        stringBuilder.append("#聊天显示\n");
        stringBuilder.append("#在线玩家\n");
        stringBuilder.append("#服务器状态\n");
        stringBuilder.append("#mcpe [ip] [port]\n");
        stringBuilder.append("#执行命令 [args...]\n");
        stringBuilder.append("#查看背包 [playerName]\n");
        stringBuilder.append("#禁止命令 [args...]  >> 禁止群玩家执行\n");
        stringBuilder.append("==群帮助命令==");
        miraiGroup.sendMessageMirai(stringBuilder.toString());
    }

    public static boolean isNumericZidai(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public Map<String, String> getBindInfoByQQ(long qq) {
        for (Map.Entry<String, Object> entry : this.bindConfig.getAll().entrySet()) {
            Map<String, String> bindInfo = (Map<String, String>) entry.getValue();
            if ((qq + "").equals(bindInfo.get("qq"))) {
                return bindInfo;
            }
        }

        return null;
    }

    public String getPlayerNameByQQ(long qq) {
        for (Map.Entry<String, Object> entry : this.bindConfig.getAll().entrySet()) {
            Map<String, String> bindInfo = (Map<String, String>) entry.getValue();
            if ((qq + "").equals(bindInfo.get("qq"))) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getPermissionByName(String name) {
        for (Map.Entry<String, Object> entry : this.bindConfig.getAll().entrySet()) {
            Map<String, String> bindInfo = (Map<String, String>) entry.getValue();
            if (entry.getKey().equals(name)) {
                return bindInfo.get("permissions");
            }
        }
        return null;
    }

    public static BotPlugin getInstance() {
        return instance;
    }

    public static void setInstance(BotPlugin instance) {
        BotPlugin.instance = instance;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public static MiraiGroup getMiraiGroup() {
        return miraiGroup;
    }

    public static void setMiraiGroup(MiraiGroup miraiGroup) {
        BotPlugin.miraiGroup = miraiGroup;
    }

    public Config getBindConfig() {
        return bindConfig;
    }

    public void setBindConfig(Config bindConfig) {
        this.bindConfig = bindConfig;
    }

    public boolean isShowConsoleMessage() {
        return showConsoleMessage;
    }

    public void setShowConsoleMessage(boolean showConsoleMessage) {
        this.showConsoleMessage = showConsoleMessage;
    }

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    public void setStringBuilder(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }
}
