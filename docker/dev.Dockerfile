FROM datasetteproject/datasette

RUN datasette install \
  datasette-vega \
  datasette-pretty-json \
  datasette-dashboards \
  datasette-sqlite-regex \
  datasette-atom

WORKDIR /app

EXPOSE 8001
ENTRYPOINT [ \
  "datasette", \
  "-i", "/data/data.db", \
  "-p", "8001", \
  "-h", "0.0.0.0", \
  "--metadata", "/data/metadata.yml", \
  "--template-dir", "/app/html-templates", \
  "--static", "assets:/app/dist", \
  "--setting", "sql_time_limit_ms", "5000", \
  "--reload"]
