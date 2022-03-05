package top.zixuan007.bot.task;

import cn.nukkit.scheduler.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.dreamvoid.miraimc.api.bot.MiraiFriend;
import me.dreamvoid.miraimc.api.bot.MiraiGroup;
import top.zixuan007.bot.BotPlugin;
import top.zixuan007.bot.pojo.BlackBEQueryInfo;
import top.zixuan007.bot.pojo.Result;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * @author zixuan007
 * @description: 云黑查询任务
 * @date: 2022/3/5 1:25 AM
 */
public class BlackBEQueryTask extends AsyncTask {

    public final static String API_VERSION = "3.1[NE]";
    public final static String API_DOMAIN = "http://api.blackbe.xyz/openapi";
    public final static int QUERY_SUCCESS = 2000;

    private String playerName;
    private String xuid;
    private MiraiGroup miraiGroup;

    public BlackBEQueryTask(String playerName, String xuid, MiraiGroup miraiGroup) {
        this.playerName = playerName;
        this.xuid = xuid;
        this.miraiGroup = miraiGroup;
    }

    @Override
    public void onRun() {

        BufferedReader bufferedReader = null;
        HttpURLConnection httpURLConnection = null;
        URL url = null;

        try {
            url = new URL(API_DOMAIN + "/v3/check?name=" + URLEncoder.encode(playerName, "UTF-8") + "&xuid=" + URLEncoder.encode(xuid, "UTF-8"));
            BotPlugin.getInstance().getLogger().info(url.toString());

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("User-Agent", "RuMao/1.3");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {

                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String inputLine;
                StringBuilder stringBuilder = new StringBuilder();
                while ((inputLine = bufferedReader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }

                Gson gson = new Gson();
                Result<BlackBEQueryInfo> result = gson.fromJson(stringBuilder.toString(), new TypeToken<Result<BlackBEQueryInfo>>() {
                }.getType());
                if (result.getStatus() == QUERY_SUCCESS) {
                    BlackBEQueryInfo blackBEQueryInfo = result.getData();
                    miraiGroup.sendMessageMirai(new GsonBuilder().setPrettyPrinting().create().toJson(blackBEQueryInfo.getInfo()));
                } else {
                    miraiGroup.sendMessageMirai(result.toString());
                }

                bufferedReader.close();
                httpURLConnection.disconnect();
            }
            // IOException MalformedURLException UnsupportedEncodingException
        } catch (Exception exception) {
            exception.printStackTrace();
            try {
                BotPlugin.getInstance().getLogger().error("在连接至云黑查询平台时出现问题,状态码=" + httpURLConnection.getResponseCode() + ",请求URL=" + url.toExternalForm());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }
}
