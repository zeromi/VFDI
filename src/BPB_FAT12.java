import java.util.HashMap;

/**
 * 解析及包存首扇区数据
 */
public class BPB_FAT12 {

    private HashMap<String,byte[]> BPB;//存放bootSector解析的各项数据

    private byte[] bootSector ;//启动扇区,BPB(BIOS Parametre Block)
    private byte[] fat1 ;//FAT1表
    private byte[] fat2 ;//FAT2表，FAT1备份
    private byte[] rootDir ;//根目录占了14个扇区，每个项占32字节，FAT12中共224项

    //偏移(local)    长度(length)	    内容	    软盘参考值
    private byte[] BS_jmpBoot ;        //local=0,length=3,一个短跳转指令,再加nop指令
    private byte[] BS_OEMName ;        //local=3,length=8,厂商名,'ForrestY'
    private byte[] BPB_BytsPerSec ;    //local=11,length=2,每扇区字节数,0x200 (512)
    private byte[] BPB_SecPerClus ;    //local=13,length=1,每簇扇区数,0x01
    private byte[] BPB_RsvdSecCnt ;    //local=14,length=2,Boot记录占用多少扇区,0x01
    private byte[] BPB_NumFATs ;       //local=16,length=1,共有多少FAT表,0x02
    private byte[] BPB_RootEntCnt ;    //local=17,length=2,根目录文件数最大值,0xE0 (224)
    private byte[] BPB_TotSec16 ;      //local=19,length=2,扇区总数,0xB40 (2880)
    private byte[] BPB_Media ;         //local=21,length=1,介质描述符,0xF0
    private byte[] BPB_FATSz16 ;       //local=22,length=2,每FAT扇区数,0x09
    private byte[] BPB_SecPerTrk ;     //local=24,length=2,每磁道扇区数,0x12
    private byte[] BPB_NumHeads ;      //local=26,length=2,磁头数,0x02
    private byte[] BPB_HiddSec ;       //local=28,length=4,隐藏扇区数,0
    private byte[] BPB_TotSec32 ;      //local=32,length=4,如果BPB_TotSec16是0，由这个值记录扇区数,0xB40（2880）
    private byte[] BS_DrvNum ;         //local=36,length=1,中断13的驱动器号,0
    private byte[] BS_Reserved1 ;      //local=37,length=1,未使用(保留位),0
    private byte[] BS_BootSig ;        //local=38,length=1,扩展引导标记,0x29
    private byte[] BS_VolD ;           //local=39,length=4,卷序列号,0(应由操作系统生成的唯一值)
    private byte[] BS_VolLab ;         //local=43,length=11,卷标,'OrangeS0.02'
    private byte[] BS_FileSysType ;    //local=54,length=8,文件系统类型,'FAT12'
    private byte[] BootCode ;          //local=62,length=448,引导代码、数据及其他填充字符等,引导代码(剩余空间被0填充)
    private byte[] BS_EndFlag ;        //local=510,length=2,结束标志,0xAA55 (操作系统以这个标志识别是否为可引导扇区)

    /**
     * 构造方法默认所有参数数组数据都为0
     * 用法：若需默认数据，请调用initBPB()
     */
    public BPB_FAT12() {

        bootSector = new byte[512];//启动扇区
        fat1 = new byte[512 * 9];//FAT1表
        fat2 = new byte[512 * 9];//FAT2表，FAT1备份
        rootDir = new byte[32 * 224];//根目录占了14个扇区，每个项占32字节，FAT12中共224项

        //偏移(local)    长度(length)	    内容	    软盘参考值
        BS_jmpBoot = new byte[3];        //local=0,length=3,一个短跳转指令,再加nop指令
        BS_OEMName = new byte[8];        //local=3,length=8,厂商名,'ForrestY'
        BPB_BytsPerSec = new byte[2];    //local=11,length=2,每扇区字节数,0x200 (512)
        BPB_SecPerClus = new byte[1];    //local=13,length=1,每簇扇区数,0x01
        BPB_RsvdSecCnt = new byte[2];    //local=14,length=2,Boot记录占用多少扇区,0x01
        BPB_NumFATs = new byte[1];       //local=16,length=1,共有多少FAT表,0x02
        BPB_RootEntCnt = new byte[2];    //local=17,length=2,根目录文件数最大值,0xE0 (224)
        BPB_TotSec16 = new byte[2];      //local=19,length=2,扇区总数,0xB40 (2880)
        BPB_Media = new byte[1];         //local=21,length=1,介质描述符,0xF0
        BPB_FATSz16 = new byte[2];       //local=22,length=2,每FAT扇区数,0x09
        BPB_SecPerTrk = new byte[2];     //local=24,length=2,每磁道扇区数,0x12
        BPB_NumHeads = new byte[2];      //local=26,length=2,磁头数,0x02
        BPB_HiddSec = new byte[4];       //local=28,length=4,隐藏扇区数,0
        BPB_TotSec32 = new byte[4];      //local=32,length=4,如果BPB_TotSec16是0，由这个值记录扇区数,0xB40（2880）
        BS_DrvNum = new byte[1];         //local=36,length=1,中断13的驱动器号,0
        BS_Reserved1 = new byte[1];      //local=37,length=1,未使用(保留位),0
        BS_BootSig = new byte[1];        //local=38,length=1,扩展引导标记,0x29
        BS_VolD = new byte[4];           //local=39,length=4,卷序列号,0(应由操作系统生成的唯一值)
        BS_VolLab = new byte[11];        //local=43,length=11,卷标,'OrangeS0.02'
        BS_FileSysType = new byte[8];    //local=54,length=8,文件系统类型,'FAT12'
        BootCode = new byte[448];        //local=62,length=448,引导代码、数据及其他填充字符等,引导代码(剩余空间被0填充)
        BS_EndFlag = new byte[3];        //local=510,length=2,结束标志,0xAA55 (操作系统以这个标志识别是否为可引导扇区)

        BPB=new HashMap<String, byte[]>();

        BPB.put("BS_jmpBoot",BS_jmpBoot) ;
        BPB.put("BS_OEMName",BS_OEMName) ;
        BPB.put("BPB_BytsPerSec",BPB_BytsPerSec) ;
        BPB.put("BPB_SecPerClus",BPB_SecPerClus) ;
        BPB.put("BPB_RsvdSecCnt",BPB_RsvdSecCnt) ;
        BPB.put("BPB_NumFATs",BPB_NumFATs) ;
        BPB.put("BPB_RootEntCnt",BPB_RootEntCnt) ;
        BPB.put("BPB_TotSec16",BPB_TotSec16) ;
        BPB.put("BPB_Media",BPB_Media) ;
        BPB.put("BPB_FATSz16",BPB_FATSz16) ;
        BPB.put("BPB_SecPerTrk",BPB_SecPerTrk) ;
        BPB.put("BPB_NumHeads",BPB_NumHeads) ;
        BPB.put("BPB_HiddSec",BPB_HiddSec) ;
        BPB.put("BPB_TotSec32",BPB_TotSec32) ;
        BPB.put("BS_DrvNum",BS_DrvNum) ;
        BPB.put("BS_Reserved1",BS_Reserved1) ;
        BPB.put("BS_BootSig",BS_BootSig) ;
        BPB.put("BS_VolD",BS_VolD) ;
        BPB.put("BS_VolLab",BS_VolLab) ;
        BPB.put("BS_FileSysType",BS_FileSysType) ;
        BPB.put("BootCode",BootCode) ;
        BPB.put("BS_EndFlag",BS_EndFlag) ;
    }

    /**
     * 使用默认FAT12格式化启动扇区数据，进行对象数据初始化
     * 建议：需要默认数据时，在new 对象后应调用该方法
     */
    public void initBPB() {

        bootSector = getInitBPB();
        setBootSector(bootSector);
        byte[] cluster={(byte)0xf0,(byte)0xff,(byte)0xff};//cluster,FAT12中0、1号簇默认值
        System.arraycopy(cluster,0,fat1,0,3);
        System.arraycopy(cluster,0,fat2,0,3);
    }

    /**
     * 获取默认格式化的启动扇区512字节数据
     * @return
     */
    public static byte[] getInitBPB() {

        //长度为512字节
        byte[] headData = new byte[]{
                //
                (byte) 0xeb, (byte) 0x3c, (byte) 0x90,              //local=0,length=3,一个短跳转指令,再加nop指令
                65, 65, 65, 65, 97, 97, 97, 97,                     //local=3,length=8,厂商名,'ForrestY'
                0x00, 0x02,                                         //local=11,length=2,每扇区字节数,0x200 (512)
                0x01,                                               //local=13,length=1,每簇扇区数,0x01
                0x01, 0x00,                                         //local=14,length=2,Boot记录占用多少扇区,0x01
                0x02,                                               //local=16,length=1,共有多少FAT表,0x02
                (byte) 0xe0, 0x00,                                  //local=17,length=2,根目录文件数最大值,0xE0 (224)
                0x40, 0x0b,                                         //local=19,length=2,扇区总数,0xB40 (2880)
                (byte) 0xf0,                                        //local=21,length=1,介质描述符,0xF0
                0x09, 0x00,                                         //local=22,length=2,每FAT扇区数,0x09
                0x12, 0x00,                                         //local=24,length=2,每磁道扇区数,0x12
                0x02, 0x00,                                         //local=26,length=2,磁头数,0x02
                0x00, 0x00, 0x00, 0x00,                             //local=28,length=4,隐藏扇区数,0
                0x00, 0x00, 0x00, 0x00,                             //local=32,length=4,如果BPB_TotSec16是0，由这个值记录扇区数,0xB40（2880）
                0x00,                                               //local=36,   length=1,中断13的驱动器号,0
                0x00,                                               //local=37,length=1,未使用(保留位),0
                0x29,                                               //local=38,length=1,扩展引导标记,0x29
                0x00, 0x00, 0x00, 0x00,                             //local=39,length=4,卷序列号,0(应由操作系统生成的唯一值)
//                65, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98,          //local=43,length=11,卷标,'OrangeS0.02'
                'I', 'm', 'g', 'E', 'd', 'i', 't', '0', '.', '0', '1',          //local=43,length=11,卷标,'OrangeS0.02'
                'F', 'A', 'T', '1', '2', ' ', ' ', ' ',                    //local=54,length=8,文件系统类型,'FAT12'
        };
        byte[] init_bootSector = new byte[512];
        System.arraycopy(headData, 0, init_bootSector, 0, 62);
//        System.arraycopy(bootCode,0,bootSector,62,448);//local=62,length=448,引导代码、数据及其他填充字符等,引导代码(剩余空间被0填充)
        //local=510,length=2,结束标志,0xAA55 (操作系统以这个标志识别是否为可引导扇区)
        init_bootSector[510] = 0x55;
        init_bootSector[511] = (byte) 0xAA;
//        parseBPB(bootSector);
        return init_bootSector;
    }

    public void parseBPB() {
        parseBPB(bootSector);
    }

    private void parseBPB(byte[] bootSector) {
        System.arraycopy(bootSector, 0, BS_jmpBoot, 0, 3);
        System.arraycopy(bootSector, 3, BS_OEMName, 0, 8);
        System.arraycopy(bootSector, 11, BPB_BytsPerSec, 0, 2);
        System.arraycopy(bootSector, 13, BPB_SecPerClus, 0, 1);
        System.arraycopy(bootSector, 14, BPB_RsvdSecCnt, 0, 2);
        System.arraycopy(bootSector, 16, BPB_NumFATs, 0, 1);
        System.arraycopy(bootSector, 17, BPB_RootEntCnt, 0, 2);
        System.arraycopy(bootSector, 19, BPB_TotSec16, 0, 2);
        System.arraycopy(bootSector, 21, BPB_Media, 0, 1);
        System.arraycopy(bootSector, 22, BPB_FATSz16, 0, 2);
        System.arraycopy(bootSector, 24, BPB_SecPerTrk, 0, 2);
        System.arraycopy(bootSector, 26, BPB_NumHeads, 0, 2);
        System.arraycopy(bootSector, 28, BPB_HiddSec, 0, 4);
        System.arraycopy(bootSector, 32, BPB_TotSec32, 0, 4);
        System.arraycopy(bootSector, 36, BS_DrvNum, 0, 1);
        System.arraycopy(bootSector, 37, BS_Reserved1, 0, 1);
        System.arraycopy(bootSector, 38, BS_BootSig, 0, 1);
        System.arraycopy(bootSector, 39, BS_VolD, 0, 4);
        System.arraycopy(bootSector, 43, BS_VolLab, 0, 11);
        System.arraycopy(bootSector, 54, BS_FileSysType, 0, 8);
        System.arraycopy(bootSector, 62, BootCode, 0, 448);
        System.arraycopy(bootSector, 510, BS_EndFlag, 0, 2);
    }

    public void buildBPB() {
        buildBPB(bootSector);
    }

    private void buildBPB(byte[] bootSector) {

        System.arraycopy(BS_jmpBoot, 0, bootSector, 0, 3);
        System.arraycopy(BS_OEMName, 0, bootSector, 3, 8);
        System.arraycopy(BPB_BytsPerSec, 0, bootSector, 11, 2);
        System.arraycopy(BPB_SecPerClus, 0, bootSector, 13, 1);
        System.arraycopy(BPB_RsvdSecCnt, 0, bootSector, 14, 2);
        System.arraycopy(BPB_NumFATs, 0, bootSector, 16, 1);
        System.arraycopy(BPB_RootEntCnt, 0, bootSector, 17, 2);
        System.arraycopy(BPB_TotSec16, 0, bootSector, 19, 2);
        System.arraycopy(BPB_Media, 0, bootSector, 21, 1);
        System.arraycopy(BPB_FATSz16, 0, bootSector, 22, 2);
        System.arraycopy(BPB_SecPerTrk, 0, bootSector, 24, 2);
        System.arraycopy(BPB_NumHeads, 0, bootSector, 26, 2);
        System.arraycopy(BPB_HiddSec, 0, bootSector, 28, 4);
        System.arraycopy(BPB_TotSec32, 0, bootSector, 32, 4);
        System.arraycopy(BS_DrvNum, 0, bootSector, 36, 1);
        System.arraycopy(BS_Reserved1, 0, bootSector, 37, 1);
        System.arraycopy(BS_BootSig, 0, bootSector, 38, 1);
        System.arraycopy(BS_VolD, 0, bootSector, 39, 4);
        System.arraycopy(BS_VolLab, 0, bootSector, 43, 11);
        System.arraycopy(BS_FileSysType, 0, bootSector, 54, 8);
        System.arraycopy(BootCode, 0, bootSector, 62, 448);
        System.arraycopy(BS_EndFlag, 0, bootSector, 510, 2);
    }

    public byte[] getBPBData(String key) {
        return BPB.get(key);
    }

    public void setBPBData(String key,byte[] data) {
        BPB.remove(key);
        BPB.put(key,data);
    }

    public byte[] getBootSector() {
        return bootSector;
    }

    public void setBootSector(byte[] bootSector) {
        this.bootSector = bootSector;
    }

    public byte[] getFat1() {
        return fat1;
    }

    public void setFat1(byte[] fat1) {
        this.fat1 = fat1;
    }

    public byte[] getFat2() {
        return fat2;
    }

    public void setFat2(byte[] fat2) {
        this.fat2 = fat2;
    }

    public byte[] getRootDir() {
        return rootDir;
    }

    public void setRootDir(byte[] rootDir) {
        this.rootDir = rootDir;
    }
}
