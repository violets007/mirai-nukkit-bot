package top.zixuan007.bot.task;

import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;
import top.zixuan007.bot.BotPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author zixuan007
 * @version 1.0
 * @description:
 * @date 2022/3/1 9:58 PM
 */
public class CheckPlayerPermissionsTask extends PluginTask<BotPlugin> {
    /**
     * 构造一个插件拥有的任务的方法。<br>Constructs a plugin-owned task.
     *
     * @param owner 这个任务的所有者插件。<br>The plugin object that owns this task.
     * @since Nukkit 1.0 | Nukkit API 1.0.0
     */
    public CheckPlayerPermissionsTask(BotPlugin owner) {
        super(owner);
    }

    @Override
    public void onRun(int currentTick) {
        Config bindConfig = getOwner().getBindConfig();
        for (Map.Entry<String, Object> entry : bindConfig.getAll().entrySet()) {
            HashMap<String, String> value = (HashMap<String, String>) entry.getValue();
            Config opConfig = getOwner().getServer().getOps();
            String permissions = value.get("permissions");
            Set<String> opList = opConfig.getKeys();
            if (!opList.contains(entry.getKey())) {
                value.put("permissions", "user");
            } else {
                value.put("permissions", "op");
            }
            bindConfig.set(entry.getKey(), value);
            bindConfig.save();

        }

    }
}
