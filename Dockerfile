FROM java:7

MAINTAINER fitnesse.org

# Add working directory /fitnesse
ADD . /fitnesse
WORKDIR /fitnesse

CMD ["./gradlew", "run"]
# Expose default port
EXPOSE 8001
