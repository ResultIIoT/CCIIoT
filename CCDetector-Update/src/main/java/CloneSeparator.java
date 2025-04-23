import java.io.*;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author CrazyYao
 * @create 2022-03-19 18:03
 */

// 实现从Nicad结果文件中提取1,2,3型克隆，并求克隆数和源码总数(去重),最后对于每种克隆类型，根据代码行范围，对克隆对进行分配。

// 输入:InputCS文件夹。把NiCad三种克隆的结果文件夹放入该文件夹下，运行即可。
// 输出：“......_clone-abstract”文件夹。

public class CloneSeparator {
    public static void main(String[] args) throws Exception {
        String InputCS = "src/main/inputCS";
        run(InputCS);
    }

    public static void run(String srcdir) throws Exception {
        File sourcefiledir = new File(srcdir);
        File[] files = sourcefiledir.listFiles();

        int index = 0;
        for (File file : files) {
            if (!file.getName().equals("Results")) {
                files[index++] = file;
            }
        }
        // 将数组大小调整到实际的文件数量
        files = java.util.Arrays.copyOf(files, index);

        assert files != null;
        Arrays.sort(files, new AlphanumFileComparator<>());

        String tmp1, tmp2, tmp3;
        int i = 0, j = 1;
        int num = 0;
        while (j < files.length) {
            num++;
            System.out.println("The " + num + " project is being detected");
            String name = files[i].getName();
            int x = files[i].getName().indexOf('_');
            String subjectname = files[i].getName().substring(0, files[i].getName().indexOf('_'));
            System.out.println("project name: " + subjectname);
            // 求1型克隆个数，以及代码行数
            String resultpath = files[j].getAbsolutePath() + File.separator + subjectname + '_' + "clone-abstract";
            File file = new File(resultpath);
            if (file.exists()) {
                i += 2;
                j += 2;
                continue;
            }
            file.mkdir();
            BufferedWriter bufferedWriterSummary = new BufferedWriter(
                    new FileWriter(resultpath + File.separator + subjectname + "-Summary.txt"));
            BufferedWriter bufferedWriter1 = new BufferedWriter(
                    new FileWriter(resultpath + File.separator + subjectname + "-Type1.txt"));

            File[] files1 = files[j].listFiles();
            assert files1 != null;
            Arrays.sort(files1, new AlphanumFileComparator<>());
            int lenj = files1.length;
            BufferedReader bufferedReader1 = new BufferedReader(new FileReader(files1[lenj - 1]));

            String tmp = bufferedReader1.readLine();
            while (tmp != null && !tmp.contains("<clone nlines"))
                tmp = bufferedReader1.readLine();

            if (tmp == null) {
                bufferedWriter1.write("there are no Type1 clone pairs!\n");
                bufferedWriter1.close();
                int clonenum1 = Utilities.GetNiCadTotalCloneNum(files1[lenj - 1]);
                int SLOCnum1 = Utilities.GetTotalSLOC(files1[lenj - 1]);
                bufferedWriterSummary
                        .write("Type1:  There are " + clonenum1 + " clone pairs and Total " + SLOCnum1 + " SLOC\n\n");
            } else {
                while (!tmp.contains("</clones>")) {
                    bufferedWriter1.write(tmp + "\n");
                    tmp = bufferedReader1.readLine();
                }

                bufferedWriter1.close();// 克隆1类型文件提取完毕！
                int clonenum1 = Utilities.GetNiCadTotalCloneNum(files1[lenj - 1]);
                int SLOCnum1 = Utilities.GetTotalSLOC(files1[lenj - 1]);
                bufferedWriterSummary
                        .write("Type1:  There are " + clonenum1 + " clone pairs and Total " + SLOCnum1 + " SLOC \n\n");
            }
            bufferedReader1.close();

            // 求2型克隆个数，以及代码行数；
            BufferedReader bufferedReader2 = new BufferedReader(new FileReader(
                    files[i].getAbsolutePath() + File.separator + subjectname + "_functions-blind-clones-0.00.xml"));
            BufferedWriter bufferedWriter2 = new BufferedWriter(
                    new FileWriter(resultpath + File.separator + subjectname + "-Type2.txt"));

            tmp1 = bufferedReader2.readLine();
            while (tmp1 != null && !tmp1.contains("<clone nlines")) {
                tmp1 = bufferedReader2.readLine();
            }
            if (tmp1 == null) {
                bufferedWriter2.write("There are no Type-2 clone pairs!\n");
                bufferedWriter2.close();
                int clonenum2 = Utilities
                        .GetNiCadTotalCloneNum(new File(resultpath + File.separator + subjectname + "-Type2.txt"));
                int SLOCnum2 = Utilities
                        .GetTotalSLOC(new File(resultpath + File.separator + subjectname + "-Type2.txt"));
                bufferedWriterSummary
                        .write("Type2:  There are " + clonenum2 + " clone pairs and Total " + SLOCnum2 + " SLOC \n\n");
            } else {
                BufferedReader bufferedReader1t = new BufferedReader(new FileReader(files1[lenj - 1]));
                tmp = bufferedReader1t.readLine();
                while (tmp != null && !tmp.contains("<clone nlines")) {
                    tmp = bufferedReader1t.readLine();
                }
                bufferedReader1t.close();
                if (tmp == null) {// 说明类型1一个克隆对都没 有
                    bufferedWriter2.write(tmp1 + "\n");
                    while (!(tmp1 = bufferedReader2.readLine()).contains("</clones>"))
                        bufferedWriter2.write(tmp1 + "\n");

                    bufferedWriter2.close();
                    int clonenum2 = Utilities
                            .GetNiCadTotalCloneNum(new File(resultpath + File.separator + subjectname + "-Type2.txt"));
                    int SLOCnum2 = Utilities
                            .GetTotalSLOC(new File(resultpath + File.separator + subjectname + "-Type2.txt"));
                    bufferedWriterSummary.write(
                            "Type2:  There are " + clonenum2 + " clone pairs and Total " + SLOCnum2 + " SLOC \n\n");
                } else {
                    while (tmp1 != null) {
                        String tmp4;
                        int ifexit = 0;
                        tmp2 = tmp1;// tmp2存储克隆对开始信息行
                        tmp3 = bufferedReader2.readLine();// 第一个克隆信息
                        tmp1 = bufferedReader2.readLine();// 第二个克隆信息；
                        tmp4 = bufferedReader2.readLine();

                        String pcid1 = Utilities.getPcid(tmp3);
                        String pcid2 = Utilities.getPcid(tmp1);

                        bufferedReader1t = new BufferedReader(new FileReader(files1[lenj - 1]));
                        tmp = bufferedReader1t.readLine();
                        while (!tmp.contains("<clone nlines")) {
                            tmp = bufferedReader1t.readLine();
                        }
                        while (tmp != null) {// 遍历完克隆文件1。
                            if (tmp.contains("<source")) {
                                String tmppcid1 = Utilities.getPcid(tmp);
                                tmp = bufferedReader1t.readLine();
                                String tmppcid2 = Utilities.getPcid(tmp);
                                tmp = bufferedReader1t.readLine();
                                if (tmppcid1.equals(pcid1) && tmppcid2.equals(pcid2)
                                        || tmppcid1.equals(pcid2) && tmppcid2.equals(pcid1)) {
                                    ifexit++;
                                    break;
                                }
                            } else
                                tmp = bufferedReader1t.readLine();
                        }
                        if (ifexit == 0) {
                            bufferedWriter2.write(tmp2 + "\n");
                            bufferedWriter2.write(tmp3 + "\n");
                            bufferedWriter2.write(tmp1 + "\n");
                            bufferedWriter2.write(tmp4 + "\n");
                            bufferedWriter2.write("\n");
                        }
                        tmp1 = bufferedReader2.readLine();
                        while (tmp1 != null && !tmp1.contains("<clone nlines")) {
                            tmp1 = bufferedReader2.readLine();
                        }
                        bufferedReader1t.close();
                    }
                    bufferedWriter2.close();
                    int clonenum2 = Utilities
                            .GetNiCadTotalCloneNum(new File(resultpath + File.separator + subjectname + "-Type2.txt"));
                    int SLOCnum2 = Utilities
                            .GetTotalSLOC(new File(resultpath + File.separator + subjectname + "-Type2.txt"));
                    bufferedWriterSummary.write(
                            "Type2:  There are " + clonenum2 + " clone pairs and Total " + SLOCnum2 + " SLOC \n\n");
                }
            }
            bufferedReader2.close();

            // 求3型克隆个数，以及代码行数；
            BufferedReader bufferedReader3 = new BufferedReader(new FileReader(
                    files[i].getAbsolutePath() + File.separator + subjectname + "_functions-blind-clones-0.30.xml"));
            BufferedWriter bufferedWriter3 = new BufferedWriter(
                    new FileWriter(resultpath + File.separator + subjectname + "-Type3.txt"));

            tmp1 = bufferedReader3.readLine();
            while (tmp1 != null && !tmp1.contains("<clone nlines")) {
                tmp1 = bufferedReader3.readLine();
            }
            if (tmp1 == null) {
                bufferedWriter3.write("There  are no Type-3 clone pairs!\n");
                bufferedWriter3.close();
                int clonenum3 = Utilities
                        .GetNiCadTotalCloneNum(new File(resultpath + File.separator + subjectname + "-Type3.txt"));
                int SLOCnum3 = Utilities
                        .GetTotalSLOC(new File(resultpath + File.separator + subjectname + "-Type3.txt"));
                bufferedWriterSummary
                        .write("Type3:  There are " + clonenum3 + " clone pairs and Total " + SLOCnum3 + " SLOC \n\n");
            } else {
                bufferedReader2 = new BufferedReader(new FileReader(files[i].getAbsolutePath() + File.separator
                        + subjectname + "_functions-blind-clones-0.00.xml"));
                tmp = bufferedReader2.readLine();
                while (tmp != null && !tmp.contains("<clone nlines")) {
                    tmp = bufferedReader2.readLine();
                }
                bufferedReader2.close();
                if (tmp == null) {// 说明类型1和2一个克隆对都没有
                    bufferedWriter3.write(tmp1 + "\n");
                    while (!(tmp1 = bufferedReader3.readLine()).contains("</clones>"))
                        bufferedWriter3.write(tmp1 + "\n");
                    bufferedWriter3.close();

                    int clonenum3 = Utilities
                            .GetNiCadTotalCloneNum(new File(resultpath + File.separator + subjectname + "-Type3.txt"));
                    int SLOCnum3 = Utilities
                            .GetTotalSLOC(new File(resultpath + File.separator + subjectname + "-Type3.txt"));
                    bufferedWriterSummary.write(
                            "Type3:  There are " + clonenum3 + " clone pairs and Total " + SLOCnum3 + " SLOC \n\n");
                } else {
                    int flag = 0;
                    while (tmp1 != null) {
                        int ifexit = 0;
                        tmp2 = tmp1;// tmp2存储克隆对开始信息行
                        tmp3 = bufferedReader3.readLine();// 第一个克隆信息
                        tmp1 = bufferedReader3.readLine();// 第二个克隆信息；
                        String tmp4 = bufferedReader3.readLine();
                        String pcid1 = Utilities.getPcid(tmp3);
                        String pcid2 = Utilities.getPcid(tmp1);
                        bufferedReader2 = new BufferedReader(new FileReader(files[i].getAbsolutePath() + File.separator
                                + subjectname + "_functions-blind-clones-0.00.xml"));
                        tmp = bufferedReader2.readLine();
                        while (!tmp.contains("<clone nlines")) {
                            tmp = bufferedReader2.readLine();
                        }
                        while (tmp != null) {// 遍历完克隆文件1。
                            if (tmp.contains("<source")) {
                                String tmppcid1 = Utilities.getPcid(tmp);
                                tmp = bufferedReader2.readLine();
                                String tmppcid2 = Utilities.getPcid(tmp);
                                tmp = bufferedReader2.readLine();
                                if (tmppcid1.equals(pcid1) && tmppcid2.equals(pcid2)
                                        || tmppcid1.equals(pcid2) && tmppcid2.equals(pcid1)) {
                                    ifexit++;
                                    break;
                                }
                            } else
                                tmp = bufferedReader2.readLine();
                        }
                        if (ifexit == 0) {
                            flag = 1;
                            bufferedWriter3.write(tmp2 + "\n");
                            bufferedWriter3.write(tmp3 + "\n");
                            bufferedWriter3.write(tmp1 + "\n");
                            bufferedWriter3.write(tmp4 + "\n");
                            bufferedWriter3.write("\n");
                        }
                        tmp1 = bufferedReader3.readLine();
                        while (tmp1 != null && !tmp1.contains("<clone nlines")) {
                            tmp1 = bufferedReader3.readLine();
                        }
                        bufferedReader2.close();
                    }
                    if (flag == 0) {
                        bufferedWriter3.write("There are no type-3 clone pairs\n");
                    }
                    bufferedWriter3.close();
                    int clonenum3 = Utilities
                            .GetNiCadTotalCloneNum(new File(resultpath + File.separator + subjectname + "-Type3.txt"));
                    int SLOCnum3 = Utilities
                            .GetTotalSLOC(new File(resultpath + File.separator + subjectname + "-Type3.txt"));
                    bufferedWriterSummary.write(
                            "Type3:  There are " + clonenum3 + " clone pairs and Total " + SLOCnum3 + " SLOC \n\n");
                }
                bufferedReader3.close();
            }

            bufferedWriterSummary.close();
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(resultpath + File.separator + subjectname + "-Summary.txt"));
            BufferedWriter bufferedWriterSummary1 = new BufferedWriter(
                    new FileWriter(resultpath + File.separator + subjectname + "-Summary.txt", true));
            String tmpt = bufferedReader.readLine();
            int clonenums = 0, slocnums = 0;
            while (tmpt != null) {
                if (tmpt.contains("are")) {
                    int x1 = tmpt.indexOf("are") + 4;
                    StringBuilder x2 = new StringBuilder();
                    while (tmpt.charAt(x1) != ' ') {
                        x2.append(tmpt.charAt(x1++));
                    }

                    int x3 = tmpt.indexOf("Total") + 6;
                    StringBuilder x4 = new StringBuilder();
                    while (tmpt.charAt(x3) != ' ') {
                        x4.append(tmpt.charAt(x3++));
                    }

                    clonenums += Integer.parseInt(x2.toString());
                    slocnums += Integer.parseInt(x4.toString());
                }
                tmpt = bufferedReader.readLine();
            }
            bufferedReader.close();
            bufferedWriterSummary1.write("\nTotal Clone pairs is " + clonenums + ",Total SLOC is " + slocnums);
            bufferedWriterSummary1.close();
            i += 2;
            j += 2;

            // *********************************************************************//
            // *********************************************************************//
            // 根据种类型SLOC求各克隆的分布。
            File[] files2 = file.listFiles();
            assert files2 != null;
            Arrays.sort(files2, new AlphanumFileComparator<>());

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(files2[0], true));
            BufferedReader bufferedReader4 = new BufferedReader(new FileReader(files2[1]));
            BufferedReader bufferedReader5 = new BufferedReader(new FileReader(files2[2]));
            BufferedReader bufferedReader6 = new BufferedReader(new FileReader(files2[3]));

            String tmp4 = bufferedReader4.readLine();// 类型1克隆
            String tmp5 = bufferedReader5.readLine();// 类型2克隆
            String tmp6 = bufferedReader6.readLine();// 类型3克隆

            int[] range1Num = new int[6];
            int[] range2Num = new int[6];
            int[] range3Num = new int[6];

            int[] range1LOC = new int[6];
            int[] range2LOC = new int[6];
            int[] range3LOC = new int[6];

            HashSet<String> set1 = new HashSet<>();
            HashSet<String> set2 = new HashSet<>();
            HashSet<String> set3 = new HashSet<>();

            if (tmp4.contains("<clone")) {
                while (tmp4 != null) {

                    while (tmp4 != null && !tmp4.contains("<clone")) {
                        tmp4 = bufferedReader4.readLine();
                    }

                    if (tmp4 != null) {

                        int loc;
                        tmp4 = bufferedReader4.readLine();
                        loc = Utilities.getEndLine(tmp4) - Utilities.getStartLine(tmp4) + 1;
                        String tmppcid1 = Utilities.getPcid(tmp4);

                        if (loc <= 20) {
                            range1Num[0]++;
                            if (set1.add(tmppcid1))
                                range1LOC[0] += loc;
                        } else if (loc <= 40) {
                            range1Num[1]++;
                            if (set1.add(tmppcid1))
                                range1LOC[1] += loc;
                        } else if (loc <= 60) {
                            range1Num[2]++;
                            if (set1.add(tmppcid1))
                                range1LOC[2] += loc;
                        } else if (loc <= 80) {
                            range1Num[3]++;
                            if (set1.add(tmppcid1))
                                range1LOC[3] += loc;
                        } else if (loc <= 100) {
                            range1Num[4]++;
                            if (set1.add(tmppcid1))
                                range1LOC[4] += loc;
                        } else {
                            range1Num[5]++;
                            if (set1.add(tmppcid1))
                                range1LOC[5] += loc;
                        }

                        tmp4 = bufferedReader4.readLine();
                        loc = Utilities.getEndLine(tmp4) - Utilities.getStartLine(tmp4) + 1;
                        String tmppcid2 = Utilities.getPcid(tmp4);

                        if (loc <= 20) {
                            range1Num[0]++;
                            if (set1.add(tmppcid2))
                                range1LOC[0] += loc;
                        } else if (loc <= 40) {
                            range1Num[1]++;
                            if (set1.add(tmppcid2))
                                range1LOC[1] += loc;
                        } else if (loc <= 60) {
                            range1Num[2]++;
                            if (set1.add(tmppcid2))
                                range1LOC[2] += loc;
                        } else if (loc <= 80) {
                            range1Num[3]++;
                            if (set1.add(tmppcid2))
                                range1LOC[3] += loc;
                        } else if (loc <= 100) {
                            range1Num[4]++;
                            if (set1.add(tmppcid2))
                                range1LOC[4] += loc;
                        } else {
                            range1Num[5]++;
                            if (set1.add(tmppcid2))
                                range1LOC[5] += loc;
                        }

                        tmp4 = bufferedReader4.readLine();
                    }

                }

            }
            if (tmp5.contains("<clone")) {
                while (tmp5 != null) {

                    while (tmp5 != null && !tmp5.contains("<clone")) {
                        tmp5 = bufferedReader5.readLine();
                    }

                    if (tmp5 != null) {
                        int loc;

                        tmp5 = bufferedReader5.readLine();

                        loc = Utilities.getEndLine(tmp5) - Utilities.getStartLine(tmp5) + 1;

                        String tmppcid1 = Utilities.getPcid(tmp5);

                        if (loc <= 20) {
                            range2Num[0]++;
                            if (set2.add(tmppcid1))
                                range2LOC[0] += loc;
                        } else if (loc <= 40) {
                            range2Num[1]++;
                            if (set2.add(tmppcid1))
                                range2LOC[1] += loc;
                        } else if (loc <= 60) {
                            range2Num[2]++;
                            if (set2.add(tmppcid1))
                                range2LOC[2] += loc;
                        } else if (loc <= 80) {
                            range2Num[3]++;
                            if (set2.add(tmppcid1))
                                range2LOC[3] += loc;
                        } else if (loc <= 100) {
                            range2Num[4]++;
                            if (set2.add(tmppcid1))
                                range2LOC[4] += loc;
                        } else {
                            range2Num[5]++;
                            if (set2.add(tmppcid1))
                                range2LOC[5] += loc;
                        }

                        tmp5 = bufferedReader5.readLine();
                        loc = Utilities.getEndLine(tmp5) - Utilities.getStartLine(tmp5) + 1;

                        String tmppcid2 = Utilities.getPcid(tmp5);

                        if (loc <= 20) {
                            range2Num[0]++;
                            if (set2.add(tmppcid2))
                                range2LOC[0] += loc;
                        } else if (loc <= 40) {
                            range2Num[1]++;
                            if (set2.add(tmppcid2))
                                range2LOC[1] += loc;
                        } else if (loc <= 60) {
                            range2Num[2]++;
                            if (set2.add(tmppcid2))
                                range2LOC[2] += loc;
                        } else if (loc <= 80) {
                            range2Num[3]++;
                            if (set2.add(tmppcid2))
                                range2LOC[3] += loc;
                        } else if (loc <= 100) {
                            range2Num[4]++;
                            if (set2.add(tmppcid2))
                                range2LOC[4] += loc;
                        } else {
                            range2Num[5]++;
                            if (set2.add(tmppcid2))
                                range2LOC[5] += loc;
                        }

                        tmp5 = bufferedReader5.readLine();
                    }
                }
            }
            if (tmp6.contains("<clone")) {

                while (tmp6 != null) {

                    while (tmp6 != null && !tmp6.contains("<clone")) {
                        tmp6 = bufferedReader6.readLine();
                    }

                    if (tmp6 != null) {
                        int loc;

                        tmp6 = bufferedReader6.readLine();
                        loc = Utilities.getEndLine(tmp6) - Utilities.getStartLine(tmp6) + 1;
                        String tmppcid1 = Utilities.getPcid(tmp6);

                        if (loc <= 20) {
                            range3Num[0]++;
                            if (set3.add(tmppcid1))
                                range3LOC[0] += loc;
                        } else if (loc <= 40) {
                            range3Num[1]++;
                            if (set3.add(tmppcid1))
                                range3LOC[1] += loc;
                        } else if (loc <= 60) {
                            range3Num[2]++;
                            if (set3.add(tmppcid1))
                                range3LOC[2] += loc;
                        } else if (loc <= 80) {
                            range3Num[3]++;
                            if (set3.add(tmppcid1))
                                range3LOC[3] += loc;
                        } else if (loc <= 100) {
                            range3Num[4]++;
                            if (set3.add(tmppcid1))
                                range3LOC[4] += loc;
                        } else {
                            range3Num[5]++;
                            if (set3.add(tmppcid1))
                                range3LOC[5] += loc;
                        }

                        tmp6 = bufferedReader6.readLine();
                        loc = Utilities.getEndLine(tmp6) - Utilities.getStartLine(tmp6) + 1;

                        String tmppcid2 = Utilities.getPcid(tmp6);

                        if (loc <= 20) {
                            range3Num[0]++;
                            if (set3.add(tmppcid2))
                                range3LOC[0] += loc;
                        } else if (loc <= 40) {
                            range3Num[1]++;
                            if (set3.add(tmppcid2))
                                range3LOC[1] += loc;
                        } else if (loc <= 60) {
                            range3Num[2]++;
                            if (set3.add(tmppcid2))
                                range3LOC[2] += loc;
                        } else if (loc <= 80) {
                            range3Num[3]++;
                            if (set3.add(tmppcid2))
                                range3LOC[3] += loc;
                        } else if (loc <= 100) {
                            range3Num[4]++;
                            if (set3.add(tmppcid2))
                                range3LOC[4] += loc;
                        } else {
                            range3Num[5]++;
                            if (set3.add(tmppcid2))
                                range3LOC[5] += loc;
                        }

                        tmp6 = bufferedReader6.readLine();
                    }
                }
            }

            bufferedWriter.write(
                    "\n\n\n/********Classifying methods in clones by LOC(Considering the number of times the method appears)*********/\n");

            bufferedWriter.write("\nThe LOC range is divided into： 5-20  21-40  41-60  61-80  81-100  >=101\n");

            bufferedWriter.write("\nType1：  " + range1Num[0] + "  " + range1Num[1] + "  " + range1Num[2] + "  "
                    + range1Num[3] + "  " + range1Num[4] + "  " + range1Num[5] + "\n");

            bufferedWriter.write("\nType2：  " + range2Num[0] + "  " + range2Num[1] + "  " + range2Num[2] + "  "
                    + range2Num[3] + "  " + range2Num[4] + "  " + range2Num[5] + "\n");

            bufferedWriter.write("\nType3：  " + range3Num[0] + "  " + range3Num[1] + "  " + range3Num[2] + "  "
                    + range3Num[3] + "  " + range3Num[4] + "  " + range3Num[5] + "\n");

            bufferedWriter.write(
                    "\n\n\n/********Classifying methods in clones by LOC(Considering the LOC size of the method)*********/\n");

            bufferedWriter.write("\nThe LOC range is divided into： 5-20  21-40  41-60  61-80  81-100  >=101\n");

            bufferedWriter.write("\nType1：  " + range1LOC[0] + "  " + range1LOC[1] + "  " + range1LOC[2] + "  "
                    + range1LOC[3] + "  " + range1LOC[4] + "  " + range1LOC[5] + "\n");

            bufferedWriter.write("\nType2：  " + range2LOC[0] + "  " + range2LOC[1] + "  " + range2LOC[2] + "  "
                    + range2LOC[3] + "  " + range2LOC[4] + "  " + range2LOC[5] + "\n");

            bufferedWriter.write("\nType3：  " + range3LOC[0] + "  " + range3LOC[1] + "  " + range3LOC[2] + "  "
                    + range3LOC[3] + "  " + range3LOC[4] + "  " + range3LOC[5] + "\n");

            bufferedWriter.close();
            bufferedReader4.close();
            bufferedReader5.close();
            bufferedReader6.close();

        }

        // 移动文件夹， 把InputCS/autokeras-Add-0_functions-clones中产生的结果文件移动到Input/Results中;
        // 在InputCS下创建Results文件夹。
        File filetmp = new File(sourcefiledir.getAbsolutePath() + File.separator + "Results");
        filetmp.mkdir();

        i = 0;
        while (i < files.length) { // 遍历每个项目
            if (files[i].getName().contains("functions-clones")) { // 是项目的functions-clones文件夹;
                File[] files1 = files[i].listFiles(); // functions-clones中的所有文件。
                // 在Results目录中创建这个项目的文件夹;
                // 目标文件夹是files1[0], 即clone-abstract文件夹
                File target = null;
                for (File file : files1) {
                    if (file.getName().contains("abstract")) {
                        target = file;
                        break;
                    }
                }
                // 调用zsh执行命令 "mv " 命令
//                String file_1 = target.getAbsolutePath().replace("\\", "/\\");
//                System.out.println(file_1);
//                String[] command = { "bash", "-c", "mv " + file_1
//                        + " " + filetmp.getAbsolutePath().replace("\\", "/\\") };
//                Utilities.implCommand(command);
            }
            i++;
        }

    }
}
