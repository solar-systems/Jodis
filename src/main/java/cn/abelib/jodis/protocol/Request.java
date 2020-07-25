package cn.abelib.jodis.protocol;

import cn.abelib.jodis.utils.StringUtils;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private String cmd;
    private List<String> args;

    public Request(String request) {
        this.request = request;
        parseRequest();
    }

    public Request(String cmd, List<String> args) {
        this.request = genRequest(cmd, args);
    }

    private void parseRequest() {
        if (!request.startsWith(ProtocolConstant.LIST_PREFIX)) {
            // todo
            throw new RequestParserException("");
        }

        int sizeEnd = request.indexOf(StringUtils.CLRF);
        int requestLength = request.length();
        String sizeString = request.substring(1, sizeEnd);
        int size = Integer.parseInt(sizeString);
        String cmdString = request.substring(sizeEnd, requestLength);
        String[] cmds = cmdString.split(StringUtils.CLRF);
        if (cmds.length != size) {
            throw new RequestParserException("");
        }
        cmd = cmds[0].toUpperCase();
        args = Lists.newArrayList(cmds);
        args.remove(0);

    }

    public boolean needLog() {
        return this.needLog;
    }

    public static Request stringSetCmd(String key, String value) {
        return new Request(ProtocolConstant.STRING_SET, Lists.newArrayList(key, value));
    }

    public static Request hashMultiSetCmd(String key, Map<String, String> value) {
        List<String> args = Lists.newArrayList();
        args.add(key);
        value.forEach((k, v) -> {
            args.add(k);
            args.add(v);
        });
        return new Request(ProtocolConstant.HASH_HMSET, args);
    }

    public static Request setAddCmd(String key, Set<String> value) {
        List<String> args = Lists.newArrayList();
        args.add(key);
        args.addAll(value);
        return new Request(ProtocolConstant.SET_SADD, args);
    }

    public static Request listPushCmd(String key, List<String> value) {
        value.add(key);
        return new Request(ProtocolConstant.LIST_LPOP, value);
    }

    public static Request zsetAddCmd(String key, Map<String, Double> value) {
        List<String> args = Lists.newArrayList();
        args.add(key);
        value.forEach((k, v) -> {
            args.add(k);
            args.add(String.valueOf(v));
        });
        return new Request(ProtocolConstant.ZSET_ZADD, args);
    }

    public String getCmd() {
        return this.cmd;
    }

    public List<String> getArgs() {
        return this.args;
    }

    /**
     * eg *3\r\n$3\r\nset\r\n$4\r\nname\r\n$3\r\nbob\r\n
     * @param cmd
     * @param args
     * @return
     */
    public String genRequest(String cmd, List<String> args) {
        args.add(0, cmd);
        int len = args.size();
        StringBuilder request = new StringBuilder(ProtocolConstant.LIST_PREFIX);
        request.append(len).append(StringUtils.CLRF);
        for (String arg : args) {
            request.append(arg);
            request.append(StringUtils.CLRF);
        }
        return request.toString();
    }

    /**
     * 返回多行字符数组格式的字符串
     * @return
     */
    @Override
    public String toString() {
        return genRequest(this.getCmd(), this.getArgs());
    }

    public String getRequest() {
        return this.request;
    }
}
