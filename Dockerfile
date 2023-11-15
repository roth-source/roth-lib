#
# Build stage
#
FROM maven:3.8.7-amazoncorretto-11 AS build
ARG APP_PATH
RUN mkdir /home/app

COPY . /home/app
COPY .mvn /home/app/${APP_PATH}/.mvn
WORKDIR /home/app/${APP_PATH}

CMD ["/bin/sh", "-c", "bash"]