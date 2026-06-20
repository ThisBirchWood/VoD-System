#!/bin/sh

# must only run on certain env files, otherwise it replaces too much
envsubst '${WEB_SERVER_URL}' \
  < /etc/nginx/templates/default.conf.template \
  > /etc/nginx/nginx.conf

# daemon off required otherwise the container would exit immediately after nginx forks to background.
exec nginx -g "daemon off;"