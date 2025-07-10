# CS 지식 노트 1 - 동기/비동기
## 동기/비동기
### 용어 정리
| 구분                     | 언제 **결과를 받느냐**                     | 호출부가 **멈추느냐**                                | 대표 키워드                                                            |
| ---------------------- | ---------------------------------- | -------------------------------------------- | ----------------------------------------------------------------- |
| **동기 (Synchronous)**   | 호출부가 *즉시* 결과를 받을 때까지 기다림           | 대부분 **Blocking**, 반드시 그런 건 아님                | `Thread.join()`, `read()`                                         |
| **비동기 (Asynchronous)** | 호출과 별개로 결과가 준비되면 **나중에** 통지됨       | 호출 시점엔 **Blocking 아님**. 결과는 콜백/Future 등으로 전달 | `CompletableFuture`, `callback`, `reactor`                        |
| **Blocking**           | 호출 스레드가 커널로부터 제어권을 돌려받지 못하고 **대기** | -                                            | `accept()`, `recv()` 등 기본 소켓 API                                  |
| **Non-blocking**       | 준비되지 않았으면 **즉시 반환**되며 스레드 계속 실행    | -                                            | `fcntl(fd, O_NONBLOCK)`, `SocketChannel.configureBlocking(false)` |

- ✅ 동기/비동기는 시간 관계
- ✅ Blocking/Non-blocking은 스레드 흐름 제어 방식

### 2x2 조합 이해하기
|                        | **Blocking**                                                                       | **Non-blocking**                                                                                                                   |
| ---------------------- | ---------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| **동기 (Synchronous)**   | - 가장 전통적인 방식<br>- 호출 → 대기 → 결과 수신<br>- 스레드 1:1 구조 (연결 수 = 스레드 수)<br>- 단순하지만 확장성 낮음 | - `select`/`epoll`로 이벤트 감지 후 준비된 소켓만 `read()`<br>- 여전히 “결과 받아야 다음 단계 진행”이므로 **동기적**<br>- 예: Netty, Nginx                           |
| **비동기 (Asynchronous)** | - 드물지만 존재<br>- 예: Java `Future.get()` 등 논리적 비동기, 내부는 블로킹일 수 있음                     | - **최고 확장성**<br>- 호출 시 바로 반환하고, 커널이 완료 이벤트를 알려줌<br>- 콜백/이벤트 기반 처리<br>- 예: Windows IOCP, 리눅스 io\_uring, `AsynchronousSocketChannel` |

## @Async 와 쓰레드풀
### 1. @Async란?
Spring에서 메서드에 @Async를 붙이면, 해당 메서드는 별도의 스레드에서 비동기적으로 실행됩니다.
- 조건
  + @EnableAsync가 필수로 선언되어 있어야 작동
  + 프록시 기반(AOP)으로 동작 (self-invocation은 적용 안 됨)
  + 반환 타입은 void, Future<T>, CompletableFuture<T> 등 사용 가능
```kotlin
@Async
fun processAsync() {
    println("비동기 실행 중")
}
```

### 2. ThreadPoolTaskExecutor란?
ThreadPoolTaskExecutor는 Spring이 제공하는 JDK의 ExecutorService를 wrapping한 비동기 작업 실행기입니다. 내부적으로 **ThreadPoolExecutor**를 사용하며, 아래 설정값으로 스레드 풀을 관리합니다:
- 주요 설정

| 설정명                        | 설명                                    |
| -------------------------- | ------------------------------------- |
| `corePoolSize`             | 항상 유지할 기본 스레드 수                       |
| `maxPoolSize`              | 최대 동시 실행 스레드 수                        |
| `queueCapacity`            | 작업 큐 용량. 큐가 가득 차면 maxPoolSize까지 늘어남   |
| `keepAliveSeconds`         | 유휴 스레드 유지 시간                          |
| `threadNamePrefix`         | 생성되는 스레드 이름 접두어                       |
| `rejectedExecutionHandler` | 큐, 스레드 다 찼을 때 처리 정책 (기본: AbortPolicy) |
```kotlin
@Bean(name = ["myExecutor"])
fun threadPoolTaskExecutor(): Executor {
    val executor = ThreadPoolTaskExecutor()
    executor.corePoolSize = 10
    executor.maxPoolSize = 20
    executor.setQueueCapacity(100)
    executor.setThreadNamePrefix("MyExecutor-")
    executor.initialize()
    return executor
}
```

### 3. SpringBoot 기본 동작
Spring Boot에서 @Async 사용 시, 별도의 Executor를 등록하지 않으면 아래의 기본 Executor를 사용합니다:
- 기본 Executor
  + SimpleAsyncTaskExecutor: 스레드 풀이 없는 방식
  + 요청마다 새 스레드를 생성함 (비효율적, 제어 어려움)
  + 성능/안정성 문제로 실무에서는 ThreadPoolTaskExecutor를 반드시 명시 등록하는 것이 좋습니다.
```kotlin
// 사용 권장 ❌
@Async
fun doSomething() {
    // SimpleAsyncTaskExecutor 사용됨
}
```

### 4. @Async에서 Executor 지정 방법
```kotlin
@Async("myExecutor") // 명시적으로 Bean 이름 지정
fun processHeavyTask() {
    ...
}
```
- 해당 이름의 ThreadPoolTaskExecutor Bean이 존재해야 함
- 지정하지 않으면 기본 Executor 사용

### 5. 여러 개의 Executor를 등록하면?
상황
```kotlin
@Bean(name = ["executorA"])
fun executorA(): Executor = ThreadPoolTaskExecutor().apply { ... }

@Bean(name = ["executorB"])
fun executorB(): Executor = ThreadPoolTaskExecutor().apply { ... }
```
- 이 경우 @Async 어노테이션에서 명시적으로 어떤 Executor를 쓸지 지정하지 않으면 예외가 발생합니다.
- 즉, 다수 존재할 경우 Ambiguous mapping으로 인해 스프링이 어떤 Executor를 쓸지 알 수 없기 때문입니다.
- 따라서 반드시 @Async("executorA")처럼 지정 필요

### 6. 예외 처리 방법
1) CompletableFuture 기반 예외 처리
```kotlin
@Async
fun asyncProcess(): CompletableFuture<String> {
    try {
        // some logic
        return CompletableFuture.completedFuture("success")
    } catch (ex: Exception) {
        return CompletableFuture.failedFuture(ex)
    }
}
```
또는
```kotlin
@Async
fun asyncProcess(): CompletableFuture<String> =
    CompletableFuture.supplyAsync {
        if (someCondition) throw IllegalStateException("Error")
        "result"
    }.exceptionally {
        println("예외 처리: ${it.message}")
        "default"
    }
```

2) 전역 예외 처리기
```kotlin
@Bean
fun asyncExceptionHandler(): AsyncUncaughtExceptionHandler {
    return AsyncUncaughtExceptionHandler { ex, method, params ->
        println("비동기 예외 발생: ${ex.message}")
    }
}

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {
    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler = asyncExceptionHandler()
}
```
- 단, 이 방식은 void 반환의 @Async 메서드에만 동작합니다. Future, CompletableFuture에서는 직접 잡아야 합니다.

### 7. 스레드 풀 동작 방식 (자바 기본 ThreadPoolExecutor 기준)
```text
수신한 작업 → (활성 스레드 수 < corePoolSize) → 새 스레드 생성
           → (활성 스레드 수 ≥ corePoolSize) → 작업 큐 저장
           → (큐 가득참 + 스레드 수 < maxPoolSize) → 새 스레드 생성
           → (maxPoolSize 도달 + 큐 가득참) → RejectedExecutionHandler 호출
```
- RejectedExecutionHandler 전략

| 전략                    | 설명                         |
| --------------------- | -------------------------- |
| `AbortPolicy`         | 기본값. 예외 발생시킴               |
| `CallerRunsPolicy`    | 호출 스레드가 직접 작업 처리           |
| `DiscardPolicy`       | 작업 무시, 예외 없음               |
| `DiscardOldestPolicy` | 큐에서 가장 오래된 작업 제거 후 새 작업 추가 |

### 8. 실무 권장 설정 예시
```yaml
spring:
  task:
    execution:
      pool:
        core-size: 10
        max-size: 20
        queue-capacity: 100
        keep-alive: 60s
      thread-name-prefix: async-exec-
```
이 설정은 별도 Executor Bean 없이도 Spring Boot에서 자동 적용됩니다.

### 🔚 정리
| 항목          | 내용                                                                      |
| ----------- | ----------------------------------------------------------------------- |
| 기본 실행기      | 등록 없으면 `SimpleAsyncTaskExecutor` (매우 비효율적)                              |
| 명시 실행기      | `@Async("executorName")` 으로 지정 가능                                       |
| 다수 Executor | 명시적 지정 없으면 에러 발생                                                        |
| 예외 처리       | `CompletableFuture.exceptionally` 또는 `AsyncUncaughtExceptionHandler` 사용 |
| 스레드 풀 제어    | core/max/threadNamePrefix/rejectionHandler 등으로 튜닝 가능                    |

### 추가: SpringBoot에서는 별도로 등록하지 않아도 ThreadPoolTaskExecutor 사용?
- ✅ 결론 먼저
  + Spring Boot에서는 별도 Executor를 등록하지 않아도 ThreadPoolTaskExecutor 기반의 AsyncTaskExecutor가 자동 구성됩니다.
  + 즉, Spring Boot에서는 기본적으로 SimpleAsyncTaskExecutor가 아니라, 스레드 풀 기반의 ThreadPoolTaskExecutor를 기본 제공합니다.
  + Spring (순수)에서는 기본이 SimpleAsyncTaskExecutor이며, 명시적으로 바꾸지 않으면 계속 그걸 씁니다.
- 🔍 차이를 만드는 핵심: Spring Boot의 TaskExecutionAutoConfiguration
  + Spring Boot에는 다음과 같은 자동 설정이 존재합니다:
    ```kotlin
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({ TaskExecutor.class, ThreadPoolTaskExecutor.class })
    @EnableConfigurationProperties(TaskExecutionProperties.class)
    public class TaskExecutionAutoConfiguration {
      @Bean
      @ConditionalOnMissingBean(Executor.class)
      public TaskExecutorBuilder taskExecutorBuilder(...) { ... }

      @Bean
      @ConditionalOnMissingBean(name = "applicationTaskExecutor")
      public ThreadPoolTaskExecutor applicationTaskExecutor(TaskExecutorBuilder builder) {
          return builder.build();
      }
    }
    ```
  + 즉, 아래 두 조건이 맞으면 Spring Boot는 자동으로 ThreadPoolTaskExecutor를 등록합니다:
    - Executor 타입의 Bean이 아무것도 없을 때
    - Bean 이름 "applicationTaskExecutor"가 등록되지 않았을 때
- 결과적으로 @Async의 기본 실행기는 바로 이 applicationTaskExecutor Bean입니다.
  
## NIO
### 🔍 NIO란?
- NIO (New I/O)는 Java 1.4부터 도입된 고성능 입출력 API로, 전통적인 java.io의 Blocking 방식의 한계를 극복하기 위해 만들어졌습니다.
- NIO는 Non-blocking I/O, 버퍼 기반, 채널(Channel) 개념을 중심으로 동작합니다.

### 📦 NIO의 핵심 구성 요소
| 요소             | 설명                                                                                         |
| -------------- | ------------------------------------------------------------------------------------------ |
| `Channel`      | 데이터 입출력의 **양방향 통로**. 소켓/파일 등과 연결됨. `SocketChannel`, `FileChannel`, `DatagramChannel` 등이 있음 |
| `Buffer`       | 데이터를 담는 **컨테이너 객체**. ByteBuffer, CharBuffer 등. 기존 IO에서는 byte\[]를 썼지만 NIO는 이걸 Buffer로 관리함   |
| `Selector`     | 하나의 스레드가 **여러 채널의 이벤트를 감시**할 수 있게 해주는 핵심 도구. 이벤트 기반으로 동작함                                  |
| `SelectionKey` | 채널과 Selector 간의 연결 정보. OP\_READ, OP\_WRITE 같은 관심 이벤트 등록                                    |

### ⚙️ 전통 IO vs NIO
| 항목     | 기존 IO (`java.io`)     | NIO (`java.nio`)                    |
| ------ | --------------------- | ----------------------------------- |
| 처리 방식  | **Stream 기반**, 순차 처리  | **Channel + Buffer 기반**, 데이터 방향성 존재 |
| 블로킹 여부 | Blocking I/O (스레드 대기) | Non-blocking I/O (바로 반환)            |
| 연결 수   | 1:1 (스레드:클라이언트)       | 1\:N (하나의 스레드가 Selector로 다수 처리)     |
| 확장성    | 낮음 (스레드 많아지면 부하)      | 높음 (이벤트 기반 처리로 스케일 가능)              |

### 📈 NIO가 적합한 상황
- 수천~수만 개의 연결을 다뤄야 하는 고성능 서버
- 빠른 응답이 중요한 채팅 서버, HTTP 서버, 프록시 서버 등
- 블로킹으로 인해 리소스 낭비가 심한 구조를 개선할 때

### 📉 주의할 점
- 코드가 상태 기반(state machine) 구조가 되기 때문에 복잡해짐
- Selector의 wakeup 문제, register 타이밍 문제 등 정교한 제어 필요
- Java 11 이전까지는 Selector 성능 이슈가 자주 제기됨 (특히 Windows)

### 번외. Netty
- Netty는 기술적으로 Non-blocking I/O 기반이며, “동기적 방식”으로 동작하는 구조를 가지고 있습니다.
- 다만, 비동기 API도 지원합니다. 그래서 "동기적이지만 비동기적 조합도 가능"한 유연한 프레임워크라고 보시면 됩니다.
- 용어정리 (복습)
  | 개념                             | 의미                               |
  | ------------------------------ | -------------------------------- |
  | **Blocking / Non-blocking**    | 커널 호출이 스레드를 멈추느냐 마느냐 (I/O 흐름 관점) |
  | **Synchronous / Asynchronous** | 결과를 지금 받느냐, 나중에 (로직 진행 방식 관점)    |
- 🧠 Netty는 왜 Non-blocking + "동기"인가?
  + Netty는 내부적으로 다음과 같은 구조를 갖습니다:
    1. Non-blocking I/O 기반
        - Java NIO 기반의 Selector, SocketChannel, SelectionKey 등을 활용하여 스레드를 블로킹하지 않고 이벤트 감지
        - 즉, read() 같은 호출은 준비된 데이터 없으면 바로 반환 → Non-blocking
    2. EventLoop가 동기적 흐름으로 처리
        - Netty는 Selector.select() 이후, 이벤트를 하나씩 순차적으로 처리합니다.
        - 예를 들어 channelRead()나 write() 등은 결과가 준비되기 전까지 대기하는 것이 아니라, 준비된 이벤트를 순차적으로 동기적으로 처리합니다.
        - 이 부분이 "동기"라는 의미입니다: 이벤트 → 처리 → 다음 순차적 흐름
          ```java
          // 이벤트 루프 내부 논리
          while (true) {
            selector.select() // 이벤트 감지 (Non-blocking select)
            processSelectedKeys() // 순차적으로 key 처리 (동기적 흐름)
          }
          ```
- 🌀 그런데 비동기 API도 있다?
  + Netty는 ChannelFuture 기반으로 비동기 API도 제공합니다:
    ```java
    ChannelFuture future = channel.writeAndFlush(msg);
    future.addListener(f -> {
      if (f.isSuccess()) {
        System.out.println("성공적으로 전송됨");
      }
    });
    ```
  + 여기서 중요한 점은:
    - 실제 처리 흐름은 EventLoop에서 동기적이지만,
    - API 레벨에서는 비동기적인 콜백 등록 가능 → 로직을 비동기 스타일로 설계할 수 있습니다.
- 📌 정리하면
  | 관점         | Netty                                                           |
  | ---------- | --------------------------------------------------------------- |
  | **I/O 방식** | Non-blocking (Java NIO 기반)                                      |
  | **처리 흐름**  | 동기적 이벤트 루프 (EventLoop가 순차 처리)                                   |
  | **API 제공** | `ChannelFuture` 등 비동기 API 지원                                    |
  | **결론**     | **“Non-blocking + 내부적으로 동기적 흐름”을 가지며, 비동기 로직도 가능**한 하이브리드 프레임워크 |
