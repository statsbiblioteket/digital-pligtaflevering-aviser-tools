FROM openjdk:8-jdk-alpine

RUN ln -s /bin/ash /bin/bash

# $HOME for root is /root



ADD crontab.running /root
ADD .bash_profile /root
ADD target/for-docker /root


RUN chmod +x /root/services/bin/*.sh
RUN crontab - < /root/crontab.running

CMD [ "crond", "-f", "-d", "0" ]
