package top.zixuan007.bot.pojo;

import java.util.List;

/**
 * @author zixuan007
 * @version 1.0
 * @description: 云黑系统展示数据
 * @date 2022/3/5 1:52 AM
 */
public class BlackBEQueryInfo {

    private boolean exist;
    private List<BlackBEQueryVO> info;

    public BlackBEQueryInfo() {
    }

    public class BlackBEQueryVO {
        private String uuid;
        private String name;
        private String black_id;
        private String xuid;
        private String info;
        private int level;
        private int qq;
        private List<String> photos;

        public BlackBEQueryVO() {
        }

        public BlackBEQueryVO(String uuid, String name, String black_id, String xuid, String info, int level, int qq, List<String> photos) {
            this.uuid = uuid;
            this.name = name;
            this.black_id = black_id;
            this.xuid = xuid;
            this.info = info;
            this.level = level;
            this.qq = qq;
            this.photos = photos;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBlack_id() {
            return black_id;
        }

        public void setBlack_id(String black_id) {
            this.black_id = black_id;
        }

        public String getXuid() {
            return xuid;
        }

        public void setXuid(String xuid) {
            this.xuid = xuid;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int getQq() {
            return qq;
        }

        public void setQq(int qq) {
            this.qq = qq;
        }

        public List<String> getPhotos() {
            return photos;
        }

        public void setPhotos(List<String> photos) {
            this.photos = photos;
        }

        @Override
        public String toString() {
            return "BlackBEQueryVO{" +
                    "uuid='" + uuid + '\'' +
                    ", name='" + name + '\'' +
                    ", black_id='" + black_id + '\'' +
                    ", xuid='" + xuid + '\'' +
                    ", info='" + info + '\'' +
                    ", level=" + level +
                    ", qq=" + qq +
                    ", photos=" + photos +
                    '}';
        }
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public List<BlackBEQueryVO> getInfo() {
        return info;
    }

    public void setInfo(List<BlackBEQueryVO> info) {
        this.info = info;
    }

    public BlackBEQueryInfo(boolean exist, List<BlackBEQueryVO> info) {
        this.exist = exist;
        this.info = info;
    }

    @Override
    public String toString() {
        return "BlackBEQueryInfo{" +
                "exist=" + exist +
                ", info=" + info +
                '}';
    }
}


