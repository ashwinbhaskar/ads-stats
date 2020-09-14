#!/bin/sh
# wait-for-server.sh
  
host="$1"
port="$2"
shift 2
other_cmd="$@"

cmd="curl http://$host:$port/ping"  
until [ "$($cmd)" = "pong" ]; do
  >&2 echo "Server is unavailable - sleeping"
  sleep 1
done
  
>&2 echo "Server is up - executing command"
>&2 echo "other command = $other_cmd"
exec $other_cmd
