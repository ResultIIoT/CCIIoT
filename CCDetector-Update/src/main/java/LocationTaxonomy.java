import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class LocationTaxonomy {
    // 本类实现与共变克隆和克隆的位置分类的功能（same file, same dir, different dir）;
    // 只需要考虑固定的文件格式即可，即ASE——Datasets;
    public static void main(String[] args) throws IOException {
        String sourcePath = "C:\\Users\\yao\\Desktop\\ASE_Datasets\\Results"; // 克隆与共变克隆的结果文件的存放处。
        run(sourcePath);
    }

    // 对共变克隆按位置进行分类
    public static void run(String sourcePath) throws IOException {
        File[] CCprojectLists = new File(sourcePath + File.separator + "Co-change").listFiles(); // 共变所有项目文件所在;
        File[] CloneprojectLists = new File(sourcePath + File.separator + "Clone" + File.separator + "DL").listFiles(); // 代码克隆所有项目文件所在
        // 开始遍历每一个项目;
        int sameFileNum = 0; // 同一文件下的共变克隆对数
        int sameFileLoc = 0; // 同一文件下的共变克隆的Loc数；这里求出的是没有去重的
        int sameDirNum = 0;  // 同一目录下的共变克隆的对数;
        int sameDirLoc = 0;  // 同一目录下的共变克隆的Loc数
        int DiffDirNum = 0;  // 不同目录下的共变克隆的对数;
        int DiffDirLoc = 0;  // 不同目录下的共变克隆的Loc数;
        int sameFileNum1 = 0; // 同一文件下的共变克隆类型1对数
        int sameFileLoc1 = 0; // 同一文件下的共变克隆类型1的Loc数；这里求出的是没有去重的
        int sameDirNum1 = 0;  // 同一目录下的共变克隆类型1的对数;
        int sameDirLoc1 = 0;  // 同一目录下的共变克隆类型1的Loc数
        int DiffDirNum1 = 0;  // 不同目录下的共变克隆类型1的对数;
        int DiffDirLoc1 = 0;  // 不同目录下的共变克隆类型1的Loc数;
        int sameFileNum2 = 0; // 同一文件下的共变克隆类型2对数
        int sameFileLoc2 = 0; // 同一文件下的共变克隆类型2的Loc数；这里求出的是没有去重的
        int sameDirNum2 = 0;  // 同一目录下的共变克隆类型2的对数;
        int sameDirLoc2 = 0;  // 同一目录下的共变克隆类型2的Loc数
        int DiffDirNum2 = 0;  // 不同目录下的共变克隆类型2的对数;
        int DiffDirLoc2 = 0;  // 不同目录下的共变克隆类型2的Loc数;
        int sameFileNum3 = 0; // 同一文件下的共变克隆类型3对数
        int sameFileLoc3 = 0; // 同一文件下的共变克隆类型3的Loc数；这里求出的是没有去重的
        int sameDirNum3 = 0;  // 同一目录下的共变克隆类型3的对数;
        int sameDirLoc3 = 0;  // 同一目录下的共变克隆类型3的Loc数
        int DiffDirNum3 = 0;  // 不同目录下的共变克隆类型3的对数;
        int DiffDirLoc3 = 0;  // 不同目录下的共变克隆类型3的Loc数;
        for (File project : CCprojectLists) {
            String projectName = project.getName(); // 获得本项目的名称。
            File[] filesList = project.listFiles(); // 获得项目所有文件列表。
            for (File file : filesList) {
                if (file.getName().contains("noduplicated")) { // 找到含共变克隆的信息的那个文件即可。
                    BufferedReader reader = new BufferedReader(new FileReader(file)); // 读取这个文件。
                    String temp1 = reader.readLine(); // temp为文件行读取变量;
                    Set<String> set = new HashSet<>(); // 对一个项目的克隆片段去重，根据pcid
                    while (temp1 != null) { // 把文件读完;
                        while (temp1 != null && !temp1.contains("<source file=")) { // 找一对克隆对；
                            temp1 = reader.readLine();
                        }
                        if (temp1 == null) break; // 文件结束。
                        String temp2 = reader.readLine(); // 克隆对的第二个克隆信息;

                        // 求出这一对的克隆的pcid
                        String pcid1 = Utilities.getPcid(temp1);
                        String pcid2 = Utilities.getPcid(temp2);
                        // 求出这一对克隆对的LOC总和;
                        int loc1 = Utilities.getEndLine(temp1) - Utilities.getStartLine(temp1) + 1;
                        int loc2 = Utilities.getEndLine(temp2) - Utilities.getStartLine(temp2) + 1;
                        // 只提取路径;
                        temp1 = Utilities.getFilePath(temp1);
                        temp2 = Utilities.getFilePath(temp2);

                        // 判断该对克隆属于哪种克隆类型
                        int flag = 0; // 表示是哪种类型的克隆
                        for (File cloneProject : CloneprojectLists) {
                            if (cloneProject.getName().contains(projectName)) { // 在克隆文件中找到此时对应的项目。
                                File[] files = cloneProject.listFiles(); // 所有文件;
                                for (File file1 : files) {
                                    if (file1.getName().contains("Type1")) {
                                        if (flag != 0) break;
                                        // 读取文件
                                        BufferedReader reader1 = new BufferedReader(new FileReader(file1));
                                        String temp3 = reader1.readLine();
                                        while (temp3 != null) {
                                            while (temp3 != null && !temp3.contains("<source file=")) {
                                                temp3 = reader1.readLine();
                                            }
                                            if (temp3 == null) break;
                                            String temp4 = reader1.readLine();
                                            String pcid3 = Utilities.getPcid(temp3);
                                            String pcid4 = Utilities.getPcid(temp4);
                                            if (pcid1.equals(pcid3) && pcid2.equals(pcid4) || pcid1.equals(pcid4) && pcid2.equals(pcid3)) {
                                                flag = 1;
                                                break;
                                            }
                                            temp3 = reader1.readLine();
                                        }

                                        reader1.close();
                                    } else if (file1.getName().contains("Type2")) {
                                        if (flag != 0) break;
                                        // 读取文件
                                        BufferedReader reader1 = new BufferedReader(new FileReader(file1));
                                        String temp3 = reader1.readLine();
                                        while (temp3 != null) {
                                            while (temp3 != null && !temp3.contains("<source file=")) {
                                                temp3 = reader1.readLine();
                                            }
                                            if (temp3 == null) break;
                                            String temp4 = reader1.readLine();
                                            String pcid3 = Utilities.getPcid(temp3);
                                            String pcid4 = Utilities.getPcid(temp4);
                                            if (pcid1.equals(pcid3) && pcid2.equals(pcid4) || pcid1.equals(pcid4) && pcid2.equals(pcid3)) {
                                                flag = 2;
                                                break;
                                            }
                                            temp3 = reader1.readLine();
                                        }

                                        reader1.close();
                                    } else if (file1.getName().contains("Type3")) {
                                        if (flag != 0) break;
                                        // 读取文件
                                        BufferedReader reader1 = new BufferedReader(new FileReader(file1));
                                        String temp3 = reader1.readLine();
                                        while (temp3 != null) {
                                            while (temp3 != null && !temp3.contains("<source file=")) {
                                                temp3 = reader1.readLine();
                                            }
                                            if (temp3 == null) break;
                                            String temp4 = reader1.readLine();
                                            String pcid3 = Utilities.getPcid(temp3);
                                            String pcid4 = Utilities.getPcid(temp4);
                                            if (pcid1.equals(pcid3) && pcid2.equals(pcid4) || pcid1.equals(pcid4) && pcid2.equals(pcid3)) {
                                                flag = 3;
                                                break;
                                            }
                                            temp3 = reader1.readLine();
                                        }
                                        reader1.close();
                                    }
                                }
                                break;
                            }
                        }
                        // 求出该对克隆是哪种位置类型。
                        if (temp1.equals(temp2)) { // 属于同一文件下克隆;
                            sameFileNum++;
                            if (flag == 1) {
                                sameFileNum1++;
                            } else if (flag == 2) {
                                sameFileNum2++;
                            } else {
                                sameFileNum3++;
                            }
                            if (set.add(pcid1)) {
                                sameFileLoc += loc1;
                                if (flag == 1) { // 1型克隆
                                    sameFileLoc1 += loc1;
                                } else if (flag == 2) { // 2型克隆
                                    sameFileLoc2 += loc1;
                                } else if (flag == 3){ // 3型克隆
                                    sameFileLoc3 += loc1;
                                }
                            }
                            if (set.add(pcid2)) {
                                sameFileLoc += loc2;
                                if (flag == 1) { // 1型克隆
                                    sameFileLoc1 += loc2;
                                } else if (flag == 2) { // 2型克隆
                                    sameFileLoc2 += loc2;
                                } else if (flag == 3){ // 3型克隆
                                    sameFileLoc3 += loc2;
                                }
                            }

                        } else if (temp1.substring(0, temp1.lastIndexOf("/")).equals(temp2.substring(0, temp2.lastIndexOf("/")))){
                            // 同一目录下的共变克隆
                            sameDirNum++;
                            if (flag == 1) {
                                sameDirNum1++;
                            } else if (flag == 2){
                                sameDirNum2++;
                            } else {
                                sameDirNum3++;
                            }
                            if (set.add(pcid1)) {
                                sameDirLoc += loc1;
                                if (flag == 1) { // 1型克隆
                                    sameDirLoc1 += loc1;
                                } else if (flag == 2) { // 2型克隆
                                    sameDirLoc2 += loc1;
                                } else if (flag == 3){ // 3型克隆
                                    sameDirLoc3 += loc1;
                                }
                            }
                            if (set.add(pcid2)) {
                                sameDirLoc += loc2;
                                if (flag == 1) { // 1型克隆
                                    sameDirLoc1 += loc2;
                                } else if (flag == 2) { // 2型克隆
                                    sameDirLoc2 += loc2;
                                } else if (flag == 3){ // 3型克隆
                                    sameDirLoc3 += loc2;
                                }
                            }
                        } else { // 不同目录下的共变克隆
                            DiffDirNum++;
                            if (flag == 1) {
                                DiffDirNum1++;
                            } else if (flag == 2) {
                                DiffDirNum2++;
                            } else {
                                DiffDirNum3++;
                            }
                            if (set.add(pcid1)) {
                                DiffDirLoc += loc1;
                                if (flag == 1) { // 1型克隆
                                    DiffDirLoc1 += loc1;
                                } else if (flag == 2) { // 2型克隆
                                    DiffDirLoc2 += loc1;
                                } else if (flag == 3){ // 3型克隆
                                    DiffDirLoc3 += loc1;
                                }
                            }
                            if (set.add(pcid2)) {
                                DiffDirLoc += loc2;
                                if (flag == 1) { // 1型克隆
                                    DiffDirLoc1 += loc2;
                                } else if (flag == 2) { // 2型克隆
                                    DiffDirLoc2 += loc2;
                                } else if (flag == 3){ // 3型克隆
                                    DiffDirLoc3 += loc2;
                                }
                            }
                        }

                        temp1 = reader.readLine();
                    }

                    reader.close();
                    break;
                }
            }
        }
        System.out.println(sameFileNum + " sameFileNum");
        System.out.println(sameFileLoc + " sameFileLoc");
        System.out.println(sameDirNum + " sameDirNum");
        System.out.println(sameDirLoc + " sameDirLoc");
        System.out.println(DiffDirNum + " DiffDirNum");
        System.out.println(DiffDirLoc + " DiffDirLoc");
    }
}
