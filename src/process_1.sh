#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: $0 <link>"
    exit 1
fi

link="$1"
filename=$(java -jar /home/ngtl/datasets/CodeSmellDatasets_jar/GitHubUrlParser.jar "$link")

#获取版本号
#project_path="/home/ngtl/datasets/$filename"
#version=$(git -C "$project_path" describe)

# 指定要创建的文件夹路径
folder_path="/home/ngtl/datasets/files/$filename"

# 使用 mkdir 命令创建文件夹
mkdir -p "$folder_path"

# 运行 cppcheck 以及存储结果 cppcheck-result.xml
cppcheck --rule=misra_c --addon="/usr/share/cppcheck/addons/misra.json" -j 8 --xml --xml-version=2 "/home/ngtl/datasets/$filename" 2> "/home/ngtl/datasets/files/$filename/cppcheck-result.xml"
# 运行cppcheck-result.sh 处理 cppcheck-result.xml 得到结果 newfile.xml
sudo bash /data/jenkins_home/workspace/cppcheck-result.sh "/home/ngtl/datasets/files/$filename/cppcheck-result.xml" "/home/ngtl/datasets/files/$filename/newfile.xml"
# 运行 JSONUtil 通过newfile.xml 产生json文件
java -jar /home/ngtl/datasets/CodeSmellDatasets_jar/JSONUtil.jar "/home/ngtl/datasets/files/$filename/newfile.xml" "/home/ngtl/datasets/files/$filename/output.json"
# 筛掉重复的代码
java -jar /home/ngtl/datasets/CodeSmellDatasets_jar/RemoveDuplicateContext.jar "/home/ngtl/datasets/files/$filename/output.json"