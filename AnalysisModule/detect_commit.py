from pydriller import Repository
from collections import defaultdict
import xml.etree.ElementTree as ET
import csv
import os
import data

def get_commit_segments_test(path_to_repo):
    print("Detecting code segments introduced in each commit...")
    count = 0
    for commit in Repository(path_to_repo).traverse_commits():
        for modification in commit.modified_files:
            if modification.diff_parsed['added']:
                print(modification.new_path)
                print(modification.filename)
        
def write_commit_to_csv(git_path, source_name):
    csv_file = "commitFile//" + source_name + ".csv"
    print("Connecting to Git repository...")
    
    # 检查文件是否存在,以确定是否需要写入标题行
    file_exists = os.path.isfile(csv_file)
    
    # 从 CSV 文件中读取所有现有的 commit hash, 避免重复写入
    existing_commits = {}
    if file_exists:
        with open(csv_file, 'r', newline='', encoding='utf-8') as csvfile:
            reader = csv.DictReader(csvfile)
            # existing_hashes = {row['hash'] for row in reader}
            for row in reader:
                file_path = row['new_path'].split(', ')
                if row['hash'] in existing_commits:
                    existing_commits[row['hash']].extend(file_path)
                else:
                    existing_commits[row['hash']] = file_path
            
    # 创建 CSV 文件并写入标题行
    with open(csv_file, 'a', newline='', encoding='utf-8') as csvfile:
        
        fieldnames = ['hash', 'committer', 'timestamp', 'old_path', 'new_path', 'change_type', 'file_name', 'code_line', 'add_line', 'delete_line']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        
        # 如果文件是新创建的,写入标题行
        if not file_exists:
            writer.writeheader()
        
        count = 0
        # 遍历仓库的所有提交
        for commit in Repository(git_path).traverse_commits():
            count += 1
            print(source_name + "'s commit " + str(count) + " is being processed...")
            for modification in commit.modified_files:
                if commit.hash not in existing_commits:
                    # 提交未写入, 获取提交相关信息
                    # 写入 CSV 文件
                    line_number = ''
                    mark = ''
                    if modification.diff_parsed['added']: 
                        mark = 'Add'
                        line_number = modification.diff_parsed['added'][0][0]
                    elif modification.diff_parsed['deleted']: 
                        mark = 'Delete'
                        line_number = modification.diff_parsed['deleted'][0][0]
                    else: 
                        mark = 'Others changed'
                        line_number = None
                    writer.writerow({
                                'hash': commit.hash,  # 提交 hash
                                'committer': commit.committer.name,  # 提交者姓名
                                'timestamp': commit.author_date.strftime('%Y-%m-%d %H:%M:%S'),  # 提交时间
                                'old_path': modification.old_path,  # 修改前路径
                                'new_path': modification.new_path,  # 修改后路径
                                'change_type': modification.change_type.name,  # 修改类型(ADD、DELETE、MODIFY、RENAME)
                                'file_name': modification.filename,  # 文件名
                                'code_line': line_number,  # 代码行号
                                'add_line': modification.added_lines,  # 新增行数
                                'delete_line': modification.deleted_lines,  # 删除行数
                            })    
                    print("Writing " + mark + " commit data to CSV file...")
                elif modification.new_path not in existing_commits[commit.hash]:
                    # 提交未写入, 获取提交相关信息
                    # 写入 CSV 文件
                    line_number = ''
                    mark = ''
                    if modification.diff_parsed['added']: 
                        mark = 'Add'
                        line_number = modification.diff_parsed['added'][0][0]
                    elif modification.diff_parsed['deleted']: 
                        mark = 'Delete'
                        line_number = modification.diff_parsed['deleted'][0][0]
                    else: 
                        mark = 'Others changed'
                        line_number = None
                    writer.writerow({
                                'hash': commit.hash,
                                'committer': commit.committer.name,
                                'timestamp': commit.author_date.strftime('%Y-%m-%d %H:%M:%S'),
                                'old_path': modification.old_path,
                                'new_path': modification.new_path,
                                'change_type': modification.change_type.name,
                                'file_name': modification.filename,
                                'code_line': line_number,
                                'add_line': modification.added_lines,
                                'delete_line': modification.deleted_lines,
                            })    
                    print("Writing " + mark + " commit data to CSV file...")
                else:
                    print("The commit_file has been written to CSV file, skip it.")
                    continue
        
        print("Write to csv successfully!")
                   

if __name__ == '__main__':

    # 指定代码克隆的xml文件地址
    source_name_list = []
    
    # 指定要分析的 Git 仓库路径
    git_path_list = data.git_path_list
    # 用于存储每个提交中引入的代码段
    commit_code_segments = defaultdict(list)
    # 存取 code_segments 信息
    code_segments = []
    
    for i in range(len(source_name_list)):
        print("Processing " + source_name_list[i] + "...")
        source_name = source_name_list[i]
        git_path = git_path_list[i] 
        write_commit_to_csv(git_path, source_name)

    print("Process Ending!")