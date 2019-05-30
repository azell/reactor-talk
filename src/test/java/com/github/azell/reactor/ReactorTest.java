package com.github.azell.reactor;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactorTest {
  private final Logger logger = LoggerFactory.getLogger(ReactorTest.class);
  private final List<String> seq = Arrays.asList("foo", "bar", "baz");

  @Test
  void simpleFlux() {
    Flux<String> publisher = Flux.fromIterable(seq);

    publisher.subscribe(this::log);

    StepVerifier.create(publisher).expectNext("foo", "bar", "baz").expectComplete().verify();
  }

  @Test
  void simpleMono() {
    Mono<String> publisher = Mono.just("one");

    publisher.subscribe(this::log);

    StepVerifier.create(publisher).expectNext("one").expectComplete().verify();
  }

  @Test
  void fluxWithDelay() throws InterruptedException {
    Flux<String> publisher = Flux.fromIterable(seq);

    publisher.delayElements(Duration.ofMillis(500)).subscribe(this::log);
    logger.info("Starting to sleep");

    Thread.sleep(5_000);
  }

  @Test
  void fluxWithDebounce() {
    Flux<String> publisher = Flux.fromIterable(seq);

    publisher.startWith("foo").distinctUntilChanged().subscribe(this::log);
  }

  @Test
  void fluxWithMultipleSubscribers() {
    Flux<String> publisher = Flux.fromStream(() -> Stream.of("a", "b", "c"));

    IntStream.range(0, 5).forEach(idx -> publisher.subscribe(this::log));
  }

  @Test
  void fluxWithContext() {
    String key = "message";
    Mono<String> publisher =
        Mono.just("Hello")
            .flatMap(s -> Mono.subscriberContext().map(ctx -> s + " " + ctx.get(key)))
            .subscriberContext(ctx -> ctx.put(key, "World"));

    publisher.subscribe(this::log);
  }

  @Test
  void fluxWithSharing() throws InterruptedException {
    Stream<String> stream = Stream.of("this", "is", "a", "test");
    Flux<String> publisher = Flux.fromStream(stream).delayElements(Duration.ofMillis(750)).share();

    publisher.subscribe(this::log);
    Thread.sleep(800);
    publisher.subscribe(this::log);

    Thread.sleep(5_000);
  }

  private void log(Object obj) {
    logger.info("Received: {}", obj);
  }
}
