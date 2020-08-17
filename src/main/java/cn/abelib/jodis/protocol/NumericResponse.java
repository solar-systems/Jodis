package cn.abelib.jodis.protocol;

import cn.abelib.jodis.utils.StringUtils;

/**
 * @author abel.huang
 * @date 2020/6/30 18:52
 * 数字类型
 */
public class NumericResponse implements Response{
    private String number;

    public NumericResponse(int number){
        this.number = String.valueOf(number);
    }

    public NumericResponse(float number){
        this.number = String.valueOf(number);
    }

    public NumericResponse(double number){
        this.number = String.valueOf(number);
    }

    public static NumericResponse numericResponse(int number) {
        return new NumericResponse(number);
    }

    public static NumericResponse numericResponse(float number) {
        return new NumericResponse(number);
    }

    public static NumericResponse numericResponse(double number) {
        return new NumericResponse(number);
    }

    @Override
    public String toString() {
        return this.toRespString();
    }

    @Override
    public String toRespString() {
        return ProtocolConstant.INTEGER_NUMBER_PREFIX + this.number + StringUtils.CLRF;
    }

    @Override
    public boolean isError() {
        return false;
    }
}
