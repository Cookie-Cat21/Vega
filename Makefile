.PHONY: up down logs monitoring monitoring-down status clean

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
