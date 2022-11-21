#
### 핵심 정리
- 어떤 객체에 대한 레퍼런스가 남아있다면 해당 객체는 가비지 컬렉션의 대상이 되지 않는다.
- 자기 메모리를 직접 관리하는 클래스라면 메모리 누수에 주의해야 한다.
    - 예) 스택, 캐시, 리스너 또는 콜백

    ```java
    public class Stack {
      ...

      public Object pop() {
        if (size == 0) {
          throw new EmptyStackException();
        }

        Object result = elements[--size];
        elements[size] = null;  // 이렇게 null 로 참조를 해제해야 함
        return result;
      }
    }
    ```

    ```java
    public class PostRepository {
      private Map<CacheKey, Post> cache;

      public PostRepository() {
        // cache = new HashMap()<>;
        cache = new WeekHashMap()<>;
      }

      public Post getPostById(Integer id) {
        CacheKey key = new CacheKey(id);
        if (cache.containsKey(key)) {
          return cache.get(key);
        } else {
          // TODO DB 또는 REST API에서 읽어와서 사용할 수 있습니다.
          Post post = new Post();
          cache.put(key, post);
          return post;
        }
      }

      public Map<CacheKey, Post> getCache() {
        return cache;
      }
    }

    ---------------------------------------------------------------------
    @Test
    public void test() {
      PostRepository repository = new PostRepository();
      Integer id = 1;
      Post postById = repository.getPostById(id);

      assertFalse(repository.getCache().isEmpty());

      p1 = null;

      // TODO run gc - 항상 gc가 바로 실행되는건 아님
      System.gc();
      Thread.sleep(3000L);

      // HashMap 일때는 테스트 실패 (캐시가 지워지지 않음)
      // WeekHashMap 인 경우 테스트 성공 (캐시가 비워짐)
      assertTrue(repository.getCache().isEmpty());
    }
    ```

    ```java
    public class ChatRoom {

        private List<WeakReference<User>> users;  // 올바른 WeakReference 사용법은 아님!!!

        public ChatRoom() {
            this.users = new ArrayList<>();
        }

        public void addUser(User user) {
            this.users.add(new WeakReference<>(user));
        }

        public void sendMessage(String message) {
            users.forEach(wr -> Objects.requireNonNull(wr.get()).receive(message));
        }

        public List<WeakReference<User>> getUsers() {
            return users;
        }
    }
    ```

- 참조 객체를 null 처리하는 일은 예외적인 경우이며 가장 좋은 방법은 유효 범위 밖으로 밀어내는 것이다.
- 방법
    - 직접 null 처리
    - 적절한 자료 구조를 사용하여 컨트롤 → LRU 캐시 구현해서 사용
    - 직접 빼거나 넣거나 하는 것
    - 백그라운드 쓰레드를 사용해 주기적으로 clean-up 하는 방법

        ```java
        @Test
        void backgroundThread() throws InterruptedException {
          ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
          PostRepository postRepository = new PostRepository();
          CacheKey key1 = new CacheKey(1);
          postRepository.getPostById(key1);

          Runnable removeOldCache = () -> {
            System.out.println("running removeOldCache task");
            Map<CacheKey, Post> cache = postRepository.getCache();
            Set<CacheKey> cacheKeys = cache.keySet();
            Optional<CacheKey> key = cacheKeys.stream().min(Comparator.comparing(CacheKey::getCreated));
              key.ifPresent((k) -> {
                System.out.println("removing " + k);
                cache.remove(k);
              });
            };

          System.out.println("The time is : " + new Date());

          executor.scheduleAtFixedRate(removeOldCache, 1, 3, TimeUnit.SECONDS);

          Thread.sleep(20000L);

          executor.shutdown();
        }
        ```
            
### 완벽 공략
- p37. NullPointException
    - Java 8 Optional을 활용해서 NPE를 최대한 피하자.
    - NullPointException을 만나는 이유
        - 메소드에서 null을 리턴하기 때문에 && null 체크를 하지 않았기 때문에
    - 메소드에서 적절한 값을 리턴할 수 없는 경우에 선택할 수 있는 대안
        - 예외를 던진다.
        - null을 리턴한다.
        - Optional을 리턴한다.
- p38. WeakHashMap
    - p38. 약한 참조 (weak reference)
    - 더이상 사용하지 않는 객체를 GC할 때 자동으로 삭제해주는 Map
    - Key가 더이상 강하게 레퍼런스되는 곳이 없다면 해당 엔트리를 제거한다.
        - 주의! String이나 Wrapper Type 을 Key로 사용하면 JVM 내부에서 캐싱되는 부분이 있기 때문에 사라지지 않을 수 있기 때문에 커스텀한 타입을 사용! (`CacheKey` 객체를 Key로 만들어서 사용한 예제 이유)
    - 레퍼런스 종류
        - Strong, Soft, Weak, Phantom
        - Strong : 직접 할당하는 경우 (`List<String> arr = new ArrayList<>();` 등등)
        - Soft : 더이상 Strong 하게 참조하는 Object가 없을 때 메모리가 없으면 GC때 삭제 (`SoftReference<Object> soft = new SoftReference(arr);`)
        - Weak : 더이상 Strong 하게 참조하는 Object가 없을 때 GC가 일어나면 무조건 삭제 (`WeakReference<Object> soft = new WeakReference(arr);`)
        - Phantom : Phantom Object가 Strong Object 대신 Queue 에 남아있음
            
            ```java
            BigObject object = new BigObject();
            ReferenceQueue<BigObject> rq = new ReferenceQueue<>();
            PhantomReference<BigObject> phantom = new PhantomReference<>(object, rq);
            ```
            
            - 자원 정리에 사용할 수 있음
            - BigObject가 사라질 때 Queue에 저장되기 때문에 사라지는 시점을 알 수 있음 (메모리 공간이 생기는 시간을 알 수 있음 → 자원 반납을 하려면 커스텀한 PhantomReference를 구현해야 함)
    - 맵의 엔트리를 맵의 Value가 아니라 Key에 의존해야 하는 경우에 사용할 수 있다.
    - 캐시를 구현하는데 사용할 수 있지만, 캐시를 직접 구현하는 것은 권장하지 않는다.
- p39. 백그라운드 쓰레드
    - p39. ScheduledThreadPoolExecutor
    - Thread와 Runnable을 학습했다면 그 다음은 Executor
    - Thread, Runnable, ExecutorService
        - ExecutorService로 쓰레드 풀을 생성
            
            ```java
            import me.whiteship.chapter01.item01.Product;
            
            import java.util.concurrent.*;
            
            public class ExecutorsExample {
            
                public static void main(String[] args) throws ExecutionException, InterruptedException {
                    ExecutorService service = Executors.newFixedThreadPool(10);
            
                    Future<String> submit = service.submit(new Task());
            
                    System.out.println(Thread.currentThread() + " hello");
            
                    System.out.println(submit.get());  // 여기서 blocking (대기)
            
                    service.shutdown();
                }
            
                static class Task implements Callable<String> {
            
                    @Override
                    public String call() throws Exception {
                        Thread.sleep(2000L);
                        return Thread.currentThread() + " world";
                    }
                }
            }
            ```
            
    - 쓰레드 풀의 개수를 정할 때 주의할 것
        - CPU, I/O
        - CPU 를 많이 쓰는 작업이라면 아무리 쓰레드 개수를 늘려도 CPU 개수를 넘어가면 어쩌피 대기 발생 → CPU에 집중된 작업이라면 쓰레드 개수를 CPU 개수에 맞춰서 작업하는게… `Runtime.getRuntime().availableProcessors();`
        - I/O 에 집중된 작업이라면 기본적으로 개수를 좀 더 잡아야 함. 적절한 개수를 잡는게 필요함.
    - 쓰레드 풀의 종류
        - Single, Fixed, Cached, Scheduled
        - `newSingleThreadPool()` 쓰레드를 하나만 만들어서 처리
        - `newFixedThreadPool()` 는 Blocking Queue를 사용해서 동시성에 안전
        - `newCachedThreadPool()` 을 사용하면 thread가 없으면 새로 생성(무한정 늘어날 수 있음), 사용하지 않으면 60초 후 삭제…
        - `newScheduledThreadPool(10)` 작업을 몇초 뒤에 시작한다거나 주기적으로 실행할 때 사용할 수 있음
    - Runnable, Collable, Future
        - Runnable은 별도의 반환이 없음
        - Collable 은 별도 쓰레드에서 진행한 작업의 결과를 return → Future로 반환됨
    - CompletableFuture, ForkJoinPool
