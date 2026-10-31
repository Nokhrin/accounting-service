# Makefile
PROJECT_ROOT := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
ENV_FILE := $(PROJECT_ROOT).env

MODULE_DIR := $(PROJECT_ROOT)tests/accounting-api-tests
VENV := $(MODULE_DIR)/.venv

PYTHON_VERSION := 3.14
SYSTEM_PYTHON := python$(PYTHON_VERSION)

PYTHON := $(VENV)/bin/python
PIP_SYNC := $(VENV)/bin/pip-sync
RUFF := $(VENV)/bin/ruff
MYPY := $(VENV)/bin/mypy
DATAMODEL_CODEGEN := $(VENV)/bin/datamodel-codegen

OPENAPI_SPEC := $(PROJECT_ROOT)src/main/resources/openapi.yml
MODELS_DIR := $(MODULE_DIR)/src/accounting_api_tests/models/generated

COMPOSE_DEV ?= docker-compose.yml

ifneq ($(wildcard $(ENV_FILE)),)
include $(ENV_FILE)
export
endif

.PHONY: env-guard venv install generate-models fix-python check-python build up test-python test-java test down

env-guard:
	@test -f $(ENV_FILE) || { echo "ERROR: $(ENV_FILE) not found. Run: cp .env.example .env"; exit 1; }

venv:
	@test -d $(VENV) || $(SYSTEM_PYTHON) -m venv $(VENV)

install: venv
	$(PYTHON) -m pip install --upgrade pip pip-tools
	$(PIP_SYNC) $(MODULE_DIR)/requirements.txt

generate-models: install
	@mkdir -p $(MODELS_DIR)
	$(DATAMODEL_CODEGEN) \
		--input $(OPENAPI_SPEC) \
		--output $(MODELS_DIR)/models.py \
		--output-model-type pydantic_v2.BaseModel \
		--input-file-type openapi \
		--snake-case-field \
		--capitalise-enum-members \
		--use-standard-collections \
		--use-union-operator \
		--field-constraints \
		--collapse-root-models \
		--target-python-version $(PYTHON_VERSION) \
		--formatters ruff-check ruff-format

check-python: generate-models
	cd $(MODULE_DIR) && $(RUFF) check .
	cd $(MODULE_DIR) && $(RUFF) format --check .
	cd $(MODULE_DIR) && $(MYPY) .

fix-python: generate-models
	cd $(MODULE_DIR) && $(RUFF) check --fix .
	cd $(MODULE_DIR) && $(RUFF) format .

test-java:
	mvn -B -ntp verify

test-python: check-python up
	cd $(MODULE_DIR) && $(PYTHON) -m pytest -m "smoke or integration" -q

test: test-java test-python

up: env-guard
	docker compose -f $(COMPOSE_DEV) --env-file $(ENV_FILE) up -d --build accounting-service
	@echo "Waiting for application readiness..."
	@for i in $$(seq 1 30); do \
		if curl -fs http://127.0.0.1:8080/actuator/health > /dev/null 2>&1; then \
			echo "Application is ready."; \
			exit 0; \
		fi; \
		sleep 2; \
	done; \
	echo "ERROR: Application not ready within 60s"; exit 1

down:
	docker compose -f $(COMPOSE_DEV) --env-file $(ENV_FILE) down