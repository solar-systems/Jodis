# RESP 协议实现详解

RESP（REdis Serialization Protocol）是 Redis 的序列化协议，Jodis 完整实现了 RESP2 协议。

## 协议格式

### 1. 请求格式（客户端 → 服务器）

**示例**：`SET name Jodis`

```
*3\r\n           // 数组长度：3 个元素
$3\r\n           // 第一个元素长度：3
SET\r\n          // 命令名
$4\r\n           // 第二个元素长度：4
name\r\n         // Key
$5\r\n           // 第三个元素长度：5
Jodis\r\n        // Value
```

**结构说明**：
- `*`：数组标记
- `$`：批量字符串标记
- `\r\n`：CRLF 换行符

### 2. 响应格式（服务器 → 客户端）

RESP 定义了 5 种响应类型：

#### Simple String（简单字符串）

**格式**：`+<data>\r\n`

**示例**：
```
+OK\r\n
```

**Java 实现**：
```java
public class SimpleResponse implements Response {
    private String data;
    
    @Override
    public String encode() {
        return "+" + data + "\r\n";
    }
    
    public static SimpleResponse ok() {
        return new SimpleResponse("OK");
    }
}
```

#### Error（错误）

**格式**：`-<error-type> <message>\r\n`

**示例**：
```
-ERR syntax error\r\n
-WRONGTYPE Operation against a key holding the wrong kind of value\r\n
```

**Java 实现**：
```java
public class ErrorResponse implements Response {
    private String errorType;
    private String message;
    
    @Override
    public String encode() {
        return "-" + errorType + " " + message + "\r\n";
    }
    
    public static ErrorResponse errorSyntax() {
        return new ErrorResponse("ERR", "syntax error");
    }
}
```

#### Integer（整数）

**格式**：`:<number>\r\n`

**示例**：
```
:1000\r\n
:0\r\n
:-1\r\n
```

**Java 实现**：
```java
public class NumericResponse implements Response {
    private Long value;
    
    @Override
    public String encode() {
        return ":" + value + "\r\n";
    }
    
    public static NumericResponse numericResponse(Long value) {
        return new NumericResponse(value);
    }
}
```

#### Bulk String（批量字符串）

**格式**：`$<length>\r\n<data>\r\n`

**示例**：
```
$5\r\nhello\r\n
$0\r\n\r\n              // 空字符串
$-1\r\n               // null 值
```

**Java 实现**：
```java
public class BulkStringResponse implements Response {
    private String value;
    
    @Override
    public String encode() {
        if (value == null) {
            return "$-1\r\n";
        }
        return "$" + value.length() + "\r\n" + value + "\r\n";
    }
}
```

#### Array（数组）

**格式**：`*<count>\r\n<element1><element2>...`

**示例**：
```
*3\r\n
$3\r\nSET\r\n
$4\r\nname\r\n
$5\r\nJodis\r\n
```

**Java 实现**：
```java
public class ListResponse implements Response {
    private List<String> items;
    
    @Override
    public String encode() {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(items.size()).append("\r\n");
        
        for (String item : items) {
            if (item == null) {
                sb.append("$-1\r\n");
            } else {
                sb.append("$").append(item.length())
                  .append("\r\n").append(item).append("\r\n");
            }
        }
        
        return sb.toString();
    }
}
```

## 协议解析器（RespParser）

### 解析流程

```java
public class RespParser {
    
    public Request parse(String request) {
        Request req;
        String[] arguments;
        boolean parsed = false;
        
        // 判断是否为内联命令（非 RESP 格式）
        if (!request.startsWith(ProtocolConstant.LIST_PREFIX)) {
            // 按空格分割：SET name Jodis
            arguments = request.split(StringUtils.SPACE);
            parsed = true;
        } else {
            // RESP 格式解析
            request = request.substring(1);  // 去掉 '*'
            String[] cmds = request.split(StringUtils.CLRF);
            
            // 解析数组长度
            String sizeStr = cmds[0].trim();
            Integer cmdSize = NumberUtils.parseInt(sizeStr);
            
            if (Objects.isNull(cmdSize)) {
                return Request.badRequest(ErrorResponse.errorSyntax());
            }
            
            // 验证长度：cmdSize 个元素应该有 cmdSize * 2 行
            if (cmds.length < cmdSize * 2) {
                return Request.badRequest(ErrorResponse.errorSyntax());
            }
            
            // 提取参数（跳过长度声明行）
            arguments = new String[cmdSize];
            int idx = 0;
            for (int i = 1; i < cmds.length; i += 2) {
                arguments[idx] = cmds[i];
                idx++;
            }
        }
        
        // 构建 Request 对象
        int len = arguments.length;
        if (len < 1) {
            return Request.badRequest(ErrorResponse.errorSyntax());
        }
        
        String command = arguments[0].toUpperCase();
        List<String> args = Lists.newArrayList(arguments);
        args.remove(0);  // 移除命令名
        
        req = new Request(command, args);
        
        // 重新构建完整的 RESP 字符串（用于 WAL 存储）
        if (!parsed) {
            request = buildRespString(arguments);
        }
        req.setRequest(request);
        
        return req;
    }
    
    private String buildRespString(String[] arguments) {
        StringBuilder request = new StringBuilder("*");
        request.append(arguments.length).append("\r\n");
        
        for (String arg : arguments) {
            request.append("$").append(arg.length()).append("\r\n");
            request.append(arg).append("\r\n");
        }
        
        return request.toString();
    }
}
```

### 关键点

1. **兼容内联命令**：支持 `SET name Jodis` 这种简单格式
2. **严格验证**：检查数组长度与实际行数是否匹配
3. **性能优化**：避免不必要的字符串复制
4. **错误处理**：解析失败时返回标准错误响应

## Request 和 Response 类

### Request 类

```java
public class Request {
    private String request;        // 原始请求字符串（RESP 格式）
    private boolean needLog;       // 是否需要记录 WAL
    private String command;        // 命令名（大写）
    private List<String> args;     // 参数列表
    
    public Request(String command, List<String> args) {
        this.command = command.toUpperCase();
        this.args = args;
        
        // 自动判断是否需要日志
        if (ProtocolConstant.NEED_LOGS.contains(this.command)) {
            this.needLog = true;
        }
        
        // 构建完整的 RESP 字符串
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(args.size() + 1).append("\r\n");
        sb.append("$").append(this.command.length()).append("\r\n");
        sb.append(this.command).append("\r\n");
        for (String arg : args) {
            sb.append("$").append(arg.length()).append("\r\n");
            sb.append(arg).append("\r\n");
        }
        this.request = sb.toString();
    }
}
```

### Response 继承体系

```
Response (接口)
├── SimpleResponse      // 简单字符串 (+OK)
├── ErrorResponse       // 错误 (-ERR ...)
├── NumericResponse     // 整数 (:100)
├── BulkStringResponse  // 批量字符串 ($5\r\nhello)
├── ListResponse        // 数组 (*3\r\n...)
└── MultiResponse       // 多行响应
```

## 协议特点

### 1. 人类可读

```bash
# 可以用 telnet 直接发送命令
$ telnet localhost 6059
Trying 127.0.0.1...
Connected to localhost.
*3
$3
SET
$4
name
$5
Jodis
+OK
```

### 2. 二进制安全

```java
// 支持二进制数据
byte[] binaryData = new byte[]{0x00, 0x01, 0xFF};
String base64 = Base64.getEncoder().encodeToString(binaryData);
client.set("binary", base64);
```

### 3. 高效解析

- 简单的文本协议，解析速度快
- 长度前缀设计，避免缓冲区溢出
- 固定格式，无需复杂的状态机

## 相关文件

- [系统架构](SystemArchitecture.md)
- [网络通信实现](NettyNetwork.md)
- [命令执行框架](CommandExecutor.md)
