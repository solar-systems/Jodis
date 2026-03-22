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
    private static final cn.abelib.jodis.utils.Logger logger = cn.abelib.jodis.utils.Logger.getLogger(RespParser.class);

    public Request parse(String request) {
        logger.info("RespParser.parse input: {}", request.replace("\r", "\\r").replace("\n", "\\n"));

        Request req;
        String[] arguments;
        boolean parsed = false;
        // maybe inline command
        if (!request.startsWith(ProtocolConstant.LIST_PREFIX)) {
            logger.info("Parsing as inline command");
            arguments = request.split(StringUtils.SPACE);
            parsed = true;
        } else {
            logger.info("Parsing as RESP protocol");
            request = request.substring(1);
            String[] cmds = request.split(StringUtils.CLRF);

            logger.info("Split result: length={}", cmds.length);
            for (int i = 0; i < cmds.length; i++) {
                logger.info("  cmds[{}] = \"{}\"", i, cmds[i]);
            }

            String sizeStr = cmds[0].trim();
            Integer cmdSize = NumberUtils.parseInt(sizeStr);
            logger.info("cmdSize from string '{}': {}", sizeStr, cmdSize);

            if (Objects.isNull(cmdSize)) {
                logger.error("Failed to parse cmdSize");
                Response errResp = ErrorResponse.errorSyntax();
                return Request.badRequest(errResp);
            }
            // RESP 协议格式：[count, $len1, val1, $len2, val2, ...]
            // count + (len + value) * cmdSize = 1 + cmdSize * 2 个元素
            if (cmds.length < cmdSize * 2 + 1) {
                logger.error("cmds.length {} < cmdSize * 2 + 1 = {}", cmds.length, cmdSize * 2 + 1);
                Response errResp = ErrorResponse.errorSyntax();
                return Request.badRequest(errResp);
            }
            arguments = new String[cmdSize];
            int idx = 0;
            // 实际值在索引 2, 4, 6, ... (跳过 count 和长度声明)
            for (int i = 2; i < cmds.length && idx < cmdSize; i += 2) {
                arguments[idx] = cmds[i];
                logger.info("  arguments[{}] = \"{}\"", idx, cmds[i]);
                idx ++;
            }
        }
        int len = arguments.length;
        if (len < 1) {
            logger.error("No arguments found");
            Response errResp = ErrorResponse.errorSyntax();
            return Request.badRequest(errResp);
        }
        String command = arguments[0].toUpperCase();
        logger.info("Parsed command: {}, args count: {}", command, arguments.length - 1);
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
     * 构建内联命令格式（用于 WAL 存储，单行，便于按行读取）
     * eg: SET name Jodis
     */
    public String parseRequest(String[] arguments) {
        StringBuilder request = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            request.append(arguments[i]);
            if (i < arguments.length - 1) {
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
