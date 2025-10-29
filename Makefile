CLJ       ?= clojure
REPRO     ?= -Srepro
TESTS_ALS ?= -M:tests-main    
REPL_ALS  ?=

.PHONY: help test test-fast ci repl clean

help:
	@echo "Targets:"
	@echo "  make test        
	@echo "  make clean       

test:
	$(CLJ) $(REPRO) $(TESTS_ALS)

test-fast:
	$(CLJ) $(TESTS_ALS)

ci: test

repl:
	$(CLJ) -M $(REPL_ALS)

clean:
	rm -rf .cpcache
