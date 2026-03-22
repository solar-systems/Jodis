  package cn.abelib.jodis.protocol;

import java.util.List;


/**
 * @author abel.huang
 * @date 2020/6/30 18:54
 *
 *  eg:
 *   *3\r
$3\r
set\r
$4\r
name\r
$4\r
huang\r

 */
public class Request {
    private String request;
    private boolean needLog = false;
    private String command;
    private List<String> args;
    private Response response;
    private boolean error = false;

    public Request() {}

    /**
     * command 为大写模式
     * @param command
     * @param args
     */
    public Request(String command, List<String> args) {
        this.command = command.toUpperCase();
        this.args = args;
        // 构建内联命令格式（用于 WAL 存储，单行便于按行读取）
        StringBuilder sb = new StringBuilder();
        sb.append(this.command);
        for (String arg : args) {
            sb.append(" ").append(arg);
        }
        this.request = sb.toString();

        // 判断是否需要写入日志
        if (ProtocolConstant.NEED_LOGS.contains(this.command)) {
            this.needLog = true;
        }
    }

    public boolean needLog() {
        return this.needLog;
    }

    public void needLog(boolean isNeed) {
        this.needLog = isNeed;
    }

    public String getCommand() {
        return this.command;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public String getRequest() {
        return this.request;
    }

    public boolean isError() {
        return this.error;
    }

    public Response errorResponse() {
        return this.response;
    }

    public static Request badRequest(Response errResp) {
        Request request = new Request();
        request.error = true;
        request.response = errResp;
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    @Override
    public String toString() {
        return this.request;
    }
}
