package cn.abelib.jodis;


/**
 * @Author: abel.huang
 * @Date: 2020-07-02 23:11
 *  todo Jodis global config
 */
public class JodisConfig {
    private String logDir;
    private String aofFName;
    private String rdbFName;

    public JodisConfig() {}

    public static JodisConfig defaultConfig() {
        return new JodisConfig();
    }
}
