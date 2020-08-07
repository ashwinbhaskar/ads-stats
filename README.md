# ads-delivery
An HTTP Server to record ad deliveries, clicks and installs, and query the statistics.

# Tech stack
`Scala` as language <br>
`Http4s` as server <br>
`Postgres` as database <br>
`doobie` as jdbc layer for scala <br>
`circe` for json encoding and decoding <br>
`flywaydb` for database migrations

# Overview
- Whenever an ad is loaded on a site, it is captured as a `delivery`.
- User clicking on the ad is captured as a `click`. Click contains the `delivery_id` that lead to the click.
- When the `click` leads to an install, it is captured as `install`. An `install` contains the `click_id` that lead to the install.

# Assumptions 
- This is a system designed to specifically record and query statistics on a large scala. It need not be 100% accurate. Hence, there is no foreign key relationships from `install` -> `click` -> `delivery`.
- This is a system that only records statistics rather than serve a business usecase. Lot of time cannot be spent per call on this. The API needs to be fast. One more reason for not adding foreign keys. Presence of foreign keys adds the extra time taken to validate the data. This extra time is very small but becomes significant when the scale is huge.

# Improvements
Given the above assumptions, a NO-SQL database would be a better approach. It would work seamlessly when we add or remove more fields while a SQL database like postgres, would require a schema change for every field added or removed.

# Setup & Run
The application along with postgres is dockerized. Execute the following commands to to start the server:
1. `make setup` - Builds a fat jar out of the application in the target directory. You will need `sbt` in your system for this.
2. `make run` - Will package postgres and the server into a docker container and expose `127.0.0.1:8080` of the outside world.
3. Run the `curl` commands in the curl folder of the repo:)