# ads-stats
An HTTP Server to record ad deliveries, clicks and installs, and query the statistics.

# Tech stack
`Scala` as language <br>
`Http4s` as server <br>
`Postgres` as database <br>
`doobie` as jdbc layer for scala <br>
`circe` for json encoding and decoding <br>
`flywaydb` for database migrations <br>
`scala open tracing` for tracing context <br>
`jaegar` as the tracer

# Overview
- Whenever an ad is loaded on a site, it is captured as a `delivery`.
- User clicking on the ad is captured as a `click`. Click contains the `delivery_id` that lead to the click.
- When the `click` leads to an install, it is captured as `install`. An `install` contains the `click_id` that lead to the install.

# Setup & Run
The application along with postgres is dockerized. Execute the following commands to to start the server:
1. `make setup` - Builds a fat jar out of the application in the target directory. You will need `sbt` in your system for this.
2. `make run` - Will package postgres and the server into a docker container and expose `127.0.0.1:8080` of the outside world.
3. Run the `curl` commands in the curl folder of the repo:)