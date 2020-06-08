import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;

public class ImgEditTest {


    @Test
    //测试读取BPB
    public void test6() {
        BPB_FAT12 fat12 = null;
        try {
            ImgEdit imgEdit = new ImgEdit("E:\\MS_带目录和文件.img");
            fat12 = imgEdit.readBPB("E:\\MS_带目录和文件.img");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(FileHandle.byteTransLong(fat12.getBPBData("BPB_RootEntCnt")));
    }

    @Test
    //测试数据初始化
    public void test7() {
        BPB_FAT12 fat12 = null;
        String imgFileFullPath="E:\\MS_带目录和文件.img";
        try {
            ImgEdit imgEdit = new ImgEdit(imgFileFullPath);
            fat12 = imgEdit.readBPB(imgFileFullPath);
            imgEdit.initData(fat12);
            System.out.println(imgEdit.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    //测试数据初始化
    public void test8() {
        BPB_FAT12 fat12 = null;
        String imgFileFullPath = "E:\\MS_带目录和文件.img";
        try {
            ImgEdit imgEdit = new ImgEdit(imgFileFullPath);
            fat12 = imgEdit.readBPB(imgFileFullPath);
            System.out.println("dirLength=" + fat12.getRootDir().length);
            imgEdit.getDirInfo(fat12.getRootDir());
            TreeMap<String, DirItem> rootTree = imgEdit.getRootDirTree();
            System.out.println(rootTree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    //测试获取软盘映像中的文件的簇组
    public void test13() {
        BPB_FAT12 fat12 = null;
        String imgFileFullPath = "E:\\MS_带目录和文件.img";
        try {
            ImgEdit imgEdit = new ImgEdit(imgFileFullPath);
            fat12 = imgEdit.readBPB(imgFileFullPath);
            imgEdit.initData(fat12);//必须进行数据初始化
            System.out.println("dirLength=" + fat12.getRootDir().length);
            imgEdit.getDirInfo(fat12.getRootDir());
            TreeMap<String, DirItem> rootTree = imgEdit.getRootDirTree();
            System.out.println(rootTree);
            int[] c = null;
//            c=imgEdit.getFileClusGroup(rootTree.get("home.txt"),fat12.getFat1());
            c = imgEdit.getFileClusGroup(rootTree.get("mid.txt"), fat12.getFat1());
            System.out.println(Arrays.toString(c));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test15() {
        String imgFileFullPath = "E:\\mytest.img";
//        String imgFileFullPath = "E:\\MS_带目录和文件.img";
        try {
            ImgEdit imgEdit = new ImgEdit(imgFileFullPath);
            TreeMap<String, DirItem> rootTree = imgEdit.getRootDirTree();
            String fileName = "cpfd.sh";
//        String fileName="mid.txt";
            DirItem dirItem = rootTree.get(fileName);
            int[] c = imgEdit.getFileClusGroup(dirItem);
            long fileLength = dirItem.getDirFileSize();
            byte[] b = imgEdit.readFile(c, fileLength);

            System.out.println("以下为文件内容：");
            String s = new String(b);
//            String s=new String(b,"ISO-8859-1");
            System.out.println("s.length=" + s.length());
            System.out.println(s);
            System.out.println("文件内容结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Begin test!");

        ImgEditTest t = new ImgEditTest();

        t.test8();

        System.out.println("The test end!");
    }

}
