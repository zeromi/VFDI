虚拟软盘读取工具计划：
功能规划：
1.可生成1.44M虚拟软盘文件
2.可将虚拟软盘格式化为FAT12格式
3.可读取虚拟软盘文件
4.可读取虚拟软盘目录和目录树
5.可将文件写入虚拟软盘和创建目录

2020-5-29,17:44
开始实现功能1 完成

2020-5-30,08:44
开始实现功能2 完成

2020-5-30,10:44
开始实现功能3
    1.首先读取扇区首部 完成

    2.获得每扇区字节数 BPB_BytsPerSec(FAT12中是512，后面出的硬盘单扇区为4096字节) 完成

        每簇扇区数 BPB_SecPerClus(1),
        Boot记录占用多少扇区 BPB_RsvdSecCnt(1);
        共有多少FAT表 BPB_NumFATs(2);
        根目录文件数最大值 BPB_RootEntCnt(224,0xe0)

        每FAT扇区数 BPB_FATSz16(0x09)
        隐藏扇区数 BPB_HiddSec(0)

    3.计算数据区位置和2号簇对应扇区 完成

        根目录所占扇区数=最大根目录文件数*32/每扇区字节数
        注意，在FAT12中：0e0h*32/512=14

        非用户数据区占用扇区数=隐藏扇区+保留扇区+FAT表数*FAT表所占扇区+根目录所占扇区
        注意，在FAT12中：0+1+2*9+14=33

        簇起始线性扇区=用户数据区起始扇区+(簇号-2)*每簇所占扇区-1
        例如：第2号簇的起始线性扇区=33+(2-2)*1-1=32，而第6号簇的起始线性扇区=33+(6-2)*1-1=36


    5.读取扇区根目录文件数据
        1.获取目录区数据 完成
        2.解析根目录项 完成
        目录项分为长文件名和短文件名，长文件名不可脱离短文件名存在，这种做法主要是为了兼容dos系统，
        因为dos（远古版本和旧版windows,目前高版本DOS已支持）仅支持短文件名(8.3格式)

            FAT32/16/12根据文件目录项判断文件名大小写的方法（仅限8.3命名规则）：
            文件目录项0CH字节的值：
            1. 此值为18H时，文件名和扩展名都小写。
            2. 此值为10H时，文件名大写而扩展名小写。
            3. 此值为08H时，文件名小写而扩展名大写。
            4. 此值为00H时，文件名和扩展名都大写。

            //文件属性
            00000000：普通文件，可随意读写
            00000001：只读文件，不可改写
            00000010：隐藏文件，浏览文件时隐藏列表
            00000100：系统文件，删除的时候会有提示
            00001000：卷标，作为磁盘的卷标识符
            00010000：目录文件，此文件是一个子目录，它的内容就是此目录下的所有文件目录项
            00100000：归档文件
        3.读取文件数据 完成
        4.读取完整目录树

