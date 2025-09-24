import xml.etree.ElementTree as ET
import csv
import re
from datetime import datetime
import data

def extract_clones(source_name, txt_file, type): 
    clones = []

    # 读取文件内容
    with open(txt_file, 'r', encoding='utf-8') as file:
        data = file.read()

    # 匹配 <clone> 标签及其内容
    clone_pattern = r'<clone nlines="(\d+)" similarity="(\d+)">(.*?)</clone>'
    matches = re.findall(clone_pattern, data, re.DOTALL)

    for match in matches:
        sources = []
        # 匹配 <source> 标签
        if (source_name == 'milo' and type == '3'):
            source_pattern = r'<source file="(.*?)" startline="(\d+)" endline="(\d+)" pcid="(\d+)"/>'
        else:
            source_pattern = r'<source file="(.*?)" startline="(\d+)" endline="(\d+)" pcid="(\d+)"></source>'
        
        source_matches = re.findall(source_pattern, match[2])
        for source in source_matches:
            file = source[0]
            base_index = file.find("latest/" + source_name)
    
            # 如果找到了索引，返回后面的路径
            if base_index != -1:
                file_path = file[base_index + len("latest/" + source_name) + 1:]
            source_info = {
                'pcid': source[3],
                'file_path': file_path,
                'start_line': int(source[1]),
                'end_line': int(source[2]),
                'similarity': match[1]
            }
            sources.append(source_info)
        clones.append(sources)
    return clones

def get_time(source_name, clone_type, pcid):
    file_path = 'cloneblock_commit_times\\' + source_name + '.csv' 
    with open(file_path, mode='r', newline='', encoding='utf-8') as csvfile:
        csvreader = csv.reader(csvfile)
        rows = list(csvreader)
    for row in rows: 
        if row[0] == clone_type and row[1] == pcid:
            return row[2]
        else:
            continue
    return None
        
def detect(source_name, clone_list, clone_type, write_file):
    count = 0
    count_in_commit = 0
    count_between_commit = 0 
    count_test_in_commit = 0
    count_test_between_commit = 0
    for clone_pair in clone_list:
        print("Detect " + source_name + "'s " + str(count) + " clone pair...")
        count += 1
        time_1 = get_time(source_name, clone_type, clone_pair[0]['pcid'])
        time_2 = get_time(source_name, clone_type, clone_pair[1]['pcid'])
        # print(time_1, time_2)
        # 跳过milo中的大量加载(防止数据污染)
        if "opc-ua-sdk/sdk-server/src/main/java/org/eclipse/milo/opcua/sdk/server/namespaces/loader" in clone_pair[0]['file_path'] or "opc-ua-sdk/sdk-server/src/main/java/org/eclipse/milo/opcua/sdk/server/namespaces/loader" in clone_pair[1]['file_path']:
            continue
        if time_1 and time_2: 
            if time_1 == time_2:
                count_in_commit += 1
                if ('test' in clone_pair[0]['file_path']) or ('test' in clone_pair[0]['file_path']):
                    count_test_in_commit += 1
                elif ('example' in clone_pair[0]['file_path']) or ('example' in clone_pair[0]['file_path']):
                    count_test_in_commit += 1
                elif 'test' in clone_pair[1]['file_path'] or ('test' in clone_pair[1]['file_path']):
                    count_test_in_commit += 1
                elif ('example' in clone_pair[1]['file_path']) or ('example' in clone_pair[1]['file_path']):
                    count_test_in_commit += 1
                else:
                    continue
            else:
                count_between_commit += 1
                if ('test' in clone_pair[0]['file_path']) or ('test' in clone_pair[0]['file_path']):
                    count_test_between_commit += 1
                elif ('example' in clone_pair[0]['file_path']) or ('example' in clone_pair[0]['file_path']):
                    count_test_between_commit += 1
                elif 'test' in clone_pair[1]['file_path'] or ('test' in clone_pair[1]['file_path']):
                    count_test_between_commit += 1
                elif ('example' in clone_pair[1]['file_path']) or ('example' in clone_pair[1]['file_path']):
                    count_test_between_commit += 1
                else:
                    continue
        else:
            count_in_commit += 1
            if ('test' in clone_pair[0]['file_path']) or ('test' in clone_pair[0]['file_path']):
                count_test_in_commit += 1
            elif ('example' in clone_pair[0]['file_path']) or ('example' in clone_pair[0]['file_path']):
                count_test_in_commit += 1
            elif 'test' in clone_pair[1]['file_path'] or ('test' in clone_pair[1]['file_path']):
                count_test_in_commit += 1
            elif ('example' in clone_pair[1]['file_path']) or ('example' in clone_pair[1]['file_path']):    
                count_test_in_commit += 1
            else:
                continue
    write(source_name, len(clone_list), count_in_commit, count_between_commit, count_test_in_commit, count_test_between_commit, clone_type, write_file)
    
def write(source_name, clone_list_len , count_in_commit, count_between_commit, count_test_in_commit, count_test_between_commit, clone_type, write_file):
    file = 'clone_type_commit_result\\' + write_file 
    with open(file, 'a', newline='') as csvfile:
        file_names = ['project_name', 'type', 'clone_pair_count', 'count_in_commit', 'count_between_commit', 'count_test_in_commit', 'count_test_between_commit']
        writer = csv.DictWriter(csvfile, fieldnames=file_names)
        writer.writerow({
            'project_name': source_name, 
            'type': clone_type,
            'clone_pair_count': clone_list_len, 
            'count_in_commit': count_in_commit, 
            'count_between_commit': count_between_commit,
            'count_test_in_commit': count_test_in_commit,
            'count_test_between_commit': count_test_between_commit})
         
if __name__ == '__main__':
    source = data.source_latest_version
    write_file = 'clone_on_commit_result.csv'
    for source_name, source_versions in source.items():
        for source_version in source_versions:
            print('Start: ' + source_name + ' ' + source_version)
            file = 'Tools\CCDetector\src\main\inputCS\Results\\' + source_name + '_clone-abstract\\' + source_name + '-Type3'  + '.txt' 
            type_3_clone_list = extract_clones(source_name, file)
            detect(source_name, type_3_clone_list, '3', write_file)
            print('Finish: ' + source_name + ' ' + source_version)
    
    print("Detect all projects' clone pairs.")
