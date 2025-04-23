import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class CCloneSeparator {

    //基于共变结果文件，对共变克隆进行类型分离，并且按照克隆类型，将方法块的代码行块对应到各自的范围。
    //输入：InputCCTA文件夹以及InputCS文件夹。将所有项目共变结果文件都放入InputCCTA文件夹中，所有项目的克隆提取的结果文件(abstract文件)放入然后运行
    //输出：FinalResults文件。在原有的FinalResults文件上添加。
    public static void main(String[] args) throws Exception {
        String InputCCTA = "src/main/outputCC";
        run(InputCCTA);
    }

    public static void run (String InputCCTA) throws IOException {
        File[] inputCCTAS = new File(InputCCTA).listFiles();//该文件夹存入的共变数据结果来作为输入。

        assert inputCCTAS != null;
        Arrays.sort(inputCCTAS, new AlphanumFileComparator<>());
        int i = 0;

        while (i < inputCCTAS.length) {//该循环遍历每个项目文件

            File[] files = inputCCTAS[i].listFiles();
//            System.out.println(inputCCTAS[i]);
            if (files.length == 0) {
                i++;
                continue;
            }

            // 在need_do中放入需要做分离的项目，仅做在need_do中有的项目的分离
//            List<String> need_do = Arrays.asList("hono", "kura", "californium", "hawkbit", "kapua", "vorto", "arrowhead", "ditto");
//            List<String> need_do = Arrays.asList("mraa", "mosquitto", "cyclonedds");
            List<String> need_do = Arrays.asList("Java");
            String[] fileNs = inputCCTAS[i].getPath().split("\\\\");
            String fileN = fileNs[3];

            if (!need_do.contains(fileN)) {
                i++;
                continue;
            }

            System.out.println("Start: " + fileN);

            int j = 0;
            while (true) {//该循环获得noduplicated后缀名的文件
                assert files != null;
                if (files[j].getName().contains("noduplicated")) break;
                j++;
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader(files[j]));//打开noduplicated文件

            j = 0;
            while (!files[j].getName().contains("FinalResults")) {//该循环获得FinalResults后缀名的文件
                j++;
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(files[j], true));//打开FinalResults文件


            String tmp;
            tmp = bufferedReader.readLine();

            int nums = 0;
            int type1nums = 0;
            int type2nums = 0;
            int type3nums = 0;


            int type1Loc = 0;
            int type2Loc = 0;
            int type3Loc = 0;

            int[] range1 = new int[6];
            int[] range2 = new int[6];
            int[] range3 = new int[6];

            int[] range1LOC = new int[6];
            int[] range2LOC = new int[6];
            int[] range3LOC = new int[6];


            HashSet<String> set1 = new HashSet<>();//用来求共变代码行的,对类型1共变克隆的方法块去重
            HashSet<String> set2 = new HashSet<>();//用来求共变代码行的,对类型2共变克隆的方法块去重
            HashSet<String> set3 = new HashSet<>();//用来求共变代码行的,对类型3共变克隆的方法块去重

            while (tmp != null) {//遍历这个项目所有共变克隆对

                if (tmp.contains("<clonepair")) {//noduplicated文件中定位到一对克隆对

                    int tmpflag = 0;

                    //取这一对克隆的PCID
                    tmp = bufferedReader.readLine();
                    String pcid1 = Utilities.getPcid(tmp);
                    String file_1 = Utilities.getCoFilePath(tmp, fileN);
                    tmp = bufferedReader.readLine();
                    String pcid2 = Utilities.getPcid(tmp);
                    String file_2 = Utilities.getCoFilePath(tmp, fileN);

                    if (file_1.contains("test") || file_1.contains("Test") || file_1.contains("example")) {
                        continue;
                    }
                    else if (file_2.contains("test") || file_2.contains("Test") || file_2.contains("example")) {
                        continue;
                    }
                    nums++;//记录检测共变克隆数

                    //在共变克隆中找到一对克隆，然后去对应的类型文件里面去匹配，从类型1文件开始。
                    String name = inputCCTAS[i].getName();

                    File[] inputCS = new File("src/main/inputCS/Results").listFiles();
                    assert inputCS != null;
                    Arrays.sort(inputCS, new AlphanumFileComparator<>());

                    int k = 0;

                    while (k < inputCS.length) {//该循环用来查找clone-abstract文件夹
                        if (inputCS[k].getName().contains(name + "_clone-abstract")) {//找到对应项目克隆分类文件
                            File[] files1 = inputCS[k].listFiles();
                            assert files1 != null;
                            Arrays.sort(files1, new AlphanumFileComparator<>());

                            int u = 1;
                            while (u < 4) {//该循环依次查找每个文件的每个克隆对
                                BufferedReader bufferedReader1 = new BufferedReader(new FileReader(files1[u]));
                                String tmp1 = bufferedReader1.readLine();

                                while (tmp1 != null) {//该循环查找该文件的每一对克隆对
                                    if (tmp1.contains("<clone nlines")) {

                                        int loc;

                                        tmp1 = bufferedReader1.readLine();
                                        String tmppcid1 = Utilities.getPcid(tmp1);

                                        int loc1 = Utilities.getEndLine(tmp1) - Utilities.getStartLine(tmp1) + 1;

                                        tmp1 = bufferedReader1.readLine();
                                        String tmppcid2 = Utilities.getPcid(tmp1);

                                        int loc2 = Utilities.getEndLine(tmp1) - Utilities.getStartLine(tmp1) + 1;


                                        if (pcid1.equals(tmppcid1) && pcid2.equals(tmppcid2) ||
                                                pcid1.equals(tmppcid2) && pcid2.equals(tmppcid1)) {
                                            switch (u) {
                                                case 1: {
                                                    tmpflag = 1;
                                                    type1nums++;

                                                    loc = loc1;
                                                    if (loc <= 20) {
                                                        range1[0]++;
                                                        if (set1.add(tmppcid1))
                                                            range1LOC[0] += loc;
                                                    } else if (loc <= 40) {
                                                        range1[1]++;
                                                        if (set1.add(tmppcid1))
                                                            range1LOC[1] += loc;
                                                    } else if (loc <= 60) {
                                                        range1[2]++;
                                                        if (set1.add(tmppcid1))
                                                            range1LOC[2] += loc;
                                                    } else if (loc <= 80) {
                                                        range1[3]++;
                                                        if (set1.add(tmppcid1))
                                                            range1LOC[3] += loc;
                                                    } else if (loc <= 100) {
                                                        range1[4]++;
                                                        if (set1.add(tmppcid1))
                                                            range1LOC[4] += loc;
                                                    } else {
                                                        range1[5]++;
                                                        if (set1.add(tmppcid1))
                                                            range1LOC[5] += loc;
                                                    }


                                                    loc = loc2;
                                                    if (loc <= 20) {
                                                        range1[0]++;
                                                        if (set1.add(tmppcid2))
                                                            range1LOC[0] += loc;
                                                    } else if (loc <= 40) {
                                                        range1[1]++;
                                                        if (set1.add(tmppcid2))
                                                            range1LOC[1] += loc;
                                                    } else if (loc <= 60) {
                                                        range1[2]++;
                                                        if (set1.add(tmppcid2))
                                                            range1LOC[2] += loc;
                                                    } else if (loc <= 80) {
                                                        range1[3]++;
                                                        if (set1.add(tmppcid2))
                                                            range1LOC[3] += loc;
                                                    } else if (loc <= 100) {
                                                        range1[4]++;
                                                        if (set1.add(tmppcid2))
                                                            range1LOC[4] += loc;
                                                    } else {
                                                        range1[5]++;
                                                        if (set1.add(tmppcid2))
                                                            range1LOC[5] += loc;
                                                    }

                                                    break;
                                                }
                                                case 2: {
                                                    tmpflag = 1;
                                                    type2nums++;

                                                    loc = loc1;
                                                    if (loc <= 20) {
                                                        range2[0]++;
                                                        if (set2.add(tmppcid1))
                                                            range2LOC[0] += loc;
                                                    } else if (loc <= 40) {
                                                        range2[1]++;
                                                        if (set2.add(tmppcid1))
                                                            range2LOC[1] += loc;
                                                    } else if (loc <= 60) {
                                                        range2[2]++;
                                                        if (set2.add(tmppcid1))
                                                            range2LOC[2] += loc;
                                                    } else if (loc <= 80) {
                                                        range2[3]++;
                                                        if (set2.add(tmppcid1))
                                                            range2LOC[3] += loc;
                                                    } else if (loc <= 100) {
                                                        range2[4]++;
                                                        if (set2.add(tmppcid1))
                                                            range2LOC[4] += loc;
                                                    } else {
                                                        range2[5]++;
                                                        if (set2.add(tmppcid1))
                                                            range2LOC[5] += loc;
                                                    }


                                                    loc = loc2;
                                                    if (loc <= 20) {
                                                        range2[0]++;
                                                        if (set2.add(tmppcid2))
                                                            range2LOC[0] += loc;
                                                    } else if (loc <= 40) {
                                                        range2[1]++;
                                                        if (set2.add(tmppcid2))
                                                            range2LOC[1] += loc;
                                                    } else if (loc <= 60) {
                                                        range2[2]++;
                                                        if (set2.add(tmppcid2))
                                                            range2LOC[2] += loc;
                                                    } else if (loc <= 80) {
                                                        range2[3]++;
                                                        if (set2.add(tmppcid2))
                                                            range2LOC[3] += loc;
                                                    } else if (loc <= 100) {
                                                        range2[4]++;
                                                        if (set2.add(tmppcid2))
                                                            range2LOC[4] += loc;
                                                    } else {
                                                        range2[5]++;
                                                        if (set2.add(tmppcid2))
                                                            range2LOC[5] += loc;
                                                    }

                                                    break;
                                                }
                                                case 3: {
                                                    tmpflag = 1;
                                                    type3nums++;


                                                    loc = loc1;
                                                    if (loc <= 20) {
                                                        range3[0]++;
                                                        if (set3.add(tmppcid1))
                                                            range3LOC[0] += loc;
                                                    } else if (loc <= 40) {
                                                        range3[1]++;
                                                        if (set3.add(tmppcid1))
                                                            range3LOC[1] += loc;
                                                    } else if (loc <= 60) {
                                                        range3[2]++;
                                                        if (set3.add(tmppcid1))
                                                            range3LOC[2] += loc;
                                                    } else if (loc <= 80) {
                                                        range3[3]++;
                                                        if (set3.add(tmppcid1))
                                                            range3LOC[3] += loc;
                                                    } else if (loc <= 100) {
                                                        range3[4]++;
                                                        if (set3.add(tmppcid1))
                                                            range3LOC[4] += loc;
                                                    } else {
                                                        range3[5]++;
                                                        if (set3.add(tmppcid1))
                                                            range3LOC[5] += loc;
                                                    }


                                                    loc = loc2;
                                                    if (loc <= 20) {
                                                        range3[0]++;
                                                        if (set3.add(tmppcid2))
                                                            range3LOC[0] += loc;
                                                    } else if (loc <= 40) {
                                                        range3[1]++;
                                                        if (set3.add(tmppcid2))
                                                            range3LOC[1] += loc;
                                                    } else if (loc <= 60) {
                                                        range3[2]++;
                                                        if (set3.add(tmppcid2))
                                                            range3LOC[2] += loc;
                                                    } else if (loc <= 80) {
                                                        range3[3]++;
                                                        if (set3.add(tmppcid2))
                                                            range3LOC[3] += loc;
                                                    } else if (loc <= 100) {
                                                        range3[4]++;
                                                        if (set3.add(tmppcid2))
                                                            range3LOC[4] += loc;
                                                    } else {
                                                        range3[5]++;
                                                        if (set3.add(tmppcid2))
                                                            range3LOC[5] += loc;
                                                    }

                                                    break;
                                                }
                                                default:
                                                    break;
                                            }
                                        }
                                        if (tmpflag == 1)
                                            break;
                                    }
                                    tmp1 = bufferedReader1.readLine();
                                }
                                if (tmpflag == 1)
                                    break;
                                u++;
                                bufferedReader1.close();
                            }
                            break;//已经进入过abstract目录，那么出来就不用在进入循环了
                        }
                        k++;
                    }
                }
                tmp = bufferedReader.readLine();
            }

            bufferedReader.close();//关闭noduplicated文件

//            开始写入匹配到的克隆类型


            bufferedWriter.write("\n\n\n/***************Here are the numbers for each clone type***************/\n");

            bufferedWriter.write("\nThere are a total of " + nums + " co-modified clones.\n\nAmong them,\n\nType1：  " + type1nums + " pairs     Type2:  " + type2nums + " pairs "
                    + "     Type3:  " + type3nums + " pairs");

            bufferedWriter.write("\n\n\n/********************Classifying methods in clones by LOC(Considering the number of times the method appears)***************/\n");

            bufferedWriter.write("\nThe LOC range is divided into： 5-20  21-40  41-60   61-80  81-100  >=101\n");

            bufferedWriter.write("\nType1：  " + range1[0] + "  " + range1[1] + "  " + range1[2] + "  " + range1[3] + "  " + range1[4] + "  " + range1[5] + "\n");

            bufferedWriter.write("\nType2：  " + range2[0] + "  " + range2[1] + "  " + range2[2] + "  " + range2[3] + "  " + range2[4] + "  " + range2[5] + "\n");

            bufferedWriter.write("\nType3：  " + range3[0] + "  " + range3[1] + "  " + range3[2] + "  " + range3[3] + "  " + range3[4] + "  " + range3[5] + "\n");

            bufferedWriter.write("\n\n\n/********************Classifying methods in clones by LOC(Considering the LOC size of the method)***************/\n");
            bufferedWriter.write("\nThe LOC range is divided into： 5-20  21-40  41-60   61-80  81-100  >=101\n");

            bufferedWriter.write("\nType1：  " + range1LOC[0] + "  " + range1LOC[1] + "  " + range1LOC[2] + "  " + range1LOC[3] + "  " + range1LOC[4] + "  " + range1LOC[5] + "\n");

            bufferedWriter.write("\nType2：  " + range2LOC[0] + "  " + range2LOC[1] + "  " + range2LOC[2] + "  " + range2LOC[3] + "  " + range2LOC[4] + "  " + range2LOC[5] + "\n");

            bufferedWriter.write("\nType3：  " + range3LOC[0] + "  " + range3LOC[1] + "  " + range3LOC[2] + "  " + range3LOC[3] + "  " + range3LOC[4] + "  " + range3LOC[5] + "\n");


            bufferedWriter.close();//关闭FinalResults文件

            //进入下一个文件循环
            i++;
        }
    }
}
