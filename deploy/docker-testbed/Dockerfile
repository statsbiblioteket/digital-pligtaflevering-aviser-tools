FROM openjdk:8-jdk-alpine

RUN apk --update add tzdata

# $HOME for root is /root
RUN mkdir -p /root/logs /root/var/locks /delivery-samples /delivery-samples-done-dir

ADD crontab.running .bash_profile configure.sh dpa-cron.properties /root/
ADD target/for-docker /root

RUN ash /root/configure.sh < /root/dpa-cron.properties
RUN chmod +x /root/services/bin/*.sh
RUN crontab - < /root/crontab.running

CMD [ "crond", "-f", "-d", "0" ]
