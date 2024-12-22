FROM jenkins/inbound-agent:latest-jdk11

# Downloading WGET and necessary Binaries for T24 to work: MAVEN and JAVA 8

USER root

RUN  apt-get update \
  && echo "Installing curl and wget" \
  && apt-get install -y curl \
  && apt-get install -y wget \
  && rm -rf /var/lib/apt/lists/*
  
ARG MAVEN_home_dir=/var/maven
ARG JAVA_home_dir=/var/java

RUN mkdir $MAVEN_home_dir $JAVA_home_dir \
	  && echo "Downloading MAVEN" \
      && wget --no-check-certificate https://nexus.projectcroatia.cloud/repository/t24lib/docker_binaries/apache-maven-3.9.2-bin.tar.gz -P $MAVEN_home_dir -nv \
	  \	  
	  && echo "Downloading JAVA" \
	  && wget --no-check-certificate https://nexus.projectcroatia.cloud/repository/t24lib/docker_binaries/jre-8u371-linux-x64.tar.gz -P $JAVA_home_dir -nv \
	  \
	  && echo "Unziping MAVEN" \
	  && tar -xzf $MAVEN_home_dir/apache-maven-3.9.2-bin.tar.gz -C $MAVEN_home_dir/ \
	  && echo "Unziping JAVA" \ 
	  && tar -xzf $JAVA_home_dir/jre-8u371-linux-x64.tar.gz -C $JAVA_home_dir \
	  \
	  && echo "Cleaning and setting links" \
	  && rm -f $MAVEN_home_dir/apache-maven-3.9.2-bin.tar.gz \
	  && rm -f $JAVA_home_dir/jre-8u371-linux-x64.tar.gz \
	  && ln -s $MAVEN_home_dir/apache-maven-3.9.2/bin/mvn \
	  && ln -s $JAVA_home_dir/jre1.8.0_371

##########################################################################

# Downloading T24 libs and bins for T24 package building

ARG TAFJ_home_dir=/var/TAFJ
ARG T24JARS_home_dir=/var/t24-jars
ARG T24Binaries_home_dir=/var/t24-binaries

RUN mkdir $TAFJ_home_dir \
    && wget https://nexus.projectcroatia.cloud/repository/t24lib/TAFJ/settings.xml -P $TAFJ_home_dir -nv \
    && wget https://nexus.projectcroatia.cloud/repository/t24lib/TAFJ/TAFJ.tgz -P $TAFJ_home_dir -nv \
    && wget --no-check-certificate https://nexus.projectcroatia.cloud/repository/t24lib/TAFJ/settings.xml -P $TAFJ_home_dir -nv \
    && wget --no-check-certificate https://nexus.projectcroatia.cloud/repository/t24lib/TAFJ/TAFJ.tgz -P $TAFJ_home_dir -nv \
	&& tar -zxvf $TAFJ_home_dir/TAFJ.tgz -C $TAFJ_home_dir \
	&& rm -f $TAFJ_home_dir/TAFJ.tgz \
	&& rm -f $TAFJ_home_dir/TAFJ.tgz 
	
RUN mkdir $T24JARS_home_dir \
	&& wget https://nexus.projectcroatia.cloud/repository/t24lib/t24-jars/t24-jars.tgz -P $T24JARS_home_dir -nv \
	&& tar -xzvf $T24JARS_home_dir/t24-jars.tgz -C $T24JARS_home_dir \
	&& rm -f $T24JARS_home_dir/t24-jars.tgz \
	&& wget --no-check-certificate https://nexus.projectcroatia.cloud/repository/t24lib/t24-jars/t24-jars.tgz -P $T24JARS_home_dir -nv \
	&& tar -xzvf $T24JARS_home_dir/t24-jars.tgz -C $T24JARS_home_dir \
	&& rm -f $T24JARS_home_dir/t24-jars.tgz

RUN mkdir $T24Binaries_home_dir \
	&& wget https://nexus.projectcroatia.cloud/repository/t24lib/t24-binaries-packed/t24-binaries.tgz -P $T24Binaries_home_dir -nv \
	&& tar -xzvf $T24Binaries_home_dir/t24-binaries.tgz  -C $T24Binaries_home_dir \
	&& rm -f $T24Binaries_home_dir/t24-binaries.tgz

##########################################################################

# Setting up ENV variables for T24 package building

ENV MAVEN_HOME $MAVEN_home_dir/apache-maven-3.9.2
ENV MAVEN_SETTINGS_XML $TAFJ_home_dir/settings.xml
ENV PATH="$MAVEN_HOME/bin:$PATH"

ENV TAFJ_HOME=$TAFJ_home_dir
ENV T24_LIB $T24JARS_home_dir
ENV T24_binaries $T24Binaries_home_dir

##########################################################################

# Granting ownership of the folders to Jenkins

RUN chown -R jenkins:jenkins $MAVEN_home_dir
RUN chown -R jenkins:jenkins $JAVA_home_dir
RUN chown -R jenkins:jenkins $TAFJ_home_dir
RUN chown -R jenkins:jenkins $T24JARS_home_dir
RUN chown -R jenkins:jenkins $T24Binaries_home_dir


USER jenkins