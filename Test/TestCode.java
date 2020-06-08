import org.junit.Test;

import java.io.File;
import java.util.Scanner;

/**
 * 单元测试用例
 */
public class TestCode {

    @Test
    public void testTemp() {
//        System.out.println("..");
        Scanner sc=new Scanner(System.in);
        String s=sc.nextLine();
        System.out.println(s);
        File file=new File(s);
//        File file=new File("E:\\20W");
        System.out.println(file.getAbsolutePath());

    }



    public static void main(String[] args) {

//        System.out.println("..");
        Scanner sc=new Scanner(System.in);
        String s1=sc.nextLine();
        File file1=new File(s1);
        System.out.println("s1.length="+s1.length());
        System.out.println("char[]="+transChar(s1));
        System.out.println("int[]="+transInt(s1));
        System.out.println("s1.path="+file1.getAbsolutePath());

        String s2=sc.nextLine();
        File file2=new File(s2);
        System.out.println("s2.length="+s2.length());
        System.out.println("char[]="+transChar(s2));
        System.out.println("int[]="+transInt(s2));
        System.out.println("s2.path="+file2.getAbsolutePath());
    }

    public static String transChar(String s){
        byte[] b=s.getBytes();
        char[] c=new char[b.length];
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<b.length;i++){
            c[i]=(char)b[i];
            sb.append(c[i]);
        }
        return sb.toString();
    }

    public static String transInt(String s){
        byte[] b=s.getBytes();
        int[] c=new int[b.length];
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<b.length;i++){
            c[i]=b[i]&0xff;
            sb.append(Integer.toHexString(c[i]));
        }
        return sb.toString();
    }

}
