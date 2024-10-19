#!/bin/bash

set -e

if [ -z "$LOCAL_DOMAIN" ]
then
    export LOCAL_DOMAIN="localhost"
else
    export LOCAL_DOMAIN="localhost $LOCAL_DOMAIN.localhost $LOCAL_DOMAIN.local"
fi

if [ ! -z "$INTERNAL_IP" ]
then
    export LOCAL_DOMAIN="$LOCAL_DOMAIN $INTERNAL_IP $(hostname -i)"
fi

caddy run --config /etc/caddy/Caddyfile --adapter caddyfile