package cn.abelib.jodis.protocol;

import java.util.List;


/**
 * @author abel.huang
 * @date 2020/6/30 18:54
 *
 *  eg:
 *   *3\r\n$3\r\nset\r\n$4\r\nname\r\n$4\r\nhuang\r\n
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
     * command为大写模式
     * @param command
     * @param args
     */
    public Request(String command, List<String> args) {
        this.command = command.toUpperCase();
        this.args = args;
        // 判断是否需要写入日志
        if (ProtocolConstant.NEED_LOGS.contains(this.command)) {
            this.needLog = true;
        }
    }

    public boolean needLog() {
        return this.needLog;
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
}
