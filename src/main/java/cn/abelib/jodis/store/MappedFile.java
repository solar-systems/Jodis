package cn.abelib.jodis.store;

import cn.abelib.jodis.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存映射文件实现，参考 RocketMQ 的 MappedFile 设计
 * 适用于大文件的顺序读写场景，如快照文件（JDB）
 *
 * @author abel.huang
 */
public class MappedFile {
    private static final Logger logger = Logger.getLogger(MappedFile.class);

    /**
     * 操作系统内存页大小，默认 4KB
     */
    public static final int OS_PAGE_SIZE = 1024 * 4;

    /**
     * 全局映射内存总量
     */
    private static final AtomicLong TOTAL_MAPPED_VIRTUAL_MEMORY = new AtomicLong(0);

    /**
     * 全局映射文件数量
     */
    private static final AtomicInteger TOTAL_MAPPED_FILES = new AtomicInteger(0);

    /**
     * 当前写入位置
     */
    protected final AtomicInteger wrotePosition = new AtomicInteger(0);

    /**
     * 当前提交位置
     */
    protected final AtomicInteger committedPosition = new AtomicInteger(0);

    /**
     * 当前刷盘位置
     */
    protected final AtomicInteger flushedPosition = new AtomicInteger(0);

    /**
     * 文件大小
     */
    protected int fileSize;

    /**
     * 文件通道
     */
    protected FileChannel fileChannel;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件起始偏移量（从文件名解析）
     */
    private long fileFromOffset;

    /**
     * 文件对象
     */
    private File file;

    /**
     * 内存映射缓冲区
     */
    private MappedByteBuffer mappedByteBuffer;

    /**
     * 写入缓冲区（可选，用于 transientStorePool）
     */
    protected ByteBuffer writeBuffer = null;

    /**
     * 最后存储时间戳
     */
    private volatile long storeTimestamp = 0;

    /**
     * 是否首次创建
     */
    private boolean firstCreateInQueue = false;

    /**
     * 是否已关闭
     */
    private volatile boolean closed = false;

    public MappedFile() {
    }

    public MappedFile(String fileName, int fileSize) throws IOException {
        init(fileName, fileSize);
    }

    public MappedFile(String fileName) throws IOException {
        init(fileName, StoreConfig.DEFAULT_JDB_FILE_SIZE);
    }

    /**
     * 初始化内存映射文件
     */
    private void init(String fileName, int fileSize) throws IOException {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.file = new File(fileName);

        // 确保父目录存在
        File parentDir = this.file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 解析文件名中的偏移量（如果文件名是数字）
        try {
            this.fileFromOffset = Long.parseLong(this.file.getName());
        } catch (NumberFormatException e) {
            this.fileFromOffset = 0L;
        }

        boolean fileExists = this.file.exists();

        try {
            this.fileChannel = new RandomAccessFile(this.file, "rw").getChannel();
            this.mappedByteBuffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);

            TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(fileSize);
            TOTAL_MAPPED_FILES.incrementAndGet();

            // 如果是新文件，初始化写入位置为 0
            // 如果是已存在的文件，需要从文件中恢复写入位置
            if (fileExists && this.file.length() > 0) {
                // 对于已存在的文件，写入位置设为文件实际大小
                this.wrotePosition.set((int) this.file.length());
                this.committedPosition.set((int) this.file.length());
                this.flushedPosition.set((int) this.file.length());
            }

            logger.info("MappedFile initialized: {}, fileSize: {}, wrotePosition: {}",
                    fileName, fileSize, this.wrotePosition.get());
        } catch (IOException e) {
            logger.error("Failed to initialize MappedFile: {}", fileName, e);
            if (this.fileChannel != null) {
                this.fileChannel.close();
            }
            throw e;
        }
    }

    /**
     * 追加数据到文件
     *
     * @param data 数据字节数组
     * @return 写入成功返回 true
     */
    public boolean appendMessage(byte[] data) {
        return appendMessage(data, 0, data.length);
    }

    /**
     * 追加数据到文件
     *
     * @param data   数据字节数组
     * @param offset 偏移量
     * @param length 长度
     * @return 写入成功返回 true
     */
    public boolean appendMessage(byte[] data, int offset, int length) {
        if (closed) {
            logger.warn("MappedFile is closed, cannot append message");
            return false;
        }

        int currentPos = wrotePosition.get();
        if (currentPos + length > fileSize) {
            logger.warn("MappedFile is full, currentPos: {}, length: {}, fileSize: {}",
                    currentPos, length, fileSize);
            return false;
        }

        try {
            mappedByteBuffer.position(currentPos);
            mappedByteBuffer.put(data, offset, length);
            wrotePosition.addAndGet(length);
            storeTimestamp = System.currentTimeMillis();
            return true;
        } catch (Exception e) {
            logger.error("Failed to append message", e);
            return false;
        }
    }

    /**
     * 获取指定位置的数据
     *
     * @param pos    位置
     * @param length 长度
     * @return 数据字节数组
     */
    public byte[] getMessage(int pos, int length) {
        if (pos < 0 || pos + length > wrotePosition.get()) {
            return null;
        }

        byte[] result = new byte[length];
        mappedByteBuffer.position(pos);
        mappedByteBuffer.get(result);
        return result;
    }

    /**
     * 提交数据到 FileChannel
     *
     * @param commitLeastPages 最少提交页数
     * @return 提交的位置
     */
    public int commit(int commitLeastPages) {
        if (writeBuffer == null) {
            // 没有使用 transientStorePool，直接返回写入位置
            return wrotePosition.get();
        }

        int commitDataLength = wrotePosition.get() - committedPosition.get();
        if (commitDataLength > 0) {
            int commitPages = commitDataLength / OS_PAGE_SIZE;
            if (commitLeastPages <= 0 || commitPages >= commitLeastPages) {
                try {
                    writeBuffer.position(committedPosition.get());
                    writeBuffer.limit(wrotePosition.get());
                    fileChannel.write(writeBuffer, committedPosition.get());
                    committedPosition.set(wrotePosition.get());
                } catch (IOException e) {
                    logger.error("Failed to commit data", e);
                }
            }
        }

        return committedPosition.get();
    }

    /**
     * 刷盘
     *
     * @param flushLeastPages 最少刷盘页数
     * @return 刷盘位置
     */
    public int flush(int flushLeastPages) {
        int flushDataLength = wrotePosition.get() - flushedPosition.get();
        if (flushDataLength > 0) {
            int flushPages = flushDataLength / OS_PAGE_SIZE;
            if (flushLeastPages <= 0 || flushPages >= flushLeastPages) {
                try {
                    mappedByteBuffer.force();
                    flushedPosition.set(wrotePosition.get());
                    logger.debug("Flushed MappedFile: {}, position: {}", fileName, flushedPosition.get());
                } catch (Exception e) {
                    logger.error("Failed to flush MappedFile: {}", fileName, e);
                }
            }
        }

        return flushedPosition.get();
    }

    /**
     * 强制刷盘（忽略页数限制）
     */
    public void force() {
        if (mappedByteBuffer != null) {
            try {
                mappedByteBuffer.force();
                flushedPosition.set(wrotePosition.get());
            } catch (Exception e) {
                logger.error("Failed to force flush MappedFile: {}", fileName, e);
            }
        }
    }

    /**
     * 获取写入位置
     */
    public AtomicInteger getWrotePosition() {
        return wrotePosition;
    }

    /**
     * 获取文件大小
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * 获取文件通道
     */
    public FileChannel getFileChannel() {
        return fileChannel;
    }

    /**
     * 获取内存映射缓冲区
     */
    public MappedByteBuffer getMappedByteBuffer() {
        return mappedByteBuffer;
    }

    /**
     * 获取文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 获取文件偏移量
     */
    public long getFileFromOffset() {
        return fileFromOffset;
    }

    /**
     * 获取存储时间戳
     */
    public long getStoreTimestamp() {
        return storeTimestamp;
    }

    /**
     * 是否已写满
     */
    public boolean isFull() {
        return wrotePosition.get() >= fileSize;
    }

    /**
     * 获取文件实际写入大小
     */
    public int getReadPosition() {
        return wrotePosition.get();
    }

    /**
     * 关闭文件
     */
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        try {
            // 先刷盘
            if (mappedByteBuffer != null) {
                mappedByteBuffer.force();
            }

            // 关闭文件通道
            if (fileChannel != null && fileChannel.isOpen()) {
                fileChannel.close();
            }

            // 清理映射缓冲区
            if (mappedByteBuffer != null) {
                // 在 JDK 9+ 可以使用 Cleaner 来释放，这里先置空
                mappedByteBuffer = null;
            }

            TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(-fileSize);
            TOTAL_MAPPED_FILES.decrementAndGet();

            logger.info("MappedFile closed: {}", fileName);
        } catch (IOException e) {
            logger.error("Failed to close MappedFile: {}", fileName, e);
        }
    }

    /**
     * 销毁文件
     */
    public void destroy() {
        close();
        if (file != null && file.exists()) {
            file.delete();
            logger.info("MappedFile destroyed: {}", fileName);
        }
    }

    /**
     * 截断文件到实际写入大小
     */
    public void truncate() throws IOException {
        int actualSize = wrotePosition.get();
        if (actualSize < fileSize && fileChannel != null) {
            fileChannel.truncate(actualSize);
            logger.info("Truncated MappedFile: {} to size: {}", fileName, actualSize);
        }
    }

    /**
     * 获取全局映射内存总量
     */
    public static long getTotalMappedVirtualMemory() {
        return TOTAL_MAPPED_VIRTUAL_MEMORY.get();
    }

    /**
     * 获取全局映射文件数量
     */
    public static int getTotalMappedFiles() {
        return TOTAL_MAPPED_FILES.get();
    }
}
