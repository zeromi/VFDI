import exception.FileException;

import java.io.*;
import java.util.Scanner;

public class ImgGenerator {

    private String sourceFileFullPath;
    private String sourceFilePath;
    private String sourceFileName;
    private String sourceFileNoExtName;//不带扩展名

    private String imgFileFullPath;
    private String imgFilePath;
    private String imgFileName;

    public ImgGenerator(String sourceFileFullPath) {

        //获取源文件路径

//        try {
//            FileInputStream fis = getSourceFile(sourceFileFullPath);
//            fis.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 解析文件名称和路径属性
     *
     * @param sourceFileFullPath
     */
    private void fileNameAndPathParse(String sourceFileFullPath) {
        this.sourceFileFullPath = sourceFileFullPath;

        int index = this.sourceFileFullPath.lastIndexOf(File.separator);
        this.sourceFilePath = (index != -1) ? this.sourceFileFullPath.substring(0, index) : "";
        this.sourceFileName = (index != -1) ? this.sourceFileFullPath.substring(index + 1) : this.sourceFileFullPath;

        index = this.sourceFileName.lastIndexOf(".");
        this.sourceFileNoExtName = (index != -1) ? this.sourceFileName.substring(0, index) : this.sourceFileName;

        System.out.println(this.sourceFileFullPath);
        System.out.println(this.sourceFilePath);
        System.out.println(this.sourceFileName);
        System.out.println(this.sourceFileNoExtName);
    }

    /**
     * 设置生成目标文件名称属性
     */
    private void imgFileNameAttribute() {
        imgFilePath = sourceFilePath;
        imgFileName = sourceFileNoExtName + ".img";
        imgFileFullPath = imgFilePath + File.separator + imgFileName;

        System.out.println("imgFileFullPath:" + this.imgFileFullPath);
        System.out.println("imgFilePath:" + this.imgFilePath);
        System.out.println("imgFileName:" + this.imgFileName);
    }

    /**
     * @param sourceFileFullPath 源文件完整路径
     * @return
     */
    private File getSourceFile(String sourceFileFullPath) throws FileException {
        System.out.println(sourceFileFullPath);
        File source = new File(sourceFileFullPath);
        if (source.exists()) {
            return source;
        } else {
            throw new FileException("源文件不存在！");
        }
    }

    /**
     * 创建空img文件
     *
     * @param sourceFileFullPath 源文件完整路径
     * @return
     * @throws FileException
     * @throws IOException
     */
    private File createImgFile(String sourceFileFullPath) throws FileException, IOException {

        System.out.println(sourceFileFullPath);
        fileNameAndPathParse(sourceFileFullPath);
        imgFileNameAttribute();

        //生成现有文件
        File img = new File(this.imgFileFullPath);
        if (img.exists()) {
            throw new FileException("文件已存在！");
        } else {
            System.out.println("是否文件夹：" + img.isDirectory());
            System.out.println("是否文件：" + img.isFile());
            img.createNewFile();
        }
        return img;
    }

    /**
     * 创建引导扇区bin文件
     *
     * @param sourceFile 源文件
     * @param writeFile  bin文件,目前以img的形式表现
     * @throws IOException
     * @throws FileException
     */
    private void createBinFile(File sourceFile, File writeFile) throws IOException, FileException {
        if (sourceFile.length() > 512) {
            throw new FileException("文件大小超出512字节！");
        }
        FileInputStream fis = new FileInputStream(sourceFile);
        int imgFileLength = 0;
        byte[] b = new byte[512];
        int readLength = 0;
        if ((readLength = fis.read(b, 0, b.length)) == -1) {
            throw new FileException("空文件！");
        }
        fis.close();
    }

    /**
     * 随机写入数据到文件，适合低频高量写入
     *
     * @param writeData
     * @param writeFile
     * @throws IOException
     * @throws FileException
     */
    private void writeImgFileFirstSector(byte[] writeData, File writeFile) throws IOException {
        byte[] b = new byte[8 * 1024];
        RandomAccessFile raf = new RandomAccessFile(writeFile, "rw");
        raf.seek(0);
        int remainLength = writeData.length;
        while (remainLength > 8 * 1024) {
            raf.write(writeData, writeData.length - remainLength, 8 * 1024);
            remainLength -= 8 * 1024;
        }
        if (remainLength > 0) {
            raf.write(writeData, writeData.length - remainLength, remainLength);
        }
        raf.close();
    }

    /**
     * 创建空软盘映像
     *
     * @param size 映像大小，单位byte
     */
    private void createNullImg(int size, String sourceFileFullPath) throws IOException, FileException {
        File img = createImgFile(sourceFileFullPath);
        byte[] b = new byte[8 * 1024];
        FileOutputStream fos = new FileOutputStream(img);
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
     * 格式化软盘映像(1.44M),格式化为FAT12
     *
     * @param imgFile 软盘映像文件
     * @return 格式化成功返回true, 否则返回false
     */
    private boolean formatImgFile(File imgFile) throws IOException {


        byte[] firstSector = new byte[512];
        //名称	偏移	(local)    长度(length)	    内容	    软盘参考值

        byte[] BS_jmpBoot = {(byte) 0xeb, (byte) 0x3c, (byte) 0x90};//local=0,length=3,一个短跳转指令,jmp LABEL_START,8086中,0x3ceb表示jmp short 0x3c即跳转到往后0x3c个字节处,0x90表示nop,短跳转指令占两字节，这里不要随意更改
        // nop;接上一行注释的指令
        System.arraycopy(BS_jmpBoot, 0, firstSector, 0, 3);
        byte[] BS_OEMName = {65, 65, 65, 65, 97, 97, 97, 97};//local=3,length=8,厂商名,'ForrestY'
        System.arraycopy(BS_OEMName, 0, firstSector, 3, 8);
        byte[] BPB_BytsPerSec = {0x00, 0x02};//local=11,length=2,每扇区字节数,0x200（即十进制512）
        System.arraycopy(BPB_BytsPerSec, 0, firstSector, 11, 2);
        byte[] BPB_SecPerClus = {0x01};//local=13,length=1,每簇扇区数,0x01
        System.arraycopy(BPB_SecPerClus, 0, firstSector, 13, 1);
        byte[] BPB_RsvdSecCnt = {0x01, 0x00};//local=14,length=2,Boot记录占用多少扇区,0x01
        System.arraycopy(BPB_RsvdSecCnt, 0, firstSector, 14, 2);
        byte[] BPB_NumFATs = {0x02};//local=16,length=1,共有多少FAT表,0x02
        firstSector[16] = BPB_NumFATs[0];
//        System.arraycopy(BPB_NumFATs,0,firstSector,16,1);
        byte[] BPB_RootEntCnt = {(byte) 0xe0, 0x00};//local=17,length=2,根目录文件数最大值,0xE0 （224）
        System.arraycopy(BPB_RootEntCnt, 0, firstSector, 17, 2);
        System.out.println("BPB_RootEntCnt=" + (int) BPB_RootEntCnt[0]);
        byte[] BPB_TotSec16 = {0x40, 0x0b};//local=19,length=2,扇区总数,0xB40（2880）
        System.arraycopy(BPB_TotSec16, 0, firstSector, 19, 2);

        byte[] BPB_Media = {(byte) 0xf0};//local=21,length=1,介质描述符,0xF0
        firstSector[21] = BPB_Media[0];
//        System.arraycopy(BPB_Media,0,firstSector,21,1);

        byte[] BPB_FATSz16 = {0x09, 0x00};//local=22,length=2,每FAT扇区数,0x09
        System.arraycopy(BPB_FATSz16, 0, firstSector, 22, 2);

        byte[] BPB_SecPerTrk = {0x12, 0x00};//local=24,length=2,每磁道扇区数,0x12
        System.arraycopy(BPB_SecPerTrk, 0, firstSector, 24, 2);

        byte[] BPB_NumHeads = {0x02, 0x00};//local=26,length=2,磁头数,0x02
        System.arraycopy(BPB_NumHeads, 0, firstSector, 26, 2);

        byte[] BPB_HiddSec = {0x00, 0x00, 0x00, 0x00};//local=28,length=4,隐藏扇区数,0
        System.arraycopy(BPB_HiddSec, 0, firstSector, 28, 4);

        byte[] BPB_TotSec32 = {0x00, 0x00, 0x00, 0x00};//local=32,length=4,如果BPB_TotSec16是0，由这个值记录扇区数,0xB40（2880）
        System.arraycopy(BPB_TotSec32, 0, firstSector, 32, 4);

        byte[] BS_DrvNum = {0x00};//local=36,length=1,中断13的驱动器号,0
        firstSector[36] = BS_DrvNum[0];
//        System.arraycopy(BS_DrvNum,0,firstSector,36,1);

        byte[] BS_Reserved1 = {0x00};//local=37,length=1,未使用(保留位),0
        firstSector[37] = BS_DrvNum[0];
//        System.arraycopy(BS_Reserved1,0,firstSector,37,1);

        byte[] BS_BootSig = {0x29};//local=38,length=1,扩展引导标记,0x29
        firstSector[38] = BS_BootSig[0];
//        System.arraycopy(BS_BootSig,0,firstSector,38,1);

        byte[] BS_VolD = {0x00, 0x00, 0x00, 0x00};//local=39,length=4,卷序列号,0
        System.arraycopy(BS_VolD, 0, firstSector, 39, 4);

        byte[] BS_VolLab = {65, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98};//local=43,length=11,卷标,'OrangeS0.02'
        System.arraycopy(BS_VolLab, 0, firstSector, 43, 11);

        byte[] BS_FileSysType = "FAT12   ".getBytes("ISO-8859-1");//local=54,length=8,文件系统类型,'FAT12'
        System.arraycopy(BS_FileSysType, 0, firstSector, 54, 8);

        byte[] 引导代码 = {};//local=62,length=448,引导代码、数据及其他填充字符等,引导代码(剩余空间被0填充)

        byte[] endFlag = {0x55, (byte) 0xaa};//结束标志,local=510,length=2,0xAA55,0xAA55
        System.arraycopy(endFlag, 0, firstSector, 510, 2);

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
        RandomAccessFile raf = new RandomAccessFile(imgFile, "rw");
        raf.seek(0);
        raf.write(fatForamtData, 0, fatForamtData.length);
        raf.close();

        return false;
    }

    /**
     * 创建目录条目
     */
    private byte[] createDirItem(byte[] dirName) throws UnsupportedEncodingException {
        byte[] dirItem=new byte[32];

        byte[] DIR_Name="mkdir".getBytes("ISO-8859-1");//文件名8字节，扩展名3字节
        byte[] DIR_Attr={};//文件属性，bit0 - readonly, bit1 - hidden, bit2 - system, bit3 - volume label, bit4 - subdirectory, bit5 - archive, bit6 - unused, bit7 - unused)"
        byte[] 保留={};//　　
        byte[] DIR_WrtTime={};//最后修改时间
        byte[] DIR_WrtDate={};//最后修改日期
        byte[] DIR_FstClus={};//此条目对应的开始簇号
        byte[] DIR_FileSize={};//文件大小

        return dirItem;
    }


    public static void main(String[] args) {


        try {
            System.out.println("FAT12   ".getBytes("ISO-8859-1").length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("请输入源文件路径：");
        Scanner scanner = new Scanner(System.in);
        String sourceFileFullPath = scanner.nextLine();
        System.out.println("sourceFileFullPath检测=" + sourceFileFullPath);
//        String sourceFileFullPath = "F:\\DOSBOXdir\\nasm-2.12.02\\boot";//F:\DOSBOXdir\nasm-2.12.02\boot
//        String sourceFileFullPath = "E:\\c\\完成程序\\二进制转换十六进制.txt";
//        String sourceFileFullPath = "E:\\c\\完成程序\\wad.txt";
        ImgGenerator imgGenerator = new ImgGenerator(sourceFileFullPath);
        try {

            imgGenerator.createNullImg(1474560, sourceFileFullPath);//E:\\awm.img

            File img = new File("E:\\awm.img");
            imgGenerator.formatImgFile(img);
            /**
             File sourceFile = imgGenerator.getSourceFile(sourceFileFullPath);
             System.out.println(sourceFile.length());
             File imgFile = imgGenerator.createImgFile(sourceFileFullPath);
             imgGenerator.writeImgFileFirstSector(sourceFile, imgFile);
             */
        } catch (FileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
