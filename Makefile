# Makefile
MODULE_DIR := tests/accounting-api-tests
VENV := $(MODULE_DIR)/.venv

PYTHON := $(CURDIR)/$(VENV)/bin/python
RUFF := $(CURDIR)/$(VENV)/bin/ruff
MYPY := $(CURDIR)/$(VENV)/bin/mypy
PIP_SYNC := $(CURDIR)/$(VENV)/bin/pip-sync

COMPOSE_DEV ?= docker-compose.yml

.PHONY: venv install fix check check-python check-java test-rest up down

venv:
	@test -d $(VENV) || python3.14 -m venv $(VENV)

install: venv
	$(PYTHON) -m pip install --upgrade pip pip-tools
	cd $(MODULE_DIR) && $(PIP_SYNC) requirements.txt

fix:
	cd $(MODULE_DIR) && $(RUFF) check --fix
	cd $(MODULE_DIR) && $(RUFF) format

check-python:
	cd $(MODULE_DIR) && $(RUFF) check
	cd $(MODULE_DIR) && $(RUFF) format --check
	cd $(MODULE_DIR) && $(MYPY)

check-java:
	mvn -B -ntp verify

check-all: check-python check-java

test-rest:
	@test -f .env || { echo "Run: make env"; exit 1; }
	@set -a; . ./.env; set +a; \
	cd $(MODULE_DIR) && $(PYTHON) -m pytest -m "api or db" -v

up:
	@test -f .env || { echo "Run: make env"; exit 1; }
	docker compose -f $(COMPOSE_DEV) --env-file .env up -d --build accounting-service

down:
	docker compose -f $(COMPOSE_DEV) --env-file .env down
