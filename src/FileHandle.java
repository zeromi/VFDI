import constant.PromptWord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 功能：处理文件写入和读取
 */
public class FileHandle {

    /**
     * 生成一个指定大小的文件
     * @param size 写入字节数,单位byte
     * @param sourceFile 目标文件
     * @throws IOException
     */
    public static void createNullImg(int size, File sourceFile) throws IOException{
        if(sourceFile.exists()){
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
     * @param size 写入字节数,单位byte
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
     * 无符号整型字节转整型int(无符号),字节数组长度不得超过3
     *
     * @param b 字节数组
     * @return 转换结果
     */
//    public static int byteTransInt(byte[] b) {
//        if (b.length < 1 || b.length > 3) {
//            throw new RuntimeException(PromptWord.TRANS_DATA_ERROR);
//        }
//
//        int result= (int) byteTransLong(b);
//        return result;
//
//    }

    /**
     * 整型int转字节数组,数字大小不得超过2^31-1
     * @param number 待转换
     * @param byteNumber 设置结果字节数组的长度
     * @return 转换结果
     */
//    public static byte[] intTransByte(int number,int byteNumber) {
//
//        return longTransByte(number,byteNumber);
//
////        byte[] b=new byte[byteNumber];
////        for(int i=0;i<byteNumber;i++){
////            b[i]=(byte)(number&0xff);
////            number=number>>>8;
////        }
////        return b;
//    }

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

        m=b[0];
        m=m&0xff;
        if(b.length==1){
            return m;
        }else{
            for(int i=1;i<b.length;i++){
                n=b[i];
                n=n&0xff;
                n=n<<(8*i);
                m=m|n;
            }
            return m;
        }
    }

    /**
     * long转字节数组,数字大小不得超过2^(8*8-1)-1
     * @param number 待转换
     * @param byteNumber 设置结果字节数组的长度
     * @return 转换结果
     */
    public static byte[] longTransByte(long number,int byteNumber) {
        byte[] b=new byte[byteNumber];
        for(int i=0;i<byteNumber;i++){
            b[i]=(byte)(number&0xff);
            number=number>>>8;
        }
        return b;
    }

}
