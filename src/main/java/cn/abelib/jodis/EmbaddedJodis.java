package cn.abelib.jodis;

import cn.abelib.jodis.impl.JodisDb;
import cn.abelib.jodis.protocol.Response;
import cn.abelib.jodis.server.JodisConfig;
import cn.abelib.jodis.utils.Logger;
import cn.abelib.jodis.utils.PropertiesUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @Author: abel.huang
 * @Date: 2020-08-09 15:53
 * Embedded Jodis
 */
public class EmbaddedJodis {
    static Logger log = Logger.getLogger(Jodis.class);

    private JodisDb jodisDb;

    EmbaddedJodis(JodisConfig jodisConfig) throws IOException {
        jodisDb = new JodisDb(jodisConfig);
    }

    public static EmbaddedJodis start(String propsFileName) throws IOException {
        Path path = Paths.get(propsFileName);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            log.error( "ERROR: EmbaddedJodis config file not exist => '{}', copy one from 'conf/jodis.properties' first.",
                    path.toAbsolutePath().toString());
            System.exit(-1);
        }

        Properties  mainProperties = PropertiesUtils.loadProps(propsFileName);
        JodisConfig config = new JodisConfig(mainProperties);
        return new EmbaddedJodis(config);
    }

    public  Response execute(String request) throws IOException {
        return jodisDb.execute(request);
    }
}
