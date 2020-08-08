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
    private boolean needLog;
    private String command;
    private List<String> args;
    private Response response;
    private boolean error = false;
    private String cmdType;

    public Request() {}

    public Request(String command, List<String> args) {
        this.command = command;
        this.args = args;
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
}
