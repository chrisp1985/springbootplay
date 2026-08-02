# springbootplay

This is a sandbox project for me to try out Spring Boot (4.x) features. It's not a real
application, just a small "football players" API I use as an excuse to wire up different
parts of the framework in one place, with a minimal working example of each.

## Spring Boot features demonstrated

| Feature | Where |
| --- | --- |
| REST controllers (`@RestController`, `@RequestMapping`) | [`PlayerController`](src/main/java/com/chrisp1985/springbootplay/controller/PlayerController.java), [`ThreadTestController`](src/main/java/com/chrisp1985/springbootplay/controller/ThreadTestController.java) |
| Request validation (`spring-boot-starter-validation`, `@Valid`) | [`PlayerController`](src/main/java/com/chrisp1985/springbootplay/controller/PlayerController.java), [`PlayerRequest`](src/main/java/com/chrisp1985/springbootplay/model/PlayerRequest.java), [`PlayerDetailsRequest`](src/main/java/com/chrisp1985/springbootplay/model/PlayerDetailsRequest.java) |
| Global exception handling (`@RestControllerAdvice`, `@ExceptionHandler`) | [`PlayerExceptions`](src/main/java/com/chrisp1985/springbootplay/exception/PlayerExceptions.java) |
| Spring Data JPA repositories | [`PlayerRespository`](src/main/java/com/chrisp1985/springbootplay/repository/PlayerRespository.java), [`AuditLogRespository`](src/main/java/com/chrisp1985/springbootplay/repository/AuditLogRespository.java) |
| Transactions (`@Transactional`) | [`PlayerService.addPlayerToDatabase`](src/main/java/com/chrisp1985/springbootplay/service/PlayerService.java) |
| Database migrations (Flyway) | [`db/migration`](src/main/resources/db/migration). Schema, seed data and an audit log table |
| Async execution (`@EnableAsync`, `@Async`) | [`WorkersService.runExternalWorkers`](src/main/java/com/chrisp1985/springbootplay/service/WorkersService.java), triggered from [`ThreadTestService.checkWhenAsyncRuns`](src/main/java/com/chrisp1985/springbootplay/service/ThreadTestService.java) |
| Scheduled tasks (`@EnableScheduling`, `@Scheduled`) | [`ThreadTestService.scheduledCheck`](src/main/java/com/chrisp1985/springbootplay/service/ThreadTestService.java) |
| DTO to entity mapping (MapStruct) | [`PlayerMapper`](src/main/java/com/chrisp1985/springbootplay/model/mapper/PlayerMapper.java), [`AuditMapper`](src/main/java/com/chrisp1985/springbootplay/model/mapper/AuditMapper.java) |
| Spring AI (OpenAI chat client) | [`PlayerService.getPlayerAiInfo`](src/main/java/com/chrisp1985/springbootplay/service/PlayerService.java). Asks an LLM for a player's season stats |
| Actuator (health/observability endpoints) | enabled in [`application.yml`](src/main/resources/application.yml) / [`application-dev.yml`](src/main/resources/application-dev.yml) (`health`, `flyway`) |
| Custom Actuator health indicator | [`PlayerDataHealthIndicator`](src/main/java/com/chrisp1985/springbootplay/health/PlayerDataHealthIndicator.java). Reports `DOWN` if the player table can't be reached or is empty, shown under `/actuator/health` |
| Custom metric (Micrometer) | [`PlayerService.timeDbCall`](src/main/java/com/chrisp1985/springbootplay/service/PlayerService.java). Times each repository call with a `db.latency` `Timer`, tagged by operation (`save`, `findByName`, `auditSave`), visible under `/actuator/metrics/db.latency` |
| Profile-specific configuration | [`application.yml`](src/main/resources/application.yml) vs [`application-dev.yml`](src/main/resources/application-dev.yml) |
| Logging with SLF4J | throughout the controller/service layer |
| OCI image build with Jib (no Dockerfile needed) | `jib` block in [`build.gradle`](build.gradle) |

## Other things demonstrated (not specific to Spring Boot)

Bits of the project that aren't really about the framework, but useful to have examples of.

| Feature | Where |
| --- | --- |
| Long-running/blocking work on a thread (plain Java, `Thread.sleep`) | [`WorkersService.longRunningWorker` / `shortRunningWorker`](src/main/java/com/chrisp1985/springbootplay/service/WorkersService.java). Stands in for a slow external call, so you can see how wrapping it in `@Async` changes when the caller gets control back |
| Java records as immutable request DTOs | [`PlayerRequest`](src/main/java/com/chrisp1985/springbootplay/model/PlayerRequest.java), [`PlayerDetailsRequest`](src/main/java/com/chrisp1985/springbootplay/model/PlayerDetailsRequest.java) |
| MapStruct as a compile-time mapping generator (the annotation processor itself, not the Spring wiring) | [`PlayerMapper`](src/main/java/com/chrisp1985/springbootplay/model/mapper/PlayerMapper.java), [`AuditMapper`](src/main/java/com/chrisp1985/springbootplay/model/mapper/AuditMapper.java), generated code lands in `build/generated/sources/annotationProcessor` |
| Flyway as a standalone schema versioning tool | [`db/migration`](src/main/resources/db/migration) |
| Gradle build setup: Java toolchain, a BOM import for Spring AI's version alignment, multiple plugins (Flyway, Jib) in one build | [`build.gradle`](build.gradle) |
| Docker Compose for a local dependency (MySQL), independent of how the app itself gets built or run | [`docker/docker-compose.yml`](docker/docker-compose.yml) |

## Testing

Different layers get tested with different slices, rather than reaching for a full
`@SpringBootTest` everywhere, so each test only pays for the Spring context it actually needs
and only breaks when the thing it's meant to check breaks.

* [`PlayerServiceTest`](src/test/java/com/chrisp1985/springbootplay/service/PlayerServiceTest.java)
  and [`PlayerDataHealthIndicatorTest`](src/test/java/com/chrisp1985/springbootplay/health/PlayerDataHealthIndicatorTest.java)
  are plain Mockito unit tests, no Spring context at all. Fastest to run, and enough to check
  the logic in each class in isolation.
* [`PlayerControllerTest`](src/test/java/com/chrisp1985/springbootplay/controller/PlayerControllerTest.java)
  uses `@WebMvcTest`. It only starts the web layer (controller, validation, exception
  handling, JSON serialisation) and mocks `PlayerService` out, so it can check things like
  "does a bad request come back as a 400" without needing a database or the AI client on the
  classpath at all.
* [`PlayerRespositoryTest`](src/test/java/com/chrisp1985/springbootplay/repository/PlayerRespositoryTest.java)
  uses `@DataJpaTest`. It only starts the JPA layer against an in-memory H2 database, so it
  can check query methods like `findByName` actually work against a real (if temporary)
  database, without the cost of starting the whole application.

Neither of those two slices proves the app works end to end against a real database though,
since H2 isn't MySQL and the web slice doesn't touch a database at all. For that,
[`PlayerControllerIntegrationTest`](src/test/java/com/chrisp1985/springbootplay/controller/PlayerControllerIntegrationTest.java)
uses `@SpringBootTest` with [Testcontainers](https://testcontainers.com/), starting a real
MySQL container, running the actual Flyway migrations against it, and hitting the controller
through MockMvc. It's slower (needs Docker) but it's the one test that would actually catch a
migration or MySQL-specific SQL problem the other two can't see.

## Running locally

The app needs a MySQL database, which comes from Docker Compose:

```bash
docker compose -f docker/docker-compose.yml up -d
```

Then run the app with Gradle:

```bash
./gradlew bootRun
```

The AI endpoint (`POST /api/v1/players/aidetails`) needs an `OPENAI_API_KEY` environment
variable set, since it calls out to OpenAI via Spring AI.

## Building

Standard Gradle build:

```bash
./gradlew build
```

## Dockerising

The project uses
[Jib Gradle plugin](https://github.com/GoogleContainerTools/jib) to build a container
image straight from the compiled classes, without needing a Dockerfile. The image config
(base image, exposed port, container JVM flags) lives in the `jib` block of
[`build.gradle`](build.gradle).

Build the image and load it into your local Docker daemon:

```bash
./gradlew jibDockerBuild
```

Spring Boot's own OCI image support also works as an alternative to Jib:

```bash
./gradlew bootBuildImage
```

`jibDockerBuild` builds straight from the compiled
classes, splitting dependencies, resources and app classes into separate layers, so rebuilds
after a small code change are fast (only the changed layer gets pushed). `bootBuildImage` goes
through Cloud Native Buildpacks instead, which needs Docker running and is slower per build,
but gives you a buildpack-produced image (SBOM, more standardised layering) if that matters
for where the image ends up. Since this project already has the `jib` block configured with
the base image, port and JVM flags it wants, `jibDockerBuild` is the path that actually uses
that config.

Neither of these needs a Dockerfile, since Jib and buildpacks both assemble the image layers
themselves. A handwritten Dockerfile is worth having if you need something they can't give
you: extra OS packages installed in the image, multi-stage steps that do more than compile
and copy a jar (native compilation, bundling non-JVM assets), a base image not supported by
either tool, or just full control over the exact layer/command structure for a production
deploy. For a project this size, that's more control than is needed.

Once built, run the container (point it at a reachable MySQL instance, e.g. the one from
`docker/docker-compose.yml`):

```bash
docker run -p 8080:8080 -e OPENAI_API_KEY=... springbootplay:latest
```

## Reference Documentation

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.5/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.5/gradle-plugin/packaging-oci-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.5/reference/web/servlet.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/4.0.5/reference/actuator/index.html)
* [OpenTelemetry](https://docs.spring.io/spring-boot/4.0.5/reference/actuator/observability.html#actuator.observability.opentelemetry)
* [Spring AI](https://docs.spring.io/spring-ai/reference/)
