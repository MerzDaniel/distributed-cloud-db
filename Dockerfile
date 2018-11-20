FROM openjdk:8-jre

ADD resources/till-beck-140mb.tar.gz enrone
ADD resources/apache-ant-1.10.5-bin.tar.gz ant

ENV PATH="/ant/apache-ant-1.10.5/bin:${PATH}"

