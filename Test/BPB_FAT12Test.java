import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BPB_FAT12Test {

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
            FileHandle.randomWriteFile(raf, 0L, fatForamtData, fatForamtData.length);
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

}
