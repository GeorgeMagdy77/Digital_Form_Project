FROM nexus.projectcroatia.cloud:18080/jenkins/croatia-base-agent:latest

USER root

RUN wget -O- https://apt.releases.hashicorp.com/gpg | \
gpg --dearmor | \
tee /usr/share/keyrings/hashicorp-archive-keyring.gpg

RUN gpg --no-default-keyring \
--keyring /usr/share/keyrings/hashicorp-archive-keyring.gpg \
--fingerprint

RUN echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] \
https://apt.releases.hashicorp.com $(lsb_release -cs) main" | \
tee /etc/apt/sources.list.d/hashicorp.list

RUN apt update && \
apt install -y \
podman=3.0.1+dfsg1-3+deb11u1 \
buildah=1.19.6+dfsg1-1+b6 \
terraform=1.3.5 \
packer=1.8.4-1 && \
apt clean

RUN pip3 install oci-cli

RUN curl -L https://get.helm.sh/helm-v3.10.1-linux-amd64.tar.gz -o /tmp/helm.tar.gz && \
tar -zxvf /tmp/helm.tar.gz -C /tmp && \
mv /tmp/linux-amd64/helm /usr/local/bin && \
rm -f /tmp/helm.tar.gz && \
rm -rf /tmp/linux-amd64

RUN curl -L https://dl.k8s.io/release/v1.25.3/bin/linux/amd64/kubectl -o /tmp/kubectl && \
chmod 755 /tmp/kubectl && \
mv /tmp/kubectl /usr/local/bin

RUN curl -L https://github.com/tmccombs/hcl2json/releases/download/0.3.5/hcl2json_linux_amd64 -o /tmp/hcl2json_linux_amd64 && \
chmod 755 /tmp/hcl2json_linux_amd64 &&\
mv /tmp/hcl2json_linux_amd64 /usr/local/bin/hcl2json

RUN rm /etc/containers/registries.conf
COPY registries.conf /etc/containers/registries.conf

RUN curl -L https://github.com/yannh/kubeconform/releases/download/v0.5.0/kubeconform-linux-amd64.tar.gz -o /tmp/kubeconform-linux-amd64.tar.gz && \
tar -zxvf /tmp/kubeconform-linux-amd64.tar.gz -C /tmp && \
mv /tmp/kubeconform /usr/local/bin && \
rm -f /tmp/LICENSE && \
rm -f /tmp/kubeconform-linux-amd64.tar.gz && \
chmod 755 /usr/local/bin/kubeconform

RUN curl -L https://github.com/zegl/kube-score/releases/download/v1.16.1/kube-score_1.16.1_linux_amd64.tar.gz -o /tmp/kube-score_1.16.1_linux_amd64.tar.gz && \
tar -zxvf /tmp/kube-score_1.16.1_linux_amd64.tar.gz -C /tmp && \
mv /tmp/kube-score /usr/local/bin && \
rm -f /tmp/LICENSE && \
rm -f /tmp/kube-score_1.16.1_linux_amd64.tar.gz && \
chmod 755 /usr/local/bin/kube-score

USER jenkins
