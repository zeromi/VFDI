import constant.PromptWord;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;

/**
 * 功能：处理文件写入和读取
 */
public class FileHandle {

    /**
     * 生成一个指定大小的文件
     *
     * @param size       写入字节数,单位byte
     * @param sourceFile 目标文件
     * @throws IOException
     */
    public static void createNullImg(int size, File sourceFile) throws IOException {
        if (sourceFile.exists()) {
            throw new RuntimeException(PromptWord.READ_FILE_ERROR);
        }
        byte[] b = new byte[8 * 1024];
        FileOutputStream fos = new FileOutputStream(sourceFile);
        for (int i = size; i > 0; i = i - b.length) {
            if (i < b.length) {
                fos.write(b, 0, i);
            } else {
                fos.write(b, 0, b.length);//1.44M---字节规格为1,474,560=80(磁道)x18(扇区)x512bytes(扇区的大小)x2(双面)
            }
        }
        fos.close();
    }

    /**
     * 生成一个指定大小的文件
     *
     * @param size               写入字节数,单位byte
     * @param sourceFileFullPath 目标文件完整名
     * @return 返回生成的文件
     * @throws IOException
     */
    public static File createNullImg(int size, String sourceFileFullPath) throws IOException {
        File img = new File(sourceFileFullPath);
        createNullImg(size, img);
        return img;
    }

    /**
     * 无符号整型字节转整型long(无符号),字节数组长度不得超过3
     *
     * @param b 字节数组
     * @return 转换结果
     */
    public static long byteTransLong(byte[] b) {
        long m, n;
        if (b.length < 1 || b.length > 7) {
            throw new RuntimeException(PromptWord.TRANS_DATA_ERROR);
        }

        m = b[0];
        m = m & 0xff;
        if (b.length == 1) {
            return m;
        } else {
            for (int i = 1; i < b.length; i++) {
                n = b[i];
                n = n & 0xff;
                n = n << (8 * i);
                m = m | n;
            }
            return m;
        }
    }

    /**
     * long转字节数组,数字大小不得超过2^(8*8-1)-1
     *
     * @param number     待转换
     * @param byteNumber 设置结果字节数组的长度
     * @return 转换结果
     */
    public static byte[] longTransByte(long number, int byteNumber) {
        byte[] b = new byte[byteNumber];
        for (int i = 0; i < byteNumber; i++) {
            b[i] = (byte) (number & 0xff);
            number = number >>> 8;
        }
        return b;
    }


    /**
     * 格式化软盘映像(1.44M),格式化为FAT12
     *
     * @param imgFilePath 虚拟软盘文件路径
     * @return 虚拟软盘文件
     * @throws IOException
     */
    public static File formatImgFile(String imgFilePath) throws IOException {
        File imgFile = new File(imgFilePath);
        if (!imgFile.exists()) {
            throw new RuntimeException(PromptWord.FILE_NO_EXISTS_ERROR);
        }
        formatImgFile(imgFile);
        return imgFile;
    }

    /**
     * 格式化软盘映像(1.44M),格式化为FAT12
     *
     * @param imgFile 软盘映像文件
     * @return 格式化成功返回true, 否则返回false
     */
    public static void formatImgFile(File imgFile) throws IOException {

        byte[] firstSector = BPB_FAT12.getInitBPB();

        byte[] fatTable = new byte[9 * 512];//占9个扇区
        //每12位称为一个FAT项（FATEntry），代表一个簇
        //通常，FAT项的值代表的是文件下一个簇号，但如果值大于或等于0xFF8，则表示当前簇已经是本文件的最后一个簇。如果值为
        //0xFF7，表示它是一个坏簇。
        //文件RIVER.TXT的开始簇号是2，对应FAT表中的值为0xFFF，表示这个簇已经是最后一个。
        fatTable[0] = (byte) 0xf0;
        fatTable[1] = (byte) 0xff;
        fatTable[2] = (byte) 0xff;

        byte[] fatForamtData = new byte[512 * (1 + 9 * 2)];//前面引导扇区+FAT表+FAT备份表

        System.arraycopy(firstSector, 0, fatForamtData, 0, firstSector.length);
        System.arraycopy(fatTable, 0, fatForamtData, firstSector.length, fatTable.length);
        System.arraycopy(fatTable, 0, fatForamtData, firstSector.length + fatTable.length, fatTable.length);

        //写入格式化数据到虚拟软盘文件
        RandomAccessFile raf = new RandomAccessFile(imgFile, "rw");
        randomWriteFile(raf, 0L, fatForamtData, fatForamtData.length);
        raf.close();
    }

    /**
     * 随机写入一个字节数组的数据,从数组头开始写入
     *
     * @param raf       随机读写流
     * @param pos       指针初始位置
     * @param writeData 待写入数据
     * @param length    写入长度
     * @throws IOException
     */
    public static void randomWriteFile(RandomAccessFile raf, Long pos, byte[] writeData, int length) throws IOException {
        randomWriteFile(raf, pos, writeData, 0, length);
    }

    /**
     * 随机写入一个字节数组的数据
     *
     * @param raf       随机读写流
     * @param pos       指针初始位置
     * @param writeData 待写入数据
     * @param off       从数组中获取数据的起始位置
     * @param length    写入文件中的数据长度
     * @throws IOException
     */
    public static void randomWriteFile(RandomAccessFile raf, Long pos, byte[] writeData, int off, int length) throws IOException {
        raf.seek(pos);
        raf.write(writeData, off, length);
    }

    /**
     * 随机读取一个字节数组的数据
     *
     * @param raf      随机读写流
     * @param pos      指针初始位置
     * @param readData 待写入数据
     * @param off      数组中的起始位置,数据从数组这里开始存放
     * @param length   写入数组中的数据长度
     * @throws IOException
     */
    public static void randomReadFile(RandomAccessFile raf, Long pos, byte[] readData, int off, int length) throws IOException {
        raf.seek(pos);
        raf.read(readData, off, length);
    }

    /**
     * 创建一个新文件，并写入指定数据
     *
     * @param fileData 待写入文件的数据
     * @param fileName 新文件名
     */
    public static void genarateNewFile(byte[] fileData, String fileName) throws IOException {
        File newFile = new File(fileName);
        genarateNewFile(fileData, newFile);
    }

    /**
     * 创建一个新文件，并写入指定数据
     *
     * @param fileData 待写入文件的数据
     * @param newFile  新文件
     */
    public static void genarateNewFile(byte[] fileData, File newFile) throws IOException {
        if (newFile.exists()) {
            throw new FileAlreadyExistsException("[" + newFile.getAbsolutePath() + "]已存在！");
        }
        newFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(newFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(fileData);
        bos.close();
    }

    /**
     * 低地址端为低位，高地址端为高位，高位字节的低4位有效
     *
     * @param a 低地址字节
     * @param b 高地址字节
     * @return
     */
    public static int transFAT12ItemToInt(byte a, byte b) {

        return transFAT12ItemToInt(a, b, true);
    }

    /**
     * 默认低地址端为低位，高地址端为高位，高位字节的低4位有效，
     * 若flag为false,则取低地址字节的高4位和高地址字节作为有效值
     *
     * @param a
     * @param b
     * @param flag 默认ture
     * @return
     */
    public static int transFAT12ItemToInt(byte a, byte b, boolean flag) {
        if (flag) {
            return (b & 0x0f) << 8 | (a & 0xff);
        }
        return (b & 0xff) << 4 | (a & 0xff) >>> 4;
    }

    /**
     * 转换为一个2个字节的字节数组，低地址端为低位，高地址端为高位，第二个字节的低4位有效
     *
     * @param clusNum 簇号
     * @return
     */
    public static byte[] transIntToFAT12Item(int clusNum) {
        byte[] b = new byte[2];
        b[1] = (byte) (clusNum >> 8);
        b[0] = (byte) (clusNum & 0xff);
        return b;
    }

}
