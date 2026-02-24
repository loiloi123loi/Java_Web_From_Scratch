#!/bin/bash

# 1. Create Docker Web Proxy Network
docker network create web_proxy 2>/dev/null || echo "Network web_proxy already exists."

# 2. Create directory structure for Global Proxy
mkdir -p ~/global-proxy/conf.d
mkdir -p ~/global-proxy/certbot/conf
mkdir -p ~/global-proxy/certbot/www

# 3. Create docker-compose for Proxy
cat <<EOF > ~/global-proxy/docker-compose.yml
version: "3.8"
services:
  nginx:
    image: nginx:latest
    container_name: global_nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./conf.d:/etc/nginx/conf.d
      - ./certbot/conf:/etc/letsencrypt
      - ./certbot/www:/var/www/certbot
    networks:
      - web_proxy
    restart: always

networks:
  web_proxy:
    external: true
EOF

# 4. Create sample Nginx configuration (Modify your domain later)
cat <<EOF > ~/global-proxy/conf.d/app.conf
server {
    listen 80;
    server_name example.com; # Replace with your real domain

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        proxy_pass http://smart_class_pro:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

echo "-------------------------------------------------------"
echo "✅ Infrastructure setup complete!"
echo "1. Run: cd ~/global-proxy && docker-compose up -d"
echo "2. Then go to your project folder and run: docker-compose up -d"
echo "-------------------------------------------------------"
