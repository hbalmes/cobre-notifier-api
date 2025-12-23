.PHONY: help build clean test compile run docker-up docker-down rebuild migrate docs docs-stop

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

rebuild: ## Rebuild and restart the application container
	@echo "$(YELLOW)Rebuilding application image...$(NC)"
	@docker-compose -f $(DOCKER_COMPOSE) build app
	@echo "$(GREEN)Restarting application container...$(NC)"
	@docker-compose -f $(DOCKER_COMPOSE) up -d app
	@echo "$(GREEN)✅ Application rebuilt and restarted$(NC)"
	@echo "$(YELLOW)Waiting for application to be ready...$(NC)"
	@sleep 5
	@docker-compose -f $(DOCKER_COMPOSE) logs --tail 20 app

docker-logs: ## Show Docker containers logs
	@docker-compose -f $(DOCKER_COMPOSE) logs -f

migrate: ## Run Flyway migrations
	@echo "$(YELLOW)Running database migrations...$(NC)"
	@mvn flyway:migrate

install: ## Install dependencies
	@echo "$(YELLOW)Installing dependencies...$(NC)"
	@mvn clean install -DskipTests

package: ## Package the application
	@echo "$(YELLOW)Packaging application...$(NC)"
	@mvn clean package -DskipTests
	@echo "$(GREEN)✅ JAR created at: target/$(APP_NAME)-1.0.0-SNAPSHOT.jar$(NC)"

stop: docker-down ## Stop all services

docs: ## Start documentation server (Docsify)
	@echo "$(GREEN)Starting documentation server...$(NC)"
	@docker-compose up -d docs
	@echo "$(GREEN)Documentation available at: http://localhost:$${DOCS_PORT:-3001}$(NC)"

docs-stop: ## Stop documentation server
	@docker-compose stop docs

