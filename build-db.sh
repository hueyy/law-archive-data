#!/bin/bash

git-history file hearings.db hearings.json --id link --start-at 5ef32d3061fffe1f6a40703ba6cbdcee5166a89d
cat create-views.sql | sqlite3 hearings.db
