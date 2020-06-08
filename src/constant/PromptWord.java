package constant;

public class PromptWord {
    public final static String READ_FILE_ERROR = "文件已存在！请更换名称或删除该文件！";
    public final static String FILE_NO_EXISTS_ERROR = "文件不存在！";
    public final static String TRANS_DATA_ERROR = "转换数据错误！";

    public final static String ERROR_2001 = "长文件名数据错误！";
    public final static String ERROR_2002 = "文件大小错误！";
    public final static String ERROR_2003 = "文件损坏！";

    public final static int CMD_LS = 0;//ls
    public final static int CMD_CD = 1;//cd
    public final static int CMD_EXIT = 2;//exit
    public final static int CMD_OUT = 3;//out，输出文件
    public static final int CMD_CF = 4;//create file，创建镜像文件
    public static final int CMD_SELECT = 5;//选择镜像文件
    public static final int CMD_FORMAT = 6;//格式化镜像文件

}
