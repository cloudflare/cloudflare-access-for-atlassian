#!/bin/bash

/docker-entrypoint.sh
/add_unproxied_connector.sh

exec "$@"
