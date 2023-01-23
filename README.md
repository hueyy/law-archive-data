# sg-courts-hearings-list

This repository contains data obtained from the hearing list on the Singapore courts' website converted into a machine-readable format.

The code and configuration files in this repository are licensed under the EUPL-1.2.

The hearing list data obtained is and remains owned by or licensed to the Singapore Courts. This repository is not affiliated with the Singapore Courts and is provided for convenience only. It does not in itself provide you with any licence or permission to use the hearing list data.

## Viewing the data

The latest data is stored in the `hearings.json` file. You can view the current version of the file in a user-friendly way using the [GitHub Flat Viewer](https://flatgithub.com/hueyy/sg-courts-hearings-list?filename=hearings.json).

The historical data across the many commits can be analysed using the [`git-history`](https://github.com/simonw/git-history) tool. You can install it using [Poetry](https://python-poetry.org/) with `poetry install`, then open a shell with `poetry shell` and then:

```bash
git-history file hearings.db hearings.json --id link
```

## How the data is obtained

A GitHub workflow runs at 9.09AM and 9.09PM (GMT +8) which obtains the data and commits it to the `hearings.json` file in this repository.
