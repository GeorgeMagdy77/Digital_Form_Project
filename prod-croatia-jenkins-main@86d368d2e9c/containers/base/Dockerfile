FROM artifactory.apps.prod-nonpci-shared.nrapp.cloud/docker/jenkins/inbound-agent:latest-jdk17

USER root

RUN apt update && \
apt upgrade -y && \
apt install -y \
gnupg=2.2.27-2+deb11u2 \
software-properties-common=0.96.20.2-2.1 \
wget=1.21-1+deb11u1 && \
unzip=6.0-26+deb11u1 && \
apt clean

RUN curl -sL https://deb.nodesource.com/setup_18.x -o /tmp/nodesource_setup.sh && \
bash /tmp/nodesource_setup.sh && \
rm -f /tmp/nodesource_setup.sh

RUN apt update && \
apt install -y \
curl=7.74.0-1.3+deb11u7 \
python3=3.9.2-3 \
python3-pip=20.3.4-4+deb11u1 \
nodejs=18.16.0-deb-1nodesource1 && \
apt clean

RUN curl -L https://go.dev/dl/go1.19.3.linux-amd64.tar.gz -o /tmp/golang.tar.gz && \
tar -zxvf /tmp/golang.tar.gz -C /usr/local && \
rm -f /tmp/golang.tar.gz

ENV PATH="${PATH}:/usr/local/go/bin:~/go/bin"

ADD croatia-ca.crt /usr/local/share/ca-certificates/croatia-ca.crt
RUN chmod 644 /usr/local/share/ca-certificates/croatia-ca.crt
RUN /opt/java/openjdk/bin/keytool -import -alias croatiaca -file /usr/local/share/ca-certificates/croatia-ca.crt -keystore /opt/java/openjdk/lib/security/cacerts -noprompt -storepass changeit
RUN update-ca-certificates

USER jenkins
