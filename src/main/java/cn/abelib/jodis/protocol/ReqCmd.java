package cn.abelib.jodis.protocol;

import cn.abelib.jodis.impl.ObjectType;
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
public class ReqCmd {
    private String request;
    private boolean needLog;
    private String cmd;
    private List<String> params;
    private ObjectType objectType;

    public ReqCmd(String request) {
        this.request = request;
        parseRequest();
    }

    public ReqCmd(String cmd, List<String> args) {
        // todo
        this.request = toRequestString(cmd, args);
    }

    private void parseRequest() {
        if (!request.startsWith(CmdConstant.LIST_PREFIX)) {
            // todo
            throw new CmdParserException("");
        }

        int sizeEnd = request.indexOf(StringUtils.CLRF);
        int requestLength = request.length();
        String sizeString = request.substring(1, sizeEnd);
        int size = Integer.parseInt(sizeString);
        String cmdString = request.substring(sizeEnd, requestLength);
        String[] cmds = cmdString.split(StringUtils.CLRF);
        if (cmds.length != size) {
            throw new CmdParserException("");
        }
        cmd = cmds[0].toUpperCase();
        params = Lists.newArrayList(cmds);
        params.remove(0);

    }

    public boolean needLog() {
        return this.needLog;
    }

    public static ReqCmd stringSetCmd(String key, String value) {
        return new ReqCmd(CmdConstant.STRING_SET, Lists.newArrayList(key, value));
    }

    public static ReqCmd hashMultiSetCmd(String key, Map<String, String> value) {
        List<String> args = Lists.newArrayList();
        args.add(key);
        value.forEach((k, v) -> {
            args.add(k);
            args.add(v);
        });
        return new ReqCmd(CmdConstant.HASH_HMSET, args);
    }

    public static ReqCmd setAddCmd(String key, Set<String> value) {
        List<String> args = Lists.newArrayList();
        args.add(key);
        args.addAll(value);
        return new ReqCmd(CmdConstant.SET_SADD, args);
    }

    public static ReqCmd listPushCmd(String key, List<String> value) {
        value.add(key);
        return new ReqCmd(CmdConstant.LIST_LPOP, value);
    }

    public static ReqCmd zsetAddCmd(String key, Map<String, Double> value) {
        List<String> args = Lists.newArrayList();
        args.add(key);
        value.forEach((k, v) -> {
            args.add(k);
            args.add(String.valueOf(v));
        });
        return new ReqCmd(CmdConstant.ZSET_ZADD, args);
    }

    public String getCmd() {
        return this.cmd;
    }

    public List<String> getParams() {
        return this.params;
    }

    public String toRequestString(String cmd, List<String> args) {
        return null;
    }

    // todo
    @Override
    public String toString() {
        return super.toString();
    }

    public String getRequest() {
        return this.request;
    }
}
