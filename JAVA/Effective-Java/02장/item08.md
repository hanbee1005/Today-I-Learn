# Item08. finalizer와 cleaner 사용을 피하라
### 핵심 정리
- 리소스 반납을 하기 위해 사용하는 것들
- finalizer와 cleaner(자바 9에서 도입)는 즉시 수행된다는 보장이 없다.
- finalizer와 cleaner는 실행되지 않을 수도 있다.
- finalizer 동작 중에 예외가 발생하면 정리 작업이 처리되지 않을 수도 있다.
- finalizer와 cleaner는 심각한 성능 문제가 있다.
- finalizer는 보안 문제가 있다.

```java
public class FinalizerClass {
  @Override
  protected void finalize() throws Throwable {
    // 이렇게 정의해서 사용할 수 있지만
    // AutoClosable을 앞으로 사용하는 것이 필요!
    // 이 기능은 자바 9부터는 Deprecated 된 내용
  }
}

------------------------------------------------------------------

public class BigObject {
  private List<Object> resource;

  public BigObject(List<Object> resource) {
    this.resource = resource;
  }

  // Runnable의 구현체로 cleaner 정의
  // inner class로 만들거면 static으로 하고
  // 정리하려는 Object를 절대 참조하면 안됨...!
  public static class ResourceCleaner implements Runnable {
    private List<Object> resourceToClean;

    public ResourceCleaner(List<Object> resourceToCleaner) {
      this.resourceToCleaner = resourceToCleaner;
    }

    @Override
    public void run() {
      resourceToCleaner = null;
    }
  }
}

public class Main {
  public static void main(String[] args) throws InterruptedException {
    Cleaner cleaner = Cleaner.create();

    List<Object> resourceToClean = new ArrayList<>();
    BigObject bigObject = new BigObject(resourceToClean);

    // 이렇게 등록!!
    cleaner.register(bigObject, new BigObject.ResourceCleaner(resourceToClean));

    bigObject = null;
    System.gc();
    Thread.sleep(3000L);
  }
}
```

- GC의 대상이 되면 finalize() 메소드가 큐(ReferenceQueue)에 들어감
- 참조가 없어도 바로 객체 반환(소멸)이 실행되는 것이 아님…
- 반납할 자원이 있는 클래스는 AutoClosable을 구현하고 클라이언트에서 close()를 호출하거나 try-with-resource 를 사용해야 한다.

```java
public class AutoClosableIsGood implements AutoClosable {
  private BufferedInputStream inputStream;

  @Override
  public void close() {
    try {
      // 여기서 자원 정리하면 됨!!!
      inputStream.close();
    } catch (IOException e) {
      throw new RuntimeException("failed to close " + inputStream);
    }
  }
}

public class Main {
  public static void main(String[] args) {
    try(AutoClosableIsGood good = new AutoClosableIsGood()) {
      // 자원 반납 처리가 됨
    }
  }
}
```

- GC 할 때 자원 반납 기회를 가질 수 있도록 사용하는 것이 Cleaner → 역시 보장은 없지만… 클라이언트가 제대로 try-with-resource 등으로 사용하지 않았을 때 안전망의 역할을 할 수 있음
### 완벽 공략
- p42. Finalizer 공격
    - 만들다 만 객체를 finalizer 메소드에서 사용하는 방법
    - Finalizer 공격

        ```java
        public class Account {
          private String accountId;

          public Account(String accountId) {
            this.accountId = accountId;

            if (accountId.equeals("notAllowAccountId")) {
              throw new IllegalArgumentException();
            }
          }

          public void transfer(BigDecimal amount, String to) {
            System.out.println("transfer %f from %s to %s\n", amount, accountId, to);
          }
        }

        public class BrokenAccount extends Account {
          public BrokenAccount(String accountId) {
            super(accountId);
          }

          @Override
          protected void finalize() throws Throwable {
            this.transfer(BigDecimal.valueOf(100), "keesun");  // 3. 여기가 진행되면서 notAllowAccountId에서 원하는 대로 돈을 보낼 수 있음
          }
        }

        public class Main {
          public static void main(String[] args) {
            Account account = null;
            try {
              account = new BrokenAccount("notAllowAccountId");  // 1. 만들다 만 객체
            } catch (Exception e) {
              System.out.println("이러고 그대로 진행해버리면...?");
            }

            System.gc();  // 2. gc가 실행되면 만들다 만 BrokenAccount 의 finalize() 메서드가 진행됨
            Thread.sleep(3000L);
          }
        }
        ```

    - 방어하는 방법
        - final 클래스로 만들거나 → finalize()에 final을 붙여서 클래스 상속은 해도 메소드는 새로 정의할 수 없게 만들기!
        - finalizer() 메소드를 오버라이딩 한 다음 final을 붙여서 하위 클래스에서 오버라이딩할 수 없도록 만든다.
- p43. AutoClosable
    - try-with-resource를 지원하는 인터페이스
    - void close() throws Exception
        - 인터페이스에 정의된 메서드에서 Exception 타입으로 예외를 던지지만
        - 실제 구현체에서는 구체적인 예외를 던지는 것을 추천하며
        - 가능하다면 예외를 던지지 않는 것도 권장한다.
    - Closeable 클래스와 차이점
        - IOException을 던지며 (IO와 관련된 작업이라면 Closeable을 구현하는 것도 좋은 선택)
        - 반드시 idempotent 해야 한다. (멱등성: 몇번이 실행되든 상관없이 같은 결과가 나와야 함)
- p45. 정적이 아닌 중첩 클래스는 자동으로 바깥 객체의 참조를 갖는다.
  ```java
  public class OuterClass {
    private void hi() {}

    class innerClass {
      public void hello() {
        OuterClass.this.hi();  // 이런 식으로 참조할 수 있음 -> 중첩 reference는 자원 정리가 안됨...
      }
    }
  }
  ```

- p45. 람다 역시 바깥 객체의 참조를 갖기 쉽다.
  ```java
  public class LambdaExample {
    private int value = 10;

    // Lambda 안에 외부 클래스의 필드를 참조하게 돼서
    // 순환 참조로 자원 정리가 안됨
    // Cleaning 작업을 이렇게 정의하면 안됨!
    // 바깥 객체를 참조하지 않거나 static 이면 상관 x
    private Runnable instanceLambda = () -> System.out.println(value);
  }
  ```
