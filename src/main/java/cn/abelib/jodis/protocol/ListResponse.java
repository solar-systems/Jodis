package cn.abelib.jodis.protocol;

import cn.abelib.jodis.utils.StringUtils;

import java.util.List;

/**
 * @author abel.huang
 * @date 2020/6/30 18:54
 */
public class ListResponse implements Response{
    private List<String> list;

    public ListResponse(List<String> list) {
        this.list = list;
    }

    /**
     * @param list
     * @return
     */
    public static ListResponse stringListResponse(List<String> list) {
        return new ListResponse(list);
    }

    /**
     * *2\r\n$4\r\nbest\r\n$6\r\npretty\r\n
     * @return
     */
    @Override
    public String toRespString() {
        StringBuilder resp = new StringBuilder(ProtocolConstant.LIST_PREFIX);
        resp.append(list.size());
        list.forEach(ans -> resp.append(StringUtils.CLRF).append(ans));

        resp.append(StringUtils.CLRF);
        return resp.toString();
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public String toString() {
        return this.toRespString();
    }
}
