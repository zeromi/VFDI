import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class FileHandleTest {

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
            FileHandle.formatImgFile("E:\\test.img");
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
    public void test11() {
//        byte[] b = {0x3f, 0x0f};//3903,高字节取低4位;243,低字节取高4位
        byte[] b = {(byte) 0x80, 0x00};//128,高字节取低4位;8,低字节取高4位
        int x = FileHandle.transFAT12ItemToInt(b[0], b[1]);
        int y = FileHandle.transFAT12ItemToInt(b[0], b[1], false);
        System.out.println("x=" + x + ",y=" + y);
    }

    @Test
    public void test12() {
        int b = 3903;
        byte[] x = FileHandle.transIntToFAT12Item(b);
        System.out.println("x=" + Integer.toHexString(x[0] & 0xff) + ",y=" + Integer.toHexString(x[1] & 0xff));
    }

}
