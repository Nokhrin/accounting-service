# Makefile
VENV ?= tests/accounting-api-tests/.venv
RUFF := $(VENV)/bin/ruff
PYTEST := $(VENV)/bin/pytest
PIP_SYNC := $(VENV)/bin/pip-sync

.PHONY: env install fix check check-python check-java test

env:
	@printf 'PROD_POSTGRES_DB=accounting_prod_db\n' > .env
	@printf 'PROD_POSTGRES_USER=accounting\n' >> .env
	@printf 'PROD_POSTGRES_PASSWORD=%s\n' "$$(openssl rand -hex 32)" >> .env
	@printf 'DEV_POSTGRES_DB=accounting_dev_db\n' >> .env
	@printf 'DEV_POSTGRES_USER=dev\n' >> .env
	@printf 'DEV_POSTGRES_PASSWORD=%s\n' "$$(openssl rand -hex 32)" >> .env
	@printf 'LOCAL_POSTGRES_DB=accounting_local_db\n' >> .env
	@printf 'LOCAL_POSTGRES_USER=local\n' >> .env
	@printf 'LOCAL_POSTGRES_PASSWORD=%s\n' "$$(openssl rand -hex 32)" >> .env

install:
	$(VENV)/bin/python -m pip install --upgrade pip pip-tools
	$(PIP_SYNC) requirements.txt

fix:
	$(RUFF) check --fix test
	$(RUFF) format test

test:
	$(PYTEST) -m smoke -v

check-python:
	$(RUFF) check test
	$(RUFF) format --check test
	$(VENV)/bin/mypy test

check-java:
	mvn spotless:check checkstyle:check compile

check:
	$(MAKE) check-python
	$(MAKE) check-java