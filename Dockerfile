FROM gradle:jdk10 as builder

COPY --chown=gradle:gradle . /src
WORKDIR /src
RUN gradle build

FROM openjdk:10-jre-slim
EXPOSE 8080
COPY --from=builder /src/build/distributions/moneytx.tar /app/
WORKDIR /app
RUN tar -xvf moneytx.tar
WORKDIR /app/moneytx
CMD bin/moneytx