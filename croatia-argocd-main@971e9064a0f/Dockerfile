FROM quay.io/argoproj/argocd:v2.6.7

USER root

ADD croatia-ca.crt /usr/local/share/ca-certificates/croatia-ca.crt
RUN chmod 644 /usr/local/share/ca-certificates/croatia-ca.crt && update-ca-certificates

USER argocd
