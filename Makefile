setup-server:
	sbt 'set test in assembly := {}' clean assembly
setup-perf-test:
	sbt perfTest/assembly
run-server:
	docker build . -t ads-stats-app
	docker-compose -f docker_compose/server/docker-compose.yml up
perf-test-time-travel:
	docker build . --build-arg no_op_tracer=true -t ads-stats-app
	docker build perf-test -t perf-test-app
	is_time_travel_mode=${is_time_travel_mode} deliveries=${deliveries} delivery_to_click_ratio=${delivery_to_click_ratio} click_to_install_ratio=${click_to_install_ratio} docker-compose -f docker_compose/perf_test_time_travel/docker-compose.yml up