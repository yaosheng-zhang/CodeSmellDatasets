#!/bin/bash
set -e

if [ $# -ne 1 ]; then
    echo "Usage: $0 <link>"
    exit 1
fi

link="$1"
filename=$(java -jar /home/ngtl/datasets/CodeSmellDatasets_jar/GitHubUrlParser.jar "$link")

project_path="/home/ngtl/datasets/$filename"
# 获取版本号，如果出错就继续执行
if version=$(git -C "$project_path" describe --tags 2>/dev/null); then
    echo "Version: $version"
else
    version='null'
    echo "Warning: Failed to get version from git describe. Continuing..."
fi

# 运行CodeSmellDatasets得到最终的json文件
nohup java -jar /home/ngtl/datasets/CodeSmellDatasets_jar/CodeSmellDatasets.jar "/home/ngtl/datasets/files/$filename/$filename.txt" "/home/ngtl/datasets/files/$filename/output.json" "/home/ngtl/datasets/files/result/$filename.json" "$link" "$version" > /home/ngtl/datasets/files/$filename/nohup.out 2>&1&

# 监控CodeSmellDatasets.jar
sudo bash /home/ngtl/datasets/monitor.sh "/home/ngtl/datasets/files/$filename/$filename.txt" "/home/ngtl/datasets/files/$filename/output.json" "/home/ngtl/datasets/files/result/$filename.json" "$link" "$version"





