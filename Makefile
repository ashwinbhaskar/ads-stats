setup:
	sbt 'set test in assembly := {}' clean assembly
run: setup
	docker build . -t ads-stats-app
	docker-compose up
run-with-no-op-tracer: setup
	docker build . --build-arg no_op_tracer=true -t ads-stats-app
	docker-compose up