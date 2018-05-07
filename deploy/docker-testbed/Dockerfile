FROM openjdk:8-jdk-alpine

# $HOME for root
ADD target/for-docker /root

RUN (echo "*/14 * * * * run-parts /etc/periodic/14min"; crontab -l ) | crontab -

CMD [ "crond", "-f", "-d", "0" ]
