import constant.PromptWord;

import java.io.*;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * 功能：处理虚拟软盘的格式化、读写文件操作
 */
public class ImgEdit {

    private TreeMap<String, DirItem> rootDirTree;//根目录树

    private File imgFile;//虚拟软盘文件

    private BPB_FAT12 fat12;//虚拟软盘文件数据

    private int noUserDataSec;//用户数据区逻辑扇区起始位置,即对应的逻辑扇区号
    private int rootDirSec;//根目录扇区数

    private int secSize;//扇区大小,byte
    private int fatSize;//FAT占用扇区数
    private int fatNum;//Fat数
    private int hiddSec;//隐藏扇区数
    private int secPerClus;//每簇扇区数
    private int rootEntCnt;//根目录文件数最大值
    private int rsvdSecCnt;//Boot记录占用多少扇区

    public ImgEdit(String imgFileFullPath) throws Exception {
        init(imgFileFullPath);
    }

    public ImgEdit(File imgFile) throws Exception {
        init(imgFile);
    }


    private void init(String imgFileFullPath) throws Exception {
        File file = new File(imgFileFullPath);
        init(file);
    }

    private void init(File file) throws Exception {
        if (!file.exists()) {
            throw new FileNotFoundException("[" + file.getAbsolutePath() + "]" + "文件不存在！");
        }
        this.imgFile = file;
        this.fat12 = readBPB(this.imgFile);//获取文件内FAT文件系统信息
        initData(this.fat12);//内部数据赋值
        this.rootDirTree = getDirInfo(this.fat12.getRootDir());
    }

    public void initData(BPB_FAT12 fat12) throws IOException {

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

    public BPB_FAT12 readBPB(String imgFilePath) throws IOException {
        File imgFile = new File(imgFilePath);
        return readBPB(imgFile);
    }

    /**
     * 读取引导扇区(首扇区)
     */
    public static BPB_FAT12 readBPB(File imgFile) throws IOException {

        if (!imgFile.exists()) {
            throw new FileNotFoundException("[" + imgFile.getAbsolutePath() + "]文件不存在！");
        }

        BPB_FAT12 fat12 = new BPB_FAT12();

        byte[] bootSector = fat12.getBootSector();//启动扇区
        byte[] fat1 = fat12.getFat1();//FAT1表
        byte[] fat2 = fat12.getFat2();//FAT2表，FAT1备份
        byte[] rootDir = fat12.getRootDir();//根目录占了14个扇区，每个项占32字节，FAT12中共224项

        FileInputStream fis = new FileInputStream(imgFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(bootSector, 0, bootSector.length);
        bis.read(fat1, 0, fat1.length);
        bis.read(fat2, 0, fat2.length);
        bis.read(rootDir, 0, rootDir.length);
        bis.close();

        fat12.parseBPB();
        return fat12;
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

    public TreeMap<String, DirItem> getSubDirInfo(byte[] dirData, DirItem dirItem) throws Exception {
        TreeMap<String, DirItem> subDirTree = getDirInfo(dirData);
        if (subDirTree.get(".") != null && subDirTree.get("..") != null) {
            subDirTree.remove(".");
            subDirTree.put(".", dirItem);
            DirItem dirItemParent = subDirTree.get("..");
            if (dirItemParent.getDirFstClus() == 0) {
                subDirTree.remove("..");
                dirItemParent.setSubDirTree(rootDirTree);
                subDirTree.put("..", dirItemParent);
            }
        }
        return subDirTree;
    }

    public TreeMap<String, DirItem> getDirInfo(byte[] dirData) throws Exception {
        TreeMap<String, DirItem> dirTree = new TreeMap<String, DirItem>();
        int x;
        for (int i = 0; i < dirData.length; ) {
            if (dirData[i] != (byte) 0x00 && dirData[i] != (byte) 0xe5) {//此处必须加byte强转，否则byte数组会被强制转int，可能转成负数
                if ((dirData[i] & 0x40) == (byte) 0x40 && dirData[i + 11] == (byte) 0x0f) {//说明该项为长目录项
                    x = dirData[i] & 0x1f;
                    byte[] longtItem = Arrays.copyOfRange(dirData, i, i + 32 * x);//长文件名目录项
                    byte[] shortItem = Arrays.copyOfRange(dirData, i + 32 * x, i + 32 * x + 32);//短文件名目录项
                    DirItem dirItem = new DirItem(shortItem, longtItem);
                    dirTree.put(dirItem.getItemName(), dirItem);
                    i += 32 * x + 32;
                } else {//说明该项为短目录项
                    byte[] shortItem = Arrays.copyOfRange(dirData, i, i + 32);//短文件名目录项
                    DirItem dirItem = new DirItem(shortItem);
                    dirTree.put(dirItem.getItemName(), dirItem);
                    i += 32;
                }
            } else {
                i += 32;
            }
        }
        return dirTree;
    }

    public int[] getFileClusGroup(DirItem dirItem) throws IOException {
        return getFileClusGroup(dirItem, this.fat12.getFat1());
    }

    public int[] getFileClusGroup(DirItem dirItem, byte[] fat1) throws IOException {
        System.out.println("开始获取簇组！");
        long fileSize = dirItem.getDirFileSize();
        if (fileSize <= 0 && !dirItem.isDir()) {
            throw new RuntimeException(PromptWord.ERROR_2002);
        }
        //计算文件占用簇的数量（扇区数）
        int clusGroupLength;//占用扇区数
        if (fileSize % secSize == 0) {
            clusGroupLength = (int) fileSize / secSize;
        } else {
            clusGroupLength = (int) fileSize / secSize + 1;
        }
        byte[] firstClus = dirItem.getSrcDIR_FstClus();
        int[] clusGroup = getAndTrackFileClusGroup(firstClus, fat1);
        return clusGroup;
    }

    public int[] getAndTrackFileClusGroup(byte[] firstClus, byte[] fat) {
        int clusNmuber = FileHandle.transFAT12ItemToInt(firstClus[0], firstClus[1]);
        if (clusNmuber >= 0xff8) {
            System.out.println("空文件！");
            return null;
        }
        int clusGroupSize = 0;
        int[] clusGroup = new int[8];
        clusGroupSize++;
        //获取文件簇号数组
        int clusIndex;
        while (true) {
            //数组扩容
            if (clusGroupSize > clusGroup.length) {
                int[] tempClusGroup = new int[clusGroup.length + 8];
                System.arraycopy(clusGroup, 0, tempClusGroup, 0, clusGroup.length);
                clusGroup = tempClusGroup;
            }
            clusGroup[clusGroupSize - 1] = clusNmuber;
            clusIndex = clusNmuber * 15 / 10;
            if ((clusNmuber & 0x01) == 0) {//表明该簇在fat中的位置是占用低位1个字节和高位低4位
                clusNmuber = FileHandle.transFAT12ItemToInt(fat[clusIndex], fat[clusIndex + 1]);
            } else {//表明该簇在fat中的位置是占用低位高4位和高位1个字节
                clusNmuber = FileHandle.transFAT12ItemToInt(fat[clusIndex], fat[clusIndex + 1], false);
            }
            if (clusNmuber == 0xff7) {
                throw new RuntimeException(PromptWord.ERROR_2003);
            }
            if (clusNmuber >= 0xff8) {
                break;
            }
            clusGroupSize++;
        }
        return Arrays.copyOfRange(clusGroup, 0, clusGroupSize);
    }

    /**
     * 随机读取文件
     *
     * @param clusGroup  文件占用簇数组
     * @param fileLength 文件实际长度
     * @return
     * @throws IOException
     */
    public byte[] readFile(int[] clusGroup, long fileLength) throws IOException {
        System.out.println("开始获取文件数据！");
        RandomAccessFile raf = new RandomAccessFile(imgFile, "rw");
        byte[] file = new byte[clusGroup.length * secSize];
        int x;
        for (int i = 0; i < clusGroup.length; i++) {

            if (clusGroup[i] > 1 && clusGroup[i] < 0xff0) {
                FileHandle.randomReadFile(raf, (long) (((clusGroup[i] - 2) + noUserDataSec) * secSize), file, i * secSize, secSize);
            }
        }
        raf.close();
        if (file.length == fileLength || fileLength == 0) {
            return file;
        }
        return Arrays.copyOfRange(file, 0, (int) fileLength);
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
