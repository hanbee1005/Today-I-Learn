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
- p38. WeakHashMap
    - p38. 약한 참조 (weak reference)
- p39. 백그라운드 쓰레드
    - p39. ScheduledThreadPoolExecutor
