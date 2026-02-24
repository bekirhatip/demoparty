#!/bin/bash

# DemoParty Docker Deployment Script
# Usage: ./deploy.sh [build|start|stop|restart|logs|status]

set -e

COMPOSE_FILE="docker-compose.yml"
SERVICE_NAME="demoparty"

case "${1:-start}" in
  build)
    echo "Building Docker image..."
    docker-compose build --no-cache
    echo "Build complete!"
    ;;
  start)
    echo "Starting DemoParty service..."
    docker-compose up -d
    echo "Service started!"
    echo "Access the application at http://localhost:8080"
    ;;
  stop)
    echo "Stopping DemoParty service..."
    docker-compose down
    echo "Service stopped!"
    ;;
  restart)
    echo "Restarting DemoParty service..."
    docker-compose restart
    echo "Service restarted!"
    ;;
  logs)
    echo "Showing logs (Ctrl+C to exit)..."
    docker-compose logs -f
    ;;
  status)
    echo "Service status:"
    docker-compose ps
    echo ""
    echo "Container health:"
    docker ps --filter "name=${SERVICE_NAME}" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    ;;
  update)
    echo "Updating and rebuilding..."
    docker-compose down
    docker-compose build --no-cache
    docker-compose up -d
    echo "Update complete!"
    ;;
  *)
    echo "Usage: $0 {build|start|stop|restart|logs|status|update}"
    echo ""
    echo "Commands:"
    echo "  build   - Build the Docker image"
    echo "  start   - Start the service"
    echo "  stop    - Stop the service"
    echo "  restart - Restart the service"
    echo "  logs    - Show logs"
    echo "  status  - Show service status"
    echo "  update  - Rebuild and restart"
    exit 1
    ;;
esac
