#!/bin/bash
###description auto install java environment,nginx,tomcat
###version v1.5
###updated 2018-03-21
BASE_PATH=/web;
if [ ! -d $BASE_PATH ]; then
  mkdir -p $BASE_PATH
fi
NGINX=false
while getopts "n" arg #选项后面的冒号表示该选项需要参数
do
    case $arg in
      n)
        echo "with NGINX MODE..."
        NGINX=true
        ;;
      ?)  #当有不认识的选项的时候arg为?
        echo "unkonw argument"
        exit 1
        ;;
    esac
done
echo 'Install environment ...';
yum install -y autoconf svn zlib zlib-devel openssl openssl-devel cronolog  pcre pcre-devel gcc-c++

APP_PATH="$BASE_PATH/app";
if [ ! -d $APP_PATH ]; then
   mkdir $APP_PATH
fi

CONF_PATH="$BASE_PATH/conf";
if [ ! -d $CONF_PATH ]; then
   mkdir $CONF_PATH
fi

BIN_PATH="$BASE_PATH/bin";
if [ ! -d $BIN_PATH ]; then
  mkdir -p $BIN_PATH
fi

TMP_PATH="$BASE_PATH/tmp";
if [ ! -d $TMP_PATH ]; then
   mkdir $TMP_PATH
fi

LOG_PATH="$BASE_PATH/log";
if [ ! -d $LOG_PATH ]; then
   mkdir $LOG_PATH
fi

##Install jdk, nginx
NGINX_HOME='nginx'
NGINX_URL='https://zhengtuo.oss-cn-qingdao.aliyuncs.com/nginx.tgz'
JAVA_HOME='java'
JAVA_URL='https://zhengtuo.oss-cn-qingdao.aliyuncs.com/jdk1.8.0.tgz'
SB_URL='https://zhengtuo.oss-cn-qingdao.aliyuncs.com/sb'

cd $BIN_PATH
wget $SB_URL -O sb
chmod +x $BIN_PATH/*

cd $TMP_PATH
echo "Downloading JDK..."
wget $JAVA_URL -O jdk.tgz
tar -zxvf jdk.tgz -C $BIN_PATH/

if [ $NGINX = true  ]; then
    echo "Downloading NGINX..."
    wget $NGINX_URL -O nginx.tgz
    echo 'Installing nginx'
    tar -zxvf nginx.tgz
    cd $TMP_PATH/nginx
    ./configure --prefix=$BIN_PATH/nginx --with-http_ssl_module --with-stream
    make && make install && make clean >> /dev/null
    cd $BIN_PATH/nginx/conf
    wget https://zhengtuo.oss-cn-qingdao.aliyuncs.com/nginx.conf -O nginx.conf
    mkdir vhost && cd vhost
    wget https://zhengtuo.oss-cn-qingdao.aliyuncs.com/ztuo-api.conf
    wget https://zhengtuo.oss-cn-qingdao.aliyuncs.com/ztuo-web.conf
fi


cd $TMP_PATH;

rm -rf $TMP_PATH/*
#config java
echo JAVA_HOME=${BIN_PATH}/jdk1.8.0 >> /etc/profile
echo PATH=\$PATH:$BIN_PATH:\$JAVA_HOME/bin >> /etc/profile
source /etc/profile

#config nginx

echo 'SUCCESS!';