FROM datasetteproject/datasette

RUN datasette install \
  datasette-vega \
  datasette-pretty-json \
  datasette-dashboards \
  datasette-sqlite-regex

COPY ./data/data.db /data/data.db
COPY ./data/metadata.yml /data/metadata.yml
COPY ./app/html-templates /app/html-templates
COPY ./app/dist /app/dist

WORKDIR /app

EXPOSE 8001
ENTRYPOINT [ \
  "datasette", "serve", \
  "-i", "/data/data.db", \
  "-p", "8001", \
  "-h", "0.0.0.0", \
  "--metadata", "/data/metadata.yml", \
  "--template-dir", "/app/html-templates", \
  "--static", "assets:/app/dist", \
  "--setting", "force_https_urls", "1"]
