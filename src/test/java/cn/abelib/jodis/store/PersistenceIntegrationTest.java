package cn.abelib.jodis.store;

import cn.abelib.jodis.impl.*;
import cn.abelib.jodis.protocol.RespParser;
import cn.abelib.jodis.server.JodisConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 测试 JDB 和 WAL 的完整持久化流程
 */
public class PersistenceIntegrationTest {
    
    private static final String TEST_LOG_DIR = "log/test/";
    private static final String TEST_WAL_FILE = "test.wal";
    private static final String TEST_JDB_FILE = "test.jdb";
    
    private JodisDb jodisDb;
    private RespParser parser;
    
    @Before
    public void setUp() throws Exception {
        // 清理旧的测试文件
        Files.deleteIfExists(Paths.get(TEST_LOG_DIR, TEST_WAL_FILE));
        Files.deleteIfExists(Paths.get(TEST_LOG_DIR, TEST_WAL_FILE + ".rewrite"));
        Files.deleteIfExists(Paths.get(TEST_LOG_DIR, TEST_JDB_FILE));
        
        // 创建测试配置
        Properties props = new Properties();
        props.setProperty("log.dir", TEST_LOG_DIR);
        props.setProperty("log.wal", TEST_WAL_FILE);
        props.setProperty("log.jdb", TEST_JDB_FILE);
        props.setProperty("log.reload.mode", "0");  // 不自动加载
        
        JodisConfig config = new JodisConfig(props);
        jodisDb = new JodisDb(config);
        parser = new RespParser();
    }
    
    @After
    public void tearDown() throws Exception {
        if (jodisDb != null) {
            jodisDb.close();
        }
    }
    
    @Test
    public void testWalWriteAndRead() throws Exception {
        // 1. 执行写操作（会写入 WAL）
        String setCmd = "*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$5\r\nJodis\r\n";
        jodisDb.execute(setCmd);
        
        // 2. 验证数据在内存中
        Assert.assertTrue(jodisDb.containsKey("name"));
        
        // 3. 关闭数据库（会保存快照）
        jodisDb.close();
        
        // 4. 重新打开数据库，从 WAL 加载
        Properties props = new Properties();
        props.setProperty("log.dir", TEST_LOG_DIR);
        props.setProperty("log.wal", TEST_WAL_FILE);
        props.setProperty("log.jdb", TEST_JDB_FILE);
        props.setProperty("log.reload.mode", "0");  // WAL 模式
        
        JodisConfig config = new JodisConfig(props);
        JodisDb newDb = new JodisDb(config);
        
        // 5. 验证数据恢复成功
        Assert.assertTrue(newDb.containsKey("name"));
        JodisObject obj = newDb.get("name");
        Assert.assertEquals("Jodis", ((JodisString)obj.getValue()).getHolder());
        
        newDb.close();
    }
    
    @Test
    public void testJdbSnapshot() throws Exception {
        // 1. 执行多个写操作
        String[] cmds = {
            "*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$5\r\nJodis\r\n",
            "*3\r\n$3\r\nSET\r\n$3\r\nage\r\n$2\r\n18\r\n",
            "*4\r\n$4\r\nHSET\r\n$4\r\nuser\r\n$4\r\nname\r\n$5\r\nAlice\r\n"
        };
        
        for (String cmd : cmds) {
            jodisDb.execute(cmd);
        }
        
        // 2. 手动保存快照
        jodisDb.saveSnapshot();
        
        // 3. 验证快照文件存在
        Assert.assertTrue(Files.exists(Paths.get(TEST_LOG_DIR, TEST_JDB_FILE)));
        
        // 4. 关闭并重新打开（不保存快照）
        jodisDb.close();
        
        Properties props = new Properties();
        props.setProperty("log.dir", TEST_LOG_DIR);
        props.setProperty("log.wal", TEST_WAL_FILE);
        props.setProperty("log.jdb", TEST_JDB_FILE);
        props.setProperty("log.reload.mode", "0");  // 使用 WAL 模式，忽略 JDB
        
        JodisConfig config = new JodisConfig(props);
        JodisDb newDb = new JodisDb(config);
        
        // 5. 验证所有数据都恢复了（从 WAL 恢复）
        Assert.assertTrue(newDb.containsKey("name"));
        Assert.assertTrue(newDb.containsKey("age"));
        Assert.assertTrue(newDb.containsKey("user"));
        
        newDb.close();
    }
    
    @Test
    public void testTtlPersistence() throws Exception {
        // 1. 设置带 TTL 的 Key
        String setexCmd = "*4\r\n$5\r\nSETEX\r\n$4\r\ntemp\r\n$2\r\n60\r\n$5\r\nvalue\r\n";
        jodisDb.execute(setexCmd);
        
        // 2. 验证 TTL 存在
        JodisObject obj = jodisDb.get("temp");
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj.getExpireTime() > 0);
        
        // 3. 保存快照
        jodisDb.saveSnapshot();
        
        // 4. 关闭并重新打开
        jodisDb.close();
        
        Properties props = new Properties();
        props.setProperty("log.dir", TEST_LOG_DIR);
        props.setProperty("log.wal", TEST_WAL_FILE);
        props.setProperty("log.jdb", TEST_JDB_FILE);
        props.setProperty("log.reload.mode", "0");  // WAL 模式
        
        JodisConfig config = new JodisConfig(props);
        JodisDb newDb = new JodisDb(config);
        
        // 5. 验证 TTL 信息被保留
        Assert.assertTrue(newDb.containsKey("temp"));
        JodisObject restoredObj = newDb.get("temp");
        Assert.assertTrue(restoredObj.getExpireTime() > 0);
        
        newDb.close();
    }
    
    @Test
    public void testWalRewrite() throws Exception {
        // 1. 执行大量写操作
        for (int i = 0; i < 100; i++) {
            String cmd = String.format("*3\r\n$3\r\nSET\r\n$%d\r\nkey%d\r\n$%d\r\nvalue%d\r\n",
                    String.valueOf(i).length(), i, String.valueOf(i).length(), i);
            jodisDb.execute(cmd);
        }
        
        // 2. 触发 WAL 重写
        jodisDb.rewriteWal();
        
        // 3. 验证重写后的文件存在
        Assert.assertTrue(Files.exists(Paths.get(TEST_LOG_DIR, TEST_WAL_FILE)));
        
        // 4. 关闭并重新打开
        jodisDb.close();
        
        Properties props = new Properties();
        props.setProperty("log.dir", TEST_LOG_DIR);
        props.setProperty("log.wal", TEST_WAL_FILE);
        props.setProperty("log.jdb", TEST_JDB_FILE);
        props.setProperty("log.reload.mode", "0");
        
        JodisConfig config = new JodisConfig(props);
        JodisDb newDb = new JodisDb(config);
        
        // 5. 验证所有数据都恢复了
        for (int i = 0; i < 100; i++) {
            String key = "key" + i;
            Assert.assertTrue("Key " + key + " should exist", newDb.containsKey(key));
        }
        
        newDb.close();
    }
}
