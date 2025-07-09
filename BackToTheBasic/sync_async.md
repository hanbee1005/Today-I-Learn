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
