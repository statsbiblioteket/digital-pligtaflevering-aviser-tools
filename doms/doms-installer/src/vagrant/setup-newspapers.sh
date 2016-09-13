#!/usr/bin/env bash
SCRIPT_DIR=$(dirname $(readlink -f $0))


function clean(){
    local artifactID=$1
    artifactFolder=$(ls  $artifactID-* -d | head -1)
    if [ -d "$artifactFolder" ]; then
         rm -rf "$artifactFolder"
    fi
}

function extract(){
    local artifactID=$1
    clean $artifactID
    local artifactFile=$(ls "$artifactID-LATEST-"* | head -1)
    tar -xzf $artifactFile
    ls  $artifactID-* -d | head -1
}

#Get the component package from where it is stored. Used to be nexus, now it is in builds
function getComponent(){
    local groupID=$1
    local artifactID=$2
    local type=$3
    local classifier=$4
    local URL="https://sbforge.org/nexus/service/local/artifact/maven/redirect?r=snapshots&g=$groupID&a=$artifactID&v=LATEST&e=$type&c=$classifier"
    echo $URL
    local file="$artifactID-LATEST-$classifier.$type"
    wget -q "$URL" -O "$file"
    echo ${file}

}

#  git clone ssh://git@sbprojects.statsbiblioteket.dk:7999/avis/devel-config.git
ln -s /vagrant/devel-config $HOME/devel-config

cd ~
wget -N "http://ftp.download-by.net/apache/zookeeper/stable/zookeeper-3.4.8.tar.gz"
tar -xzf zookeeper-3.4.8.tar.gz
cp /vagrant/zoo.cfg  $HOME/zookeeper-3.4.8/conf/
bash $HOME/zookeeper-3.4.8/bin/zkServer.sh restart



mkdir -p "$HOME/done-batches"

artifact="batch-trigger"
getComponent "dk.statsbiblioteket.newspaper" "$artifact" "tar.gz" "package"
folder=$(extract $artifact)
rm -rf $folder/conf
ln -s $HOME/devel-config/${artifact}-config/ $PWD/$folder/conf



#doms ingester
artifact="newspaper-prompt-doms-ingester"
getComponent "dk.statsbiblioteket.newspaper" "$artifact" "tar.gz" "package"
folder=$(extract $artifact)
rm -rf $folder/conf
ln -s $HOME/devel-config/${artifact}-config/ $PWD/$folder/conf


