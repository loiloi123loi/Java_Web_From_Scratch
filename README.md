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

The fastest and most reliable way:

```bash
# 1. Initialize the environment
./setup.sh

# 2. Start the entire system
docker-compose up -d --build
```

- **Production Site**: [http://localhost:8080](http://localhost:8080)
- **Development Site**: [http://localhost:8081](http://localhost:8081)
- **Database External Port**: `3307` (Default)

### 3. Manual Setup (Non-Docker)

1. Set up a MySQL server and create a database named `smart_class_db`.
2. Update `src/main/resources/application.yml` (`local` profile) with your database credentials.
3. Run the following command:

```bash
mvn clean compile exec:java -Dexec.mainClass="com.polime.SocialApp" -Dapp.env=local
```

## ☁️ Cloud Deployment (CI/CD)

The project is fully automated via GitHub Actions (`pipeline.yml`).

### Automated Workflow:

1. **Push Code**: Automatically runs Checkstyle, PMD, and Unit Tests.
2. **Build Image**: If tests pass, packages the application into a Docker Image and pushes it to GitHub Container Registry (GHCR).
3. **Deploy**: Automatically SSHs into your Cloud server, pulls the latest image, and restarts the containers.

### Required GitHub Secrets:

To enable automated deployment, add the following secrets to your GitHub repository:

- `CLOUD_HOST`: Cloud server IP address.
- `CLOUD_USER`: SSH username (e.g., root, ubuntu).
- `CLOUD_SSH_KEY`: Content of your Private SSH Key.
- `CLOUD_PATH`: Absolute path to the project directory on the server (e.g., `/home/ubuntu/smart_class`).

## 🛡️ Cloud Deployment Setup (Step-by-Step)

Follow these steps for the first-time setup on your Cloud VPS:

### 1. Initial Server Setup

```bash
# 1. Clone your repository
git clone <your-repo-url>
cd smart_class

# 2. Run the setup script (Creates .env and nginx/conf.d)
chmod +x setup.sh
./setup.sh

# 3. Update secrets
nano .env
```

### 2. Manual Nginx Configuration

Since domain-specific configs are gitignored, create the file manually:

```bash
nano nginx/conf.d/app.conf
```

_Paste your Nginx configuration (refer to the template in step 4)._

### 3. Start the System

```bash
docker-compose up -d --build
```

### 4. Nginx Configuration Template (HTTPS)

```nginx
# PRO Site: example.com
server {
    listen 80;
    server_name example.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name example.com;

    ssl_certificate /etc/letsencrypt/live/example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/example.com/privkey.pem;

    location / {
        proxy_pass http://{{your-domain}}:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ {
        proxy_pass http://{{your-domain}}:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 5. SSL with Certbot (Optional)

If you don't have certificates yet, use the pre-configured `certbot` volume:

```bash
# PRO Site: example.com
docker run -it --rm --name certbot \
  -v "$(pwd)/certbot/conf:/etc/letsencrypt" \
  -v "$(pwd)/certbot/www:/var/www/certbot" \
  certbot/certbot certonly --webroot -w /var/www/certbot -d example.com
```

## 📜 Coding Standards

- **SQL**: Always uppercase keywords (`SELECT`, `INSERT`, `FROM`).
- **Lombok**: STRICTLY FORBIDDEN. Write Getters/Setters manually to maintain full control.
- **Exceptions**: Use Custom Exceptions and handle them centrally in `BaseHandler`.
- **Responses**: Every API must return data through `BaseResponseDto<T>`.
- **Linting**: Always run `mvn checkstyle:check pmd:check` before pushing code.

---

_Developed by polime-team_ 🚀
