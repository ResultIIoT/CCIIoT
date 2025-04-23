import org.apache.commons.collections4.iterators.UnmodifiableIterator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.Field;

import java.io.*;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Pattern;

public class FindBuggyCClone {
    public static void main(String[] args) throws Exception {
        String projectsDir = "/home/haosun/yao/tmp1/"; // 所有项目处理结果的目录，其中的每个文件夹都是一个项目;
        String gitRepo = "/home/haosun/yao/gitRepo"; // 每个项目的git仓库所在地;
        String Input = "/home/haosun/yao/gitRepo/datasets.com/Input"; // 共变克隆检测结果文件所存放的目录
        String Output = "/home/haosun/yao/gitRepo/datasets.com/Output"; // 共变结果文件所在的目录;
        String targetFile = "/home/haosun/yao/42target.txt"; // 格式化的文件，里面按空格分割，每一行是项目名 git链接 最新版本号 最远版本号
        String NiCadSystemsDir = "/home/haosun/yao/software/NiCad-6.2/systems1/"; // Nicad 对项目执行克隆检测的目录;
        String InputBC = "/home/haosun/yao/gitRepo/datasets.com/InputBC"; // 待放
        String InputPath = "/home/haosun/yao/gitRepo/datasets.com/sourcePath"; // 待放
        String CSVFile = "/home/haosun/yao/Halstead";
        String sourceCSVDir = "C:\\Users\\yao\\Desktop\\CK";
        String targetCSV = "C:\\Users\\yao\\Desktop\\CK.csv";
        // 1. 实现从格式化target.txt文件中自动提取commit区间的信息;
//        extractLogForProjects(projectsDir, gitRepo, targetFile);

//        // 2. 提取每个项目的最新的版本作为base版本，放入到Nicad/systems/下;
//            generateLatestProjects(targetFile, gitRepo, NiCadSystemsDir);
//
//        // 3. 提取commit 和bug-fixing commit,及分配一个id
//        // 每个项目的目录所在的目录; 自定义
//        calCommitNum(projectsDir);
//
//        // 4. 提取所有bug-fixing commit的diff结果到文件中;
//        extractAllDiffOfBugFixingCommitToFile(projectsDir, gitRepo);
////
//////       5. 从diff文件提取修改行;
//        CalAllChangedLinesToFile(projectsDir);
//
////         6. 保存所有项目的版本到Nicad/systems下; (这个方式被遗弃了)
//        saveAllBugFixingCommitDir(projectsDir, gitRepo, NiCadSystemsDir);
//
//        // 7. 对Nicad/system目录下的所有项目进行克隆共变检测;（被遗弃了）
          // 建议单独使用终端执行，否则一但远程连接终端，就中断了;
//        // 注意： 这里记得要提供Nicad/scripts.sh文件
//        Utilities.implCommand(new String[] {"bash", "-c", "cd " + NiCadSystemsDir + File.separator + ".." + "; ./scripts.sh"});
//
//        // 8. 调用共变检测函数，实施共变检测
//        // 先把Nicad结果文件移动到Input文件夹下;
//        Utilities.implCommand(new String[] {"bash", "-c", "cd " + NiCadSystemsDir + "; cp -r ./*functions-blind-clones " + Input});
//        FindCoChangeClone.run(Input, Output);
//
//         // 9. 提取buggy的共变克隆。
       extractAllBuggyCochangedClones(projectsDir, gitRepo, Output);

        // 10. 根据之前生成的共变结果文件，手动生成需要检测的共变文件，即name-ZZZ-999_functions-blind-clones,
        // 里面是自己手动生成的0.30和0.30-withsource.xml文件，用来与其它的bug-fixing commit检测共变克隆;
//        generateFilesForCClone(InputBC, Input);

        // 11. 直接使用git仓库，来检测代码克隆，不需要创建每个commit的副本了;（新的NiCad检测方式，没有副本！）。
//        oneFunc(NiCadSystemsDir, gitRepo, projectsDir, Input);

        // 12. 由于NiCad的检测方式不同，其文件路径不一定以systems开头，因此该函数把所有文件路径都替换为systems开头
        // 并把结果移到Input目录下。
//         func1(InputPath, Input);

        // 13. 让gitRepo中的项目都克隆好，并切换到对应的Base版本；
    //    returnOrigin(gitRepo, targetFile);
        // 14. // 遍历一个项目中所有的.py文件，并计算相应的halstead度量;
//        extractAllHalsteadMetric(gitRepo, CSVFile);

        // 15. 提取Understand里面生成的metric的文件，并求和其中的度量。 这个是用来求CK和圈复杂度等函数。
        // extractMetricFromCSV(sourceCSVDir, targetCSV);
    }

    // 1. 实现一个小功能， 自动提取 一个commit区间中的所有commit信息;
    // 给定一个格式化的文件 "target.txt" ，里面的每一行都是 项目名 git克隆链接 最新版本号 最远版本号;
    // 然后clone 项目，克隆在gitRepo目录下：
    // 在projectsDir目录下创建一个项目名的文件夹，在该目录下创建name_log.txt文件
    public static void extractLogForProjects(String projectsDir, String gitRepo, String targetFile) throws IOException {
        File target = new File(targetFile);
        BufferedReader targetReader = new BufferedReader(new FileReader(target));
        String line;
        while ((line = targetReader.readLine()) != null) { // 读取每一行;
            String[] info = line.split(" ");
            String name = info[0], gitLink = info[1], latestVersion = info[2], olderVersion = info[3];
            File dir = new File(projectsDir + File.separator + name); // 存储每个项目的目录;
            if (!dir.exists()) dir.mkdir(); // 创建这个文件夹;

            // 在gitRepo目录下克隆该项目, 如果已存在该项目，不会执行克隆;
            String[] clone = {"bash", "-c", "cd " + gitRepo + ";git clone " + gitLink};
            Utilities.implCommand(clone);

            // 来到gitRepo目录下， 执行git log命令;
            String[] gitLog = {"bash", "-c", "cd " + gitRepo + File.separator + name + "; git log " + olderVersion + ".." + latestVersion + " > " + dir.getAbsolutePath() + File.separator + name + "_log.txt"};
            Utilities.implCommand(gitLog);

        }
        targetReader.close();
    }
    // 2. 提取每个项目的最新的版本作为base版本，放入到Nicad/systems/下;
    public static void generateLatestProjects(String targetFile, String gitRepo, String NiCadSystemsDir) throws IOException {
        BufferedReader targetReader = new BufferedReader(new FileReader(targetFile)); // 读取每一行文件;
        String line;
        while ((line = targetReader.readLine()) != null) {
            String[] str = line.split(" ");
            String name = str[0], gitLink = str[1], latestVersion = str[2], olderVersion = str[3];

            // 把最新版本的项目复制一份到NiCadSystemsDir下;
            String[] command1 = {"bash", "-c", "cd " + gitRepo + File.separator + name + "; git checkout " + latestVersion + "; cp -r ../" + name + " " + NiCadSystemsDir};
            Utilities.implCommand(command1);
            // 改名一下：
            String[] command2 = {"bash", "-c", "cd " + NiCadSystemsDir + "; mv ./" + name + " ./" + name + "-ZZZ-999"};
            Utilities.implCommand(command2);
        }
        targetReader.close();
    }




    // 功能3： 1. 计算 'git log commit1..commit2'产生的commit信息文件中所有的commit数量
    // 2. 计算其中bug-fixing commit的数量
    // 3. 提取出bug-fixing 的所有commit id;
    public static void calCommitNum(String projectsDir) throws IOException {
        // 用于bug-fixing commit的关键字匹配
        String[] keyWords = new String[]{"bug", "fix", "wrong", "error", "fail", "problem", "patch"};

        File[] Dir = new File(projectsDir).listFiles(); // 项目目录所在目录;
        assert Dir != null;
        Arrays.sort(Dir, Comparator.comparing(File::getName));
        for (File project : Dir) { // 每次取一个项目目录
            String name = project.getName(); // 项目名
            // 找到项目文件夹中的目标log文件;
            File target = null;
            for (File file : Objects.requireNonNull(project.listFiles())) {
                if (file.getName().contains("_log.txt")) {
                    target = file;
                    break;
                }
            }

            // 把所有的bug-fixing ID都写入以commitIds结尾的文件中;
            BufferedWriter bW = new BufferedWriter(new FileWriter(project.getAbsolutePath() + File.separator + name + "_commitIds.txt"));
            int k = 0;

            // 开始读取文件，看文件中有多少commit和bug-fixing commit
            int totalCommit = 0, bugCommit = 0;
            assert target != null;
            BufferedReader bR = new BufferedReader(new FileReader(target)); // 读取这个文件;
            String temp = bR.readLine();
            while (temp != null) {
                // 找到一个commit的位置
                while (temp != null && !checkIfIsCommit(temp)) temp = bR.readLine();
                if (temp == null) break; // 到达末尾;

                String commitId = temp; // 保存commit的信息
                // 再次确认是否是需要的commit
                String Author = bR.readLine();
                if (!Author.startsWith("Author")) continue;
                String Date = bR.readLine();
                if (!Date.startsWith("Date")) continue;

                // 找到了需要的commit hunk
                ++totalCommit; // 找到了一个commit;
                temp = bR.readLine(); // 开始读取内容
                boolean bugFlag = false; // 只需要找一个关键词匹配到了就行。
                while (temp != null && !checkIfIsCommit(temp)) {
                    if (!bugFlag) {
                        temp = temp.toLowerCase();
                        if (checkIfKeyWordsMatch(temp, keyWords)) {
                            bW.write(commitId.split(" ")[1] + " " + k++ + "\n");
                            bugFlag = true;
                            bugCommit++;
                        }
                    }
                    temp = bR.readLine();
                }
            }
            bW.close();
            bR.close();
            // 打印结果出来;
            System.out.println("commit总数: " + totalCommit + ", bugfix数量: " + bugCommit + "    " + name);
        }
    }

    // 检查commit的格式是否是 commit + 空格 + 40位hash值
    public static boolean checkIfIsCommit(String str) {
        String regex = "commit\\s[0-9a-f]{40}";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(str).find() && str.startsWith("commit");
    }

    // 检查关键字是否匹配;
    public static boolean checkIfKeyWordsMatch(String str, String[] keyWords) {
        for (String keyWord : keyWords) {
            if (Pattern.compile(keyWord).matcher(str).find()) return true;
        }
        return false;
    }


    // 功能4： 利用每个项目的所有bug-fixing Id，提取所有的bug_fixing commit的diff结果存到文件中;
    public static void extractAllDiffOfBugFixingCommitToFile(String projectsDir, String gitRepoPath) throws IOException {
        File[] projects = new File(projectsDir).listFiles();
        assert projects != null;
        for (File project : projects) { // 针对每一个项目，把所有bug-fixing commit的diff结果存到不同的文件;
            String name = project.getName();
            // 找到项目文件夹中的以_commitIds结尾的文件;
            File target = null;
            for (File file : Objects.requireNonNull(project.listFiles())) {
                if (file.getName().contains("_commitIds")) {
                    target = file;
                    break;
                }
            }

            // 读取每个commitId，然后对这个commit使用 "git diff commitId commitId^"
            // 求得这个修改行的信息;
            assert target != null;
            BufferedReader bR = new BufferedReader(new FileReader(target));
            String line;
            while ((line = bR.readLine()) != null) {
                // 拿到commit;
                String commitId = line.split(" ")[0];
                int index = Integer.parseInt(line.split(" ")[1]); // 第几个bug-fixing

                // 开始执行命令行命令，将每个bug-fixing commit的 diff内容求出，并得到一个bug_fixing_commit_index.txt文件;：
                List<String> command = new ArrayList<>(); // 执行的命令;

                command.add("bash");
                command.add("-c");
                command.add("cd " + gitRepoPath + File.separator + name + "; git diff " + commitId + " " + commitId + "^");
                implLongCommand(command, index, target); // 执行命令，将每个commitId对应的diff结果存到文件中;
            }
            bR.close();
        }
    }

    // 执行一个长命令，把命令结果输出到target文件所在目录的新文件中，文件的名字格式为 "bug_fixing_commit_" + index;
    public static void implLongCommand(List<String> command, int index, File target) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new FileWriter(target.getParent() + File.separator + "bug_fixing_commit_" + index + ".txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }

            process.waitFor();
            reader.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 功能5. 从diff文件中提取修改行到目标文件中;
    public static void CalAllChangedLinesToFile(String projectDir) throws IOException {
        File[] projects = new File(projectDir).listFiles();

        // 开始读取每个项目的bug_fixing_commit文件，对每一个diff hunk， 求出它的文件所在位置，求出它的修改的行号;
        assert projects != null;
        for (File project : projects) { // 针对每一个项目，提取每个bug-fixing的修改行，存到对应的文件中;
            for (File file : Objects.requireNonNull(project.listFiles())) {
                String name = file.getName();
                if (!name.startsWith("bug_fixing_commit")) continue; // 只考虑每一个buf-fixing文件;
                int index = Integer.parseInt(name.substring(name.lastIndexOf("_") + 1, name.lastIndexOf(".txt"))); // 当前commit的序号

                BufferedReader reader = new BufferedReader(new FileReader(file)); // 读取bug-fixing文件;
                // 目标结果文件，保存新增行的结果;
                BufferedWriter addWriter = new BufferedWriter(new FileWriter(project.getAbsolutePath() + File.separator + "ChangedLines_commit_Add_" + index + ".txt"));
                // 目标结果文件，保存删除行的结果;
                BufferedWriter minusWriter = new BufferedWriter(new FileWriter(project.getAbsolutePath() + File.separator + "ChangedLines_commit_Minus_" + index + ".txt"));

                String line = reader.readLine();
                while (line != null) { // 读取文件的每一行;
                    // 处理每一个diff，一个diff就是一个文件内的变化，因此保存文件的路径，以及这个文件中发生修改的这些行号。
                    if (line.startsWith("diff --git")) {
                        String[] str = line.split(" ");
                        String minusFilePath = project.getName() + "/" + str[2].substring(str[2].indexOf("/") + 1); // 被比较文件的路径;
                        String addFilePath = project.getName() + "/" + str[3].substring(str[3].indexOf("/") + 1); // 比较文件的路径;
                        minusWriter.write("***** " + minusFilePath + " *****\n"); //每一diff都是一个文件内的变化;
                        addWriter.write("***** " + addFilePath + " *****\n"); // 每一个diff都是一个文件内的变化;
                        // 保存发生修改的行号，是离散的行号;一个diff都是一个文件内的修改行号;
                        StringBuilder msb = new StringBuilder(); // 被比较的 修改行
                        StringBuilder asb = new StringBuilder(); // 比较的 修改行

                        line = reader.readLine(); // 读到diff的第二行
                        // 只考虑同时修改，仅删除，仅新加的情况，像什么文件更名，文件的权限更改的请求，都不考虑。
                        if (line.startsWith("index") || line.startsWith("deleted") || line.startsWith("new")) {
                            // 提取所有的hunk块
                            while (true) {
                                while (line != null && !line.startsWith("diff --git") && !line.startsWith("@@"))
                                    line = reader.readLine();
                                if (line == null || line.startsWith("diff --git")) break; // 提前结束
                                // 找到了一对hunk块;
                                // 提取被比较文件的起始行，和比较文件的起始行;
                                int[] startsLine = analyzeHunk(line); // startLines[0]，是被比较文件的起始行，[1]是比较文件的起始行;
                                int mK = startsLine[0] - 1, aK = startsLine[1] - 1;
                                line = reader.readLine(); // 开始读取hunk的下每一个内容
                                while (line != null && !line.startsWith("diff --git") && !line.startsWith("@@")) {
                                    if (line.startsWith(" ")) {
                                        mK++;
                                        aK++;
                                    } else if (line.startsWith("-")) {
                                        mK++;
                                        msb.append(mK).append(" ");
                                    } else if (line.startsWith("+")) {
                                        aK++;
                                        asb.append(aK).append(" ");
                                    }
                                    line = reader.readLine(); // 一直遍历当前hunk块;
                                }
                                if (line == null || line.startsWith("diff --git")) break;
                            }
                            // 删除最后一个空格
                            if (msb.length() != 0) msb.deleteCharAt(msb.length() - 1);
                            minusWriter.write(msb.toString());
                            minusWriter.newLine();
                            minusWriter.newLine();
                            minusWriter.newLine();
                            // 删除最后一个空格
                            if (asb.length() != 0) asb.deleteCharAt(asb.length() - 1);
                            addWriter.write(asb.toString());
                            addWriter.newLine();
                            addWriter.newLine();
                            addWriter.newLine();

                        }
                        minusWriter.newLine();
                        minusWriter.newLine();
                        addWriter.newLine();
                        addWriter.newLine();
                    } else {
                        line = reader.readLine();
                    }
                }
                // 一个bug-fixing commit的文件读取结束，关闭所有流;
                addWriter.close();
                minusWriter.close();
                reader.close();
            }
        }
    }

    // 提取每一个的hunk "@@...@@"的起始行;
    public static int[] analyzeHunk(String hunk) {
        // 先求被比较的起始行号;
        int i = hunk.indexOf("-");
        int j;
        for (j = i + 1; j < hunk.length(); ++j) {
            if (!Character.isDigit(hunk.charAt(j))) break;
        }
        int minusK = Integer.parseInt(hunk.substring(i + 1, j)); // 求得被比较的起始行号;

        i = hunk.indexOf("+");
        for (j = i + 1; j < hunk.length(); ++j) {
            if (!Character.isDigit(hunk.charAt(j))) break;
        }
        int addK = Integer.parseInt(hunk.substring(i + 1, j));
        return new int[]{minusK, addK};
    }


    // 功能6： 到每个项目的git仓库中，切换到每一个bug-fixing commit版本以及commit^版本， 并将当前目录的所有文件都复制一份到
    // Nicad/systems目录下即可; 生成的目录名称格式为 name-Add-index， 名字-比较版本(记为Add)-第几个bug-fixing commit的序号
    // 比较版本（bug-fixing commit）记为Add， 被比较版本(bug-fixing commit^)记为Minus
    public static void saveAllBugFixingCommitDir(String projectsDir, String gitRepo, String NiCadSystemsDir) throws IOException {
        File projectDir = new File(projectsDir); // 项目所在的目录;
        for (File project : Objects.requireNonNull(projectDir.listFiles())) { // 遍历每一个项目的目录;
            String name = project.getName(); // 项目的名称;

            // 找到保存所有commitId的文件;
            File commitIdsFile = null;
            for (File file : Objects.requireNonNull(project.listFiles())) {
                if (!file.getName().endsWith("commitIds.txt")) continue;
                commitIdsFile = file;
                break;
            }

            // 开始读取每一个commitId
            assert commitIdsFile != null;
            BufferedReader reader = new BufferedReader(new FileReader(commitIdsFile));
            String line;
            while ((line = reader.readLine()) != null) { // 读取每一个;
                String[] arr = line.split(" ");
                String commit = arr[0]; // commitId的值
                int index = Integer.parseInt(arr[1]); // 第几个id;
                System.out.println("正在处理项目 " + name + "的第 " + index + " 个commit, 它为：  " + commit);
                // 拿到Id, 开始去git仓库里面切换版本，并保存版本文件;
                // 第一次执行命令，拿到commitId的版本文件；
                String[] command1 = {"bash", "-c", "cd " + gitRepo + "/" + name + ";" + "git switch --detach " + commit + ";" + "cp -r ../" + name + " " + NiCadSystemsDir + ";" + "mv " + NiCadSystemsDir + "/" + name + " " + NiCadSystemsDir + "/" + name + "-Add-" + index};
                Utilities.implCommand(command1);
                // 第二次执行命令，拿到commitId^的版本文件;
                String[] command2 = {"bash", "-c", "cd " + gitRepo + "/" + name + ";" + "git switch --detach " + commit + "^;" + "cp -r ../" + name + " " + NiCadSystemsDir + ";" + "mv " + NiCadSystemsDir + "/" + name + " " + NiCadSystemsDir + "/" + name + "-Minus-" + index};
                Utilities.implCommand(command2);
            }
            reader.close();
        }
    }



    // 功能7： 实现共变克隆的共变检测的问题， 并记录每个bug-fixing中，有多少commit是与共变克隆相关的；
    public static void extractAllBuggyCochangedClones(String projectsDir, String gitRepo, String Output) throws IOException {
        File[] projectsList = new File(Output).listFiles(); // 共变结果文件的列表;
        assert projectsList != null;
        Arrays.sort(projectsList, Comparator.comparing(File::getName)); // 排序;

        // 取每一个项目进行讨论
        for (File outPutProject : projectsList) {
            // 取得项目的名称
            String fullNameBase = outPutProject.getName(); // （包含-ZZZ），同时该文件名就是该项目的base版本名
            String name = fullNameBase.substring(0, fullNameBase.indexOf("-ZZZ")); // 纯项目名字;（不包含-ZZZ, -Add, -Minus）;

            Set<Integer> commitNumSet = new HashSet<>(); // 对bug-fixing进行去重;
            Set<Integer> commitRTCCloneNumSet = new HashSet<>(); // 对buggy共变克隆数量去重;
            for (File aFile : Objects.requireNonNull(outPutProject.listFiles())) { // 考虑项目的每个A文件,一对AB项目，就是一个具体版本项目的克隆的映射;
                // 找到一个A文件，即保存发生共变的base版本中的克隆的文件;
                // 一个A文件就是一个被比较版本文件， 其实也是一个bug-fixing commit/commit^ 版本;
                if (aFile.getName().endsWith("-A.txt")) {

                    // base文件就是fullName文件， 现在取得被比较的文件;
                    String fileName = aFile.getName();
                    // 取得被比较文件的全名;
                    String fullNameCompare = fileName.substring(fileName.indexOf("___") + 3, fileName.lastIndexOf("-A.txt"));

                    // 现在去取对应的B文件，即保存发生共变的被比较版本中的克隆
                    File bFile = null;
                    for (File file : Objects.requireNonNull(outPutProject.listFiles())) {
                        if (file.getName().contains(fullNameCompare + "-B.txt")) {
                            bFile = file;
                            break;
                        }
                    }

                    // 现在去取对应的changedFile文件; 根据纯项目名确定对应的位置; changedFile位于tmp目录下;
                    File tmpProjectDir = new File(projectsDir + File.separator + name);
                    // 找到对应的changedFile文件;
                    // 所以先拿标记，bug-fixing commit 为 Add， bug-fixing commit^ 为 Minus
                    // 还有index, 表示第一个bug-fixing
                    int index = Integer.parseInt(fullNameCompare.substring(fullNameCompare.lastIndexOf("-") + 1));
                    // 获得标记;
                    String sign;
                    if (fullNameCompare.contains("-Add-")) {
                        sign = "Add";
                    } else {
                        sign = "Minus";
                    }

                    // 考虑所有的bug-fixing commit, 不考虑重复的index;
                    commitNumSet.add(index);

                    // 获得修改行中的那个文件，ChangedFile, 保存当前被比较版本项目中所有的修改行;
                    File changedFile = null;
                    for (File file : Objects.requireNonNull(tmpProjectDir.listFiles())) { // 遍历tmp/project目录下的每个文件;
                        if (file.getName().endsWith("_" + sign + "_" + index + ".txt")) {
                            changedFile = file;
                            break;
                        }
                    }

                    // 保存具有bug倾向的base版本的共变克隆, 一个项目版本一个文件。
                    BufferedWriter changedWriter = new BufferedWriter(new FileWriter(outPutProject.getAbsolutePath() + File.separator + fullNameBase + "___" + fullNameCompare + "-BuggyCClone.txt"));
                    // 读取修改行的每个修改的文件路径，并记录发生的修改行;
                    assert changedFile != null;
                    BufferedReader changedFileReader = new BufferedReader(new FileReader(changedFile));
                    String line;
                    // 只要一次bug-fixing中任意一修改行和CClone相关，我们就认为这次bug-fixing为与CClone相关的bug-fixing;
                    while ((line = changedFileReader.readLine()) != null) {
                        if (line.startsWith("***** ")) { // 读取每一个文件路径;
                            TreeSet<Integer> changedLinesSet = new TreeSet<>(); // 该路径下发生的修改行记录到有序集合中;
                            // 首先提取发生的路径是什么; 该求得的路径不包含纯文件名，默认是在对应文件的根目录下;
                            String path = line.substring(line.indexOf("/") + 1, line.lastIndexOf(" "));
                            changedWriter.write("以下路径有---buggy共变克隆--- " + name + File.separator + path + "\n");
                            // 提取发生的修改行;
                            line = changedFileReader.readLine();
                            for (String s : line.split(" ")) {
                                if (!s.equals("")) {
                                    changedLinesSet.add(Integer.parseInt(s));
                                }
                            }
                            // 去bFile中，找是否有对应的路径，并发生了修改; 要求找出每一个路径发生修改的克隆对;
                            assert bFile != null;
                            BufferedReader bFileReader = new BufferedReader(new FileReader(bFile));
                            String bLine;
                            while ((bLine = bFileReader.readLine()) != null) {
                                if (bLine.startsWith("<clonepair")) {
                                    // 读取每一对克隆对，看是否有路径匹配;
                                    String clonepair = bLine; // 连续保存三条记录;
                                    String firstFile = bFileReader.readLine();
                                    String nextFile = bFileReader.readLine();

                                    // 开始判断是否两个克隆片段中是否任意一克隆片段涉及到修改，如果是，则这个克隆片段有bug倾向
                                    // 然后就能把对应的A文件的对应的克隆对，存入文件中；
                                    boolean isChanged = checkIfIsChanged(changedLinesSet, path, firstFile) || checkIfIsChanged(changedLinesSet, path, nextFile);
                                    // 如果这一对克隆对发生了修改，那就去对应的A文件，找到这对克隆对应的克隆。
                                    if (isChanged) { // 发生了修改，找到了发生修改的共变克隆;
                                        commitRTCCloneNumSet.add(index);
                                        BufferedReader aFileReader = new BufferedReader(new FileReader(aFile));
                                        String aLine;
                                        while ((aLine = aFileReader.readLine()) != null) {
                                            if (aLine.contains(clonepair)) { // 找到了a(即base)文件中的对应的克隆;
                                                // 先把base的克隆放在前头，compare的放在后头;
                                                changedWriter.write(aFileReader.readLine() + "\n");
                                                changedWriter.write(aFileReader.readLine() + "\n");
                                                changedWriter.write(firstFile + "\n");
                                                changedWriter.write(nextFile + "\n");
                                                changedWriter.newLine();
                                                changedWriter.newLine();
                                                changedWriter.newLine();
                                                break;
                                            }
                                        }
                                        aFileReader.close();
                                    }
                                }
                            }
                            changedWriter.newLine();
                            bFileReader.close();
                        }
                    }
                    changedWriter.close();
                    changedFileReader.close();
                }
            }


            // 下面开始对base版本的buggy共变克隆进行去重； 这里采用的方法和对共变克隆去重的方式是相同，可能不太准确！！！
            int num = 0; // 计算不重复的buggy共变的数量;
            Set<String> set = new HashSet<>(); // 对当前项目进行去重;
            for (File file : Objects.requireNonNull(outPutProject.listFiles())) { // 遍历这些文件;
                if (file.getName().contains("-BuggyCClone.txt")) { // 目标文件;
                    BufferedReader reader = new BufferedReader(new FileReader(file)); // 读取这个文件;
                    // 因为一个项目的不同克隆对的pcid是不同的，因此只需要根据pcid去重即可：
                    String line = reader.readLine();
                    while (line != null) {
                        if ((line.contains("---buggy"))) { // 目标路径;
                            // 读当前路径下的所有buggy对;
                            line = reader.readLine();
                            while (line != null && !line.contains("---buggy")) {
                                if (line.startsWith("<source file=")) {
                                    String pcid1 = Utilities.getPcid(line);
                                    String pcid2 = Utilities.getPcid((line = reader.readLine()));
                                    line = reader.readLine();
                                    line = reader.readLine();
                                    line = reader.readLine();

                                    boolean t1 = set.add(pcid1 + pcid2);
                                    boolean t2 = set.add(pcid2 + pcid1);
                                    if (t1 && t2) {
                                        ++num;
                                    }
                                    // 标准的写法
//                                    int hashValue = pcid1.hashCode() ^ pcid2.hashCode();
//                                    if (set.add(hashValue)) {
//                                        num++;
//                                    }
                                }
                                line = reader.readLine();
                            }
                        } else {
                            line = reader.readLine();
                        }
                    }
                    reader.close();
                }
            }

            // 把最终结果写入大FinalResult文件中, 首先拿到这个文件。
            File FinalResult = null;
            for (File file : Objects.requireNonNull(outPutProject.listFiles())) {
                if (file.getName().contains("FinalResult")) {
                    FinalResult = file;
                    break;
                }
            }

            // 把结果写入
            assert FinalResult != null;
            BufferedWriter writer = new BufferedWriter(new FileWriter(FinalResult, true));
            writer.newLine();
            writer.newLine();

            writer.write("--------去除掉重复的buggy共变克隆，还有" + num +"对--------------\n\n");
            writer.write("--------当前项目总共处理了" + commitNumSet.size() + "个bugfixing, 其中有" + commitRTCCloneNumSet.size()+ "个是跟CClone相关的");


            writer.close();
        }
    }

    private static boolean checkIfIsChanged(TreeSet<Integer> set, String path, String file) {
        boolean flag = false;
        if (file.contains(path)) {
            int startLine = Utilities.getStartLine(file);
            int endLine = Utilities.getEndLine(file);
            // 判断是否有修改行在区间内：
            Integer ceiling = set.ceiling(startLine);
            if (ceiling != null && ceiling <= endLine) {
                flag = true;
            }
        }
        return flag;
    }

    // 10. 根据之前的共变结果，产生新的共变文件。
    // 构造我们需要的 name_functions-blind-clones-0.30.xml 和 name_functions-blind-clones-0.30-classes-withsource.xml
    // 1. 遍历 InputBC 中的每一个项目;
    // 2. 在 Input 中生成类似 name-ZZZ-999_functions-blind-clones的文件夹;
    // 3. 在上述文件夹中，创建0.30.xml文件 和 withsource文件;
    // 4. 取得 name_noduplicated.txt 文件;
    // 5. 获得文件中的每一对克隆放入0.30.xml文件中;
    // 6. 再次取得name_noduplicated.txt, 每取一对克隆就把对应的源码放入到withsource文件夹下;
    public static void generateFilesForCClone(String InputBC, String Input) throws IOException {
        File InputBCFile = new File(InputBC);
        for (File project : Objects.requireNonNull(InputBCFile.listFiles())) { // 遍历每个项目;
            System.out.println("正在处理项目" + project.getName());
            // 获得项目的纯名字;
            String fullName = project.getName();
            String name = fullName.substring(0, fullName.lastIndexOf("-"));
            String targetName = name + "-ZZZ-999"; // 改名
            // 在 Input中创建 name-ZZZ-999_functions-blind-clones即对应的文件;
            File NiCadDir = new File(Input + File.separator + targetName + "_functions-blind-clones");
            NiCadDir.mkdir();
            BufferedWriter file030 = new BufferedWriter(new FileWriter(NiCadDir.getAbsolutePath() + File.separator + NiCadDir.getName() + "-0.30.xml"));
            BufferedWriter file030WithSource = new BufferedWriter(new FileWriter(NiCadDir.getAbsolutePath() + File.separator + NiCadDir.getName() + "-0.30-classes-withsource.xml"));

            // 获得noduplicated文件;
            File noduplicatedFile = null;
            for (File file : Objects.requireNonNull(project.listFiles())) {
                if (file.getName().contains("noduplicated.txt")) {
                    noduplicatedFile = file;
                    break;
                }
            }
            System.out.println("获得noduplicated文件");
            assert noduplicatedFile != null;
            BufferedReader noduplicatedReader = new BufferedReader(new FileReader(noduplicatedFile));
            // 先把030文件中的头部写进入;
            file030.write("<clones>\n");
            file030.write("<systeminfo processor=\"nicad6\" system=\"" + targetName + "\" granularity=\"functions-blind\" threshold=\"30%\" minlines=\"10\" maxlines=\"2500\"/>\n");
            file030.write("<cloneinfo npcs=\"9999\" npairs=\"9999\"/>\n");
            file030.write("<runinfo ncompares=\"9999\" cputime=\"9999\"/>\n\n");
            // 然后读取所有的克隆对放入030文件;
            String line;
            while ((line = noduplicatedReader.readLine()) != null) {
                if (line.startsWith("<clonepair")) { // 找到一对克隆;
                    file030.write("<clone nlines=\"999\" similarity=\"999\">\n");
                    line = noduplicatedReader.readLine();
                    file030.write( line.replace(fullName, targetName) + "\n");
                    line = noduplicatedReader.readLine();
                    file030.write(line.replace(fullName, targetName)+ "\n");
                    file030.write("</clone>\n\n");
                }
            }
            file030.write("</clones>");
            noduplicatedReader.close();
            file030.close();
            System.out.println("克隆对存储完毕;");

            // 开始读取所有的源码文件，放入withsource文件下;
            // 首先写入头;
            file030WithSource.write("<clones>\n");
            file030WithSource.write("<systeminfo processor=\"nicad6\" system=\"" + targetName + "\" granularity=\"functions-blind\" threshold=\"30%\" minlines=\"10\" maxlines=\"2500\"/>\n");
            file030WithSource.write("<cloneinfo npcs=\"9999\" npairs=\"9999\"/>\n");
            file030WithSource.write("<runinfo ncompares=\"9999\" cputime=\"9999\"/>\n");
            file030WithSource.write("<classinfo nclasses=\"999\"/>\n\n");
            // 新开一个noduplicatedReader;
            noduplicatedReader = new BufferedReader(new FileReader(noduplicatedFile));
            String currFile = null; // 获得对应的比较信息;
            line = noduplicatedReader.readLine();
            System.out.println("开始读取源码");
            int index = 0; // 记录目前已经到哪一个对比文件了;
            while (line != null) {
                if (line.startsWith("以下是")) { // 定位到一个共变文件;
                    System.out.println("开始读取第" + index++ + "文件");
                    currFile = line.substring(3, line.lastIndexOf("-A.txt"));
                    // 读取该文件下对应的共变克隆对源码;
                    line = noduplicatedReader.readLine();
                    int i1 = 0;
                    while (line != null && !line.startsWith("以下是")) {
                        if (line.startsWith("<clonepair")) { // 读取一对克隆对，写入一对源码;
                            System.out.println("开始读取第" + index + "文件的第" + i1++ + "克隆");
                            String pair = line; // 是对应的第几对;

                            // 读取对应的源码文件;
                            BufferedReader AWithCodeFile = new BufferedReader(new FileReader(project.getAbsolutePath() + File.separator + currFile + "-A-withcode.txt"));
                            String line1;
                            while ((line1 = AWithCodeFile.readLine()) != null) {
                                // 把两对源码全部录入文件;
                                if (line1.startsWith(pair)) {
                                    line1 = AWithCodeFile.readLine();
                                    String t1 = line1.replace(fullName, targetName);
                                    String t2 = t1.substring(0, t1.lastIndexOf("</source>"));
                                    file030WithSource.write(t2 + "\n");
                                    while (!(line1 = AWithCodeFile.readLine()).startsWith("</clonepair")) {
                                        file030WithSource.write(line1 + "\n");
                                    }
                                    file030WithSource.write("</source>\n\n");
                                }
                            }
                            AWithCodeFile.close();
                            line = noduplicatedReader.readLine();
                        } else {
                            line = noduplicatedReader.readLine();
                        }
                    }
                } else {
                    line = noduplicatedReader.readLine();
                }
            }
            System.out.println("源码读取结束");
            file030WithSource.write("</clones>");
            noduplicatedReader.close();
            file030WithSource.close();
        }
    }

    // 11. 一次性完成项目所有的克隆检测， 不需要创建太多副本;
    // 实现功能：检测所有的bug-fixing commit 与 对应的 commit^的代码克隆，并将检测结果文件复制一份到Input目录下;
    // 缺点： gitRepo目录下，会产生 NiCad的检测文件;
    public static void oneFunc(String NiCadSystemsDir, String gitRepo, String projectsDir, String Input) throws IOException {
        File[] projectsList = new File(projectsDir).listFiles();
        assert projectsList != null;
        Arrays.sort(projectsList, Comparator.comparing(File::getName));
        for (File project : Objects.requireNonNull(projectsList)) { // 遍历所有项目;
            String name = project.getName(); // 获得项目的名称;

            File commitIdsFile = null; // 找到存储所有bug-fixing 所在的文件;
            for (File file : Objects.requireNonNull(project.listFiles())) {
                if (file.getName().endsWith("commitIds.txt")) {
                    commitIdsFile = file;
                    break;
                }
            }

            // 对每一个commit Id进行克隆检测
            assert commitIdsFile != null;
            BufferedReader commitIdsReader = new BufferedReader(new FileReader(commitIdsFile));
            String line;
            while ((line = commitIdsReader.readLine()) != null) { // 读取每一个bug-fixing commit；
                String[] arr = line.split(" ");
                String commitId = arr[0]; // commit
                int index = Integer.parseInt(arr[1]); // bug-fixing commit的序号;
                String name_Add = name + "-Add-" + index;
                String name_Minus = name + "-Minus-" + index;

                executeSomeLinuxCommand(gitRepo, NiCadSystemsDir, name, name_Add, commitId, Input);
                executeSomeLinuxCommand(gitRepo, NiCadSystemsDir, name, name_Minus, commitId + "^", Input);
            }
            commitIdsReader.close();
        }
    }
    public static void executeSomeLinuxCommand(String gitRepo, String NiCadSystemsDir, String name, String name_target, String commit, String Input) {
        // 开始检测克隆;
        // 1. 首先到了git仓库，切换到commit;
        String[] checkout = {"bash", "-c", "cd " + gitRepo + File.separator + name + "; git checkout " + commit};
        Utilities.implCommand(checkout);
        // 2. 将当前git仓库改名为目标名称;
        String[] rename = {"bash", "-c", "cd " + gitRepo + "; mv " + name + " " + name_target};
        Utilities.implCommand(rename);
        // 3. 检测克隆;
        String[] clone1 = {"bash", "-c", "cd " + NiCadSystemsDir + File.separator + "..;" + "./nicad6 functions py " + gitRepo + File.separator + name_target + " t1"};
        String[] clone2 = {"bash", "-c", "cd " + NiCadSystemsDir + File.separator + "..;" + "./nicad6 functions py " + gitRepo + File.separator + name_target + " t2"};
        String[] clone3 = {"bash", "-c", "cd " + NiCadSystemsDir + File.separator + "..;" + "./nicad6 functions py " + gitRepo + File.separator + name_target + " t3"};
        Utilities.implCommand(clone1);
        Utilities.implCommand(clone2);
        Utilities.implCommand(clone3);
        // 4. 检测完克隆之后，将仓库名改回去;
        String[] renameAgain = {"bash", "-c", "cd " + gitRepo + "; mv " + name_target + " " + name};
        Utilities.implCommand(renameAgain);

        // 5. 将目标结果移动到Input目录下;
        String[] moveToInput = {"bash", "-c", "cd " + gitRepo + "; cp -r " + name_target + "_functions-blind-clones " + Input};
        Utilities.implCommand(moveToInput);
    }
    
    // 将比较版本中路径不对的项目，修改为systems/...的路径。
    // 只需要修改0.30.xml和0.30-classes-withsource.xml文件
    // 将sourcePath目录下的所有项目中的0.30.xml和0.30-classes-withsource.xml文件，进行修改，然后存放到Input目录下;
    public static void func1(String InputPath, String Input) throws IOException {
        for (File project : new File(InputPath).listFiles()) {
            String name = project.getName(); // 项目的名称; name-Add-index_functions-blind-clones
            String purename = name.substring(0, name.indexOf("_functions"));
            //找到file030 和 file030WithSource文件
            File file030 = null;
            File file030WithSource = null;
            for (File listFiles : project.listFiles()) {
                if (listFiles.getName().contains("-0.30.xml")) {
                    file030 = listFiles;
                } 
                if (listFiles.getName().contains("-0.30-classes-withsource.xml")) {
                    file030WithSource = listFiles;
                }
            }
            File dir = new File(Input + File.separator + project.getName());
            dir.mkdir();
            BufferedReader file030Reader = new BufferedReader(new FileReader(file030));
            BufferedWriter file030Writer = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + File.separator + file030.getName()));

            // 开始写入文件file030;
            String line;
            while((line = file030Reader.readLine()) != null) {
                if (line.startsWith("<source file=")) {
                    String newLine = "<source file=\"systems/" + line.substring(line.indexOf(purename));
                    file030Writer.write(newLine + "\n");
                } else {
                    file030Writer.write(line + "\n");
                }
            }
            file030Reader.close();
            file030Writer.close();

            // 开始写入文件file030withsource;
            BufferedReader file030WithSourceReader = new BufferedReader(new FileReader(file030WithSource));
            BufferedWriter file030WithSourceWriter = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + File.separator + file030WithSource.getName()));

            while ((line = file030WithSourceReader.readLine()) != null) {
                if (line.startsWith("<source file=")) {
                    String newLine = "<source file=\"systems/" + line.substring(line.indexOf(purename));
                    file030WithSourceWriter.write(newLine + "\n");
                } else {
                    file030WithSourceWriter.write(line + "\n");
                }
            }
            file030WithSourceReader.close();
            file030WithSourceWriter.close();
        }
    }

    // 让gitRepo中的项目都克隆好，并切换到对应的Base版本；
    public static void returnOrigin(String gitRepo, String target) throws IOException {
        File gitRepoDir = new File(gitRepo);
        if (!gitRepoDir.exists()) gitRepoDir.mkdir();

        // 首先克隆每个项目，然后让每个项目都处在Base版本;
        File targetFile = new File(target);
        BufferedReader targetReader = new BufferedReader(new FileReader(targetFile));
        String line;
        while ((line = targetReader.readLine()) != null) {
            String[] s = line.split(" ");
            String name = s[0], gitLink = s[1], latestVersion = s[2], oldestVersion = s[3];
            String osName = System.getProperty("os.name");
            String terminal = null, option = null;
            if (osName.contains("Windows")) {
                 terminal = "cmd";
                 option = "/c";
            } else if (osName.contains("Linux")) {
                 terminal = "bash";
                 option = "-c";
            }
            String[] cloneCommand = {terminal, option, "cd " + gitRepo + "; git clone " + gitLink + "; cd " + name + "; git checkout " + latestVersion};
            Utilities.implCommand(cloneCommand);
        }
        targetReader.close();
    }

    // 遍历一个项目中所有的.py文件，并计算相应的halstead度量;
    public static void extractAllHalsteadMetric(String gitrepo, String CSVFile) throws IOException {
        CSVPrinter printer = new CSVPrinter(new FileWriter(CSVFile + File.separator + "42halstead.csv"), CSVFormat.DEFAULT);
        printer.printRecord("Name", "halstead_bugprop", "halstead_difficulty", "halstead_effort", "halstead_timerequired", "halstead_volume");
        for (File file : Objects.requireNonNull(new File(gitrepo).listFiles())) {
            String name = file.getName(); // 遍历每个项目
            if (name.contains("dataset")) continue;
            double[] arr = new double[5];
            processFiles(file, printer, arr);
            printer.printRecord(name, arr[0], arr[1], arr[2], arr[3], arr[4]);
            break;
        }
        printer.close();
    }
    // 利用命令行工具 multimetric计算halstead度量
    private static void processFiles(File file, CSVPrinter printer, double[] arr) throws IOException {
        // 如果是目录，递归遍历子文件
        if (file.isDirectory()) {
            System.out.println("开始处理目录" + file.getAbsolutePath());
            File[] files = file.listFiles();
            assert files != null;
            for (File subFile : files) {
                processFiles(subFile, printer, arr);
            }
        }
        // 如果是.py文件，执行命令
        else if (file.isFile() && file.getName().endsWith(".py")) {
            System.out.println("开始处理文件" + file.getAbsolutePath());
            // 构造执行命令的字符串
           String osName = System.getProperty("os.name");
           String terminal = null, option = null;
           if (osName.contains("Windows")) {
                terminal = "cmd";
                option = "/c";
           } else if (osName.contains("Linux")) {
                terminal = "bash";
                option = "-c";
           }
            String[] command = {terminal, option, "multimetric " + file.getAbsolutePath()};
            // 构造ProcessBuilder对象
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.redirectErrorStream(true);
            processBuilder.command(command);
            Process process = processBuilder.start();
            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.contains("halstead")) {
                    String substr = line.substring(line.indexOf(":") + 2, line.indexOf(","));
                    arr[0] += Double.parseDouble(substr);
                    for (int i = 1; i < 5; ++i) {
                        line = reader.readLine();
                        substr = line.substring(line.indexOf(":") + 2, line.indexOf(","));
                        arr[i] += Double.parseDouble(substr);
                    }
                    break;
                }
            }
            reader.close();
        }
    }

    // 提取Understand里面生成的metric，并求和。
    public static void extractMetricFromCSV(String sourceCSVDir, String targetCSV) throws IOException {
        CSVPrinter printer = new CSVPrinter(new FileWriter(targetCSV), CSVFormat.DEFAULT);
        printer.printRecord("project", "Cyclomatic", "WMC(SumCyclomatic)", "DIT(MaxInheritanceTree)", "NOC(CountClassDerived)", "CBO(CountClassCoupled)", "RFC(CountDeclMethodAll)");
        // 遍历所有的CSV文件
        for (File file : Objects.requireNonNull(new File(sourceCSVDir).listFiles())) {
            String fullname = file.getName(); // 包含后缀名
            String name = fullname.substring(0, fullname.indexOf(".csv")); // 不包含后缀名的项目名称;

            FileReader reader = new FileReader(file);
            CSVParser parse = CSVFormat.EXCEL.parse(reader);
            List<CSVRecord> records = parse.getRecords();
            // 确定对应指标所在的位置;
            int[] arr = new int[6];
            for (CSVRecord record : records) {
                for (int i = 0; i < record.size(); ++i) {
                    if (record.get(i).equals("Cyclomatic")) {
                        arr[0] = i;
                    } else if (record.get(i).equals("SumCyclomatic")) {
                        arr[1] = i;
                    } else if (record.get(i).equals("MaxInheritanceTree")) {
                        arr[2] = i;
                    } else if (record.get(i).equals("CountClassDerived")) {
                        arr[3] = i;
                    } else if (record.get(i).equals("CountClassCoupled")) {
                        arr[4] = i;
                    } else if (record.get(i).equals("CountDeclMethodAll")) {
                        arr[5] = i;
                    }
                }
                break;
            }

            double[] ans = new double[6]; // 记录每个指标的值;
            for (int i = 1; i < records.size(); i++) {
                CSVRecord csvRecord = records.get(i); // 考虑每一行;
                for (int j = 0; j < 6; ++j) {
                    if (!csvRecord.get(arr[j]).equals("")) {
                        ans[j] += Double.parseDouble(csvRecord.get(arr[j]));
                    }
                }
            }
            printer.printRecord(name, ans[0], ans[1], ans[2], ans[3], ans[4], ans[5]);
            parse.close();
        }
        printer.close();
    }
}
