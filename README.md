# Smart Class API (Pure Java Scratch)

A Java Web backend project built from the ground up (**Pure Scratch**), adhering to the principle of not using frameworks (No Spring, No Hibernate) to optimize performance and deeply understand system internals.

## 🚀 Project Philosophy

- **Zero Frameworks**: No Spring Boot, no Hibernate, no JPA.
- **Pure Java 17**: Utilizing modern features like Records, Text Blocks, and Switch expressions.
- **Clear Layered Architecture**: Strictly follows Separation of Concerns (Controller -> Service -> Repository).
- **Automation First**: Built-in Docker support and automated CI/CD to the cloud.

## 🛠 Tech Stack

- **Core**: `com.sun.net.httpserver.HttpServer`
- **Database**: Pure JDBC with `mysql-connector-j` and `PreparedStatement`
- **JSON**: Google `Gson`
- **Security**: `jjwt` (JWT) & `jbcrypt` (Password hashing)
- **Build & Quality**: Maven, Checkstyle, PMD (Lint)
- **Infrastructure**: Docker, Docker Compose, GitHub Actions

## 🏗 Project Structure

The project is organized using the standard Java Web pattern:

- `com.polime.core`: Core infrastructure (WebServer, Config, DatabaseManager).
- `com.polime.controller`: HTTP Request/Response handling (BaseHandler).
- `com.polime.service`: Business logic processing.
- `com.polime.repository`: Data access with pure SQL and manual ResultSet mapping.
- `com.polime.model`: Plain Old Java Objects (Entities).
- `com.polime.dto`: Data Transfer Objects for API requests and responses.

## 💻 Setup Instructions

### 1. Requirements

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose (Recommended)
- MySQL 9.1 (If running locally without Docker)

### 2. Local Setup (Docker)

The fastest way to run the project. Note: By default, ports are not mapped to avoid conflicts with the Global Proxy. For local testing, add `ports` to `docker-compose.yml`.

```bash
# 1. Initialize environment
./setup.sh

# 2. Start services (For local access, map ports 8080/8081 in docker-compose)
docker-compose up -d --build
```

- **Production API**: Accessed via Proxy or manual port mapping (default: `site-pro:8080`)
- **Development API**: Accessed via Proxy or manual port mapping (default: `site-dev:8080`)
- **Database Port**: `3307` (Default external port)

### 3. Manual Setup (Non-Docker)

1. Set up a MySQL server and create a database named `smart_class_pro` and `smart_class_dev`.
2. Update `src/main/resources/application.yml` with your credentials.
3. Run using Maven:

```bash
mvn clean compile exec:java -Dexec.mainClass="com.polime.SocialApp" -Dapp.env=local
```

## ☁️ Cloud Deployment (CI/CD)

The project is fully automated via GitHub Actions (`pipeline.yml`).

### Automated Workflow:

1. **Push Code**: Automatically runs Checkstyle, PMD, and Unit Tests.
2. **Build Image**: If tests pass, packages the application into a Docker Image and pushes it to **Docker Hub**.
3. **Deploy**: Automatically SSHs into your Cloud server, performs `docker login`, pulls the latest image (`docker-compose pull`), and restarts the containers.

### Required GitHub Secrets:

To enable automated deployment, add the following secrets to your GitHub repository:

- `CLOUD_HOST`: Cloud server IP address.
- `CLOUD_USER`: SSH username (e.g., root, ubuntu).
- `CLOUD_SSH_KEY`: Content of your Private SSH Key.
- `CLOUD_PATH`: Absolute path to the project directory on the server (e.g., `/home/ubuntu/smart_class`).
- `DOCKERHUB_USERNAME`: Your Docker Hub username.
- `DOCKERHUB_TOKEN`: Your Docker Hub Personal Access Token (PAT).

> **Note on Security**: To allow GitHub Actions to SSH into your server, ensure you have added the corresponding Public Key to `~/.ssh/authorized_keys` on your cloud server. If your cloud provider has a firewall (Security Group), you must allow inbound traffic on port 22.

## ☁️ Cloud Quick Start

To deploy on a fresh server, follow these simple steps:

### Step 1: Initialize Infrastructure (One-time)

Copy the content of `infra-setup.sh` to your server and run:

```bash
chmod +x infra-setup.sh && ./infra-setup.sh
cd ~/global-proxy && docker-compose up -d
```

_This script automatically creates the `web_proxy` network, proxy directories, and a sample config._

### Step 2: Setup Smart Class Project

```bash
cd ~/your-path/smart_class
chmod +x setup.sh && ./setup.sh
nano .env # Update DB_ROOT_PASSWORD and JWT_SECRET
```

### Step 3: Launch

```bash
docker-compose up -d --build
```

---

## 🛡️ Proxy & SSL Management (The Secure Way)

Setting up SSL can be tricky because Nginx won't start if certificate files don't exist yet. Follow this **2-Phase** process:

### Phase 1: HTTP Verification (Port 80)

1. Modify `~/global-proxy/conf.d/app.conf` to use **HTTP only** first (the default in `infra-setup.sh`):

```nginx
server {
    listen 80;
    server_name yourdomain.com;
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }
    location / {
        proxy_pass http://smart_class_pro:8080;
    }
}
```

2. Reload Nginx: `docker exec global_nginx nginx -s reload`

### Phase 2: Obtain SSL & Enable HTTPS

1. Obtain certificates using Certbot for both domains:

```bash
# Obtain certs for both Main and Dev domains
docker run -it --rm --name certbot \
  -v "$(pwd)/certbot/conf:/etc/letsencrypt" \
  -v "$(pwd)/certbot/www:/var/www/certbot" \
  certbot/certbot certonly --webroot -w /var/www/certbot -d yourdomain.com -d dev.yourdomain.com
```

2. Once successful, update `~/global-proxy/conf.d/app.conf` to use **HTTPS** for both sites:

```nginx
# HTTP Redirect for all domains
server {
    listen 80;
    server_name yourdomain.com dev.yourdomain.com;
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }
    location / {
        return 301 https://$host$request_uri;
    }
}

# PRO Site (Main Domain)
server {
    listen 443 ssl;
    server_name yourdomain.com;
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    location /api/ {
        proxy_pass http://smart_class_pro:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    location / {
        proxy_pass http://react_pro_container:3000; # Production FE
    }
}

# DEV Site (Development Domain)
server {
    listen 443 ssl;
    server_name dev.yourdomain.com;
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    location /api/ {
        proxy_pass http://smart_class_dev:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    location / {
        proxy_pass http://react_dev_container:3000; # Development FE
    }
}
```

3. Reload Nginx: `docker exec global_nginx nginx -s reload`

## 📜 Coding Standards

- **SQL**: Always uppercase keywords (`SELECT`, `INSERT`, `FROM`).
- **Lombok**: STRICTLY FORBIDDEN. Write Getters/Setters manually to maintain full control.
- **Exceptions**: Use Custom Exceptions and handle them centrally in `BaseHandler`.
- **Responses**: Every API must return data through `BaseResponseDto<T>`.
- **Linting**: Always run `mvn checkstyle:check pmd:check` before pushing code.

---

_Developed by polime-team_ 🚀
