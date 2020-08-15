package cn.abelib.jodis.protocol;

import cn.abelib.jodis.utils.NumberUtils;
import cn.abelib.jodis.utils.StringUtils;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @Author: abel.huang
 * @Date: 2020-08-07 01:06
 */
public class RespParser {

    public Request parse(String request) {
        Request req;
        String[] arguments;
        boolean parsed = false;
        // maybe inline command
        if (!request.startsWith(ProtocolConstant.LIST_PREFIX)) {
            arguments = request.split(StringUtils.SPACE);
            parsed = true;
        } else {
            request = request.substring(1);
            String[] cmds = request.split(StringUtils.CLRF);
            String sizeStr = cmds[0].trim();
            Integer cmdSize = NumberUtils.toInt(sizeStr);
            if (Objects.isNull(cmdSize)) {
                Response errResp = ErrorResponse.errorSyntax();
                return Request.badRequest(errResp);
            }
            if (cmdSize * 2 + 1 != cmds.length || cmds.length < 3) {
                Response errResp = ErrorResponse.errorSyntax();
                return Request.badRequest(errResp);
            }
            arguments = new String[cmdSize];
            int idx = 0;
            for (int i = 2; i < cmds.length; i += 2) {
                arguments[idx] = cmds[i];
                idx ++;
            }
        }
        int len = arguments.length;
        if (len < 1) {
            Response errResp = ErrorResponse.errorSyntax();
            return Request.badRequest(errResp);
        }
        String command = arguments[0].toUpperCase();
        List<String> args = Lists.newArrayList(arguments);
        args.remove(0);
        req = new Request(command, args);
        if (!parsed) {
            request = parseRequest(arguments);
        }
        req.setRequest(request);
        return req;
    }

    /**
     * eg *3\r\n$3\r\nset\r\n$4\r\nname\r\n$3\r\nbob\r\n
    */
    public String parseRequest(String[] arguments) {
        StringBuilder request = new StringBuilder(ProtocolConstant.LIST_PREFIX);
        for (int i = 0; i < arguments.length; i ++) {
            String arg = arguments[i];
            request.append(arg);
            if (i != arguments.length - 1) {
                request.append(StringUtils.SPACE);
            }
        }
        return request.toString();
    }

    public Request stringSetCmd(String key, String value) {
        return new Request(ProtocolConstant.STRING_SET, Lists.newArrayList(key, value));
    }

    public Request hashMultiSetCmd(String key, Map<String, String> value) {
        List<String> args = Lists.newArrayList();
        args.add(key);
        value.forEach((k, v) -> {
            args.add(k);
            args.add(v);
        });
        return new Request(ProtocolConstant.HASH_HMSET, args);
    }

    public Request setAddCmd(String key, Set<String> value) {
        List<String> args = Lists.newArrayList();
        args.add(key);
        args.addAll(value);
        return new Request(ProtocolConstant.SET_SADD, args);
    }

    public Request listPushCmd(String key, List<String> value) {
        value.add(key);
        return new Request(ProtocolConstant.LIST_LPOP, value);
    }

    public Request sortedSetAddCmd(String key, Map<String, Double> value) {
        List<String> args = Lists.newArrayList();
        args.add(key);
        value.forEach((k, v) -> {
            args.add(k);
            args.add(String.valueOf(v));
        });
        return new Request(ProtocolConstant.ZSET_ZADD, args);
    }
}
