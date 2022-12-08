#!/bin/bash

set -e
cd $(dirname $0)
cd ..

branch=$1
mainBranch=$2

git checkout ${mainBranch}
git merge --squash ${branch}

# 前置步骤

read -p "手动修改版本进行提交:" isOk
if [ "${isOk}" == "1" ]; then
    git checkout ${branch}
    git merge --no-edit ${mainBranch}
fi

read -p "检查是否存在冲突:" isOk
if [ "${isOk}" == "1" ]; then
    # 后置步骤
	chmod 777 gradlew
	./gradlew clean publish
fi