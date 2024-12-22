FROM artifactory.apps.prod-nonpci-shared.nrapp.cloud/docker/croatia-base-agent:latest

USER root

RUN npm i -g yarn

RUN apt update && \
apt install -y ruby=1:2.7+2 && \
apt install -y ruby-dev=1:2.7+2 && \
apt clean

RUN gem install bundler

ARG android_home_dir=/var/lib/android-sdk/
ARG commandlinetools_zip_file=commandlinetools-linux-9477386_latest.zip
RUN mkdir $android_home_dir && \
    wget https://dl.google.com/android/repository/$commandlinetools_zip_file -P $android_home_dir -nv && \
    unzip $android_home_dir$commandlinetools_zip_file -d $android_home_dir/cmdline-tools && \
    rm $android_home_dir$commandlinetools_zip_file && \
    chmod 777 -R $android_home_dir &&\
    mv $android_home_dir/cmdline-tools/cmdline-tools/ $android_home_dir/cmdline-tools/tools/

ENV ANDROID_SDK_ROOT=$android_home_dir
ENV ANDROID_HOME=$android_home_dir
ENV PATH="${PATH}:$android_home_dir/cmdline-tools/tools/bin:$android_home_dir/platform-tools"

RUN yes | sdkmanager --licenses && sdkmanager "platform-tools" \
                                              "ndk;23.1.7779620"  \
                                              "platforms;android-33" \
                                              "emulator" \
                                              "build-tools;33.0.0"

RUN chmod 777 -R "$android_home_dir/"

RUN chown -R jenkins:jenkins /usr/local/bin && \
chown -R jenkins:jenkins /var/lib/gems

RUN curl -L https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.8.0.2856-linux.zip -o /tmp/sonar-scanner-cli.zip && \
unzip /tmp/sonar-scanner-cli.zip -d /tmp/sonar-scanner-cli && \
mv /tmp/sonar-scanner-cli/sonar-scanner-4.8.0.2856-linux /sonar-scanner

ENV PATH="${PATH}:/sonar-scanner/bin"

USER jenkins