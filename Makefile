# Makefile
PROJECT_ROOT := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
ENV_FILE := $(PROJECT_ROOT).env

MODULE_DIR := tests/accounting-api-tests
VENV := $(MODULE_DIR)/.venv
PYTHON := $(PROJECT_ROOT)$(VENV)/bin/python
PIP_SYNC := $(PROJECT_ROOT)$(VENV)/bin/pip-sync
RUFF := $(PROJECT_ROOT)$(VENV)/bin/ruff
MYPY := $(PROJECT_ROOT)$(VENV)/bin/mypy

COMPOSE_DEV ?= docker-compose.yml

ifneq ($(wildcard $(ENV_FILE)),)
include $(ENV_FILE)
export
endif

.PHONY: env-guard install test-java test-python test check-python up down

env-guard:
	@test -f $(ENV_FILE) || { echo "ERROR: $(ENV_FILE) not found. Run: cp .env.example .env"; exit 1; }

install:
	@test -d $(VENV) || python3.14 -m venv $(VENV)
	$(PYTHON) -m pip install --upgrade pip pip-tools
	cd $(MODULE_DIR) && $(PIP_SYNC) requirements.txt

test-java: env-guard
	mvn -B -ntp verify

test-python: env-guard install
	cd $(MODULE_DIR) && $(PYTHON) -m pytest -m "smoke or integration" -q

check-python:
	cd $(MODULE_DIR) && $(RUFF) check && $(RUFF) format --check && $(MYPY)

test: test-java test-python

up: env-guard
	docker compose -f $(COMPOSE_DEV) --env-file $(ENV_FILE) up -d --build accounting-service

down:
	docker compose -f $(COMPOSE_DEV) --env-file $(ENV_FILE) down