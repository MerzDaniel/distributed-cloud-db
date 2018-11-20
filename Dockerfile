FROM openjdk:8-jdk

ADD resources/till-beck-140mb.tar.gz enrone
ADD resources/apache-ant-1.10.5-bin.tar.gz ant

COPY build.xml project/build.xml
COPY src project/src
COPY libs project/libs

ENV PATH="/ant/apache-ant-1.10.5/bin:${PATH}"

