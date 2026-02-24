# Docker Deployment Guide

This guide explains how to build and deploy the DemoParty chat application using Docker.

## Prerequisites

- Docker installed on your system
- Docker Compose (optional, for easier deployment)

## Building the Docker Image

### Option 1: Using Docker directly

```bash
# Build the image
docker build -t demoparty:latest .

# Run the container
docker run -d \
  --name demoparty-chat \
  -p 8080:8080 \
  --restart unless-stopped \
  demoparty:latest
```

### Option 2: Using Docker Compose (Recommended)

```bash
# Build and start the container
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the container
docker-compose down
```

## Accessing the Application

Once the container is running, access the application at:
- **Local**: http://localhost:8080
- **Network**: http://YOUR_SERVER_IP:8080

## Configuration

### Environment Variables

You can customize the configuration using environment variables:

```bash
docker run -d \
  --name demoparty-chat \
  -p 8080:8080 \
  -e SERVER_PORT=8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  demoparty:latest
```

### Port Configuration

To change the port, modify the port mapping:
```bash
docker run -d -p 8080:8080 ...  # Change 8080:8080 to YOUR_PORT:8080
```

Or in `docker-compose.yml`:
```yaml
ports:
  - "YOUR_PORT:8080"
```

## Production Deployment

### On VDS Server

1. **Copy files to server:**
   ```bash
   scp -r . user@your-server:/opt/demoparty/
   ```

2. **SSH into server:**
   ```bash
   ssh user@your-server
   cd /opt/demoparty
   ```

3. **Build and run:**
   ```bash
   docker-compose up -d --build
   ```

4. **Check status:**
   ```bash
   docker-compose ps
   docker-compose logs -f
   ```

### Using Nginx as Reverse Proxy (Optional)

If you want to use a domain name and SSL:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## Troubleshooting

### Check container logs
```bash
docker logs demoparty-chat
# or
docker-compose logs -f
```

### Check container status
```bash
docker ps -a
docker-compose ps
```

### Restart container
```bash
docker restart demoparty-chat
# or
docker-compose restart
```

### Remove and rebuild
```bash
docker-compose down
docker-compose up -d --build
```

## Security Notes

- The container runs as a non-root user for security
- WebSocket connections are allowed from all origins (configure as needed for production)
- Consider using a reverse proxy (Nginx) with SSL/TLS for production deployments

## Firewall Configuration

Make sure port 8080 is open on your VDS server:

```bash
# Ubuntu/Debian
sudo ufw allow 8080/tcp

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```
