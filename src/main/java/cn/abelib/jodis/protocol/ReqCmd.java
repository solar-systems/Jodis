package cn.abelib.jodis.protocol;

import cn.abelib.jodis.impl.ObjectType;
import cn.abelib.jodis.utils.StringUtils;
import com.google.common.collect.Lists;

import java.util.List;

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

    public ReqCmd(String cmd, List<String> params) {
        // todo
        this.request = toRequestString(cmd, params);
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

    public String getCmd() {
        return this.cmd;
    }

    public List<String> getParams() {
        return this.params;
    }

    public String toRequestString(String cmd, List<String> param) {
        return null;
    }

    @Override
    public String toString() {
        return super.toString();
    }


















}
