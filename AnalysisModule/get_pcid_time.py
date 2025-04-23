import csv
import re
import data
from datetime import datetime
import pandas as pd

# 获取此类型下的所有code block
def extract_clones(source_name, file):
    sources = []
    # 读取文件内容
    with open(file, 'r') as file:
        data = file.read()

    # 匹配 <clone> 标签及其内容
    clone_pattern = r'<clone nlines="(\d+)" similarity="(\d+)">(.*?)</clone>'
    matches = re.findall(clone_pattern, data, re.DOTALL)

    for match in matches:
        # 匹配 <source> 标签
        source_pattern = r'<source file="(.*?)" startline="(\d+)" endline="(\d+)" pcid="(\d+)"></source>'
        source_matches = re.findall(source_pattern, match[2])
        for source in source_matches:
            file = source[0]
            base_index = file.find("latest/" + source_name)
    
            # 如果找到了索引，返回后面的路径
            if base_index != -1:
                file_path = file[base_index + len("latest/" + source_name) + 1:]
            source_info = {
                'file_path': file_path,
                'start_line': source[1],
                'end_line': source[2],
                'pcid': source[3]
            }
            sources.append(source_info)
            
    seen = set()
    unique_data = []
    for item in sources:
        if item['pcid'] not in seen:
            unique_data.append(item)
            seen.add(item['pcid'])
    return unique_data

def get_earliest_name(file_path, rows):
    # print(file_path)
    for row in rows[1:]:
        if row[4].replace('\\', '/') == file_path:
            # 该文件为最早的新增文件，直接返回
            if row[5] == 'ADD':
                return file_path
            # 发生重命名，递归
            elif row[5] == 'RENAME':
                return get_earliest_name(row[3].replace('\\', '/'), rows)
            else:
                continue
        else:
            continue
    return file_path 

def get_earliest_time(file_path, rows, start_line, end_line):
    file_path = get_earliest_name(file_path, rows).replace('\\', '/')
    commit_start_line = 0
    commit_end_line = 0
    time = ''
    for row in rows[1:]:
        if row[4].replace('\\', '/') == file_path and row[5] != 'DELETE':
            # print(file_path.replace("/", "\\"), start_line, end_line, commit_start_line, commit_end_line)
            if row[5] == 'ADD':
                time = row[2]
                if pd.notna(row[7]) and row[7] != '':
                    commit_start_line = int(row[7])
                    if int(row[8]) > int(row[9]):
                        commit_end_line = commit_start_line + int(row[8])
                    else:
                        commit_end_line = commit_start_line + int(row[9])
                else:
                    commit_start_line = 0
                    commit_end_line = 0
            elif row[5] == 'MODIFY':
                if pd.notna(row[7]) and row[7] != '':
                    commit_start_line = int(row[7])
                    if int(row[8]) > int(row[9]):
                        commit_end_line = commit_start_line + int(row[8])
                    else:
                        commit_end_line = commit_start_line + int(row[9])
                else:
                    commit_start_line = 0
                    commit_end_line = 0
        elif row[3].replace('\\', '/') == file_path and row[5] == 'RENAME':
            # print(file_path.replace("/", "\\"), start_line, end_line, commit_start_line, commit_end_line)
            file_path = row[4].replace('\\', '/')
            if pd.notna(row[7]) and row[7] != '':
                commit_start_line = int(row[7])
                if int(row[8]) > int(row[9]):
                    commit_end_line = commit_start_line + int(row[8])
                else:
                    commit_end_line = commit_start_line + int(row[9])
        # 没有发生任何代码变更，直接查找下一个
        else:
            continue
        # 若代码变更在代码块中，则更新时间
        if (commit_start_line <= int(end_line)) and (commit_end_line >= int(start_line)):
            time = row[2]
        else:
            continue
    return time


def get_clone_block_time(source_name, clone_block):
    commit_file = 'commitFile\\' + source_name + '.csv'
    time_format = '%Y-%m-%d %H:%M:%S'
    with open(commit_file, mode='r', newline='', encoding='utf-8') as csvfile:
        csvreader = csv.reader(csvfile)
        rows = list(csvreader)
        file_path = clone_block['file_path']
        start_line = clone_block['start_line']
        end_line = clone_block['end_line'] 
        # 找到文件最早更新时间
        earliest_time = get_earliest_time(file_path, rows, start_line, end_line)
        if earliest_time == '':
            return None
        else:
            return datetime.strptime(earliest_time, time_format)

if __name__ == '__main__':
    source_names = data.source_latest_version
    clone_types = ['1', '2', '3']
    # for source_name,source_version in source_names.items():
    for source_name in data.java_source_name:
        for clone_type in clone_types:
            print("Start: " + source_name + " Type: " + clone_type)
            file_path = 'Tools\CCDetector\src\main\inputCS\\' + source_name + "_functions-clones\\" + source_name + '_clone-abstract\\' + source_name + '-Type' + clone_type + '.txt' 
            clone_blocks = extract_clones(source_name, file_path)
            file = 'cloneblock_commit_times\\' + source_name + '.csv'
            with open(file, 'a', newline='') as csvfile:
                file_names = ['type', 'pcid', 'time']
                writer = csv.DictWriter(csvfile, fieldnames=file_names)
                for clone_block in clone_blocks:
                    time = get_clone_block_time(source_name, clone_block)

                    writer.writerow({
                        'type': clone_type,
                        'pcid': clone_block['pcid'],
                        'time': time
                    })
        

