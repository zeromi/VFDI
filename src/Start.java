import constant.PromptWord;

import java.io.*;
import java.util.*;

public class Start {

    class Operator {
        final static int ADD = 0;
        final static int REMOVE = 1;
        int operaType;//操作类型,0添加，1删除
        String fileName;//操作对象

        public Operator(int operaType, String fileName) {
            this.operaType = operaType;
            this.fileName = fileName;
        }
    }

    private boolean selectFDFlag;//选择软盘标志

    private ArrayList<String> dirPath;//当前路径
    private LinkedList<Operator> tempDirPathOper = new LinkedList<>();//操作缓存

    private TreeMap<String, DirItem> nowDirTree;//当前目录树

    private DirItem nowDirItem;//临时当前目录项
    private DirItem tempDirItem;//临时下个目录项

    private HashMap<String, Integer> cmdMap;//命令组
    private TreeMap<String, DirItem> rootDirTree;//根目录树
    private ImgEdit imgEdit;

    private String imgFileFullPath;//imgFile文件路径

    public Start() {
        selectFDFlag = false;
        this.initCMD();
    }

    public void initData(String imgFileFullPath) throws Exception {
        this.imgFileFullPath = imgFileFullPath;
        imgEdit = new ImgEdit(imgFileFullPath);
        rootDirTree = imgEdit.getRootDirTree();

//        dirTree = rootDirTree;
        nowDirTree = rootDirTree;
        dirPath = new ArrayList<>();
        dirPath.clone();
    }

    public void initCMD() {
        cmdMap = new HashMap<>();
        cmdMap.put("exit", PromptWord.CMD_EXIT);
        cmdMap.put("ls", PromptWord.CMD_LS);
        cmdMap.put("cd", PromptWord.CMD_CD);
        cmdMap.put("out", PromptWord.CMD_OUT);
        cmdMap.put("select", PromptWord.CMD_SELECT);
        cmdMap.put("cf", PromptWord.CMD_CF);
        cmdMap.put("format", PromptWord.CMD_FORMAT);
    }

    public static void main(String[] args) {
        System.out.println("本程序目前仅支持查看和导出文件(不包括文件夹)，暂不支持写入和修改文件！");
        System.out.println("使用帮助：");
        System.out.println("中括号表示文件夹");
        System.out.println("exit:退出程序");
        System.out.println("cf:创建一个空的1.44M标准虚拟软盘文件\n\t格式: cf e:\\test.img\n\t需使用绝对路径");
        System.out.println("format:格式化虚拟软盘文件为FAT12\n\t格式: format e:\\test.img\n\t需使用绝对路径");
        System.out.println("select:选择一个标准1.44M虚拟软盘文件\n\t格式: select e:\\test.img\n\t需使用绝对路径");

        System.out.println("以下命令需使用select选择虚拟软盘文件后可用！！！");

        System.out.println("ls:显示当前文件列表");
        System.out.println("cd:改变目录,不支持绝对路径");
        System.out.println("out:输出文件到本地(不支持文件夹)，\n\t格式：out test.txt E:\\xxx\\\n\t或者out test.txt E:\\xxx\\xxx.txt\n\t注意目标路径仅可使用绝对路径");

        Scanner sc = new Scanner(System.in);
        Start sta = null;
        try {
            sta = new Start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("程序异常退出！");
            return;
        }
        boolean exitFlag = true;//false表示结束程序
        while (exitFlag) {
            sta.displayPath(sta.dirPath);
            System.out.print(">");
            exitFlag = sta.exeCmd(sc.nextLine());
        }
    }

    /**
     * @param cmd
     * @return 是否结束程序标志，false表示结束程序
     */
    public boolean exeCmd(String cmd) {
        String[] cmdParse = cmd.split(" ");
        String action = cmdParse[0];//操作指令
        int index;
        if (cmdMap.get(action) != null) {
            index = cmdMap.get(action);
        } else {
            index = -1;
        }
        if (selectFDFlag) {
            switch (index) {
                case PromptWord.CMD_LS:
                    //列出当前路径的目录下的文件列表
                    System.out.println("显示：文件列表！");
                    this.displayDirList(nowDirTree);
                    break;
                case PromptWord.CMD_CD:
                    //列出当前路径的目录下的文件列表
                    this.changeDiretory(cmdParse);
                    break;
                case PromptWord.CMD_OUT:
                    //将指定的文件内容输出到指定文件
                    System.out.println("开始输出文件！");
                    this.outFile(cmdParse);
                    break;
                case PromptWord.CMD_EXIT:
                    //结束程序
                    System.out.println("程序退出！");
                    return false;
                default:
                    System.out.println("Command not found!");
            }
        } else {
            switch (index) {
                case PromptWord.CMD_SELECT:
                    this.selectImgFile(cmdParse);
                    break;
                case PromptWord.CMD_CF:
                    this.createImgFile(cmdParse);
                    break;
                case PromptWord.CMD_FORMAT:
                    this.formatImgFile(cmdParse);
                    break;
                case PromptWord.CMD_EXIT:
                    //结束程序
                    System.out.println("程序退出！");
                    return false;
                default:
                    System.out.println("Command not found!");
            }
        }
        return true;
    }

    private void formatImgFile(String[] cmdParse) {
        if (cmdParse.length < 2) {
            return;
        }
        System.out.println("将格式化虚拟软盘文件为1.44M标准FAT12软盘");
        String fileName = cmdParse[1].trim();
        try {
            FileHandle.formatImgFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to format file!");
        }
    }

    private void selectImgFile(String[] cmdParse) {
        if (cmdParse.length < 2) {
            return;
        }
        if (selectFDFlag) {
            System.out.println("已选择镜像文件！");
            return;
        }
        String fileName = cmdParse[1].trim();
        try {
            this.initData(fileName);
            selectFDFlag = true;
            System.out.println("选择镜像文件，imgFilePath=" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("选择镜像文件失败!");
        }
    }

    private void createImgFile(String[] cmdParse) {
        if (cmdParse.length < 2) {
            return;
        }
        String fileName = cmdParse[1].trim();
        try {
            FileHandle.createNullImg(1440 * 1024, fileName);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to generate file!");
        }
        System.out.println("创建文件成功！");
    }

    private void outFile(String[] cmdParse) {
        if (cmdParse.length < 3) {
            return;
        }
        String fileName = cmdParse[1].trim();
        String aimFileName = cmdParse[2].trim();
        if (aimFileName.substring(aimFileName.length() - 1).equals("" + File.separator)) {
            aimFileName += fileName;
        }
        DirItem fileItem = nowDirTree.get(fileName);
        if (fileItem == null) {
            System.out.println("File not found!");
        } else if (fileItem.isDir()) {
            System.out.println("The directories cannot be copied!");
        } else {
//            int[] clusGroup = new int[0];
            try {
                int[] clusGroup = imgEdit.getFileClusGroup(fileItem);
                long fileLength = fileItem.getDirFileSize();
                if (fileLength <= 0) {
                    return;
                }
                byte[] fileContent = imgEdit.readFile(clusGroup, fileLength);
                File newFile = new File(aimFileName);
                if (newFile.exists()) {
                    System.out.println("The file alredy exists！");
                    return;
                } else {
                    FileHandle.genarateNewFile(fileContent, newFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void changeDiretory(String[] cmdParse) {
        /*
                 情况分析：
                 1. 单路径模式
                    cd ./
                    cd ../
                    cd test
                 2. 多路径模式
                    cd ./xx/xxx
                    cd ../xx/xxx
                    cd /xx/xx
        */
        if (cmdParse.length < 2) {
            return;
        }
        String path = cmdParse[1].trim();
        if (path == null || path.equals("")) {
            return;
        }
        System.out.println("进入该目录！");

        boolean pathCheck = true;//路径有效性检查，ture表示有效

        String[] pathSet = path.split("/");
        //处理单条或多条路径
        for (int i = 0; i < pathSet.length; i++) {
            if (nowDirTree.get(pathSet[i]) == null) {
                System.out.println("The directory not found!");
                pathCheck = false;
            } else {
                if (!pathSet[i].equals(".")) {//如果不是“.”，那么开始执行普通cd，否则不做操作
                    tempDirItem = nowDirTree.get(pathSet[i]);
                    if (this.subDirTreeInit(tempDirItem)) {
                        nowDirItem = tempDirItem;
                        if (pathSet[i].equals("..")) {
                            dirPath.remove(dirPath.size() - 1);
                            tempDirPathOper.push(new Operator(Operator.REMOVE, pathSet[i]));
                        } else {
                            dirPath.add(pathSet[i]);
                            tempDirPathOper.push(new Operator(Operator.ADD, pathSet[i]));
                        }
                    }
                }
            }
        }
        //path回滚
        if (!pathCheck) {
            System.out.println("path回滚");
            while (!tempDirPathOper.isEmpty()) {
                Operator op = tempDirPathOper.pop();
                if (op.operaType == Operator.ADD) {
                    dirPath.remove(dirPath.size() - 1);
                } else {
                    dirPath.add(op.fileName);
                }
            }
        } else {
            nowDirTree = nowDirItem.getSubDirTree();
        }
        tempDirPathOper.clear();
    }


    public void displayPath(ArrayList<String> dirPath) {
        if (!selectFDFlag) {
            return;
        }
        System.out.print("当前目录：/" + "imgfile");
        Iterator<String> iter = dirPath.iterator();
        while (iter.hasNext()) {
            System.out.print("/" + iter.next());
        }
        System.out.print("\n");
    }

    public void displayDirList(TreeMap<String, DirItem> dirTree) {
        Set<String> fileNameSet = dirTree.keySet();
        for (String fileName : fileNameSet) {
            if (dirTree.get(fileName).isDir()) {
                System.out.println("[" + fileName + "]");
            } else {
                System.out.println(fileName);
            }
        }
    }

    public boolean subDirTreeInit(DirItem dirItem) {
        if (!dirItem.isDir()) {
            System.out.println("it isn't a dir!!!");
            return false;
        }
        if (dirItem.getSubDirTree() == null) {//当目录还未初始化时
            try {
                setSubDirTree(dirItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void setSubDirTree(DirItem dirItem) throws Exception {
        //1.取得目录文件的簇组
        int[] clusGroup = imgEdit.getFileClusGroup(dirItem);
        //2.得到目录文件数据
        byte[] data = imgEdit.readFile(clusGroup, dirItem.getDirFileSize());
        //3.解析目录文件数据得到目录树
        TreeMap<String, DirItem> dirTree = imgEdit.getSubDirInfo(data, dirItem);
        //4.将得到的目录树与该DirItem对象绑定
        dirItem.setSubDirTree(dirTree);
    }

    public TreeMap<String, DirItem> getRootDirTree() {
        return rootDirTree;
    }
}
