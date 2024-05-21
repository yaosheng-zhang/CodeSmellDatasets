#!/bin/bash

# 设置Jar包的名称和路径
JAR_NAME="CodeSmellDatasets.jar"
JAR_PATH="/home/ngtl/datasets/CodeSmellDatasets_jar"


if [ $# -ne 6 ]; then
    echo "Usage: $0 <param1> <param2> <param3> <param4> <param5> <param6>"
    exit 1
fi

PARAM1="$1"
PARAM2="$2"
PARAM3="$3"
PARAM4="$4"
PARAM5="$5"
PARAM6="$6"

#获取文件名filename
filename=$(java -jar /home/ngtl/datasets/CodeSmellDatasets_jar/GitHubUrlParser.jar "$PARAM6")

# 循环检查程序是否在运行
while true; do
  sleep 5
    # 使用适当的方法检查程序是否在运行，比如pgrep命令
    if pgrep -f "$JAR_NAME" >/dev/null; then
        # 程序在运行中，等待一段时间后继续检查
        sleep 10
    else
        # 程序停止运行

        # 获取上一次退出的状态码
        exit_status=$?
#        echo $exit_status

        # 如果是因为报错停止（非正常退出）
        if [ $exit_status -ne 0 ]; then
            echo "程序因为报错停止，正在重新运行..."
            # 重启程序
            nohup java -jar "$JAR_PATH/$JAR_NAME" "$PARAM1" "$PARAM2" "$PARAM3" "$PARAM4" "$PARAM5" > /home/ngtl/datasets/files/$filename/nohup.out 2>&1&
        else
            echo "程序正常停止，不再重新运行。"
            # 退出脚本
            exit
        fi
    fi
done
