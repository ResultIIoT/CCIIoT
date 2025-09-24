import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;


public class FindCPCoChangeClone {

    public static void main(String[] args) throws Exception {
        String Input = "src/main/inputCC/" + Utilities.Object;
        String Output = "src/main/outputCC/" + Utilities.Object;
        run(Input, Output);
    }

    public static void run(String Input, String Output) throws Exception {
        new File(Output).mkdir();
        long starttime = System.currentTimeMillis();  //时间戳

        File input = new File(Input); // 所有项目的输入文件所在处;
        File output = new File(Output); // 所有项目的结果文件所在处;
        if (!output.exists()) output.mkdir();

        File[] projectsList = input.listFiles(); // 所有项目文件;
        assert projectsList != null;
        Arrays.sort(projectsList, new AlphanumFileComparator<>()); // 按照项目进行块分类。

        int index = 0; // 表示某个的项目块的起始位置；
        int projectNum = 0; // 表示到第几个项目了
        while (index < projectsList.length) { // 遍历Input文件夹中的所有项目，一个项目是一个整块
            // 获取项目的名称, 这个不是base版本的名称;
            String fullName = projectsList[index].getName(); // 包含project-Add-index_functions-blind-clones的全部名称;
            String pureName = fullName.substring(0, fullName.indexOf("-"));  // 项目的前一部分名称，用来判断是否是同一个项目块;
            System.out.println(pureName);

            // 获取当前项目块的最后一个位置; 就base版本项目的位置；
            int nextIndex = index + 1;
            while (nextIndex < projectsList.length && projectsList[nextIndex].getName().contains(pureName)) ++nextIndex;
            --nextIndex;

            // 即目前的版本块的范围为[index, nextIndex]

            // 选取最新版本为基版本，然后分别跟剩下的版本进行共变比较选择。
            String subjectwholename1 = projectsList[nextIndex].getName(); // 即 name-functions-blind-clones形式
            String subjectname1 = subjectwholename1.substring(0, subjectwholename1.indexOf('_')); // 即进行Nicad检测之前的名字
            String inputf1, inputf1c, inputf2, inputf2c, outputAFile, outputBFile, AllResults, outputAWithCodeFile, outputBWithCodeFile;
            inputf1 = projectsList[nextIndex].getAbsolutePath() + File.separator + subjectwholename1 + "-0.30.xml"; // 项目所有克隆对所在的文件
            inputf1c = projectsList[nextIndex].getAbsolutePath() + File.separator + subjectwholename1 + "-0.30-classes-withsource.xml"; // 项目所有克隆源码所在的文件， 用来提取源码的;

            // 在Output文件夹中创建项目结果所存放的目录;
            File file1 = new File(output.getAbsolutePath() + File.separator + subjectname1);
            if (!file1.exists()) {
                file1.mkdir();
            }

            int i = nextIndex - 1;
            while (i >= index) {//该循环，输出1-2，1-3,1-4,1-5....版本的共变信息。

                String subjectwholename2 = projectsList[i].getName();

                String subjectname2 = subjectwholename2.substring(0, subjectwholename2.indexOf('_'));
                
                System.out.println("Start " + subjectname1 + "-------->" + subjectname2);
                inputf2 = projectsList[i].getAbsolutePath() + File.separator + subjectwholename2 + "-0.30.xml";
                inputf2c = projectsList[i].getAbsolutePath() + File.separator + subjectwholename2 + "-0.30-classes-withsource.xml";

                outputAFile = output.getAbsolutePath() + File.separator + subjectname1 + File.separator + subjectname1 + "___" + subjectname2 + "-A.txt";
                outputBFile = output.getAbsolutePath() + File.separator + subjectname1 + File.separator + subjectname1 + "___" + subjectname2 + "-B" + ".txt";
                AllResults = output.getAbsolutePath() + File.separator + subjectname1 + File.separator + "Allresults___" + subjectname1 + ".txt";

                outputAWithCodeFile = output.getAbsolutePath() + File.separator + subjectname1 + File.separator + subjectname1 + "___" + subjectname2 + "-A-withcode.txt";
                outputBWithCodeFile = output.getAbsolutePath() + File.separator + subjectname1 + File.separator + subjectname1 + "___" + subjectname2 + "-B-withcode" + ".txt";

                File existFile = new File(outputAFile);
                if (!existFile.exists()) {
                    // 克隆对信息和源码文件。
                    run(inputf1, inputf1c, inputf2, inputf2c, outputAFile, outputBFile, subjectname2, AllResults, outputAWithCodeFile, outputBWithCodeFile);
                } else {
                    System.out.println("已存在，检查下一个版本");
                    --i;
                }
            }

            //*************************************//
            //以下去除各版本之间的非重复共变克隆对,并提取出来；
            // 即找到一个项目的所有A文件，然后提取其中的克隆对，然后进行去重。
            File[] outputProject = output.listFiles(); // 很多项目都在一个结果文件中
            assert outputProject != null;

            // 选择我们目标的那个结果项目，名字就是Base版本的名字;
            File target = null;
            for (File file : outputProject) {
                if (file.getName().contains(subjectname1)) {
                    target = file;
                    break;
                }
            }

            assert target != null;
            File[] resultLists = target.listFiles(); // 获得Output/该项目/*
            assert resultLists != null;
            Arrays.sort(resultLists, new AlphanumFileComparator<>()); // 排个序;
            i = resultLists.length - 1;

            // 把不重复的Base版本的克隆对写入noduplicated文件中;
            BufferedWriter noDupWriter = new BufferedWriter(new FileWriter(output.getAbsolutePath() + File.separator + target.getName() + "__noduplicated.txt"));
            // 去重，保存base版本不重复的克隆对到文件noduplicated.txt中;
            HashSet<String> set = new HashSet<>();
            while (i >= 0) {// 倒序遍历结果目录，提取基版本的非重复克隆对
                if (resultLists[i].getName().endsWith("A.txt")) { // 找到一个版本的A文件;
                    BufferedReader aFileReader = new BufferedReader(new FileReader(resultLists[i])); // 读取这个A文件
                    noDupWriter.write("以下是" + resultLists[i].getName() + "文件的共变克隆对\n"); // 写下这个共变克隆的项目信息
                    String tmp1 = aFileReader.readLine();
                    String tmp2, tmp3;
                    int num = 0; // 记录当前文件非重复克隆对的数量;
                    while (tmp1 != null) { // 读取这个文件所有的非重复共变克隆对;
                        if (tmp1.contains("<clonepair")) { // 找到一个克隆对;
                            tmp2 = tmp1;
                            tmp3 = aFileReader.readLine();
                            tmp1 = aFileReader.readLine();
                            String pcid1 = Utilities.getPcid(tmp3);
                            String pcid2 = Utilities.getPcid(tmp1);
                            // 对克隆对进行全局去重，一个base版本的克隆对，不能重复;
                            boolean t1 = set.add(pcid1 + pcid2);
                            boolean t2 = set.add(pcid2 + pcid1);
                            if (!t1 || !t2) {
                                tmp1 = aFileReader.readLine();
                                continue;
                            }

                            // 是一对新的pcid
                            num++;
                            noDupWriter.write(tmp2 + "\n");
                            noDupWriter.write(tmp3 + "\n");
                            noDupWriter.write(tmp1 + "\n");
                            tmp1 = aFileReader.readLine();
                            noDupWriter.write(tmp1 + "\n\n\n");
                        }
                        tmp1 = aFileReader.readLine();
                    }
                    // 如果这个A文件没有共变克隆对，那么没关系，来几个回车隔绝一下：
                    if (num == 0) {
                        noDupWriter.write("\n\n\n\n\n");
                    }
                    aFileReader.close();
                    noDupWriter.write("\n\n\n\n\n"); // 每个文件之间的位置空一点点;
                }
                --i;
            }
            noDupWriter.close();

            // 开始写入总结果。
            // 把结果写入到 FinalResult文件中
            BufferedWriter FinalResultFiles = new BufferedWriter(new FileWriter(output.getAbsolutePath() + File.separator + target.getName() + "__FinalResults.txt"));

            // 读取Allresult中的所有数据;
            // 首先找到这个文件
            File AllresultFile = null;
            for (File file : Objects.requireNonNull(target.listFiles())) {
                if (file.getName().contains("Allresults")) {
                    AllresultFile = file;
                    break;
                }
            }
            // 开始读取Allresults文件
            assert AllresultFile != null;
            BufferedReader AllresultReader = new BufferedReader(new FileReader(AllresultFile));

            String tmp = AllresultReader.readLine();
            while (tmp != null) {
                FinalResultFiles.write(tmp + "\n");
                tmp = AllresultReader.readLine();
            }
            FinalResultFiles.write("\n\n");
            AllresultReader.close(); // 读完该文件即关闭

            // 再次读取这个文件
            AllresultReader = new BufferedReader(new FileReader(AllresultFile));
            tmp = AllresultReader.readLine();

            // 读取base版本项目共有多少的代码克隆
            StringBuilder str = new StringBuilder();
            int x = tmp.indexOf("检测了") + 3;
            while (tmp.charAt(x) != '个') {
                str.append(tmp.charAt(x++));
            }
            // 把base版本的代码克隆数量打印出来；
            FinalResultFiles.write("本次基版本项目" + target.getName() + "共有" + str + "个克隆对。\n\n");

            // 开始记录base版本和所有比较版本发生出现共变的次数;
            int sum = 0;
            int n = 0; // 记录当前被比较版本有多少个！
            while (tmp != null) {
                if (tmp.contains("发生了")) {
                    str.delete(0, str.length());
                    x = tmp.indexOf("发生了") + 3;
                    while (tmp.charAt(x) != '次') {
                        str.append(tmp.charAt(x++));
                    }
                    sum += Integer.parseInt(str.toString());
                    ++n;
                }
                tmp = AllresultReader.readLine();
            }
            // 写入最终结果, Utilities.GetTotalCloneNum是根据文件中"<clonepair"的数量，来求克隆对是数量的;
            FinalResultFiles.write(n + " 次共变检测过程中，共检测到了" + sum + "对共变克隆对\n\n" + "去掉重复的共变克隆对，还有" +
                    Utilities.GetTotalCloneNum(new File(output.getAbsolutePath() + File.separator + target.getName() + "__noduplicated.txt")) + "对共变的克隆对！！\n\n");
            FinalResultFiles.close();
            AllresultReader.close();


            //    删除中间文件，同时移动两个结果文件放进同一个文件夹。
            for (File file : resultLists) {
                if (file.getName().contains("Allresult")) {
                    file.delete();
                    break;
                }
            }
            File[] files3 = output.listFiles();
            File FinalResult = null, noduplicated = null, other = null;
            assert files3 != null;
            for (File file : files3) {
                if (file.getName().contains("FinalResult") && file.getName().contains(subjectname1)) {
                    FinalResult = file;
                } else if (file.getName().contains("noduplicated") && file.getName().contains(subjectname1)) {
                    noduplicated = file;
                } else if (file.getName().contains(subjectname1) && !file.getName().contains("FinalResult") && !file.getName().contains("noduplicated")) {
                    other = file;
                }
            }
            assert FinalResult != null;
            assert other != null;
            String[] command1 = {"bash", "-c", "mv " + FinalResult.getAbsolutePath() + " " + other.getAbsolutePath()};
            Utilities.implCommand(command1);
            assert noduplicated != null;
            String[] command2 = new String[]{"bash", "-c", "mv " + noduplicated.getAbsolutePath() + " " + other.getAbsolutePath()};
            Utilities.implCommand(command2);

            long endtime = System.currentTimeMillis();
            Utilities.printTime(endtime - starttime);
            System.out.println("\n\n------------------\n\n已经处理完第" + ++projectNum + "个项目的共变情况");
            index = nextIndex + 1;
        }
    }

    // 本方法实现从版本1文件到版本2文件的共变克隆的查找！包括克隆对的路径的文件和源码的文件两类。
    // 输入文件
    // inputf1 : 版本1克隆对所在文件
    // inputf1c : 版本1 克隆源码所在文件
    // inputf2 : 版本2 克隆对所在文件
    // inputf2c : 版本2 克隆源码所在文件;
    // outputAFile : A.txt文件
    // outputBFile B.txt文件
    // outputAWithCodeFile A-WithCode.txt文件
    // outputBWithCodeFile B-withCode.txt文件。
    public static void run(String inputf1, String inputf1c, String inputf2, String inputf2c, String outputAFile, String outputBFile, String subjectname2, String AllResults, String outputAWithCodeFile, String outputBWithCodeFile) throws Exception {

        //打开版本1克隆对的文件
        FileReader fileReader = new FileReader(inputf1);
        BufferedReader bufferedReader = new BufferedReader(fileReader);  //bufferedReader是版本1文件的遍历流。tmp是版本1文件的中介。

        //打开需要写入的文件。
        BufferedWriter bufferedWriterOld = new BufferedWriter(new FileWriter(outputAFile));
        BufferedWriter bufferedWriterNew = new BufferedWriter(new FileWriter(outputBFile));
        BufferedWriter bufferedWriterALL = new BufferedWriter(new FileWriter(AllResults, true));
        BufferedWriter bufferedWriterOldWithCode = new BufferedWriter(new FileWriter(outputAWithCodeFile));
        BufferedWriter bufferedWriterNewWithCode = new BufferedWriter(new FileWriter(outputBWithCodeFile));

        String tmp;//用来遍历版本1克隆对文件。
        String pcid;//使用Pcid定位。
        String tmpfptmp3;
        String tmpfptmp4;
        int j;//添加行到String中的中介

        String newfilename = null;
        StringBuilder oldfilename = null;

        //存放源码！
        String[] string1 = new String[4096];
        String[] string2 = new String[4096];
        String[] string3 = new String[4096];
        String[] string4 = new String[4096];

        //对版本1文件进行遍历，拿每对克隆对去对应文件寻找共变；
        int num = 0; //标记检测到哪一对克隆对了
        int cflag = 0;//标记写入克隆对数；
        int mark = 1;
        while ((tmp = bufferedReader.readLine()) != null) {

            //每次扫描完一对克隆，之前保存源码没用了；
            Arrays.fill(string1, null);
            Arrays.fill(string2, null);

            //找到版本1一个克隆对位置，取克隆1.1的信息；
            while (tmp != null && (!tmp.contains("<source file="))) {
                tmp = bufferedReader.readLine();
            }
            if (tmp == null) {
                System.out.println("\n\n共变克隆检测结束！");
                System.out.println("\n总共检测了" + num + "对克隆，共发现" + cflag + "对共变");
                break;
            }

            num++; //tmp非空，在版本1文件中找到克隆对！

            System.out.println("开始检测第" + num + "对克隆");

            String filepath1 = Utilities.getFilePath(tmp);//取克隆1.1文件路径filepath1；
            String fptmp1 = tmp;//保存所取克隆1.1的文件信息保存在fptmp1；
            String pcid1 = Utilities.getPcid(tmp);//求克隆1.1的pcid1；

            //取克隆对中下一个克隆1.2的信息
            tmp = bufferedReader.readLine();
            String filepath2 = Utilities.getFilePath(tmp);//取克隆2文件路径filepath2；
            String fptmp2 = tmp;//保存所取文件信息；
            String pcid2 = Utilities.getPcid(tmp);//取版本1克隆2的pcid2；

            if (mark <= 84470) {
                mark++;
                continue;
            }

            if (filepath1.contains("test") || filepath1.contains("Test") || filepath1.contains("example")) {
                // System.out.println(fileName);
                continue;
            }
            else if (filepath1.contains("opc-ua-sdk/sdk-server/src/main/java/org/eclipse/milo/opcua/sdk/server/namespaces")) {
                continue;
            }
            else if (filepath2.contains("test") || filepath2.contains("Test") || filepath2.contains("example")) {
                // System.out.println(fileName);
                continue;
            }
            else if (filepath2.contains("opc-ua-sdk/sdk-server/src/main/java/org/eclipse/milo/opcua/sdk/server/namespaces")) {
                continue;
            }

            //在克隆对文件找好一对克隆信息后，用pcid从版本1c文件中定位取这对克隆对应的源码，用字符串数组存储。
            FileReader fileReader1 = new FileReader(inputf1c);  //bufferedReader1是版本1c的遍历流。tmp1遍历1c文件的中介。
            BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
            String tmp1 = bufferedReader1.readLine();
            pcid = "pcid=" + '\"' + pcid1 + '\"';

            //遍历整个文件，用pcid定位
            while (tmp1 != null) {
                if (tmp1.contains(pcid))
                    break;
                tmp1 = bufferedReader1.readLine();
            }
            while (tmp1 != null) {
                if (tmp1.contains("<source")) {
                    tmp1 = bufferedReader1.readLine();
                    continue;
                }
                break;
            }

            //把版本1c克隆1.1源码按行存入字符串数组string1;string1数组存放1c克隆1
            j = 0;
            string1[j++] = tmp1;
            while (!(tmp1 = bufferedReader1.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                string1[j++] = tmp1;
            }
            bufferedReader1.close();//使命完成

            //重新打开文件1c，从头开始遍历，取克隆1.2的源码；
            FileReader fileReader2 = new FileReader(inputf1c);
            BufferedReader bufferedReader2 = new BufferedReader(fileReader2);//去对应class文件找源码;bufferedReader2是1c文件克隆2的提取,tmp2是对应中介
            String tmp2 = bufferedReader2.readLine();
            pcid = "pcid=" + '\"' + pcid2 + '\"';//用pcid2定位
            while (tmp2 != null) {
                if (tmp2.contains(pcid))
                    break;
                tmp2 = bufferedReader2.readLine();
            }
            while (tmp2 != null) {
                if (tmp2.contains("<source")) {
                    tmp2 = bufferedReader2.readLine();
                    continue;
                }
                break;
            }
            //取克隆1.2源码存入string2数组；
            j = 0;
            string2[j++] = tmp2;
            while (!(tmp2 = bufferedReader2.readLine()).contains("</source>")) {
                string2[j++] = tmp2;
            }
            bufferedReader2.close();//使命完成

            //现在开始，拿着路径和函数名去版本2找共变克隆对
            //在找之前，需要改一下两个名字，因为版本不同,使用ft1和ft2存储克隆1.1和1.2修改版本号之后的路径，拿f1和f2去匹配！

            //获得版本2的文件名；
            newfilename = subjectname2;

            //找到之前版本名；
            oldfilename = new StringBuilder();
            int x = filepath1.indexOf('/') + 1;
            // 检查起始位置是否有效
            if (x > 0 && x < filepath1.length()) {
                while (x < filepath1.length() && filepath1.charAt(x) != '/') {
                    oldfilename.append(filepath1.charAt(x));
                    x++;
                }
            }
//            System.out.println(filepath1);
            //把之前版本名oldfilename换成新版本号名；
            String ft1 = filepath1.replace(oldfilename.toString(), newfilename);
            String ft2 = filepath2.replace(oldfilename.toString(), newfilename);//这是我们的拿去做匹配的目标路径；
//            System.out.println(filepath1);
//            System.out.println(filepath2);
//            System.out.println(ft1);
//            System.out.println(ft2);
            //存放版本2中匹配到的克隆对的文件路径。
            String filepath3;
            String filepath4;

            //开始遍历版本2文件；
            FileReader fileReader3 = new FileReader(inputf2);   //bufferedReader3是版本2的克隆匹配；len就是版本2文件。
            BufferedReader bufferedReader3 = new BufferedReader(fileReader3); //去对应class文件找源码；
            String tmp3 = bufferedReader3.readLine();  //tmp3是版本2的克隆匹配中介；
            String tmp4 = null;//暂存下面的匹配的克隆对2.2的文件行；

            while (tmp3 != null) {
                //在版本2中寻找新的克隆对源码，因此需要清零3和4；
                Arrays.fill(string3, null);
                Arrays.fill(string4, null);

                //在本次中，在版本2文件中寻找匹配路径的克隆对。
                while (tmp3 != null) {
                    if (tmp3.contains(ft1) || tmp3.contains(ft2)) {
                        tmp4 = bufferedReader3.readLine(); //由于检测是从上往下检测，tmp3只能匹配克隆对2.1,不能是2.2;
                        if (tmp4.contains("Eclipse")) {
                            break;
                        }
                        tmp3 = tmp4;
                    } else
                        tmp3 = bufferedReader3.readLine();
                } //该循环得到一对克隆，克隆对中第一个克隆是匹配f1或f2的，第二个不确定。

                //如果版本2文件已经到达末尾，代表本次版本1的克隆对检测结束，也没有找到合适的，退出循环，进行下一对克隆对检测。
                if (tmp3 == null) {
                    break;
                }


                // tmp3和tmp4存储新的克隆对信息串；
                filepath3 = tmp3.substring(tmp3.indexOf("Eclipse"), tmp3.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆2.1文件路径；
                filepath4 = tmp4.substring(tmp4.indexOf("Eclipse"), tmp4.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆2.2文件路径；
                //保存所取克隆对的文件信息；
                String fptmp3 = tmp3;//保存所取文件信息；
                String fptmp4 = tmp4;


                //现在判断是否匹配第二个克隆，两种情况ft1==ft2为true; 情况2：ft1==f2为false。
                //情况1进入入口：ft1==ft2为true
                if (ft1.equals(ft2) && ft1.equals(filepath3) && ft1.equals(filepath4)) {
                    //四个路径完全相同。
                    //先取匹配到的克隆的函数名字；
                    String pcid3t = Utilities.getPcid(tmp3);
                    FileReader fileReader5t = new FileReader(inputf2c);
                    BufferedReader bufferedReader5t = new BufferedReader(fileReader5t);//去class文件找源码；
                    String tmp6t = bufferedReader5t.readLine();
                    pcid = "pcid=" + '\"' + pcid3t + '\"';//  用pcid进行定位

                    while (tmp6t != null) {//遍历整个文件，用pcid定位；
                        if (tmp6t.contains(pcid))
                            break;
                        tmp6t = bufferedReader5t.readLine();
                    }
                    while (tmp6t != null) {
                        if (tmp6t.contains("<source")) {
                            tmp6t = bufferedReader5t.readLine();
                            continue;
                        }
                        break;
                    }
                    String funcname3 = tmp6t;

                    //取名字；
                    String pcid4t = Utilities.getPcid(tmp4);
                    FileReader fileReader6t = new FileReader(inputf2c);
                    BufferedReader bufferedReader6t = new BufferedReader(fileReader6t);//去class文件找源码；   //版本2c中的克隆2源码提出来,   bufferedReader6,tmp7;
                    String tmp7t = bufferedReader6t.readLine();
                    pcid = "pcid=" + '\"' + pcid4t + '\"';//  用pcid进行定位

                    while (tmp7t != null) {//遍历整个文件，用pcid定位；
                        if (tmp7t.contains(pcid))
                            break;
                        tmp7t = bufferedReader6t.readLine();
                    }
                    while (tmp7t != null) {
                        if (tmp7t.contains("<source")) {
                            tmp7t = bufferedReader6t.readLine();
                            continue;
                        }
                        break;
                    }

                    //把版本2c克隆2源码按行存入字符串数组string4；
                    String funcname4 = tmp7t;

                    //情况1.1：相同路径，且函数名相同一模一样.
                    //函数名相同，那么就拿代码片段大小去作为判断起始行的误差阈值，可认为统一文件下的相同克隆的变化幅度不大！。
                    if (string1[0].trim().equals(string2[0].trim()) && funcname3.trim().equals(funcname4.trim()) && string1[0].trim().equals(funcname3.trim())) {
                        int startline1 = Utilities.getStartLine(fptmp1);
                        int endline1 = Utilities.getEndLine(fptmp1);
                        int len1 = endline1 - startline1;

                        int startline2 = Utilities.getStartLine(fptmp2);
                        int endline2 = Utilities.getEndLine(fptmp2);
                        int len2 = endline2 - startline2;

                        int startline3 = Utilities.getStartLine(fptmp3);
                        int endline3 = Utilities.getEndLine(fptmp3);
                        int len3 = endline3 - startline3;

                        int startline4 = Utilities.getStartLine(fptmp4);
                        int endline4 = Utilities.getEndLine(fptmp4);
                        int len4 = endline4 - startline4;

                        int s1 = Math.abs(startline1 - startline3);
                        int s2 = Math.abs(startline1 - startline4);
                        int s3 = Math.abs(startline2 - startline3);
                        int s4 = Math.abs(startline2 - startline4);

                        //路径相同函数名相同情况1：区分克隆片段是否对齐：
                        //那么1&3且2&4对齐；
                        if (s1 < s2 && s4 < s3) {
                            //先取源码
                            j = 0;
                            string3[j++] = tmp6t;
                            while (!(tmp6t = bufferedReader5t.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                                string3[j++] = tmp6t;
                            }

                            j = 0;
                            string4[j++] = tmp7t;
                            while (!(tmp7t = bufferedReader6t.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                                string4[j++] = tmp7t;
                            }

                            //遍历剩下克隆对，看是否还有更接近
                            tmp3 = bufferedReader3.readLine();
                            while (tmp3 != null) {
                                if (tmp3.contains(ft1) || tmp3.contains(ft2)) {
                                    tmp4 = bufferedReader3.readLine();
                                    if (tmp4.contains("Eclipse")) {

                                        String tmpfilepath3 = tmp3.substring(tmp3.indexOf("Eclipse"), tmp3.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆1文件路径；
                                        String tmpfilepath4 = tmp4.substring(tmp4.indexOf("Eclipse"), tmp4.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆2文件路径；

                                        tmpfptmp3 = tmp3;//保存所取文件信息；
                                        tmpfptmp4 = tmp4;

//                                        相同的，路径完全相同。
                                        if (ft1.equals(ft2) && ft1.equals(tmpfilepath3) && tmpfilepath3.equals(tmpfilepath4)) {

                                            //把版本2c中的克隆1源码提取出来，用字符串数组存储；先把对应位置的克隆源码提取出来再说！
                                            String tmppcid3 = Utilities.getPcid(tmp3);
                                            FileReader tmpfileReader5 = new FileReader(inputf2c);
                                            BufferedReader tmpbufferedReader5 = new BufferedReader(tmpfileReader5);//去class文件找源码；
                                            String tmptmp6 = tmpbufferedReader5.readLine();

                                            pcid = "pcid=" + '\"' + tmppcid3 + '\"';//  用pcid进行定位

                                            while (tmptmp6 != null) {//遍历整个文件，用pcid定位；
                                                if (tmptmp6.contains(pcid))
                                                    break;
                                                tmptmp6 = tmpbufferedReader5.readLine();
                                            }
                                            while (tmptmp6 != null) {
                                                if (tmptmp6.contains("<source")) {
                                                    tmptmp6 = tmpbufferedReader5.readLine();
                                                    continue;
                                                }
                                                break;
                                            }
                                            String tmpfuncname3 = tmptmp6;//获得该函数名;

                                            //取二次遍历2.2函数名。
                                            String tmppcid4 = Utilities.getPcid(tmp4);
                                            FileReader tmpfileReader6 = new FileReader(inputf2c);
                                            BufferedReader tmpbufferedReader6 = new BufferedReader(tmpfileReader6);//去class文件找源码；   //版本2c中的克隆2源码提出来,   bufferedReader6,tmp7;
                                            String tmptmp7 = tmpbufferedReader6.readLine();

                                            pcid = "pcid=" + '\"' + tmppcid4 + '\"';//  用pcid进行定位

                                            while (tmptmp7 != null) {//遍历整个文件，用pcid定位；
                                                if (tmptmp7.contains(pcid))
                                                    break;
                                                tmptmp7 = tmpbufferedReader6.readLine();
                                            }
                                            while (tmptmp7 != null) {
                                                if (tmptmp7.contains("<source")) {
                                                    tmptmp7 = tmpbufferedReader6.readLine();
                                                    continue;
                                                }
                                                break;
                                            }

                                            String tmpfuncname4 = tmptmp7;//获得二次遍历中的克隆2.2的函数名。

                                            //函数名是否相等。
                                            if (string1[0].trim().equals(string2[0].trim()) && string1[0].trim().equals(tmpfuncname3.trim()) && tmpfuncname3.trim().equals(tmpfuncname4.trim())) {
                                                int tmpstartline3 = Utilities.getStartLine(tmpfptmp3);
                                                int tmpstartline4 = Utilities.getStartLine(tmpfptmp4);

                                                int tmps1 = Math.abs(startline1 - tmpstartline3);
                                                int tmps2 = Math.abs(startline1 - tmpstartline4);
                                                int tmps3 = Math.abs(startline2 - tmpstartline3);
                                                int tmps4 = Math.abs(startline2 - tmpstartline4);

//                                                仍然是1&3匹配。
                                                if (tmps1 <= s1 && tmps4 <= s4) {//对fptmp3,fptmp4进行更新。
                                                    fptmp3 = tmpfptmp3;
                                                    fptmp4 = tmpfptmp4;

                                                    j = 0;
                                                    Arrays.fill(string3, null);
                                                    string3[j++] = tmptmp6;
                                                    while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                        string3[j++] = tmptmp6;
                                                    }

                                                    j = 0;
                                                    Arrays.fill(string4, null);
                                                    string4[j++] = tmptmp7;
                                                    while (!(tmptmp7 = tmpbufferedReader6.readLine()).contains("</source>")) {
                                                        string4[j++] = tmptmp7;
                                                    }
                                                }
//                                                此时是1&4匹配
                                                else if (tmps2 <= s1 && tmps3 <= s4) {
                                                    fptmp3 = tmpfptmp4;
                                                    fptmp4 = tmpfptmp3;

                                                    j = 0;
                                                    Arrays.fill(string4, null);
                                                    string4[j++] = tmptmp6;
                                                    while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                        string4[j++] = tmptmp6;
                                                    }

                                                    j = 0;
                                                    Arrays.fill(string3, null);
                                                    string3[j++] = tmptmp7;
                                                    while (!(tmptmp7 = tmpbufferedReader6.readLine()).contains("</source>")) {
                                                        string3[j++] = tmptmp7;
                                                    }
                                                }
                                            }
                                            tmpfileReader5.close();
                                            tmpfileReader6.close();
                                        }
                                    }
                                    tmp3 = tmp4;
                                } else
                                    tmp3 = bufferedReader3.readLine();
                            }// 此时代表while循环结束，已经找遍了所有克隆对

                            // 路径相同且函数名相同的1&3对齐情况结束
                            // 可以判断开始写入克隆了。
                            if (Utilities.judge1_3CCClone(string1, string2, string3, string4)) {
                                System.out.println("Finding co-modified! write to file...");
                                ++cflag;
                                //把文件信息和克隆信息写入文件；
                                //如果是共变克隆对之间的，用换行分开
                                if (cflag != 1) {//不是第一行，各克隆对之间用换行分开。
                                    bufferedWriterOld.write("\n\n\n");
                                    bufferedWriterNew.write("\n\n\n");
                                }

                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOld.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOld.write(fptmp1 + "\n");

                                //开始写入版本1克隆2克隆信息；
                                bufferedWriterOld.write(fptmp2 + "\n");
                                bufferedWriterOld.write("</clonepair" + cflag + ">\n");

                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNew.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNew.write(fptmp3 + "\n");

                                bufferedWriterNew.write(fptmp4 + "\n");
                                bufferedWriterNew.write("</clonepair" + cflag + ">\n");

                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp1 + "\n");
                                for (int i = 0; string1[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string1[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write("\n");


                                //开始写入版本1克隆2克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp2 + "\n");
                                for (int i = 0; string2[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string2[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");


                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp3 + "\n");
                                for (int i = 0; string3[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string3[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write("\n");


                                //开始写入版本2克隆2信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp4 + "\n");
                                for (int i = 0; string4[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string4[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                            }
                        }

                        //路径相同函数名相同情况2：那么路径相同函数相同的1&4且2&3对齐;
                        else if (s1 > s2 && s4 > s3) {
                            //先取源码
                            j = 0;
                            string3[j++] = tmp6t;
                            while (!(tmp6t = bufferedReader5t.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                                string3[j++] = tmp6t;
                            }

                            j = 0;
                            string4[j++] = tmp7t;
                            while (!(tmp7t = bufferedReader6t.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                                string4[j++] = tmp7t;
                            }

                            //遍历剩下克隆对，看是否还有更接近
                            tmp3 = bufferedReader3.readLine();
                            while (tmp3 != null) {
                                if (tmp3.contains(ft1) || tmp3.contains(ft2)) {
                                    tmp4 = bufferedReader3.readLine();
                                    if (tmp4.contains("Eclipse")) {

                                        String tmpfilepath3 = tmp3.substring(tmp3.indexOf("Eclipse"), tmp3.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆1文件路径；
                                        String tmpfilepath4 = tmp4.substring(tmp4.indexOf("Eclipse"), tmp4.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆2文件路径；

                                        tmpfptmp3 = tmp3;//保存所取文件信息；
                                        tmpfptmp4 = tmp4;

//                                        相同的，路径完全相同。
                                        if (ft1.equals(ft2) && ft1.equals(tmpfilepath3) && tmpfilepath3.equals(tmpfilepath4)) {

                                            //把版本2c中的克隆1源码提取出来，用字符串数组存储；先把对应位置的克隆源码提取出来再说！
                                            String tmppcid3 = Utilities.getPcid(tmp3);
                                            FileReader tmpfileReader5 = new FileReader(inputf2c);
                                            BufferedReader tmpbufferedReader5 = new BufferedReader(tmpfileReader5);//去class文件找源码；
                                            String tmptmp6 = tmpbufferedReader5.readLine();

                                            pcid = "pcid=" + '\"' + tmppcid3 + '\"';//  用pcid进行定位

                                            while (tmptmp6 != null) {//遍历整个文件，用pcid定位；
                                                if (tmptmp6.contains(pcid))
                                                    break;
                                                tmptmp6 = tmpbufferedReader5.readLine();
                                            }
                                            while (tmptmp6 != null) {
                                                if (tmptmp6.contains("<source")) {
                                                    tmptmp6 = tmpbufferedReader5.readLine();
                                                    continue;
                                                }
                                                break;
                                            }
                                            String tmpfuncname3 = tmptmp6;//获得该函数名;

                                            //取二次遍历2.2函数名。
                                            String tmppcid4 = Utilities.getPcid(tmp4);
                                            FileReader tmpfileReader6 = new FileReader(inputf2c);
                                            BufferedReader tmpbufferedReader6 = new BufferedReader(tmpfileReader6);//去class文件找源码；   //版本2c中的克隆2源码提出来,   bufferedReader6,tmp7;
                                            String tmptmp7 = tmpbufferedReader6.readLine();

                                            pcid = "pcid=" + '\"' + tmppcid4 + '\"';//  用pcid进行定位

                                            while (tmptmp7 != null) {//遍历整个文件，用pcid定位；
                                                if (tmptmp7.contains(pcid))
                                                    break;
                                                tmptmp7 = tmpbufferedReader6.readLine();
                                            }
                                            while (tmptmp7 != null) {
                                                if (tmptmp7.contains("<source")) {
                                                    tmptmp7 = tmpbufferedReader6.readLine();
                                                    continue;
                                                }
                                                break;
                                            }

                                            String tmpfuncname4 = tmptmp7;//获得二次遍历中的克隆2.2的函数名。

                                            //函数名是否相等。
                                            if (string1[0].trim().equals(string2[0].trim()) && string1[0].trim().equals(tmpfuncname3.trim()) && tmpfuncname3.trim().equals(tmpfuncname4.trim())) {
                                                int tmpstartline3 = Utilities.getStartLine(tmpfptmp3);
                                                int tmpstartline4 = Utilities.getStartLine(tmpfptmp4);

                                                int tmps1 = Math.abs(startline1 - tmpstartline3);
                                                int tmps2 = Math.abs(startline1 - tmpstartline4);
                                                int tmps3 = Math.abs(startline2 - tmpstartline3);
                                                int tmps4 = Math.abs(startline2 - tmpstartline4);

//                                                1&4d对齐，仍然是1&3匹配
                                                if ((tmps1 <= s2 && tmps4 <= s3)) {//对fptmp3,fptmp4进行更新。
                                                    fptmp3 = tmpfptmp4;
                                                    fptmp4 = tmpfptmp3;

                                                    j = 0;
                                                    Arrays.fill(string4, null);
                                                    string4[j++] = tmptmp6;
                                                    while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                        string4[j++] = tmptmp6;
                                                    }

                                                    j = 0;
                                                    Arrays.fill(string3, null);
                                                    string3[j++] = tmptmp7;
                                                    while (!(tmptmp7 = tmpbufferedReader6.readLine()).contains("</source>")) {
                                                        string3[j++] = tmptmp7;
                                                    }
                                                }
//                                                此时是1&4匹配对齐。
                                                else if (tmps2 <= s2 && tmps3 <= s3) {
                                                    fptmp3 = tmpfptmp3;
                                                    fptmp4 = tmpfptmp4;

                                                    j = 0;
                                                    Arrays.fill(string3, null);
                                                    string3[j++] = tmptmp6;
                                                    while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                        string3[j++] = tmptmp6;
                                                    }

                                                    j = 0;
                                                    Arrays.fill(string4, null);
                                                    string4[j++] = tmptmp7;
                                                    while (!(tmptmp7 = tmpbufferedReader6.readLine()).contains("</source>")) {
                                                        string4[j++] = tmptmp7;
                                                    }
                                                }
                                            }
                                            tmpfileReader5.close();
                                            tmpfileReader6.close();
                                        }
                                    }
                                    tmp3 = tmp4;
                                } else
                                    tmp3 = bufferedReader3.readLine();
                            }//此时代表while循环结束，已经找遍了所有克隆对

                            // 1&4情况结束
                            // 可以开始判断写入克隆了。
                            if (Utilities.judge1_4CCClone(string1, string2, string3, string4)) {
                                System.out.println("Finding co-modified! Write to file...");
                                ++cflag;
                                //把文件信息和克隆信息写入文件；
                                //如果是共变克隆对之间的，用换行分开
                                if (cflag != 1) {//不是第一行，各克隆对之间用换行分开。
                                    bufferedWriterOld.write("\n\n\n");
                                    bufferedWriterNew.write("\n\n\n");
                                }

                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOld.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOld.write(fptmp1 + "\n");

                                bufferedWriterOld.write(fptmp2 + "\n");
                                bufferedWriterOld.write("</clonepair" + cflag + ">\n");

                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNew.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNew.write(fptmp4 + "\n");

                                bufferedWriterNew.write(fptmp3 + "\n");
                                bufferedWriterNew.write("</clonepair" + cflag + ">\n");

                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp1 + "\n");
                                for (int i = 0; string1[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string1[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write("\n");

                                //开始写入版本1克隆2克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp2 + "\n");
                                for (int i = 0; string2[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string2[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");

                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp4 + "\n");
                                for (int i = 0; string4[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string4[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write("\n");


                                //开始写入版本2克隆2信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp3 + "\n");
                                for (int i = 0; string3[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string3[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                            }
                        }

                        //路径相同函数名相同情况三：
                        else if (Math.abs(startline1 - startline3) < (len1 + len3) / 2 && Math.abs(startline2 - startline4) < (len2 + len4) / 2) {
                            j = 0;
                            string3[j++] = tmp6t;
                            while (!(tmp6t = bufferedReader5t.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                                string3[j++] = tmp6t;
                            }

                            j = 0;
                            string4[j++] = tmp7t;
                            while (!(tmp7t = bufferedReader6t.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                                string4[j++] = tmp7t;
                            }

                            if (Utilities.judge1_3CCClone(string1, string2, string3, string4)) {

                                System.out.println("Finding co-modified! Write to file...");
                                ++cflag;
                                //把文件信息和克隆信息写入文件；
                                //如果是共变克隆对之间的，用换行分开
                                if (cflag != 1) {//不是第一行，各克隆对之间用换行分开。
                                    bufferedWriterOld.write("\n\n\n");
                                    bufferedWriterNew.write("\n\n\n");
                                }

                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOld.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOld.write(fptmp1 + "\n");

                                bufferedWriterOld.write(fptmp2 + "\n");
                                bufferedWriterOld.write("</clonepair" + cflag + ">\n");


                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNew.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNew.write(fptmp3 + "\n");

                                bufferedWriterNew.write(fptmp4 + "\n");
                                bufferedWriterNew.write("</clonepair" + cflag + ">\n");


                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp1 + "\n");
                                for (int i = 0; string1[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string1[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write("\n");


                                //开始写入版本1克隆2克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp2 + "\n");
                                for (int i = 0; string2[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string2[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");


                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp3 + "\n");
                                for (int i = 0; string3[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string3[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write("\n");


                                //开始写入版本2克隆2信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp4 + "\n");
                                for (int i = 0; string4[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string4[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                            }
                        }

                        //路径相同函数相同情况四：如果1对4&&2对3成立
                        else if (Math.abs(startline1 - startline4) < (len1 + len4) / 2 && Math.abs(startline2 - startline3) < (len2 + len3) / 2) {
                            j = 0;
                            string3[j++] = tmp6t;
                            while (!(tmp6t = bufferedReader5t.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                                string3[j++] = tmp6t;
                            }

                            j = 0;
                            string4[j++] = tmp7t;
                            while (!(tmp7t = bufferedReader6t.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                                string4[j++] = tmp7t;
                            }

                            //发生共变则写入文件。
                            if (Utilities.judge1_4CCClone(string1, string2, string3, string4)) {

                                System.out.println("Finding co-modified! Write to file...");
                                ++cflag;
                                //把文件信息和克隆信息写入文件；
                                //如果是共变克隆对之间的，用换行分开
                                if (cflag != 1) {//不是第一行，各克隆对之间用换行分开。
                                    bufferedWriterOld.write("\n\n\n");
                                    bufferedWriterNew.write("\n\n\n");
                                }

                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOld.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOld.write(fptmp1 + "\n");

                                bufferedWriterOld.write(fptmp2 + "\n");
                                bufferedWriterOld.write("</clonepair" + cflag + ">\n");


                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNew.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNew.write(fptmp4 + "\n");

                                bufferedWriterNew.write(fptmp3 + "\n");
                                bufferedWriterNew.write("</clonepair" + cflag + ">\n");


                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp1 + "\n");
                                for (int i = 0; string1[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string1[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write("\n");


                                //开始写入版本1克隆2克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp2 + "\n");
                                for (int i = 0; string2[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string2[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");


                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp4 + "\n");
                                for (int i = 0; string4[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string4[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write("\n");


                                //开始写入版本2克隆2信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp3 + "\n");
                                for (int i = 0; string3[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string3[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                            }

                        }

                    }

                    //情况1.2：相同路径但函数名不同,和下面的情况的类似。
                    else if (!string1[0].trim().equals(string2[0].trim())) {
                        j = 0;
                        string3[j++] = tmp6t;
                        while (!(tmp6t = bufferedReader5t.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                            string3[j++] = tmp6t;
                        }

                        j = 0;
                        string4[j++] = tmp7t;
                        while (!(tmp7t = bufferedReader6t.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                            string4[j++] = tmp7t;
                        }

                        int startline1 = Utilities.getStartLine(fptmp1);

                        int startline2 = Utilities.getStartLine(fptmp2);

                        int startline3 = Utilities.getStartLine(fptmp3);

                        int startline4 = Utilities.getStartLine(fptmp4);

                        //此时路径相同，函数名不同，对应匹配为1-3&&2-4
                        if (string1[0].trim().equals(string3[0].trim()) && string2[0].trim().equals(string4[0].trim())) {

                            //此时已经找到第一对合适的克隆对了。
                            //继续往下寻找，直到把整个版本2文件全部找完，然后选startline差距最小的克隆对，与版本1克隆对作为共变比较！
                            tmp3 = bufferedReader3.readLine();
                            while (tmp3 != null) {
                                if (tmp3.contains(ft1) || tmp3.contains(ft2)) {
                                    tmp4 = bufferedReader3.readLine();
                                    if (tmp4.contains("Eclipse")) {

                                        String tmpfilepath3 = tmp3.substring(tmp3.indexOf("Eclipse"), tmp3.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆1文件路径；
                                        String tmpfilepath4 = tmp4.substring(tmp4.indexOf("Eclipse"), tmp4.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆2文件路径；

                                        tmpfptmp3 = tmp3;//保存所取文件信息；
                                        tmpfptmp4 = tmp4;

                                        if (ft1.equals(ft2) && ft1.equals(tmpfilepath3) && ft1.equals(tmpfilepath4) && (!string1[0].trim().equals(string2[0].trim()))) {

                                            //把版本2c中的克隆1源码提取出来，用字符串数组存储；先把对应位置的克隆源码提取出来再说！
                                            String tmppcid3 = Utilities.getPcid(tmp3);

                                            FileReader tmpfileReader5 = new FileReader(inputf2c);
                                            BufferedReader tmpbufferedReader5 = new BufferedReader(tmpfileReader5);//去class文件找源码；   //版本2c中的克隆1源码提出来,   bufferedReader5,tmp6;

                                            String tmptmp6 = tmpbufferedReader5.readLine();

                                            pcid = "pcid=" + '\"' + tmppcid3 + '\"';//  用pcid进行定位

                                            while (tmptmp6 != null) {//遍历整个文件，用pcid定位；
                                                if (tmptmp6.contains(pcid))
                                                    break;
                                                tmptmp6 = tmpbufferedReader5.readLine();
                                            }
                                            while (tmptmp6 != null) {
                                                if (tmptmp6.contains("<source")) {
                                                    tmptmp6 = tmpbufferedReader5.readLine();
                                                    continue;
                                                }
                                                break;
                                            }
                                            String tfuncname3 = tmptmp6;//获得该函数名;

                                            //把版本2c中的克隆2源码提取出来，用字符串数组存储；
                                            String tmppcid4 = Utilities.getPcid(tmp4);

                                            FileReader tmpfileReader6 = new FileReader(inputf2c);
                                            BufferedReader tmpbufferedReader6 = new BufferedReader(tmpfileReader6);//去class文件找源码；   //版本2c中的克隆2源码提出来,   bufferedReader6,tmp7;

                                            String tmptmp7 = tmpbufferedReader6.readLine();

                                            pcid = "pcid=" + '\"' + tmppcid4 + '\"';//  用pcid进行定位

                                            while (tmptmp7 != null) {//遍历整个文件，用pcid定位；
                                                if (tmptmp7.contains(pcid))
                                                    break;
                                                tmptmp7 = tmpbufferedReader6.readLine();
                                            }
                                            while (tmptmp7 != null) {
                                                if (tmptmp7.contains("<source")) {
                                                    tmptmp7 = tmpbufferedReader6.readLine();
                                                    continue;
                                                }
                                                break;
                                            }

                                            String tfuncname4 = tmptmp7;

                                            //遍历整个过程中，匹配上了1-3&&2-4
                                            if (string1[0].equals(tfuncname3) && string2[0].equals(tfuncname4) && ft1.equals(tmpfilepath3) && ft2.equals(tmpfilepath4)) {

                                                int tmpstartline3 = Utilities.getStartLine(tmpfptmp3);
                                                int tmpstartline4 = Utilities.getStartLine(tmpfptmp4);

                                                int c1 = Math.abs(startline1 - tmpstartline3);
                                                int c2 = Math.abs(startline1 - startline3);
                                                int c3 = Math.abs(startline2 - tmpstartline4);
                                                int c4 = Math.abs(startline2 - startline4);
                                                if (c1 <= c2 || c3 <= c4) {     //存在一个距离版本1克隆更近，那么则更新String3和string4；
                                                    //更新string3;
                                                    fptmp3 = tmpfptmp3;
                                                    fptmp4 = tmpfptmp4;
                                                    j = 0;
                                                    Arrays.fill(string3, null);
                                                    string3[j++] = tmptmp6;
                                                    while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                        string3[j++] = tmptmp6;
                                                    }

                                                    j = 0;
                                                    Arrays.fill(string4, null);
                                                    string4[j++] = tmptmp7;
                                                    while (!(tmptmp7 = tmpbufferedReader6.readLine()).contains("</source>")) {
                                                        string4[j++] = tmptmp7;
                                                    }
                                                }
                                            }
                                            //遍历整个过程中，匹配上了1-4&&2-3
                                            else if (string1[0].equals(tfuncname4) && string2[0].equals(tfuncname3)
                                                    && ft1.equals(tmpfilepath4) && ft2.equals(tmpfilepath3)) {

                                                int tmpstartline3 = Utilities.getStartLine(tmpfptmp3);
                                                int tmpstartline4 = Utilities.getStartLine(tmpfptmp4);

                                                int c1 = Math.abs(startline1 - tmpstartline4);
                                                int c2 = Math.abs(startline1 - startline3);
                                                int c3 = Math.abs(startline2 - tmpstartline3);
                                                int c4 = Math.abs(startline2 - startline4);

                                                if (c1 <= c2 || c3 <= c4) {     //存在一个距离版本1克隆更近，那么则更新String3和string4；
                                                    //更新string3;
                                                    fptmp3 = tmpfptmp4;
                                                    fptmp4 = tmpfptmp3;
                                                    j = 0;
                                                    Arrays.fill(string4, null);
                                                    string4[j++] = tmptmp6;
                                                    while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                        string4[j++] = tmptmp6;
                                                    }
                                                    j = 0;
                                                    Arrays.fill(string3, null);
                                                    string3[j++] = tmptmp7;
                                                    while (!(tmptmp7 = tmpbufferedReader6.readLine()).contains("</source>")) {
                                                        string3[j++] = tmptmp7;
                                                    }
                                                }
                                            }
                                            tmpbufferedReader5.close();
                                            tmpbufferedReader6.close();
                                        }
                                    }
                                    tmp3 = tmp4;
                                } else
                                    tmp3 = bufferedReader3.readLine();
                            }//遍历整个版本2的while tmp4结束。

                            //while遍历完了，版本2已全部遍历一遍，此时可以比较两对克隆是否发生共变。
                            if (Utilities.judge1_3CCClone(string1, string2, string3, string4)) {
                                System.out.println("Finding co-modified! Write to file...");
                                ++cflag;
                                //把文件信息和克隆信息写入文件；
                                //如果是共变克隆对之间的，用换行分开
                                if (cflag != 1) {//不是第一行，各克隆对之间用换行分开。
                                    bufferedWriterOld.write("\n\n\n");
                                    bufferedWriterNew.write("\n\n\n");
                                }

                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOld.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOld.write(fptmp1 + "\n");

                                bufferedWriterOld.write(fptmp2 + "\n");
                                bufferedWriterOld.write("</clonepair" + cflag + ">\n");

                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNew.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNew.write(fptmp3 + "\n");

                                bufferedWriterNew.write(fptmp4 + "\n");
                                bufferedWriterNew.write("</clonepair" + cflag + ">\n");

                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp1 + "\n");
                                for (int i = 0; string1[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string1[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write("\n");

                                //开始写入版本1克隆2克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp2 + "\n");
                                for (int i = 0; string2[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string2[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");

                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp3 + "\n");
                                for (int i = 0; string3[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string3[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write("\n");

                                //开始写入版本2克隆2信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp4 + "\n");
                                for (int i = 0; string4[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string4[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                            }
                        }

                        //此时路径相同，函数名不同，对应匹配为1-4&&2-3;
                        else if (string1[0].equals(string4[0].trim()) && string2[0].trim().equals(string3[0].trim())) {
                            // 如果两个版本对应代码片段的，总行数只差小于一百，并且，代码Startline位置的差距小于100，则认为是不同版本下的相同函数片段。
                            // 那么可以认为版本1和版本2的这两对克隆是匹配的，并且string1对应string3,string2对应string4;
                            // 此时已经找到第一对合适的克隆！
                            // 继续往下寻找，直到把整个版本2文件全部找完，然后选startline差距最小的克隆对，与版本1克隆对作为共变比较！
                            tmp3 = bufferedReader3.readLine();
                            while (tmp3 != null) {
                                if (tmp3.contains(ft1) || tmp3.contains(ft2)) {
                                    tmp4 = bufferedReader3.readLine();
                                    if (tmp4.contains("Eclipse")) {
                                        String tmpfilepath3 = tmp3.substring(tmp3.indexOf("Eclipse"), tmp3.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆1文件路径；
                                        String tmpfilepath4 = tmp4.substring(tmp4.indexOf("Eclipse"), tmp4.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆2文件路径；

                                        tmpfptmp3 = tmp3;//保存所取文件信息；
                                        tmpfptmp4 = tmp4;

                                        if (ft1.equals(ft2) && ft1.equals(tmpfilepath3) && ft1.equals(tmpfilepath4) && (!string1[0].trim().equals(string2[0].trim()))) {

                                            //把版本2c中的克隆1源码提取出来，用字符串数组存储；先把对应位置的克隆源码提取出来再说！
                                            String tmppcid3 = Utilities.getPcid(tmp3);

                                            FileReader tmpfileReader5 = new FileReader(inputf2c);
                                            BufferedReader tmpbufferedReader5 = new BufferedReader(tmpfileReader5);//去class文件找源码；   //版本2c中的克隆1源码提出来,   bufferedReader5,tmp6;

                                            String tmptmp6 = tmpbufferedReader5.readLine();

                                            pcid = "pcid=" + '\"' + tmppcid3 + '\"';//  用pcid进行定位

                                            while (tmptmp6 != null) {//遍历整个文件，用pcid定位；
                                                if (tmptmp6.contains(pcid))
                                                    break;
                                                tmptmp6 = tmpbufferedReader5.readLine();
                                            }
                                            while (tmptmp6 != null) {
                                                if (tmptmp6.contains("<source")) {
                                                    tmptmp6 = tmpbufferedReader5.readLine();
                                                    continue;
                                                }
                                                break;
                                            }
                                            String tfuncname3 = tmptmp6;//获得该函数名;

                                            //把版本2c中的克隆2源码提取出来，用字符串数组存储；
                                            String tmppcid4 = Utilities.getPcid(tmp4);

                                            FileReader tmpfileReader6 = new FileReader(inputf2c);
                                            BufferedReader tmpbufferedReader6 = new BufferedReader(tmpfileReader6);//去class文件找源码；   //版本2c中的克隆2源码提出来,   bufferedReader6,tmp7;

                                            String tmptmp7 = tmpbufferedReader6.readLine();

                                            pcid = "pcid=" + '\"' + tmppcid4 + '\"';//  用pcid进行定位

                                            while (tmptmp7 != null) {//遍历整个文件，用pcid定位；
                                                if (tmptmp7.contains(pcid))
                                                    break;
                                                tmptmp7 = tmpbufferedReader6.readLine();
                                            }
                                            while (tmptmp7 != null) {
                                                if (tmptmp7.contains("<source")) {
                                                    tmptmp7 = tmpbufferedReader6.readLine();
                                                    continue;
                                                }
                                                break;
                                            }

                                            String tfuncname4 = tmptmp7;

                                            //遍历整个过程中，匹配上了1-3&&2-4,之前是1-4&&2-3
                                            if (string1[0].equals(tfuncname3) && string2[0].equals(tfuncname4) && ft1.equals(tmpfilepath3) && ft2.equals(tmpfilepath4)) {
                                                int tmpstartline3 = Utilities.getStartLine(tmpfptmp3);

                                                int tmpstartline4 = Utilities.getStartLine(tmpfptmp4);

                                                int c1 = Math.abs(startline1 - tmpstartline3);
                                                int c2 = Math.abs(startline1 - startline4);
                                                int c3 = Math.abs(startline2 - tmpstartline4);
                                                int c4 = Math.abs(startline2 - startline3);
                                                if (c1 <= c2 || c3 <= c4) {     //存在一个距离版本1克隆更近，那么则更新String3和string4；
                                                    fptmp3 = tmpfptmp4;
                                                    fptmp4 = tmpfptmp3;
                                                    //更新string4
                                                    j = 0;
                                                    Arrays.fill(string4, null);
                                                    string4[j++] = tmptmp6;
                                                    while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                        string4[j++] = tmptmp6;
                                                    }

                                                    j = 0;
                                                    Arrays.fill(string3, null);
                                                    string3[j++] = tmptmp7;
                                                    while (!(tmptmp7 = tmpbufferedReader6.readLine()).contains("</source>")) {
                                                        string3[j++] = tmptmp7;
                                                    }
                                                }
                                            }
                                            //遍历整个过程中，匹配上了1-4&&2-3
                                            else if (string1[0].equals(tfuncname4) && string2[0].equals(tfuncname3)
                                                    && ft1.equals(tmpfilepath4) && ft2.equals(tmpfilepath3)) {

                                                int tmpstartline3 = Utilities.getStartLine(tmpfptmp3);
                                                int tmpstartline4 = Utilities.getStartLine(tmpfptmp4);

                                                int c1 = Math.abs(startline1 - tmpstartline4);
                                                int c2 = Math.abs(startline1 - startline4);
                                                int c3 = Math.abs(startline2 - tmpstartline3);
                                                int c4 = Math.abs(startline2 - startline3);

                                                if (c1 <= c2 || c3 <= c4) {     //存在一个距离版本1克隆更近，那么则更新String3和string4；
                                                    //更新string3;
                                                    fptmp3 = tmpfptmp3;
                                                    fptmp4 = tmpfptmp4;
                                                    j = 0;
                                                    Arrays.fill(string3, null);
                                                    string3[j++] = tmptmp6;
                                                    while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                        string3[j++] = tmptmp6;
                                                    }

                                                    j = 0;
                                                    Arrays.fill(string4, null);
                                                    string4[j++] = tmptmp7;
                                                    while (!(tmptmp7 = tmpbufferedReader6.readLine()).contains("</source>")) {
                                                        string4[j++] = tmptmp7;
                                                    }
                                                }
                                            }
                                            tmpbufferedReader5.close();
                                            tmpbufferedReader6.close();
                                        }

                                    }
                                    tmp3 = tmp4;
                                } else
                                    tmp3 = bufferedReader3.readLine();
                            }//遍历整个版本2的while tmp4结束。
                            //while遍历完了，版本2已全部遍历一遍，此时可以比较两对克隆是否发生共变。
                            if (Utilities.judge1_4CCClone(string1, string2, string3, string4)) {
                                System.out.println("Finding co-modified! Write to file...");
                                ++cflag;
                                //把文件信息和克隆信息写入文件；
                                //如果是共变克隆对之间的，用换行分开
                                if (cflag != 1) {//不是第一行，各克隆对之间用换行分开。
                                    bufferedWriterOld.write("\n\n\n");
                                    bufferedWriterNew.write("\n\n\n");
                                }
                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOld.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOld.write(fptmp1 + "\n");
                                bufferedWriterOld.write(fptmp2 + "\n");
                                bufferedWriterOld.write("</clonepair" + cflag + ">\n");
                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNew.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNew.write(fptmp4 + "\n");
                                bufferedWriterNew.write(fptmp3 + "\n");
                                bufferedWriterNew.write("</clonepair" + cflag + ">\n");
                                //开始写入版本1克隆1克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp1 + "\n");
                                for (int i = 0; string1[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string1[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write("\n");
                                //开始写入版本1克隆2克隆信息；
                                bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterOldWithCode.write(fptmp2 + "\n");
                                for (int i = 0; string2[i] != null; ++i)
                                    bufferedWriterOldWithCode.write(string2[i] + "\n");
                                bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");
                                //开始写入版本2克隆1克隆信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp4 + "\n");
                                for (int i = 0; string4[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string4[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write("\n");
                                //开始写入版本2克隆2信息；
                                bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                                bufferedWriterNewWithCode.write(fptmp3 + "\n");
                                for (int i = 0; string3[i] != null; ++i)
                                    bufferedWriterNewWithCode.write(string3[i] + "\n");
                                bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                            }
                        }
                    }
                    bufferedReader5t.close();
                    bufferedReader6t.close();
                }

                //情况2进入入口：ft1==ft2为false                                                                                                                                         //情况2：ft1==ft2为假
                //如果f1==ft2为false，那么多匹配到的也只能只存在1-3&&2-4和1-4&&2-3两种情况
                else if (ft1.equals(filepath3) && ft2.equals(filepath4) || ft1.equals(filepath4) && ft2.equals(filepath3)) {
                    //filepath3匹配到了f1或ft2，filepath4正好匹配到了剩下一个，那就是合适的匹配。
                    //我们目前仍然不知道，匹配到的路径中对应函数名字是否匹配我们版本1的函数名字，有可能只是恰好，路径相同，函数不同！！！！
                    //先把对应位置的克隆源码提取出来再说！把版本2c中的克隆1源码提取出来，用字符串数组存储；
                    String pcid3 = Utilities.getPcid(tmp3);

                    FileReader fileReader4 = new FileReader(inputf2c);
                    BufferedReader bufferedReader4 = new BufferedReader(fileReader4);//去class文件找源码；          //版本2c中的克隆2.1源码提出来,   bufferedReader4,tmp5;
                    String tmp5 = bufferedReader4.readLine();
                    pcid = "pcid=" + '\"' + pcid3 + '\"';//  用pcid进行定位

                    while (tmp5 != null) {//遍历整个文件，用pcid定位；
                        if (tmp5.contains(pcid))
                            break;
                        tmp5 = bufferedReader4.readLine();
                    }
                    while (tmp5 != null) {
                        if (tmp5.contains("<source")) {
                            tmp5 = bufferedReader4.readLine();
                            continue;
                        }
                        break;
                    }

                    //把版本2c克隆2.1源码按行存入字符串数组string3；
                    j = 0;
                    string3[j++] = tmp5;
                    while (!(tmp5 = bufferedReader4.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                        string3[j++] = tmp5;
                    }

                    bufferedReader4.close();

                    //把版本2c中的克隆2源码提取出来，用字符串数组存储；
                    String pcid4 = Utilities.getPcid(tmp4);
                    FileReader fileReader5 = new FileReader(inputf2c);
                    BufferedReader bufferedReader5 = new BufferedReader(fileReader5);//去class文件找源码；   //版本2c中的克隆2.2源码提出来,   bufferedReader5,tmp6;
                    String tmp6 = bufferedReader5.readLine();
                    pcid = "pcid=" + '\"' + pcid4 + '\"';//  用pcid进行定位

                    while (tmp6 != null) {//遍历整个文件，用pcid定位；
                        if (tmp6.contains(pcid))
                            break;
                        tmp6 = bufferedReader5.readLine();
                    }
                    while (tmp6 != null) {
                        if (tmp6.contains("<source")) {
                            tmp6 = bufferedReader5.readLine();
                            continue;
                        }
                        break;
                    }

                    //把版本2c克隆2源码按行存入字符串数组string4；
                    j = 0;
                    string4[j++] = tmp6;
                    while (!(tmp6 = bufferedReader5.readLine()).contains("</source>")) {//</source>的这行，代表这个克隆的源码结束位置；
                        string4[j++] = tmp6;
                    }

                    bufferedReader5.close();

                    int startline1 = Utilities.getStartLine(fptmp1);

                    int startline2 = Utilities.getStartLine(fptmp2);

                    int startline3 = Utilities.getStartLine(fptmp3);

                    int startline4 = Utilities.getStartLine(fptmp4);

                    // 情况2.1：仅1-3,2-4
                    // 路径不同，只存在1-3&&和2-4情况。
                    if (string1[0].equals(string3[0]) && string2[0].equals(string4[0]) && ft1.equals(filepath3) && ft2.equals(filepath4)) {

                        // 情况1：此时1和3路径匹配了，2和4路径匹配了，并且函数名也相应匹配了。
                        // 此时已经找到第一对合适的克隆对了。
                        // 继续往下寻找，直到把整个版本2文件全部找完，然后选startline差距最小的克隆对，与版本1克隆对作为共变比较！
                        tmp3 = bufferedReader3.readLine();
                        while (tmp3 != null) {
                            if (tmp3.contains(ft1) || tmp3.contains(ft2)) {
                                tmp4 = bufferedReader3.readLine();
                                if (tmp4.contains("Eclipse")) {

                                    String tmpfilepath3 = tmp3.substring(tmp3.indexOf("Eclipse"), tmp3.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆1文件路径；
                                    String tmpfilepath4 = tmp4.substring(tmp4.indexOf("Eclipse"), tmp4.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆2文件路径；

                                    tmpfptmp3 = tmp3;//保存所取文件信息；
                                    tmpfptmp4 = tmp4;

                                    //路径不同1&3,二次遍历中，同样的如果匹配到了路径，则分为1&3或1&4的情况
                                    if (ft1.equals(filepath3) && ft2.equals(filepath4) || ft1.equals(filepath4) && ft2.equals(filepath3)) {

                                        //把版本2c中的克隆1源码提取出来，用字符串数组存储；先把对应位置的克隆源码提取出来再说！
                                        String tmppcid3 = Utilities.getPcid(tmp3);

                                        FileReader tmpfileReader4 = new FileReader(inputf2c);
                                        BufferedReader tmpbufferedReader4 = new BufferedReader(tmpfileReader4);
                                        String tmptmp5 = tmpbufferedReader4.readLine();

                                        pcid = "pcid=" + '\"' + tmppcid3 + '\"';//  用pcid进行定位

                                        while (tmptmp5 != null) {//遍历整个文件，用pcid定位；
                                            if (tmptmp5.contains(pcid))
                                                break;
                                            tmptmp5 = tmpbufferedReader4.readLine();
                                        }
                                        while (tmptmp5 != null) {
                                            if (tmptmp5.contains("<source")) {
                                                tmptmp5 = tmpbufferedReader4.readLine();
                                                continue;
                                            }
                                            break;
                                        }
                                        String funcname3 = tmptmp5;//获得该函数名;

                                        //把版本2c中的克隆2源码提取出来，用字符串数组存储；
                                        String tmppcid4 = Utilities.getPcid(tmp4);
                                        FileReader tmpfileReader5 = new FileReader(inputf2c);
                                        BufferedReader tmpbufferedReader5 = new BufferedReader(tmpfileReader5);

                                        String tmptmp6 = tmpbufferedReader5.readLine();

                                        pcid = "pcid=" + '\"' + tmppcid4 + '\"';//  用pcid进行定位

                                        while (tmptmp6 != null) {//遍历整个文件，用pcid定位；
                                            if (tmptmp6.contains(pcid))
                                                break;
                                            tmptmp6 = tmpbufferedReader5.readLine();
                                        }
                                        while (tmptmp6 != null) {
                                            if (tmptmp6.contains("<source")) {
                                                tmptmp6 = tmpbufferedReader5.readLine();
                                                continue;
                                            }
                                            break;
                                        }
                                        String funcname4 = tmptmp6;//提取函数名即可。

                                        //不同路径1&3情况下，二次遍历整个过程中，匹配上了1-3&&2-4的情况。
                                        if (string1[0].equals(funcname3) && string2[0].equals(funcname4) && ft1.equals(tmpfilepath3) && ft2.equals(tmpfilepath4)) {

                                            int tmpstartline3 = Utilities.getStartLine(tmpfptmp3);

                                            int tmpstartline4 = Utilities.getStartLine(tmpfptmp4);

                                            int c1 = Math.abs(startline1 - tmpstartline3);
                                            int c2 = Math.abs(startline1 - startline3);
                                            int c3 = Math.abs(startline2 - tmpstartline4);
                                            int c4 = Math.abs(startline2 - startline4);

                                            if (c1 <= c2 || c3 <= c4) {     //存在一个距离版本1克隆更近的克隆对，那么则更新String3和string4；
                                                fptmp3 = tmpfptmp3;//更新保存的文件路径信息
                                                fptmp4 = tmpfptmp4;

                                                //更新string3;
                                                j = 0;
                                                Arrays.fill(string3, null);
                                                string3[j++] = tmptmp5;
                                                while (!(tmptmp5 = tmpbufferedReader4.readLine()).contains("</source>")) {
                                                    string3[j++] = tmptmp5;
                                                }

                                                //更新string4
                                                j = 0;
                                                Arrays.fill(string4, null);
                                                string4[j++] = tmptmp6;
                                                while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                    string4[j++] = tmptmp6;
                                                }
                                            }
                                        }
                                        //不同路径1&3情况下：二次遍历整个过程中，匹配上了1-4&&2-3
                                        else if (string1[0].equals(funcname4) && string2[0].equals(funcname3)
                                                && ft1.equals(tmpfilepath4) && ft2.equals(tmpfilepath3)) {
                                            int tmpstartline3 = Utilities.getStartLine(tmpfptmp3);
                                            int tmpstartline4 = Utilities.getStartLine(tmpfptmp4);

                                            int c1 = Math.abs(startline1 - tmpstartline4);
                                            int c2 = Math.abs(startline1 - startline3);
                                            int c3 = Math.abs(startline2 - tmpstartline3);
                                            int c4 = Math.abs(startline2 - startline4);

                                            if (c1 <= c2 || c3 <= c4) {     //存在一个距离版本1克隆更近，那么则更新String3和string4；以及对应fptmp3信息。
                                                fptmp3 = tmpfptmp4;//更新文件信息
                                                fptmp4 = tmpfptmp3;
                                                //错位匹配，因此更新string4;
                                                j = 0;
                                                Arrays.fill(string4, null);
                                                string4[j++] = tmptmp5;
                                                while (!(tmptmp5 = tmpbufferedReader4.readLine()).contains("</source>")) {
                                                    string4[j++] = tmptmp5;
                                                }

                                                //错位更新string3
                                                j = 0;
                                                Arrays.fill(string3, null);
                                                string3[j++] = tmptmp6;
                                                while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                    string3[j++] = tmptmp6;
                                                }
                                            }
                                        }
                                        //创建的中间流可以关闭了。
                                        tmpbufferedReader4.close();
                                        tmpbufferedReader5.close();
                                    }

                                }
                                tmp3 = tmp4;
                            } else
                                tmp3 = bufferedReader3.readLine();
                        }//二次遍历整个版本2的while tmp3结束，版本2克隆对检测完毕。

                        //while遍历完了，版本2已全部遍历一遍，此时可以比较两对克隆是否发生共变。
                        //检测是否发生共变，发生共变则写入文件。
                        if (Utilities.judge1_3CCClone(string1, string2, string3, string4)) {
                            System.out.println("Finding co-modified! Write to file...");
                            ++cflag;
                            //把文件信息和克隆信息写入文件；
                            //如果是共变克隆对之间的，用换行分开
                            if (cflag != 1) {//不是第一行，各克隆对之间用换行分开。
                                bufferedWriterOld.write("\n\n\n");
                                bufferedWriterNew.write("\n\n\n");
                            }

                            //开始写入版本1克隆1克隆信息；
                            bufferedWriterOld.write("<clonepair" + cflag + ">\n");
                            bufferedWriterOld.write(fptmp1 + "\n");

                            bufferedWriterOld.write(fptmp2 + "\n");
                            bufferedWriterOld.write("</clonepair" + cflag + ">\n");

                            //开始写入版本2克隆1克隆信息；
                            bufferedWriterNew.write("<clonepair" + cflag + ">\n");
                            bufferedWriterNew.write(fptmp3 + "\n");

                            bufferedWriterNew.write(fptmp4 + "\n");
                            bufferedWriterNew.write("</clonepair" + cflag + ">\n");

                            //开始写入版本1克隆1克隆信息；
                            bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                            bufferedWriterOldWithCode.write(fptmp1 + "\n");
                            for (int i = 0; string1[i] != null; ++i)
                                bufferedWriterOldWithCode.write(string1[i] + "\n");
                            bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");
                            bufferedWriterOldWithCode.write("\n");

                            //开始写入版本1克隆2克隆信息；
                            bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                            bufferedWriterOldWithCode.write(fptmp2 + "\n");
                            for (int i = 0; string2[i] != null; ++i)
                                bufferedWriterOldWithCode.write(string2[i] + "\n");
                            bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");

                            //开始写入版本2克隆1克隆信息；
                            bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                            bufferedWriterNewWithCode.write(fptmp3 + "\n");
                            for (int i = 0; string3[i] != null; ++i)
                                bufferedWriterNewWithCode.write(string3[i] + "\n");
                            bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                            bufferedWriterNewWithCode.write("\n");

                            //开始写入版本2克隆2信息；
                            bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                            bufferedWriterNewWithCode.write(fptmp4 + "\n");
                            for (int i = 0; string4[i] != null; ++i)
                                bufferedWriterNewWithCode.write(string4[i] + "\n");
                            bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                        }
                    }

                    //情况2.2，仅1-4,2-3成立。
                    //路径不同，仅1-4&&2-3情况
                    else if (string1[0].equals(string4[0]) && string2[0].equals(string3[0]) && ft1.equals(filepath4) && ft2.equals(filepath3)) {
//                                此时已经找到第一对合适的克隆！
                        //继续往下寻找，直到把整个版本2文件全部找完，然后选startline差距最小的克隆对，与版本1克隆对作为共变比较！
                        tmp3 = bufferedReader3.readLine();
                        while (tmp3 != null) {
                            if (tmp3.contains(ft1) || tmp3.contains(ft2)) {
                                tmp4 = bufferedReader3.readLine();
                                if (tmp4.contains("Eclipse")) {

                                    String tmpfilepath3 = tmp3.substring(tmp3.indexOf("Eclipse"), tmp3.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆1文件路径；
                                    String tmpfilepath4 = tmp4.substring(tmp4.indexOf("Eclipse"), tmp4.indexOf(Utilities.postfix) + Utilities.postfix.length());//取版本2中克隆2文件路径；

                                    tmpfptmp3 = tmp3;//保存所取文件信息；
                                    tmpfptmp4 = tmp4;

                                    //路径不同1&4情况下，二次遍历路径匹配，同样的分为1&3和1&4两种情况
                                    if (ft1.equals(filepath3) && ft2.equals(filepath4) || ft1.equals(filepath4) && ft2.equals(filepath3)) {

                                        //把版本2c中的克隆1源码提取出来，用字符串数组存储；先把对应位置的克隆源码提取出来再说！
                                        String tmppcid3 = Utilities.getPcid(tmp3);
                                        FileReader tmpfileReader5 = new FileReader(inputf2c);
                                        BufferedReader tmpbufferedReader5 = new BufferedReader(tmpfileReader5);//去class文件找源码；
                                        String tmptmp6 = tmpbufferedReader5.readLine();
                                        pcid = "pcid=" + '\"' + tmppcid3 + '\"';//  用pcid进行定位

                                        while (tmptmp6 != null) {//遍历整个文件，用pcid定位；
                                            if (tmptmp6.contains(pcid))
                                                break;
                                            tmptmp6 = tmpbufferedReader5.readLine();
                                        }
                                        while (tmptmp6 != null) {
                                            if (tmptmp6.contains("<source")) {
                                                tmptmp6 = tmpbufferedReader5.readLine();
                                                continue;
                                            }
                                            break;
                                        }
                                        String funcname3 = tmptmp6;//获得该函数名;

                                        //把版本2c中的克隆2源码提取出来，用字符串数组存储；
                                        String tmppcid4 = Utilities.getPcid(tmp4);
                                        FileReader tmpfileReader6 = new FileReader(inputf2c);
                                        BufferedReader tmpbufferedReader6 = new BufferedReader(tmpfileReader6);//去class文件找源码；
                                        String tmptmp7 = tmpbufferedReader6.readLine();
                                        pcid = "pcid=" + '\"' + tmppcid4 + '\"';//  用pcid进行定位

                                        while (tmptmp7 != null) {//遍历整个文件，用pcid定位；
                                            if (tmptmp7.contains(pcid))
                                                break;
                                            tmptmp7 = tmpbufferedReader6.readLine();
                                        }
                                        while (tmptmp7 != null) {
                                            if (tmptmp7.contains("<source")) {
                                                tmptmp7 = tmpbufferedReader6.readLine();
                                                continue;
                                            }
                                            break;
                                        }
                                        String funcname4 = tmptmp7;

                                        //二次遍历整个过程中，匹配上了1-3&&2-4,一次是1-4&&2-3。
                                        if (string1[0].equals(funcname3) && string2[0].equals(funcname4) && ft1.equals(tmpfilepath3) && ft2.equals(tmpfilepath4)) {
                                            int tmpstartline3 = Utilities.getStartLine(tmpfptmp3);
                                            int tmpstartline4 = Utilities.getStartLine(tmpfptmp4);

                                            int c1 = Math.abs(startline1 - tmpstartline3);
                                            int c2 = Math.abs(startline1 - startline4);
                                            int c3 = Math.abs(startline2 - tmpstartline4);
                                            int c4 = Math.abs(startline2 - startline3);

                                            if (c1 <= c2 || c3 <= c4) {     //存在一个距离版本1克隆更近，那么则更新String3和string4；
                                                fptmp3 = tmpfptmp4;
                                                fptmp4 = tmpfptmp3;
                                                //更新string4
                                                j = 0;
                                                Arrays.fill(string4, null);
                                                string4[j++] = tmptmp6;
                                                while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                    string4[j++] = tmptmp6;
                                                }

                                                //交叉更新string3
                                                j = 0;
                                                Arrays.fill(string3, null);
                                                string3[j++] = tmptmp7;
                                                while (!(tmptmp7 = tmpbufferedReader6.readLine()).contains("</source>")) {
                                                    string3[j++] = tmptmp7;
                                                }
                                            }
                                        }
                                        //遍历整个过程中，匹配上了1-4&&2-3
                                        else if (string1[0].equals(funcname4) && string2[0].equals(funcname3)
                                                && ft1.equals(tmpfilepath4) && ft2.equals(tmpfilepath3)) {
                                            int tmpstartline3 = Utilities.getStartLine(tmpfptmp3);
                                            int tmpstartline4 = Utilities.getStartLine(tmpfptmp4);

                                            int c1 = Math.abs(startline1 - tmpstartline4);
                                            int c2 = Math.abs(startline1 - startline4);
                                            int c3 = Math.abs(startline2 - tmpstartline3);
                                            int c4 = Math.abs(startline2 - startline3);

                                            if (c1 <= c2 || c3 <= c4) {     //存在一个距离版本1克隆更近，那么则更新String3和string4；
                                                //更新string3;
                                                fptmp3 = tmpfptmp3;
                                                fptmp4 = tmpfptmp4;
                                                j = 0;
                                                Arrays.fill(string3, null);
                                                string3[j++] = tmptmp6;
                                                while (!(tmptmp6 = tmpbufferedReader5.readLine()).contains("</source>")) {
                                                    string3[j++] = tmptmp6;
                                                }

                                                j = 0;
                                                Arrays.fill(string4, null);
                                                string4[j++] = tmptmp7;
                                                while (!(tmptmp7 = tmpbufferedReader6.readLine()).contains("</source>")) {
                                                    string4[j++] = tmptmp7;
                                                }
                                            }
                                        }
                                        tmpbufferedReader5.close();
                                        tmpbufferedReader6.close();
                                    }
                                }
                                tmp3 = tmp4;
                            } else
                                tmp3 = bufferedReader3.readLine();
                        }//遍历整个版本2的while tmp4结束。

                        //while遍历完了，版本2已全部遍历一遍，此时可以比较两对克隆是否发生共变。

                        if (Utilities.judge1_4CCClone(string1, string2, string3, string4)) {
                            System.out.println("Finding co-modified! Write to file...");
                            ++cflag;
                            //把文件信息和克隆信息写入文件；
                            //如果是共变克隆对之间的，用换行分开
                            if (cflag != 1) {//不是第一行，各克隆对之间用换行分开。
                                bufferedWriterOld.write("\n\n\n");
                                bufferedWriterNew.write("\n\n\n");
                            }

                            //开始写入版本1克隆1克隆信息；
                            bufferedWriterOld.write("<clonepair" + cflag + ">\n");
                            bufferedWriterOld.write(fptmp1 + "\n");

                            bufferedWriterOld.write(fptmp2 + "\n");
                            bufferedWriterOld.write("</clonepair" + cflag + ">\n");

                            //开始写入版本2克隆1克隆信息；
                            bufferedWriterNew.write("<clonepair" + cflag + ">\n");
                            bufferedWriterNew.write(fptmp4 + "\n");

                            bufferedWriterNew.write(fptmp3 + "\n");
                            bufferedWriterNew.write("</clonepair" + cflag + ">\n");

                            //开始写入版本1克隆1克隆信息；
                            bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                            bufferedWriterOldWithCode.write(fptmp1 + "\n");
                            for (int i = 0; string1[i] != null; ++i)
                                bufferedWriterOldWithCode.write(string1[i] + "\n");
                            bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");
                            bufferedWriterOldWithCode.write("\n");

                            //开始写入版本1克隆2克隆信息；
                            bufferedWriterOldWithCode.write("<clonepair" + cflag + ">\n");
                            bufferedWriterOldWithCode.write(fptmp2 + "\n");
                            for (int i = 0; string2[i] != null; ++i)
                                bufferedWriterOldWithCode.write(string2[i] + "\n");
                            bufferedWriterOldWithCode.write("</clonepair" + cflag + ">\n");

                            //开始写入版本2克隆1克隆信息；
                            bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                            bufferedWriterNewWithCode.write(fptmp4 + "\n");
                            for (int i = 0; string4[i] != null; ++i)
                                bufferedWriterNewWithCode.write(string4[i] + "\n");
                            bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                            bufferedWriterNewWithCode.write("\n");

                            //开始写入版本2克隆2信息；
                            bufferedWriterNewWithCode.write("<clonepair" + cflag + ">\n");
                            bufferedWriterNewWithCode.write(fptmp3 + "\n");
                            for (int i = 0; string3[i] != null; ++i)
                                bufferedWriterNewWithCode.write(string3[i] + "\n");
                            bufferedWriterNewWithCode.write("</clonepair" + cflag + ">\n");
                        }
                    }
                }

                //情况3进入入口：前面两个if条件必进，但是中间可能终止，所有tmp3可能为null或者值；
                if (tmp3 != null)
                    tmp3 = bufferedReader3.readLine();
            }
            bufferedReader3.close();
        }
        bufferedWriterALL.write("本次克隆从" + oldfilename + "----->" + newfilename + "，一共检测了" + num + "个克隆对，发生了" + cflag + "次共变\n\n\n");
        bufferedWriterOld.close();
        bufferedReader.close();
        bufferedWriterNew.close();
        bufferedWriterALL.close();
        bufferedWriterOldWithCode.close();
        bufferedWriterNewWithCode.close();
    }
}

