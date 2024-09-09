FROM gradle:8.9-jdk21

WORKDIR /app

COPY /app .

RUN ./gradlew installDist

CMD ./build/install/app/bin/app