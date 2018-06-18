FROM openjdk:8-jdk-alpine

# $HOME for root is /root

RUN mkdir -p /root/logs /root/var/locks

ADD crontab.running /root
ADD .bash_profile /root
ADD target/for-docker /root

RUN chmod +x /root/services/bin/*.sh
RUN crontab - < /root/crontab.running

CMD [ "crond", "-f", "-d", "0" ]
