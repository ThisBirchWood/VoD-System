FROM tiangolo/nginx-rtmp

RUN apt-get update && apt-get install -y gettext-base && rm -rf /var/lib/apt/lists/*
COPY default.conf.template /etc/nginx/templates/default.conf.template
COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]