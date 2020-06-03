import constant.PromptWord;

import java.io.*;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * 功能：处理虚拟软盘的格式化、读写文件操作
 */
public class ImgEdit {

    private TreeMap<String, DirItem> rootDirTree;//根目录树

    private int noUserDataSec;//用户数据区逻辑扇区起始位置
    private int rootDirSec;//根目录扇区数

    private int secSize;//扇区大小,byte
    private int fatSize;//FAT占用扇区数
    private int fatNum;//Fat数
    private int hiddSec;//隐藏扇区数
    private int secPerClus;//每簇扇区数
    private int rootEntCnt;//根目录文件数最大值
    private int rsvdSecCnt;//Boot记录占用多少扇区

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
     * @param off       写入在数组中的起始位置
     * @param length    写入长度
     * @throws IOException
     */
    public static void randomWriteFile(RandomAccessFile raf, Long pos, byte[] writeData, int off, int length) throws IOException {
        raf.seek(pos);
        raf.write(writeData, off, length);
    }

    public static BPB_FAT12 readBPB(String imgFilePath) throws IOException {
        File imgFile = new File(imgFilePath);
        if (!imgFile.exists()) {
            throw new FileNotFoundException(imgFilePath + "，文件不存在！");
        }
        return readBPB(imgFile);
    }

    /**
     * 读取引导扇区(首扇区)
     */
    public static BPB_FAT12 readBPB(File imgFile) throws IOException {

        BPB_FAT12 fat12 = new BPB_FAT12();

        byte[] bootSector = fat12.getBootSector();//启动扇区
        byte[] fat1 = fat12.getFat1();//FAT1表
        byte[] fat2 = fat12.getFat2();//FAT2表，FAT1备份
        byte[] rootDir = fat12.getRootDir();//根目录占了14个扇区，每个项占32字节，FAT12中共224项

        FileInputStream fis = new FileInputStream(imgFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(bootSector, 0, bootSector.length);
        System.out.println("bootSector:" + Arrays.toString(bootSector));
        bis.read(fat1, 0, fat1.length);
        System.out.println("fat1:" + Arrays.toString(fat1));
        bis.read(fat2, 0, fat2.length);
        System.out.println("fat2:" + Arrays.toString(fat2));
        bis.read(rootDir, 0, rootDir.length);
        System.out.println("rootDir:" + Arrays.toString(rootDir));
        bis.close();

        fat12.parseBPB();
        return fat12;
    }

    public void initData(BPB_FAT12 fat12) {

        secSize = (int) FileHandle.byteTransLong(fat12.getBPBData("BPB_BytsPerSec"));//扇区大小,byte
        fatSize = (int) FileHandle.byteTransLong(fat12.getBPBData("BPB_FATSz16"));//FAT占用扇区数
        fatNum = (int) FileHandle.byteTransLong(fat12.getBPBData("BPB_NumFATs"));//Fat数
        hiddSec = (int) FileHandle.byteTransLong(fat12.getBPBData("BPB_HiddSec"));//隐藏扇区数
        rootEntCnt = (int) FileHandle.byteTransLong(fat12.getBPBData("BPB_RootEntCnt"));//根目录文件数最大值
        rsvdSecCnt = (int) FileHandle.byteTransLong(fat12.getBPBData("BPB_RsvdSecCnt"));//Boot记录占用多少扇区
        secPerClus = (int) FileHandle.byteTransLong(fat12.getBPBData("BPB_SecPerClus"));//每簇扇区数

        rootDirSec = rootEntCnt * 32 / secSize;//根目录扇区数
        noUserDataSec = rsvdSecCnt + hiddSec + fatSize * fatNum + rootDirSec;//用户数据区逻辑扇区起始位置

    }

    /**
     * LBA从0开始计数,获取逻辑扇区号
     *
     * @param clusNmu 簇号，需大于2
     * @return
     */
    public int getLogicSec(int clusNmu) {
        return (clusNmu - 2) * secPerClus + (noUserDataSec - 1);
    }

    public void getRootDirInfo(byte[] rootDir) throws Exception {
        System.out.println("rootDir.length=" + rootDir.length);

        rootDirTree = new TreeMap<String, DirItem>();
        int x;
        for (int i = 0; i < rootDir.length; ) {
            System.out.println("rootDir的i=" + i);
            int y=((int)rootDir[i])&0xff;
            System.out.println("rootDir[i]=" + Integer.toHexString(y));
            if (rootDir[i] != (byte) 0x00 && rootDir[i] != (byte) 0xe5) {//此处必须加byte强转，否则byte数组会被强制转int，可能转成负数
                if ((rootDir[i] & 0x40) == (byte) 0x40 && rootDir[i + 11] == (byte) 0x0f) {//说明该项为长目录项
                    System.out.println("长目录项");
                    x = rootDir[i] & 0x1f;
                    byte[] longtItem = Arrays.copyOfRange(rootDir, i, i + 32 * x);//长文件名目录项
                    byte[] shortItem = Arrays.copyOfRange(rootDir, i + 32 * x, i + 32 * x + 32);//短文件名目录项
                    DirItem dirItem = new DirItem(shortItem, longtItem);
                    rootDirTree.put(dirItem.getItemName(), dirItem);
                    i += 32 * x + 32;
                } else {
                    System.out.println("短目录项");
                    byte[] shortItem = Arrays.copyOfRange(rootDir, i, i + 32);//短文件名目录项
                    DirItem dirItem = new DirItem(shortItem);
                    rootDirTree.put(dirItem.getItemName(), dirItem);
                    i += 32;
                }
            } else {
                i += 32;
                System.out.println("无效目录项");
            }

        }
    }


    @Override
    public String toString() {
        return "ImgEdit{" +
                "dataSec=" + noUserDataSec +
                ", rootDirSec=" + rootDirSec +
                ", secSize=" + secSize +
                ", fatSize=" + fatSize +
                ", fatNum=" + fatNum +
                ", hiddSec=" + hiddSec +
                ", secPerClus=" + secPerClus +
                ", rootEntCnt=" + rootEntCnt +
                ", rsvdSecCnt=" + rsvdSecCnt +
                '}';
    }

    public TreeMap<String, DirItem> getRootDirTree() {
        return rootDirTree;
    }
}
