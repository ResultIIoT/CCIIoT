import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Others {

    public static void main(String[] args) throws Exception {
        // 获取文件克隆对数
        String file_path = "src/main/inputCC/";
        File dir = new File(file_path);
        for (File file : dir.listFiles()) {
            getClonePairs("src/main/inputCC/" + file.getName());
        }

        //getClonePairs("E:\\Git\\II\\honos\\hono-2.6.0_functions-blind-clones");

        //getCloneLines("src/main/inputCS/Results/milo_clone-abstract/milo-Type2.txt");
        //getCloneLines("src/main/inputCS/Results/milo_clone-abstract/milo-Type3.txt");

        // 获取多出来的克隆对
        String type2File = "src/main/inputCS/milo_functions-blind-clones/milo_functions-blind-clones-0.00.xml";
        String type3File = "src/main/inputCS/milo_functions-blind-clones/milo_functions-blind-clones-0.30-classes.xml";
        String outputFile = "src/main/inputCS/milo_functions-blind-clones/diff_clones_type2.txt";
        // compareClones(type2File, type3File, outputFile);

        String inputFile = "src/main/inputCC/ditto/ditto-3.5.10_functions-blind-clones/ditto-3.5.10_functions-blind-clones-0.30.xml";
        // 在所需文件里找目标克隆对
        //getTargetClones(type3File, outputFile);

//        getTopNFoldersByCloneCount(inputFile,"volttron", 10);
    }

    // 对分离出来的克隆类型做统计
    public static void ccSeparate() throws Exception {
        File sourcefiledir = new File("src/main/inputCS");
        File[] files = sourcefiledir.listFiles();

        assert files != null;
        Arrays.sort(files, new AlphanumFileComparator<>());
        int i = 0, j = 1;
        while (j < files.length) {
            String name = files[i].getName();
            if (name.contains("要单独做的项目名")) {
                String subjectname = files[i].getName().substring(0, files[i].getName().indexOf('_'));
                String resultpath = files[i].getAbsolutePath() + File.separator + subjectname + '_' + "clone-abstract";
                File file = new File(resultpath);
                BufferedWriter bufferedWriterSummary = new BufferedWriter(
                        new FileWriter(resultpath + File.separator + subjectname + "-Summary.txt"));
                File[] files1 = files[i].listFiles();
                assert files1 != null;
                Arrays.sort(files1, new AlphanumFileComparator<>());
                int lenj = files1.length;
                int clonenum1 = Utilities.GetNiCadTotalCloneNum(files1[lenj - 1]);
                int SLOCnum1 = Utilities.GetTotalSLOC(files1[lenj - 1]);
                bufferedWriterSummary
                        .write("Type1:  There are " + clonenum1 + " clone pairs and Total " + SLOCnum1 + " SLOC\n\n");
                int clonenum2 = Utilities
                        .GetNiCadTotalCloneNum(new File(resultpath + File.separator + subjectname + "-Type2.txt"));
                int SLOCnum2 = Utilities
                        .GetTotalSLOC(new File(resultpath + File.separator + subjectname + "-Type2.txt"));
                bufferedWriterSummary
                        .write("Type2:  There are " + clonenum2 + " clone pairs and Total " + SLOCnum2 + " SLOC \n\n");
                int clonenum3 = Utilities
                        .GetNiCadTotalCloneNum(new File(resultpath + File.separator + subjectname + "-Type3.txt"));
                int SLOCnum3 = Utilities
                        .GetTotalSLOC(new File(resultpath + File.separator + subjectname + "-Type3.txt"));
                bufferedWriterSummary
                        .write("Type3:  There are " + clonenum3 + " clone pairs and Total " + SLOCnum3 + " SLOC \n\n");
                System.out.println("done");
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
                break;
            } else {
                i += 2;
                j += 2;
            }
        }
    }

    // 获取xml文件中克隆对数量
    public static void getClonePairs(String fileDir) {
        File dir = new File(fileDir);
        for (File file : dir.listFiles()) {
            // 如果file还是一个文件夹
            if (file.isDirectory()) {
                for (File subFile : file.listFiles()) {
                    if (subFile.getName().contains("clones-0.30.xml")) {
                        try {
                            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                            Document doc = dBuilder.parse(subFile);
                            doc.getDocumentElement().normalize();
                            NodeList clones = doc.getElementsByTagName("clone");
                            System.out.println(subFile.getName() + ", " + clones.getLength());

                            // 统计去掉test文件后的克隆情况
                            int testClonesCount = 0;
                            for (int i = 0; i < clones.getLength(); i++) {
                                Node clone = clones.item(i);
                                NodeList sources = ((Element) clone).getElementsByTagName("source");
                                for (int j = 0; j < sources.getLength(); j++) {
                                    Element source = (Element) sources.item(j);
                                    String fileName = source.getAttribute("file");
                                    String[] parts = fileName.split("/");
                                    for (int k = 0; k < parts.length; k++) {
                                        if (parts[k].equals("latest")) {
                                            fileName = null;
                                            for (int kk = k + 1; kk < parts.length; kk++) {
                                                fileName += parts[kk] + File.separator;
                                            }
                                            break;
                                        }
                                    }
                                    if (fileName.contains("test") || fileName.contains("Test") || fileName.contains("example")) {
                                        // System.out.println(fileName);
                                        testClonesCount++;
                                        break; // 如果一个clone包含一个Test文件,就可以跳出内层循环,进入下一个clone
                                    }
                                    else if (fileName.contains("opc-ua-sdk/sdk-server/src/main/java/org/eclipse/milo/opcua/sdk/server/namespaces/loader/VariableNodeLoader.java")) {
                                        testClonesCount++;
                                        break; // 如果一个clone包含一个Test文件,就可以跳出内层循环,进入下一个clone
                                    }
                                }
                            }
                            System.out.println("No Test: " + (clones.getLength() - testClonesCount));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (file.getName().contains("clones-0.30.xml")) {
                try {
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    NodeList clones = doc.getElementsByTagName("clone");
                    System.out.println(file.getName() + ", " + clones.getLength());

                    // 以下代码用于检测克隆行中Type3的数量
//                    int numType3 = 0;
//                    for (int i = 0; i < clones.getLength(); i++) {
//                        Element clone = (Element) clones.item(i);
//                        String similarity = clone.getAttribute("similarity");
//                        if (Integer.parseInt(similarity) == 100) {
//                            numType3++;
//                        }
//                    }
//                    System.out.println(numType3);

                    // 统计去掉test文件后的克隆情况
                    int testClonesCount = 0;
                    for (int i = 0; i < clones.getLength(); i++) {
                        Node clone = clones.item(i);
                        NodeList sources = ((Element) clone).getElementsByTagName("source");
                        for (int j = 0; j < sources.getLength(); j++) {
                            Element source = (Element) sources.item(j);
                            String fileName = source.getAttribute("file");
                            String[] parts = fileName.split("/");
                            for (int k = 0; k < parts.length; k++) {
                                if (parts[k].equals("latest")) {
                                    fileName = null;
                                    for (int kk = k + 1; kk < parts.length; kk++) {
                                        fileName += parts[kk] + File.separator;
                                    }
                                    break;
                                }
                            }
                            if (fileName.contains("test") || fileName.contains("Test") || fileName.contains("examples")) {
//                                System.out.println(fileName);
                                testClonesCount++;
                                break; // 如果一个clone包含一个Test文件,就可以跳出内层循环,进入下一个clone
                            }
                        }
                    }
                    System.out.println("No Test: " + (clones.getLength() - testClonesCount));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // 获取txt中的克隆对数量
    public static void getCloneLines(String txtFile) {
        int num = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("<clone")) {
                    num++;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("错误: 无法找到文件 " + txtFile);
        } catch (IOException e) {
            System.out.println("错误: " + e.getMessage());
        }
        System.out.println(num);
    }

    // 获取克隆文件中，被克隆最多的前n个文件
    public static void getTopNFoldersByCloneCount(String filePath, String directoryName, int n) {
        List<Map> folderCloneCount_list = parseFolderCloneCountFromNiCadXml(filePath, directoryName);
        for (Map<String, Integer> map : folderCloneCount_list) {
            printTopFolders(getTopNFolders(map, n));
        }
    }
    private static List<Map> parseFolderCloneCountFromNiCadXml(String file_Path, String directoryName) {
        Map<String, Integer> folderCloneCount = new HashMap<>();
        Map<String, Integer> more_folderCloneCount = new HashMap<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(file_Path));
            doc.getDocumentElement().normalize();
            NodeList cloneClassList = doc.getElementsByTagName("clone");
            for (int i = 0; i < cloneClassList.getLength(); i++) {
                Element cloneClass = (Element) cloneClassList.item(i);
                NodeList sourceList = cloneClass.getElementsByTagName("source");
                for (int j = 0; j < sourceList.getLength(); j++) {
                    Element source = (Element) sourceList.item(j);
                    String filePath = source.getAttribute("file");
                    if (filePath != null) {
                        Path folderPath = Paths.get(filePath).getParent();
                        more_folderCloneCount.put(folderPath.toString(),more_folderCloneCount.getOrDefault(folderPath.toString(), 0) + 1);
                        String[] parts = folderPath.toString().split(File.separator + File.separator);
                        int index = -1;
                        for (int k = 0; k < parts.length; k++) {
                            if (parts[k].equals(directoryName)) {
                                index = k;
                                break;
                            }
                        }
                        if (index != -1) {
                            String nextDirectory = parts[index + 1];
                            folderCloneCount.put(nextDirectory, folderCloneCount.getOrDefault(nextDirectory, 0) + 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Map> folderCloneCount_list = new ArrayList<>();
        folderCloneCount_list.add(folderCloneCount);
        folderCloneCount_list.add(more_folderCloneCount);
        return folderCloneCount_list;
    }
    private static List<Map.Entry<String, Integer>> getTopNFolders(Map<String, Integer> folderCloneCount, int n) {
        List<Map.Entry<String, Integer>> sortedFolders = folderCloneCount.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        return sortedFolders.subList(0, Math.min(n, sortedFolders.size()));
    }
    private static void printTopFolders(List<Map.Entry<String, Integer>> topFolders) {
        System.out.println("Top folders by clone count:");
        for (Map.Entry<String, Integer> entry : topFolders) {
            System.out.printf("Folder: %s, Clone count: %d%n", entry.getKey(), entry.getValue());
        }
    }

    // 获取两个txt文件中，多出来的克隆对
    public static void compareClones(String type2File, String type3File, String outputFile) {
        // 读取Type2和Type3克隆文件
        Map<String, CloneInfo> type2Clones = readClonesFromXML(type2File);
        Map<String, CloneInfo> type3Clones = readClonesFromXML(type3File);

        // 找出两种克隆类型的相同克隆对
        Set<String> commonClones = findCommonClones(type2Clones, type3Clones);

        // 找出Type2文件中多出来的克隆对
        Set<String> extraType2Clones = findExtraClones(type2Clones, commonClones);

        // 将差异克隆写入输出文件
        writeClonesToFile(extraType2Clones, outputFile);

        System.out.println("任务完成,共有 " + extraType2Clones.size() + " 对差异的克隆对已保存到 " + outputFile + " 文件中。");
    }
    private static Map<String, CloneInfo> readClonesFromXML(String xmlFile) {
        Map<String, CloneInfo> clones = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(xmlFile));
            NodeList cloneNodes = document.getElementsByTagName("clone");
            for (int i = 0; i < cloneNodes.getLength(); i++) {
                Element cloneElement = (Element) cloneNodes.item(i);
                if (Integer.parseInt(cloneElement.getAttribute("similarity")) == 100) {
                    List<CloneSource> sources = new ArrayList<>();
                    NodeList sourceNodes = cloneElement.getElementsByTagName("source");
                    for (int j = 0; j < sourceNodes.getLength(); j++) {
                        Element sourceElement = (Element) sourceNodes.item(j);
                        CloneSource source = new CloneSource(
                                sourceElement.getAttribute("file"),
                                Integer.parseInt(sourceElement.getAttribute("startline")),
                                Integer.parseInt(sourceElement.getAttribute("endline")),
                                sourceElement.getAttribute("pcid")
                        );
                        sources.add(source);
                    }
                    CloneInfo cloneInfo = new CloneInfo(
                            Integer.parseInt(cloneElement.getAttribute("nlines")),
                            100,
                            sources
                    );
                    clones.put(cloneInfo.toString(), cloneInfo);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return clones;
    }
    private static Set<String> findCommonClones(Map<String, CloneInfo> type2Clones, Map<String, CloneInfo> type3Clones) {
        Set<String> commonClones = new HashSet<>(type2Clones.keySet());
        commonClones.retainAll(type3Clones.keySet());
        return commonClones;
    }
    private static Set<String> findExtraClones(Map<String, CloneInfo> type2Clones, Set<String> commonClones) {
        Set<String> extraClones = new HashSet<>(type2Clones.keySet());
        extraClones.removeAll(commonClones);
        return extraClones;
    }
    private static void writeClonesToFile(Set<String> clones, String outputFile) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            for (String clone : clones) {
                writer.write(clone);
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class CloneInfo {
        int nlines;
        int similarity;
        List<CloneSource> sources;

        public CloneInfo(int nlines, int similarity, List<CloneSource> sources) {
            this.nlines = nlines;
            this.similarity = similarity;
            this.sources = sources;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("<clone nlines=\"").append(nlines).append("\" similarity=\"").append(similarity).append("\">\n");
            for (CloneSource source : sources) {
                sb.append("  <source file=\"").append(source.file)
                        .append("\" startline=\"").append(source.startLine)
                        .append("\" endline=\"").append(source.endLine)
                        .append("\" pcid=\"").append(source.pcid).append("\"/>\n");
            }
            sb.append("</clone>");
            return sb.toString();
        }
    }
    private static class CloneSource {
        String file;
        int startLine;
        int endLine;
        String pcid;

        public CloneSource(String file, int startLine, int endLine, String pcid) {
            this.file = file;
            this.startLine = startLine;
            this.endLine = endLine;
            this.pcid = pcid;
        }
    }

    // 在所需文件里找目标克隆对
    public static List<CodeClone> getTargetClones(String xmlFilePath, String targetCloneFilePath) {
        List<CodeClone> clones = getClones(xmlFilePath);
        List<CodeClone> targetClones = getTargetClonesFromXml(targetCloneFilePath);
        int num = 0;
        for (CodeClone targetClone : targetClones) {
            boolean found = false;
            for (CodeClone clone : clones) {
                if (isSameClone(targetClone, clone)) {
                    found = true;
                    break;
                }
            }
            System.out.println("Target clone " + (found ? "found" : "not found"));
            num++;
        }
        System.out.println("Found:"  + num);
        return targetClones;
    }
    private static List<CodeClone> getClones(String xmlFilePath) {
        List<CodeClone> clones = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(xmlFilePath));
            doc.getDocumentElement().normalize();

            NodeList cloneNodes = doc.getElementsByTagName("clone");
            for (int i = 0; i < cloneNodes.getLength(); i++) {
                Element cloneElement = (Element) cloneNodes.item(i);
                int nlines = Integer.parseInt(cloneElement.getAttribute("nlines"));
                String similarity = cloneElement.getAttribute("similarity");

                NodeList sourceNodes = cloneElement.getElementsByTagName("source");
                Element source1 = (Element) sourceNodes.item(0);
                String file1 = source1.getAttribute("file");
                int startline1 = Integer.parseInt(source1.getAttribute("startline"));
                int endline1 = Integer.parseInt(source1.getAttribute("endline"));
                int pcid1 = Integer.parseInt(source1.getAttribute("pcid"));

                Element source2 = (Element) sourceNodes.item(1);
                String file2 = source2.getAttribute("file");
                int startline2 = Integer.parseInt(source2.getAttribute("startline"));
                int endline2 = Integer.parseInt(source2.getAttribute("endline"));
                int pcid2 = Integer.parseInt(source2.getAttribute("pcid"));

                clones.add(new CodeClone(nlines, similarity,
                        new SourceInfo(file1, startline1, endline1, pcid1),
                        new SourceInfo(file2, startline2, endline2, pcid2)));
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return clones;
    }
    private static List<CodeClone> getTargetClonesFromXml(String targetCloneFilePath) {
        List<CodeClone> targetClones = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(targetCloneFilePath));
            doc.getDocumentElement().normalize();

            NodeList cloneNodes = doc.getElementsByTagName("clone");
            for (int i = 0; i < cloneNodes.getLength(); i++) {
                Element cloneElement = (Element) cloneNodes.item(i);
                int nlines = Integer.parseInt(cloneElement.getAttribute("nlines"));
                String similarity = cloneElement.getAttribute("similarity");

                NodeList sourceNodes = cloneElement.getElementsByTagName("source");
                Element source1 = (Element) sourceNodes.item(0);
                String file1 = source1.getAttribute("file");
                int startline1 = Integer.parseInt(source1.getAttribute("startline"));
                int endline1 = Integer.parseInt(source1.getAttribute("endline"));
                int pcid1 = Integer.parseInt(source1.getAttribute("pcid"));

                Element source2 = (Element) sourceNodes.item(1);
                String file2 = source2.getAttribute("file");
                int startline2 = Integer.parseInt(source2.getAttribute("startline"));
                int endline2 = Integer.parseInt(source2.getAttribute("endline"));
                int pcid2 = Integer.parseInt(source2.getAttribute("pcid"));

                targetClones.add(new CodeClone(nlines, similarity,
                        new SourceInfo(file1, startline1, endline1, pcid1),
                        new SourceInfo(file2, startline2, endline2, pcid2)));
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return targetClones;
    }
    private static boolean isSameClone(CodeClone target, CodeClone clone) {
        if (target.getSource1().getFile().equals(clone.getSource1().getFile())
                && target.getSource1().getStartline() >= clone.getSource1().getStartline()
                && target.getSource1().getEndline() <= clone.getSource1().getEndline()
                && target.getSource2().getFile().equals(clone.getSource2().getFile())
                && target.getSource2().getStartline() >= clone.getSource2().getStartline()
                && target.getSource2().getEndline() <= clone.getSource2().getEndline()) {
            System.out.println("-----");
            System.out.println(target.toString());
            System.out.println(clone.toString());

            return true;
        }
        return false;
    }
    static class CodeClone {
        private int nlines;
        private String similarity;
        private SourceInfo source1;
        private SourceInfo source2;

        public CodeClone(int nlines, String similarity, SourceInfo source1, SourceInfo source2) {
            this.nlines = nlines;
            this.similarity = similarity;
            this.source1 = source1;
            this.source2 = source2;
        }

        public int getNlines() {
            return nlines;
        }

        public String getSimilarity() {
            return similarity;
        }

        public SourceInfo getSource1() {
            return source1;
        }

        public SourceInfo getSource2() {
            return source2;
        }

        @Override
        public String toString() {
            return "CodeClone{" +
                    "nlines=" + nlines +
                    ", similarity='" + similarity + '\'' +
                    ", source1=" + source1.toString() +
                    ", source2=" + source2.toString() +
                    '}';
        }
    }
    static class SourceInfo {
        private String file;
        private int startline;
        private int endline;
        private int pcid;

        public SourceInfo(String file, int startline, int endline, int pcid) {
            this.file = file;
            this.startline = startline;
            this.endline = endline;
            this.pcid = pcid;
        }

        public String getFile() {
            return file;
        }

        public int getStartline() {
            return startline;
        }

        public int getEndline() {
            return endline;
        }

        public int getPcid() {
            return pcid;
        }

        @Override
        public String toString() {
            return "SourceInfo{" +
                    "file='" + file + '\'' +
                    ", startline=" + startline +
                    ", endline=" + endline +
                    ", pcid=" + pcid +
                    '}';
        }
    }
}
