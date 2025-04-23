import java.io.*;
import java.util.HashSet;
import java.util.List;


public class Utilities {

    public static String Object = "Java";
    public static String postfix = ".java";

    public static String getCoFilePath(String str, String object) {

        // 起始索引
        int startIndex = str.indexOf(object);
        // 找到 ".*" 的结束索引
        int endIndex = str.indexOf(postfix);

        // 检查 "systems" 和 ".*" 是否都存在
        if (startIndex == -1 || endIndex == -1) {
            System.out.println("File not exist!");
            return "Invalid input: required substring not found";
        }

        // 计算结束索引，包括 ".*" 的长度
        endIndex += postfix.length(); // 如".java" 长度是 5

        // 返回从 startIndex 到 endIndex 的子串
        return str.substring(startIndex, endIndex);
    }

    // 读取文件路径
    public static String getFilePath(String str) {

        // 起始索引
//        int startIndex = str.indexOf(Object);
        int startIndex = str.indexOf("Eclipse");
        // 找到 ".*" 的结束索引
        int endIndex = str.indexOf(postfix);

        // 检查 "systems" 和 ".*" 是否都存在
        if (startIndex == -1 || endIndex == -1) {
            System.out.println("File not exist!");
            return "Invalid input: required substring not found";
        }

        // 计算结束索引，包括 ".*" 的长度
        endIndex += postfix.length(); // 如".java" 长度是 5
//        System.out.println(str.substring(startIndex, endIndex));
        // 返回从 startIndex 到 endIndex 的子串
        return str.substring(startIndex, endIndex);
    }
    // 获得pcid
    public static String getPcid(String str) {
        StringBuilder pcid = new StringBuilder();
        int t1 = str.indexOf("pcid") + 6;
        while (str.charAt(t1) != '\"') {
            pcid.append(str.charAt(t1));
            ++t1;
        }
        return pcid.toString();
    }
    // 获得起始行
    public static int getStartLine(String str) {
        StringBuilder strtmp = new StringBuilder();
        int t1 = str.indexOf("startline=") + 11;
        while (str.charAt(t1) != '\"') {
            strtmp.append(str.charAt(t1));
            ++t1;
        }
        return Integer.parseInt(strtmp.toString());
    }
    // 获得结束行
    public static int getEndLine(String str) {
        StringBuilder strtmp = new StringBuilder();
        int t1 = str.indexOf("endline=") + 9;
        while (str.charAt(t1) != '\"') {
            strtmp.append(str.charAt(t1));
            ++t1;
        }
        return Integer.parseInt(strtmp.toString());
    }
    // 比较1-4位置的共变克隆
    public static boolean judge1_4CCClone(String[] string1, String[] string2, String[] string3, String[] string4) {
        System.out.println("函数匹配成功！开始检测是否发生共变！");
        int changeflag1 = 0;
        for (int i = 0; i < string1.length; ++i) {
            if (string1[i] == null && string4[i] == null)
                break;
            if (string1[i] == null && string4[i] != null || string1[i] != null && string4[i] == null) {
                ++changeflag1;
                break;
            }
            if (!string1[i].trim().equals(string4[i].trim())) {
                ++changeflag1;
                break;
            }
        }

        int changeflag2 = 0;
        for (int i = 0; i < string2.length; ++i) {
            if (string2[i] == null || string3[i] == null)
                break;
            if (string2[i] == null && string3[i] != null || string2[i] != null && string3[i] == null) {
                changeflag2++;
                break;
            }
            if (!string2[i].trim().equals(string3[i].trim())) {
                ++changeflag2;
                break;
            }

        }
        return changeflag1 != 0 && changeflag2 != 0;
    }
    // 比较1-3位置的共变克隆
    public static boolean judge1_3CCClone(String[] string1, String[] string2, String[] string3, String[] string4) {
        System.out.println("函数匹配成功！开始检测是否发生共变！");
        int changeflag1 = 0;
        for (int i = 0; i < string1.length; ++i) {
            if (string1[i] == null && string3[i] == null)
                break;
            if (string1[i] == null && string3[i] != null || string1[i] != null && string3[i] == null) {
                ++changeflag1;
                break;
            }
            if (!string1[i].trim().equals(string3[i].trim())) {
                ++changeflag1;
                break;
            }
        }

        int changeflag2 = 0;
        for (int i = 0; i < string2.length; ++i) {
            if (string2[i] == null || string4[i] == null)
                break;
            if (string2[i] == null && string4[i] != null || string2[i] != null && string4[i] == null) {
                changeflag2++;
                break;
            }
            if (!string2[i].trim().equals(string4[i].trim())) {
                ++changeflag2;
                break;
            }
        }

        return changeflag1 != 0 && changeflag2 != 0;
    }
    // 打印时间
    public static void printTime(long l) {
        long minute, second;
        minute = l / 1000 / 60;
        second = l / 1000 % 60;
        System.out.println("运行时间为：" + minute + "分" + second + "秒");
    }

    // 获得一个克隆文件中的所有片段的总共的sloc
    public static int GetTotalSLOC(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        HashSet<String> set = new HashSet<>();

        String tmp;
        int sum = 0;
        int startline, endline;

        while ((tmp = bufferedReader.readLine()) != null) {
            if (!tmp.contains("startline") || !set.add(Utilities.getPcid(tmp)))
                continue;
            startline = tmp.indexOf("startline=") + 11;
            endline = tmp.indexOf("endline=") + 9;
            StringBuilder str1 = new StringBuilder();
            StringBuilder str2 = new StringBuilder();

            while ((tmp.charAt(startline)) != '"') {
                str1.append(tmp.charAt(startline));
                ++startline;
            }
            while (tmp.charAt(endline) != '"') {
                str2.append(tmp.charAt(endline));
                ++endline;
            }
            sum += Integer.parseInt(str2.toString()) - Integer.parseInt(str1.toString()) + 1;
        }
        bufferedReader.close();
        return sum;
    }

    public static int GetNiCadTotalCloneNum(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String tmp;
        int a, sum = 0;
        while ((tmp = bufferedReader.readLine()) != null) {
            a = tmp.indexOf("<clone nlines");
            if (a != -1)
                ++sum;
        }
        bufferedReader.close();
        return sum;
    }
    // 获得克隆文件，所有克隆对的数量;
    public static int GetTotalCloneNum(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String tmp;
        int sum = 0;
        while ((tmp = bufferedReader.readLine()) != null) {
            if (tmp.contains("<clonepair")) {
                sum++;
            }
        }
        bufferedReader.close();
        return sum;
    }

    // 仅仅执行一次传入的pwsh命令， 不要求返回值;
    public static void implCommand(List<String> command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 仅仅执行一次传入的命令， 不要求返回值，这是数组参数;
    public static void implCommand(String[] command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

