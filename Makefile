.PHONY: help build clean test compile run docker-up docker-down migrate

# Variables
APP_NAME=cobre-notifier-api
DOCKER_COMPOSE=docker-compose.yml

# Colors for output
GREEN=\033[0;32m
YELLOW=\033[1;33m
NC=\033[0m # No Color

help: ## Show this help message
	@echo "$(GREEN)Available commands:$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, $$2}'

build: clean compile ## Build the application
	@echo "$(GREEN)✅ Build completed$(NC)"

clean: ## Clean Maven build artifacts
	@echo "$(YELLOW)Cleaning...$(NC)"
	@mvn clean

compile: ## Compile the project
	@echo "$(YELLOW)Compiling...$(NC)"
	@mvn clean compile

test: ## Run tests
	@echo "$(YELLOW)Running tests...$(NC)"
	@mvn test

test-coverage: ## Run tests with coverage report
	@echo "$(YELLOW)Running tests with coverage...$(NC)"
	@mvn clean test jacoco:report
	@echo "$(GREEN)Coverage report generated at: target/site/jacoco/index.html$(NC)"

run: ## Run the application
	@echo "$(GREEN)Starting application...$(NC)"
	@mvn spring-boot:run

docker-up: ## Start Docker containers (Postgres, Kafka, Prometheus, Grafana)
	@echo "$(GREEN)Starting Docker containers...$(NC)"
	@docker-compose -f $(DOCKER_COMPOSE) up -d
	@echo "$(GREEN)✅ Containers started$(NC)"
	@echo "$(YELLOW)Waiting for services to be ready...$(NC)"
	@sleep 10
	@docker-compose -f $(DOCKER_COMPOSE) ps

docker-down: ## Stop Docker containers
	@echo "$(YELLOW)Stopping Docker containers...$(NC)"
	@docker-compose -f $(DOCKER_COMPOSE) down
	@echo "$(GREEN)✅ Containers stopped$(NC)"

docker-logs: ## Show Docker containers logs
	@docker-compose -f $(DOCKER_COMPOSE) logs -f

migrate: ## Run Flyway migrations
	@echo "$(YELLOW)Running database migrations...$(NC)"
	@mvn flyway:migrate

migrate-info: ## Show Flyway migration info
	@mvn flyway:info

migrate-clean: ## Clean Flyway migrations (WARNING: drops all objects)
	@echo "$(YELLOW)⚠️  WARNING: This will drop all database objects!$(NC)"
	@read -p "Are you sure? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		mvn flyway:clean; \
	fi

install: ## Install dependencies
	@echo "$(YELLOW)Installing dependencies...$(NC)"
	@mvn clean install -DskipTests

package: ## Package the application
	@echo "$(YELLOW)Packaging application...$(NC)"
	@mvn clean package -DskipTests
	@echo "$(GREEN)✅ JAR created at: target/$(APP_NAME)-1.0.0-SNAPSHOT.jar$(NC)"

lint: ## Run code quality checks
	@echo "$(YELLOW)Running code quality checks...$(NC)"
	@mvn checkstyle:check || echo "$(YELLOW)Checkstyle plugin not configured$(NC)"

format: ## Format code (if formatter plugin is configured)
	@echo "$(YELLOW)Formatting code...$(NC)"
	@mvn formatter:format || echo "$(YELLOW)Formatter plugin not configured$(NC)"

dev: docker-up run ## Start Docker containers and run the application

stop: docker-down ## Stop all services

restart: stop dev ## Restart all services

# Database operations
db-connect: ## Connect to PostgreSQL database
	@echo "$(GREEN)Connecting to database...$(NC)"
	@PGPASSWORD=$${DB_PASSWORD:-cobre_password} psql -h $${DB_HOST:-localhost} -p $${DB_PORT:-5432} -U $${DB_USER:-cobre_user} -d $${DB_NAME:-cobre_notifier}

# Health checks
health: ## Check application health
	@echo "$(YELLOW)Checking application health...$(NC)"
	@curl -s http://localhost:$${SERVER_PORT:-8080}/actuator/health | jq . || echo "$(YELLOW)Application not running or jq not installed$(NC)"

metrics: ## Show Prometheus metrics
	@echo "$(YELLOW)Fetching metrics...$(NC)"
	@curl -s http://localhost:$${SERVER_PORT:-8080}/actuator/prometheus | head -20 || echo "$(YELLOW)Application not running$(NC)"

# Swagger/OpenAPI
swagger: ## Open Swagger UI in browser (macOS)
	@open http://localhost:$${SERVER_PORT:-8080}/swagger-ui.html || echo "$(YELLOW)Application not running$(NC)"

api-docs: ## Show API docs JSON
	@curl -s http://localhost:$${SERVER_PORT:-8080}/api-docs | jq . || echo "$(YELLOW)Application not running or jq not installed$(NC)"

# Cleanup
clean-all: clean ## Clean everything including Docker volumes
	@echo "$(YELLOW)Cleaning Docker volumes...$(NC)"
	@docker-compose -f $(DOCKER_COMPOSE) down -v 2>/dev/null || true
	@echo "$(GREEN)✅ All cleaned$(NC)"

# Quick start for development
quick-start: docker-up migrate run ## Quick start: Docker + Migrations + Run app

