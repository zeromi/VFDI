import org.junit.Test;

public class DirItemTest {

    @Test
    //测试长、短目录项解析
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
    /**
     * 测试文件名解析
     */
    public void test10() {
//        byte[] b = {0x41, (byte) 0xb0, 0x65, (byte) 0xfa, (byte) 0x5e, (byte) 0x87, 0x65, (byte) 0xf6,
//                (byte) 0x4e, 0x39, 0x59, (byte) 0x0f, 0x00, 0x75, 0x00, 0x00,
//                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
//                (byte) 0xff, (byte) 0xff, 0x00, 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        byte[] a = new byte[]{//短文件名数组
                0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x7e, 0x31,
                0x51, 0x57, 0x45, 0x20, 0x00, 0x64, 0x56, 0x2a,
                (byte) 0xc3, 0x50, (byte) 0xc3, 0x50, 0x00, 0x00, 0x56, 0x2a,
                (byte) 0xc3, 0x50, 0x05, 0x00, (byte) 0xfa, 0x03, 0x00, 0x00
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

}
