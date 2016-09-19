#!/usr/bin/env bash

$HOME/7880-doms/bin/doms.sh update
$HOME/batch-trigger-1.9-SNAPSHOT/bin/trigger-on-new-batch.sh $HOME/batch-trigger-1.9-SNAPSHOT/conf/trigger-config.sh
$HOME/newspaper-prompt-doms-ingester-1.9-SNAPSHOT/bin/pollAndWork.sh
