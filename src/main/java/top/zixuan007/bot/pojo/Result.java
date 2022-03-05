package top.zixuan007.bot.pojo;

/**
 * @author zixuan007
 */

public class Result<T> {

    /**
     * 业务错误码
     */
    private Integer status;

    /**
     * 信息描述
     */
    private String message;

    private String version;

    /**
     * 返回参数
     */
    private T data;


    public Result(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", version='" + version + '\'' +
                ", data=" + data +
                '}';
    }
}
