FROM artifactory.apps.dev-nonpci-shared.npnbank.local/docker/croatia-base-agent:latest

USER root

RUN apt update && \
apt install -y \
podman=3.0.1+dfsg1-3+deb11u1 \
buildah=1.19.6+dfsg1-1+b6 && \
apt clean

RUN npm i -g yarn

RUN curl -L https://github.com/tmccombs/hcl2json/releases/download/0.3.5/hcl2json_linux_amd64 -o /tmp/hcl2json_linux_amd64 && \
chmod 755 /tmp/hcl2json_linux_amd64 &&\
mv /tmp/hcl2json_linux_amd64 /usr/local/bin/hcl2json

RUN mkdir -m 0755 -p /etc/apt/keyrings
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
RUN echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

RUN apt update && \
apt install -y \
docker-ce-cli=5:23.0.1-1~debian.11~bullseye \
docker-buildx-plugin=0.10.2-1~debian.11~bullseye \
docker-compose-plugin=2.16.0-1~debian.11~bullseye && \
apt clean

RUN rm /etc/containers/registries.conf
COPY registries.conf /etc/containers/registries.conf

USER jenkins

RUN echo 'testcontainers.reuse.enable=true' > /home/jenkins/.testcontainers.properties

RUN mkdir -p ~/.gradle/wrapper/dists/
RUN curl -L https://services.gradle.org/distributions/gradle-7.4.2-bin.zip -o ~/.gradle/wrapper/dists/gradle-7.4.2-bin.zip
