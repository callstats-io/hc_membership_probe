FROM hseeberger/scala-sbt
WORKDIR /code
COPY ./build.sbt ./
COPY ./project/*.sbt  ./project/

# install the dependencies
RUN sbt update

# copy the application code
COPY ./src ./src
RUN sbt compile

ENTRYPOINT ["sbt", "run"]

