.PHONY: up down logs monitoring monitoring-down status clean build test validate bootstrap teardown

build:
	cd connectors/wikimedia && mvn package -DskipTests -q
	cd connectors/eonet && mvn package -DskipTests -q
	cd connectors/slnews && mvn package -DskipTests -q
	cd flink-jobs && mvn package -DskipTests -q

test:
	cd connectors/wikimedia && mvn test -q
	cd connectors/eonet && mvn test -q
	cd connectors/slnews && mvn test -q
	cd flink-jobs && mvn test -q

up:
	docker compose up -d
	@echo ""
	@echo "=== Vega Local Stack ==="
	@echo "Kafka UI:          http://localhost:8080"
	@echo "Flink UI:          http://localhost:8081"
	@echo "Schema Registry:   http://localhost:8082"
	@echo "Kafka Connect:     http://localhost:8083"
	@echo "Kafka Bootstrap:   localhost:9092"

down:
	docker compose down

logs:
	docker compose logs -f

monitoring:
	docker compose -f docker-compose.monitoring.yml up -d
	@echo ""
	@echo "=== Vega Monitoring ==="
	@echo "Prometheus:        http://localhost:9090"
	@echo "Grafana:           http://localhost:3000  (admin / vega)"

monitoring-down:
	docker compose -f docker-compose.monitoring.yml down

status:
	@docker compose ps
	@docker compose -f docker-compose.monitoring.yml ps 2>/dev/null || true

clean:
	docker compose down -v
	docker compose -f docker-compose.monitoring.yml down -v 2>/dev/null || true

validate:
	docker compose config
	docker compose -f docker-compose.monitoring.yml config
	cd terraform && terraform init -backend=false && terraform validate

bootstrap:
	./scripts/bootstrap-local.sh

teardown:
	./scripts/teardown-local.sh
