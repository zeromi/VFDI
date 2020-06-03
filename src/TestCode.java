import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * 单元测试用例
 */
public class TestCode {

    public static void main(String[] args) {
        System.out.println("Begin test!");

        TestCode t = new TestCode();

        t.test8();

        System.out.println("The test end!");
    }

    @Test
    //CreateNullImg，创建空的虚拟软盘文件
    public void test1() {
        try {
            FileHandle.createNullImg(1474560, "E:\\test.img");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    //formatImgFile，格式化虚拟软盘文件的文件系统为FAT12
    public void test2() {
        try {
            ImgEdit.formatImgFile("E:\\test.img");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    //测试无符号整型字节转整型int(无符号)
    public void test3() {
        byte[] b1 = {0x02};
        byte[] b2 = {0x20, 0x11};
        byte[] b3 = {0x12, 0x00, (byte) 0xe0};

        int a1 = (int) FileHandle.byteTransLong(b1);
        int a2 = (int) FileHandle.byteTransLong(b2);
        int a3 = (int) FileHandle.byteTransLong(b3);

//        输出结果:a1=2
//        输出结果:a2=4384
//        输出结果:a3=14680082
        System.out.println("输出结果:a1=" + a1);
        System.out.println("输出结果:a2=" + a2);
        System.out.println("输出结果:a3=" + a3);
    }

    @Test
    //测试整型int转无符号整型字节(无符号)
    public void test4() {

//        输出结果:a1=2
//        输出结果:a2=4384
//        输出结果:a3=14680082
//        byte[] b1={0x02};
//        byte[] b2={0x20,0x11};
//        byte[] b3={0x12,0x00,(byte)0xe0};
        int a1 = 2;
        int a2 = 4384;
        int a3 = 14680082;

        byte[] b1 = FileHandle.longTransByte(a1, 1);
        byte[] b2 = FileHandle.longTransByte(a2, 2);
        byte[] b3 = FileHandle.longTransByte(a3, 3);

        System.out.println("输出结果:b1=" + Arrays.toString(b1));
        System.out.println("输出结果:b2=" + Arrays.toString(b2));
        System.out.println("输出结果:b3=" + Arrays.toString(b3));
    }

    @Test
    //BPB_FAT12.getInitBPB()，确认数组数据是否正确
    public void test5() {
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

        RandomAccessFile raf = null;
        try {
            File imgFile = FileHandle.createNullImg(1474560, "E:\\imgTest.img");
            //写入格式化数据到虚拟软盘文件
            raf = new RandomAccessFile(imgFile, "rw");
            ImgEdit.randomWriteFile(raf, 0L, fatForamtData, fatForamtData.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    //测试读取
    public void test6() {
        BPB_FAT12 fat12 = null;
        try {
            fat12 = ImgEdit.readBPB("E:\\MS_带目录和文件.img");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(FileHandle.byteTransLong(fat12.getBPBData("BPB_RootEntCnt")));
    }

    @Test
    //测试数据初始化
    public void test7() {
        BPB_FAT12 fat12 = null;
        try {
            fat12 = ImgEdit.readBPB("E:\\MS_带目录和文件.img");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImgEdit imgEdit = new ImgEdit();
        imgEdit.initData(fat12);
        System.out.println(imgEdit.toString());

    }

    @Test
    //测试数据初始化
    public void test8() {
        BPB_FAT12 fat12 = null;
        try {
            fat12 = ImgEdit.readBPB("E:\\MS_带目录和文件.img");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImgEdit imgEdit = new ImgEdit();
        try {
            System.out.println("dirLength="+fat12.getRootDir().length);
            imgEdit.getRootDirInfo(fat12.getRootDir());
        } catch (Exception e) {
            e.printStackTrace();
        }
        TreeMap<String, DirItem> rootTree = imgEdit.getRootDirTree();
        System.out.println(rootTree);
    }

    @Test
    //测试数据初始化
    public void test9() {
        /*
            41,b0,65,fa,5e,87,65,f6,4e,39,59,0f,00,75,00,00,
            ff,ff,ff,ff,ff,ff,ff,ff,ff,ff,00,00,ff,ff,ff,ff,
            短文件名项
            d0,c2,bd,a8,ce,c4,7e,31,20,20,20,10,00,a8,e6,8a,
            8b,50,8b,50,00,00,e7,8a,8b,50,02,00,00,00,00,00,
         */
        byte[][] b = new byte[][]{
                {0x41, (byte) 0xb0, 0x65, (byte) 0xfa, (byte) 0x5e, (byte) 0x87, 0x65, (byte) 0xf6,
                        (byte) 0x4e, 0x39, 0x59, (byte) 0x0f, 0x00, 0x75, 0x00, 0x00,
                        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                        (byte) 0xff, (byte) 0xff, 0x00, 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}
//                {(byte)0xd0,(byte)0xc2,(byte)0xbd,(byte)0xa8,(byte)0xce,(byte)0xc4,(byte)0x7e,0x31,
//                        0x20,0x20,0x20,0x10,0x00,(byte)0xa8,(byte)0xe6,(byte)0x8a,
//                        (byte)0x8b,0x50,(byte)0x8b,0x50,0x00,0x00,(byte)0xe7,(byte)0x8a,
//                        (byte)0x8b,0x50,0x02,0x00,0x00,0x00,0x00,0x00}
        };
        /*
            0x43,0x2e,0x00,0x71,0x00,0x77,0x00,0x65,0x00,0x72,0x00,0x0f,0x00,0x57,0x00,0x00,
            0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff,0x00,0x00,0xff,0xff,0xff,0xff,

            0x02,0x6e,0x00,0x6f,0x00,0x70,0x00,0x71,0x00,0x72,0x00,0x0f,0x00,0x57,0x73,0x00,
            0x74,0x00,0x75,0x00,0x76,0x00,0x77,0x00,0x78,0x00,0x00,0x00,0x79,0x00,0x7a,0x00,

            0x01,0x61,0x00,0x62,0x00,0x63,0x00,0x64,0x00,0x65,0x00,0x0f,0x00,0x57,0x66,0x00,
            0x67,0x00,0x68,0x00,0x69,0x00,0x6a,0x00,0x6b,0x00,0x00,0x00,0x6c,0x00,0x6d,0x00,
            短文件名项
            0x41,0x42,0x43,0x44,0x45,0x46,0x7e,0x31,0x51,0x57,0x45,0x20,0x00,0x64,0x56,0x2a,
            0xc3,0x50,0xc3,0x50,0x00,0x00,0x56,0x2a,0xc3,0x50,0x05,0x00,0xfa,0x03,0x00,0x00,
         */
//        byte[][] b=new byte[][]{
//                {0x43,0x2e,0x00,0x71,0x00,0x77,0x00,0x65,
//                 0x00,0x72,0x00,0x0f,0x00,0x57,0x00,0x00,
//                 (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,
//                 (byte)0xff,(byte)0xff,0x00,0x00,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff},
//
//                {0x02,0x6e,0x00,0x6f,0x00,0x70,0x00,0x71,
//                 0x00,0x72,0x00,0x0f,0x00,0x57,0x73,0x00,
//                 0x74,0x00,0x75,0x00,0x76,0x00,0x77,0x00,
//                 0x78,0x00,0x00,0x00,0x79,0x00,0x7a,0x00},
//
//                {0x01,0x61,0x00,0x62,0x00,0x63,0x00,0x64,
//                 0x00,0x65,0x00,0x0f,0x00,0x57,0x66,0x00,
//                 0x67,0x00,0x68,0x00,0x69,0x00,0x6a,0x00,
//                 0x6b,0x00,0x00,0x00,0x6c,0x00,0x6d,0x00}
//        };
        System.out.println("b[0][0]=" + (int) b[0][0]);
        System.out.println("b.length=" + b.length);
        String s = null;
        try {
            s = DirItem.parseLongFileName(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(s);
    }

    @Test
    public void test10() {
//        byte[] b = {0x41, (byte) 0xb0, 0x65, (byte) 0xfa, (byte) 0x5e, (byte) 0x87, 0x65, (byte) 0xf6,
//                (byte) 0x4e, 0x39, 0x59, (byte) 0x0f, 0x00, 0x75, 0x00, 0x00,
//                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
//                (byte) 0xff, (byte) 0xff, 0x00, 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        byte[] a= new byte[]{//短文件名数组
                0x41,0x42,0x43,0x44,0x45,0x46,0x7e,0x31,
                0x51,0x57,0x45,0x20,0x00,0x64,0x56,0x2a,
                (byte)0xc3,0x50,(byte)0xc3,0x50,0x00,0x00,0x56,0x2a,
                (byte)0xc3,0x50,0x05,0x00,(byte)0xfa,0x03,0x00,0x00
        };
        byte[] b = new byte[]{//长文件名数组
                0x43, 0x2e, 0x00, 0x71, 0x00, 0x77, 0x00, 0x65,
                0x00, 0x72, 0x00, 0x0f, 0x00, 0x57, 0x00, 0x00,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, 0x00, 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,

                0x02, 0x6e, 0x00, 0x6f, 0x00, 0x70, 0x00, 0x71,
                0x00, 0x72, 0x00, 0x0f, 0x00, 0x57, 0x73, 0x00,
                0x74, 0x00, 0x75, 0x00, 0x76, 0x00, 0x77, 0x00,
                0x78, 0x00, 0x00, 0x00, 0x79, 0x00, 0x7a, 0x00,

                0x01, 0x61, 0x00, 0x62, 0x00, 0x63, 0x00, 0x64,
                0x00, 0x65, 0x00, 0x0f, 0x00, 0x57, 0x66, 0x00,
                0x67, 0x00, 0x68, 0x00, 0x69, 0x00, 0x6a, 0x00,
                0x6b, 0x00, 0x00, 0x00, 0x6c, 0x00, 0x6d, 0x00};
        System.out.println("b.length=" + b.length);
        String s = null;
        try {
//            DirItem dirItem = new DirItem(a,b);
            DirItem dirItem = new DirItem(a);
            s = dirItem.getItemName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(s);
    }

    @Test
    public void testTemp() {
        String s="   ";
        s=s.trim();
        System.out.println("s.length()="+s.length());
    }
}
