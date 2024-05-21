#!/bin/bash
if [ $# -ne 1 ]; then
    echo "Usage: $0 <link>"
    exit 1
fi
link="$1"
#获取文件名filename
filename=$(java -jar /home/ngtl/datasets/CodeSmellDatasets_jar/GitHubUrlParser.jar "$link")
#使用git clone获取项目
git clone $link /home/ngtl/datasets/$filenameclon