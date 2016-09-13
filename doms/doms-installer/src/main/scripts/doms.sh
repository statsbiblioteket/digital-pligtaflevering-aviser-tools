#!/bin/bash

#
# Detect Java version
#
if [ -z "$JAVA_HOME" ] ; then
    JAVA_EXEC=java
else
    JAVA_EXEC="$JAVA_HOME/bin/java"
fi

#TODO
# status method to check if running

#
# Setup environment
#
SCRIPT_DIR=$(dirname $(readlink -f $BASH_SOURCE[0]))
pushd $SCRIPT_DIR > /dev/null
SCRIPT_DIR=$(pwd)
popd > /dev/null

DOMS_BIN="$SCRIPT_DIR/../tomcat/bin/"
SBOI_BIN="$SCRIPT_DIR/../sboi-summarise/bin"
DOMSWUI_SUMMA_BIN="$SCRIPT_DIR/../domswui-summarise/bin"


function start_doms {
    echo "Starting DOMS .."
    $DOMS_BIN/startup.sh
    echo "Done."
}

function stop_doms {
    echo "Stopping DOMS .."
    $DOMS_BIN/shutdown.sh
    echo "Done."
}

function start_summa {
    echo "Starting summa (you can safely ignore the 'SEVERE Catalina.stop' error) .."
    $SBOI_BIN/start_resident.sh
    $DOMSWUI_SUMMA_BIN/start_resident.sh
    echo "Done."
}

function stop_summa {
    echo "Stopping summa .."
    $SBOI_BIN/stop_resident.sh
    $DOMSWUI_SUMMA_BIN/stop_resident.sh
    echo "Done."
}

function start {
    start_doms
    start_summa
}

function stop {
    stop_summa
    stop_doms
}

function restart {
    stop
    start
}

function import {
    echo "Ingesting all data in SBOI .."
    $SBOI_BIN/ingest_full.sh
    echo "Re-building the index in SBOI.."
    $SBOI_BIN/index_full.sh

    echo "Ingesting all data in domsGui .."
    $DOMSWUI_SUMMA_BIN/ingest_full.sh
    echo "Re-building the index in domsGui.."
    $DOMSWUI_SUMMA_BIN/index_full.sh
    echo "Restarting summa .."

    start_summa
}

function update {


    echo "Ingesting updates .."
    $SBOI_BIN/ingest_update.sh
    $DOMSWUI_SUMMA_BIN/ingest_update.sh
    echo "Updating the index .."
    $SBOI_BIN/index_update.sh
    $DOMSWUI_SUMMA_BIN/index_update.sh
}

case $1 in
    start)
        start
        ;;
    start_summa)
        start_summa
        ;;
    start_doms)
        start_doms
        ;;
    stop)
        stop
        ;;
    stop_summa)
        stop_summa
        ;;
    stop_doms)
        stop_doms
        ;;
    restart)
        restart
        ;;
    import)
        import
        ;;
    update)
        update
        ;;
    *)
        echo $"Usage: $0 {start|start_summa|start_doms|stop|stop_summa|stop_doms|restart|import|update}"
        exit 1

esac
