package top.zixuan007.bot.command;

import cn.nukkit.Server;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.permission.Permission;
import top.zixuan007.bot.BotPlugin;
import top.zixuan007.bot.utils.CommandUtils;

/**
 * @author zixuan007
 * @version 1.0
 * @description: 实现控制台 & QQ群消息互发
 * @date 2022/3/1 1:04 AM
 */
public class ConsoleCommandSenderC extends ConsoleCommandSender {

    private boolean isOp = false;

    @Override
    public void sendMessage(String message) {
        StringBuilder stringBuilder = new StringBuilder();
        message = this.getServer().getLanguage().translateString(message);

        stringBuilder.append(message.trim());

        if (BotPlugin.getInstance().isShowConsoleMessage() && stringBuilder.toString().length() > 1) {
            BotPlugin.getMiraiGroup().sendMessageMirai(CommandUtils.filterColor(stringBuilder.toString()));
        }

    }

    @Override
    public boolean hasPermission(String name) {
        Permission perm = Server.getInstance().getPluginManager().getPermission(name);

        if (perm != null) {
            String permission = perm.getDefault();

            return Permission.DEFAULT_TRUE.equals(permission) || (this.isOp() && Permission.DEFAULT_OP.equals(permission)) || (!this.isOp() && Permission.DEFAULT_NOT_OP.equals(permission));
        } else {
            return Permission.DEFAULT_TRUE.equals(Permission.DEFAULT_PERMISSION) || (this.isOp() && Permission.DEFAULT_OP.equals(Permission.DEFAULT_PERMISSION)) || (!this.isOp() && Permission.DEFAULT_NOT_OP.equals(Permission.DEFAULT_PERMISSION));
        }
    }

    @Override
    public boolean isOp() {
        return isOp;
    }

    @Override
    public void setOp(boolean op) {
        isOp = op;
    }
}
