FROM datasetteproject/datasette

RUN datasette install datasette-vega datasette-pretty-json 

WORKDIR /app
COPY ./hearings.db /app
COPY ./metadata.json /app

EXPOSE 8001
ENTRYPOINT ["datasette", "-p", "8001", "-h", "0.0.0.0", "--metadata", "metadata.json", "hearings.db"]

