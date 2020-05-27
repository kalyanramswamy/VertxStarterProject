FROM openjdk:8-jdk-alpine

#RUN apt-get install software-properties-common
#RUN add-apt-repository ppa:webupd8team/java
#RUN apt-get -y update
#RUN apt-get install default-jdk

ENV VERTICLE_FILE vertxtest*-fat.jar

ENV CONFIG config


# Create the directory
RUN mkdir /usr/share/verticles

# Set the location of the verticles
ENV VERTICLE_HOME /usr/share/verticles

EXPOSE 8080

# Copy your fat jar to the container
COPY target/$VERTICLE_FILE $VERTICLE_HOME/
COPY $CONFIG $VERTICLE_HOME/$CONFIG

RUN mkdir -p /opt/mount/config/
COPY $CONFIG/config.json /opt/mount/config/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java $JVM -jar $VERTICLE_FILE -conf /opt/mount/config/config.json"]