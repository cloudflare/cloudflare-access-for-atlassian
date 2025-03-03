#!/bin/bash
#
# Init script to be run on the first time the Postgresql container
# starts. It will setup the required databases in the environment variable
# granting access to the same user as the one defined in the environment variable
# for setting up the user.
#

set -e
set -u

function create_db() {
	local database=$1
	echo ">>>>> Creating database '$database'"
	psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
	    CREATE DATABASE $database;
	    GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
	echo ">>>>> Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
	for db in $(echo $POSTGRES_MULTIPLE_DATABASES | tr ',' ' '); do
		create_db $db
	done
	echo ">>>>> Multiple databases created"
fi