import analysis_codeclone as ac
import re
import data
import csv 

def extract_clones(source_name, txt_file): 
    clones = []

    # 读取文件内容
    with open(txt_file, 'r', encoding='utf-8') as file:
        data = file.read()

    # 匹配 <clone> 标签及其内容
    clone_pattern = r'<clonepair\d+>(.*?)</clonepair\d+>'
    matches = re.findall(clone_pattern, data, re.DOTALL)

    for match in matches:
        sources = []
        # 匹配 <source> 标签
        source_pattern = r'<source file="(.*?)" startline="(\d+)" endline="(\d+)" pcid="(\d+)"></source>'
        source_matches = re.findall(source_pattern, match)
        for source in source_matches:
            file = source[0]
            base_index = file.find(source_name + '-' + source_version)
    
            # 如果找到了索引，返回后面的路径
            if base_index != -1:
                file_path = file[base_index + len(source_name + '-' + source_version) + 1:]
            source_info = {
                'file_path': file_path,
                'start_line': source[1],
                'end_line': source[2],
                'pcid': source[3]
            }
            sources.append(source_info)
        clones.append(sources)
    return clones
        
        
if __name__ == '__main__':
    source = data.source_latest_version
    write_file = 'coclone_on_commit_result.csv'
    for source_name, source_versions in source.items():
        for source_version in source_versions:
            print('Start: ' + source_name + ' ' + source_version)
            file_path = 'Tools\CCDetector\src\main\outputCC\\' + source_name + '\\' + source_name + '-' + source_version + '__noduplicated.txt'
            clone_list = extract_clones(source_name, file_path)
            ac.detect(source_name, clone_list, '2', write_file)
            print('Finish: ' + source_name + ' ' + source_version)