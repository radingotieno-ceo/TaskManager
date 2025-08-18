#!/bin/bash

# Task Management System - Docker Management Scripts

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to build and start development environment
dev_start() {
    print_status "Starting development environment..."
    check_docker
    
    # Copy environment file if it doesn't exist
    if [ ! -f .env ]; then
        print_warning "No .env file found. Creating from template..."
        cp env.example .env
        print_success "Created .env file from template"
    fi
    
    # Build and start services
    docker-compose up -d --build
    
    print_success "Development environment started!"
    print_status "Services available at:"
    echo "  - Frontend: http://localhost:4200"
    echo "  - Backend API: http://localhost:8080"
    echo "  - Swagger UI: http://localhost:8080/swagger-ui/index.html"
    echo "  - Database: localhost:3306"
}

# Function to stop development environment
dev_stop() {
    print_status "Stopping development environment..."
    docker-compose down
    print_success "Development environment stopped"
}

# Function to restart development environment
dev_restart() {
    print_status "Restarting development environment..."
    dev_stop
    dev_start
}

# Function to view logs
dev_logs() {
    if [ -z "$1" ]; then
        print_status "Showing logs for all services..."
        docker-compose logs -f
    else
        print_status "Showing logs for service: $1"
        docker-compose logs -f "$1"
    fi
}

# Function to build and start production environment
prod_start() {
    print_status "Starting production environment..."
    check_docker
    
    # Check if .env file exists
    if [ ! -f .env ]; then
        print_error "No .env file found. Please create one from env.example"
        exit 1
    fi
    
    # Build and start production services
    docker-compose -f docker-compose.prod.yml up -d --build
    
    print_success "Production environment started!"
    print_status "Services available at:"
    echo "  - Application: http://localhost"
    echo "  - HTTPS: https://localhost (if SSL configured)"
}

# Function to stop production environment
prod_stop() {
    print_status "Stopping production environment..."
    docker-compose -f docker-compose.prod.yml down
    print_success "Production environment stopped"
}

# Function to clean up Docker resources
cleanup() {
    print_status "Cleaning up Docker resources..."
    
    # Stop all containers
    docker-compose down
    docker-compose -f docker-compose.prod.yml down
    
    # Remove unused containers, networks, and images
    docker system prune -f
    
    # Remove unused volumes (be careful with this in production)
    read -p "Do you want to remove unused volumes? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker volume prune -f
        print_success "Removed unused volumes"
    fi
    
    print_success "Cleanup completed"
}

# Function to show status of services
status() {
    print_status "Checking service status..."
    
    echo "Development Environment:"
    docker-compose ps
    
    echo -e "\nProduction Environment:"
    docker-compose -f docker-compose.prod.yml ps
    
    echo -e "\nDocker System Info:"
    docker system df
}

# Function to show help
show_help() {
    echo "Task Management System - Docker Management Script"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  dev-start     Start development environment"
    echo "  dev-stop      Stop development environment"
    echo "  dev-restart   Restart development environment"
    echo "  dev-logs      Show development logs (optional: service name)"
    echo "  prod-start    Start production environment"
    echo "  prod-stop     Stop production environment"
    echo "  cleanup       Clean up Docker resources"
    echo "  status        Show status of all services"
    echo "  help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 dev-start"
    echo "  $0 dev-logs backend"
    echo "  $0 cleanup"
}

# Main script logic
case "$1" in
    "dev-start")
        dev_start
        ;;
    "dev-stop")
        dev_stop
        ;;
    "dev-restart")
        dev_restart
        ;;
    "dev-logs")
        dev_logs "$2"
        ;;
    "prod-start")
        prod_start
        ;;
    "prod-stop")
        prod_stop
        ;;
    "cleanup")
        cleanup
        ;;
    "status")
        status
        ;;
    "help"|"--help"|"-h"|"")
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
