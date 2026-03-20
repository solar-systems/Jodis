package cn.abelib.jodis.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author abel.huang
 * @version 1.0
 * @date 2022/6/26 17:46
 * todo 参考RocketMQ的MappedFile
 */
public class MappedFile {
    // 内存页大小，默认4K
    public static final int OS_PAGE_SIZE = 1024 * 4;

    // 映射文件的内存大小，CommitLog default is 1G，ConsumeQueue default is 30W
    private static final AtomicLong TOTAL_MAPPED_VIRTUAL_MEMORY = new AtomicLong(0);

    // mappedFile文件计数，初始化后+1，摧毁后-1
    private static final AtomicInteger TOTAL_MAPPED_FILES = new AtomicInteger(0);

    // 文件写入位置
    protected final AtomicInteger wrotePosition = new AtomicInteger(0);

    // 文件提交位置
    protected final AtomicInteger committedPosition = new AtomicInteger(0);

    // 文件刷新位置
    private final AtomicInteger flushedPosition = new AtomicInteger(0);

    // 文件大小
    protected int fileSize;

    // 该MappedFile对应的channel
    protected FileChannel fileChannel;

    /**
     * Message will put to here first, and then reput to FileChannel if writeBuffer is not null.
     */
    // 如果启用了TransientStorePool，则writeBuffer为TransientStorePool获取，
    // 此时消息会先写入该writeBuffer，commit到fileChannel，对fileChannel进行flush刷盘
    protected ByteBuffer writeBuffer = null;

    // 文件名，就是该文件内容默认起始位置
    private String fileName;

    // 该文件中内容相于整个文件的偏移，和文件名相同
    private long fileFromOffset;

    // MappedFile对应的实际文件
    private File file;

    // fileChannel.map得到的可读写的内存映射buffer盘时直接调用该
    // 映射buffer的force函数，而不需要进行commit操作
    private MappedByteBuffer mappedByteBuffer;

    // 存储时间戳
    private volatile long storeTimestamp = 0;

    // mappedFile是是否是ConsumerQueue的创建的第一个文件
    private boolean firstCreateInQueue = false;

    public MappedFile() {

    }

    public MappedFile(final String fileName, final int fileSize) throws IOException {
        init(fileName, fileSize);
    }

    /**
     * 初始化MappedFile
     * @param fileName
     * @param fileSize
     * @throws IOException
     */
    private void init(final String fileName, final int fileSize) throws IOException {
        this.fileName = fileName;
        this.fileSize = fileSize;
        // 创建文件对象
        this.file = new File(fileName);
        // 从文件名解析位移
        this.fileFromOffset = Long.parseLong(this.file.getName());

//        ensureDirOK(this.file.getParent());
//
//        try {
//            // 创建fileChannel
//            this.fileChannel = new RandomAccessFile(this.file, "rw").getChannel();
//            // 从fileChannel获取内存映射缓存
//            this.mappedByteBuffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
//            TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(fileSize);
//            TOTAL_MAPPED_FILES.incrementAndGet();
//        } catch (FileNotFoundException e) {
//            // ....
//        } finally {
//            if (!ok && this.fileChannel != null) {
//                this.fileChannel.close();
//            }
//        }
    }
}