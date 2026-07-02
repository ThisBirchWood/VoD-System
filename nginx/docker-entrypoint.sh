#!/bin/sh

# must only run on certain env files, otherwise it replaces too much
envsubst '${WEB_SERVER_URL} ${STREAM_RING_LENGTH_SECOND}' \
  < /etc/nginx/templates/default.conf.template \
  > /etc/nginx/nginx.conf

# daemon off required otherwise the container would exit immediately after nginx forks to background.
exec nginx -g "daemon off;"