setup:
	sbt 'set test in assembly := {}' clean assembly
run:
	docker build . -t ads-stats-app
	docker-compose up