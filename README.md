# law-archive-data

This repository contains Singapore legal data obtained various public sources and converted into a machine-readable format, including relating to the following:

- Court hearings: [`/data/hearings.json`](./data/hearings.json)
- Senior Counsels: [`/data/sc.json`](./data/sc.json)
- PDPC undertakings: `/data/pdpc-undertakings.json`(./data/pdpc-undertakings.json)
- PDPC decisions: `/data/pdpc-decisions.json`(./data/pdpc-decisions.json)

You can view and query the data using [this Datasette instance](https://law-archive-data.fly.dev/data).

The code and configuration files in this repository are licensed under the EUPL-1.2 as set out in the [LICENCE](./LICENCE) file.

The data remains owned by its respective owners. This repository is not affiliated with the Singapore Academy of Law, Singapore Courts, or any government agency, and is provided for convenience only.

## Development

In this project, everything is just a script (or a microservice™). Although most of the scripts are [Babashka](https://github.com/babashka/babashka) scripts written in [Clojure](https://clojure.org/), new scripts can be in any laguage. 

The data is obtained periodically via scheduled GitHub action workflows and committed to this repository. Each Github Action runs one of the input scripts in the [`/input` folder](./input/). Each input script stores the data obtained in a JSON file in the [`/data` folder](./data/). Each JSON file is just a snapshot in time, i.e. it contains only the data obtained in the last run of the respective script as opposed to all data ever obtained using that script. 

The [`/.github/workflows/deploy.yml`](./.github/workflows/deploy.yml) runs the [`/scripts/build_db.bb`](./scripts/build_db.bb) script which uses the [`git-history`](https://github.com/simonw/git-history) tool to create a SQLite database from the historical data across all the commits in this repository. The script then builds a [Datasette](https://datasette.io/) Docker image and deploys that via [Fly.io](https://fly.io/).

### Local development

After cloning this repository, you can generate the SQLite database on your machine by running the [`/scripts/build_db.bb` script](./scripts/build_db.bb). This may take some time (possibly >1h) as there have been many commits to this repository. The `build_db.bb` script also does some processing on the data, e.g. it creates and populates certain columns for ease of use based on the raw data (see e.g. [`/scripts/computed_columns.bb`](./scripts/computed_columns.bb)). Alternatively, you can download a copy of the database from [law-archive-data.fly.dev](https://law-archive-data.fly.dev).

Once you have the SQLite data, you can analyse it by running [Datasette](https://datasette.io/) locally. You can use the [`/scripts/dev_docker.bb` script](./scripts/dev_docker.bb). 

```bash
cd law-archive-data
bb 
```

