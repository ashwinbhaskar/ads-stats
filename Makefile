setup-server:
	sbt 'set test in assembly := {}' clean assembly
setup-perf-test:
	sbt perfTest/assembly
run-server:
	docker build . -t ads-stats-app
	is_perf_test_mode="false" docker-compose up
perf-test-time-travel:
	docker build . --build-arg no_op_tracer=true -t ads-stats-app
	docker build perf-test -t perf-test-app
	is_perf_test_mode="true" is_time_travel_mode="true" deliveries=${deliveries} delivery_to_click_ratio=${delivery_to_click_ratio} click_to_install_ratio=${click_to_install_ratio} docker-compose up
perf-test:
	docker build . --build-arg no_op_tracer=false -t ads-stats-app
	docker build perf-test -t perf-test-app
	is_perf_test_mode="true" is_time_travel_mode="false" delivery_to_query_ratio=${delivery_to_query_ratio} delivery_to_click_ratio=${delivery_to_click_ratio} click_to_install_ratio=${click_to_install_ratio} running_time_in_seconds=${running_time_in_seconds} docker-compose up