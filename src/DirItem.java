import constant.PromptWord;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.TreeMap;

public class DirItem {

    private TreeMap<String, DirItem> subDirTree;//子目录树

    private byte[] srcLongNameItem;//长文件名目录项数组
    private byte[][] srclongNameGroup;//长文件名分组

    private byte[] srcShortNameItem;//短文件名目录项数组

    private byte[] srcDIR_Name;//文件名8字节，扩展名3字节,11(0x0b),短文件名
    private byte[] srcDIR_Attr;//文件属性,1
    private byte[] srcReserve;//保留位,10
    private byte[] srcDIR_WrtTime;//最后修改时间,2
    private byte[] srcDIR_WrtDate;//最后修改日期,2
    private byte[] srcDIR_FstClus;//此条目对应的开始簇号,2
    private byte[] srcDIR_FileSize;//文件大小byte,4

    private String itemName;//文件名
    private String shortName;//短文件名
    private boolean isDir;//是否目录
    private int dirAttr;//文件属性,1
    private byte[] reserve;//保留位,10
    private Date dirWrtDate;//最后修改日期和时间
    private int dirFstClus;//此条目对应的开始簇号,2
    private long dirFileSize;//文件大小,4

    /*
        文件属性
        00000000：普通文件，可随意读写
        00000001：只读文件，不可改写
        00000010：隐藏文件，浏览文件时隐藏列表
        00000100：系统文件，删除的时候会有提示
        00001000：卷标，作为磁盘的卷标识符
        00010000：目录文件，此文件是一个子目录，它的内容就是此目录下的所有文件目录项
        00100000：归档文件（类似于压缩包被分包，但是只需打开其中一个其余的会自动解压）
     */

    public DirItem(byte[] shortItem) throws Exception {
        initData(shortItem, null);
    }

    public DirItem(byte[] shortItem, byte[] longItem) throws Exception {
        if (shortItem.length != 32 || longItem.length % 32 != 0) {
            throw new RuntimeException("目录数据异常！");
        }
        initData(shortItem, longItem);
    }

    private void initData(byte[] shortItem, byte[] longItem) throws Exception {

        this.srcShortNameItem = shortItem;
        this.srcLongNameItem = longItem;

        srcDIR_Name = new byte[11];//文件名8字节，扩展名3字节,11(0x0b)
        srcDIR_Attr = new byte[1];//文件属性,1
        srcReserve = new byte[10];//保留位,10
        srcDIR_WrtTime = new byte[2];//最后修改时间,2
        srcDIR_WrtDate = new byte[2];//最后修改日期,2
        srcDIR_FstClus = new byte[2];//此条目对应的开始簇号,2
        srcDIR_FileSize = new byte[4];//文件大小byte,4

        System.arraycopy(this.srcShortNameItem, 0, srcDIR_Name, 0, 11);
        System.arraycopy(this.srcShortNameItem, 11, srcDIR_Attr, 0, 1);
        System.arraycopy(this.srcShortNameItem, 12, srcReserve, 0, 10);
        System.arraycopy(this.srcShortNameItem, 22, srcDIR_WrtTime, 0, 2);
        System.arraycopy(this.srcShortNameItem, 24, srcDIR_WrtDate, 0, 2);
        System.arraycopy(this.srcShortNameItem, 26, srcDIR_FstClus, 0, 2);
        System.arraycopy(this.srcShortNameItem, 28, srcDIR_FileSize, 0, 4);

        if (srcLongNameItem != null) {
            //现在基本都使用长文件名
            itemName = parseLongFileName(srcLongNameItem);
        } else {
            //对旧版系统的FAT12文件系统进行兼容
            shortName = parseShortFileName(srcDIR_Name, srcReserve[0]);
            itemName = shortName;
        }
        dirFstClus = (int) FileHandle.byteTransLong(srcDIR_FstClus);
        ;
        if ((srcDIR_Attr[0] & 0x10) == (byte) 0x10) {
            isDir = true;
            this.subDirTree = null;
        } else {
            isDir = false;
        }
        dirFileSize = FileHandle.byteTransLong(srcDIR_FileSize);
    }

    /**
     * 解析短文件名，会根据传入标志判断文件名大小写
     *
     * @param srcDIR_Name    文件名数组，11byte
     * @param srcReserveFlag 文件名大小写标志，即短文件名目录项第13位字节
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String parseShortFileName(byte[] srcDIR_Name, byte srcReserveFlag) throws UnsupportedEncodingException {
        String nameTemp = new String(srcDIR_Name, "ISO-8859-1");
        //去掉文件名的空白部分
        String fileName = nameTemp.substring(0, 8).trim();
        String extendName = nameTemp.substring(8).trim();
        /*
            FAT32/16/12根据文件目录项判断文件名大小写的方法（仅限8.3命名规则，常用于旧版本dos6.x和window3.x）：
             文件目录项0CH字节的值：
             1. 此值为18H时，文件名和扩展名都小写。
             2. 此值为10H时，文件名大写而扩展名小写。
             3. 此值为08H时，文件名小写而扩展名大写。
             4. 此值为00H时，文件名和扩展名都大写。
         */
        switch (srcReserveFlag) {
            case (byte) 0x18://文件名和扩展名都小写。
                fileName = fileName.toLowerCase();
                extendName = extendName.toLowerCase();
                break;
            case (byte) 0x10://文件名大写而扩展名小写。
                fileName = fileName.toUpperCase();
                extendName = extendName.toLowerCase();
                break;
            case (byte) 0x08://文件名小写而扩展名大写。
                fileName = fileName.toLowerCase();
                extendName = extendName.toUpperCase();
                break;
            case (byte) 0x00://文件名和扩展名都大写。
                fileName = fileName.toUpperCase();
                extendName = extendName.toUpperCase();
                break;
            default:
                ;//不做处理
        }
        if (extendName.length() == 0) {
            nameTemp = fileName;
        } else {
            nameTemp = fileName + "." + extendName;
        }
        return nameTemp;
    }

    public String parseLongFileName(byte[] longDirItem) throws Exception {
        byte[][] srcLNI = new byte[longDirItem.length / 32][];
        for (int i = 0; i < longDirItem.length; i += 32) {
            srcLNI[i / 32] = Arrays.copyOfRange(longDirItem, i, i + 32);
        }
        this.srclongNameGroup = srcLNI;
        return parseLongFileName(srclongNameGroup);
    }

    /**
     * 处理长文件名项，返回长文件名
     *
     * @param srcLNI 意思：srcLongNameItem
     * @return
     * @throws Exception
     */
    public static String parseLongFileName(byte[][] srcLNI) throws Exception {
        int longItemNum = srcLNI[0][0] & 0x0f;
        if ((srcLNI[0][0] & 0x40) != (byte) 0x40) {//检测第一个长目录项是否为最后一项的标志
            throw new Exception(PromptWord.ERROR_2001);
        }
        byte check = srcLNI[0][13];

        byte[] p = new byte[13 * 2];
        byte[] reslut = new byte[13 * 2 * srcLNI.length];

        boolean flag = false;//解析完毕应修改为true

        for (int i = 0; i < srcLNI.length; i++) {
            //检测第一个长目录项的序号(从1开始)、长目录项的标志位0x0f、校验和是否正确
            if ((srcLNI[i][0] & 0x1f) != (srcLNI.length - i) || srcLNI[i][11] != (byte) 0x0f || srcLNI[i][13] != check) {
                throw new Exception(PromptWord.ERROR_2001);
            }
            System.arraycopy(srcLNI[i], 1, p, 0, 10);
            System.arraycopy(srcLNI[i], 14, p, 10, 12);
            System.arraycopy(srcLNI[i], 28, p, 22, 4);
            if (i == 0) {
                for (int j = 0; j < p.length; j += 2) {
                    if (p[j] == 0 && p[j + 1] == 0) {
                        //复制尾部数据，若连续为00，则表明是文件名末尾，后面都是FF
                        System.arraycopy(p, 0, reslut, 26 * (srcLNI.length - 1), j);
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    continue;//如果上面提前截断了，那么直接开始下一次循环
                }
            }
            System.arraycopy(p, 0, reslut, 26 * (srcLNI.length - 1 - i), 26);
        }
        return unicodeToChar(reslut);//将unicode编码转换成字符串
    }

    public static String unicodeToChar(byte[] b) {
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        int x, y;
        for (int i = 0; i < b.length - 1; i += 2) {
            x = b[i];
            x = x & 0xff;
            y = b[i + 1];
            y = y & 0xff;
            y = y << 8;
            y += x;
            if (y == 0) {
                break;
            }
            sb.append((char) y);
        }

        return sb.toString();
    }

    public String getItemName() {
        return itemName;
    }

    public int getDirFstClus() {
        return dirFstClus;
    }

    public long getDirFileSize() {
        return dirFileSize;
    }

    public byte[] getSrcDIR_FstClus() {
        return srcDIR_FstClus;
    }

    public boolean isDir() {
        return isDir;
    }

    public TreeMap<String, DirItem> getSubDirTree() {
        return subDirTree;
    }

    public void setSubDirTree(TreeMap<String, DirItem> subDirTree) {
        this.subDirTree = subDirTree;
    }

    @Override
    public String toString() {
        return "DirItem{" +
                "itemName='" + itemName + '\'' +
                ", dirFstClus=" + dirFstClus +
                ", dirFileSize=" + dirFileSize +
                '}';
    }
}

