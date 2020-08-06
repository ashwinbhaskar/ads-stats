setup:
	sbt assembly
run:
	docker build . -t ads-stats-app
	docker-compose up