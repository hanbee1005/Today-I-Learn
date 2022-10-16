# Item01. 생성자 대신 정적 팩터리 메서드를 고려하라

### 핵심 정리
+ 장점
  - 이름을 가질 수 있다. (동일한 시그니처의 생성자를 두 개 가질 수 없다.) 
    ```java
    public class Order {
      private Product product;
      private boolean prime;
      private boolean urgent;

      public static Order primeOrder(Product product) {
        Order order = new Order();
        order.product = product;
        order.prime = true;
        return order;
      }

      public static Order urgentOrder(Product product) {
        Order order = new Order();
        order.product = product;
        order.urgent = true;
        return order;
      }
    }
    ```

  - 호출될 때마다 인스턴스를 새로 생성하지 않아도 된다. (`Boolean.valueOf`)
    ```java
    public class Settings {
      private boolean useAutoSteering;
      private boolean useABS;
      private Difficulty difficulty;

      private Settings() {}

      private static final Settings SETTINGS = new Settings();

      public static Settings newInstance() {
        return SETTINGS;
      }
    }
    ```

  - 반환 타입의 하위 타입 객체를 반환할 수 있는 능력이 있다. (인터페이스 기반 프레임워크, 인터페이스에 정적 메소드)      
    ```java
    public class HelloServiceFactory {
      public static HelloService of(String lang) {
        if (lang.equals("ko")) {
          return KoreanHelloService();
        } else {
          return EnglishHelloService();
        }
      }
    }

    ---
    public interface HelloService {}

    public class KoreanHelloService implements HelloService {}
    public class EnglishHelloService implements HelloService {}
    ```

    - Java 8부터 인터페이스 내부에 static 메소드 선언이 가능!

        ```java
        public interface HelloService {
          String hello();

          static HelloService of(String lang) {
            if (lang.equals("ko")) {
              return KoreanHelloService();
            } else {
              return EnglishHelloService();
            }
          }
        }
        ```

  - 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다. (EnumSet)
  - 정적 팩터리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다. (서비스 제공자 프레임워크)
    ```java
    public static void main(String[] args) {
      // 구현체에 의존적이지 않음!!!
      ServiceLoader<HelloService> loader = ServiceLoader.load(HelloService.class);
      Optional<HelloService> helloService = loader.findFirst();
      helloService.ifPresent(service -> System.out.println(service.hello()));
    }
    ```
+ 단점
  - 상속을 하려면 `public` 이나 `protected` 생성자가 필요하니 정적 팩터리 메소드만 제공하면 안된다.
  - 정적 팩터리 메서드는 프로그래머가 찾기 어렵다. → 자주 사용하는 네이밍을 통일해서 쓰자! 또는 주석을 통해 문서화를 하자!
+ 디자인 패턴과 무관! (추상 팩토리, 팩토리 패턴 등)

### 추가 이론
+ 열거 타입 (**Enum**eration)
    - 상수 목록을 담을 수 있는 데이터 타입
    - 특정한 변수가 가질 수 있는 값을 제한할 수 있다. Type-Safety를 보장할 수 있다.
    - 싱글톤 패턴을 구현할 때 사용하기도 한다.
    - 기본적으로 알고 있어야 할 것
        - `values()`를 통해 가능한 모든 값을 출력 가능
        - 클래스처럼 생성자, 메서드, 필드를 가질 수 있음
        - enum 값은 `==` 으로 동일성 비교를 할 수 있음

            → JVM 내부에서 1개의 인스턴스만 있다는 것이 보장되기 때문에… `equals()`를 쓰면 `NPE` 이 발생할 수 있기 때문에 `==`으로 비교!!

        - `EnumSet`, `EnumMap`을 사용해야 효율적!!
+ 플라이웨이트 패턴 (flyweight)
    - 같은 객체가 자주 사용되는 경우 미리 자주 사용하는 객체들을 만들어 놓고 필요로하는 인스턴스를 꺼내 쓸 수 있는 방식 (장점 2와 연결)
    - 객체를 가볍게 만들어 메모리 사용을 줄이는 패턴
    - 자주 변하는 속성(또는 외적인 속성, extrinsit)과 변하지 않는 속성(또는 내적인 속성, intrinsit)을 분리하고 재사용하여 메모리 사용을 줄일 수 있다.
+ 인터페이스와 정적 메서드
    - 자바 8, 9에서 주요 인터페이스 변화 : 기본메서드와 정적 메서드를 가질 수 있다.
        - 기본 메서드 (`default`)
            - 인터페이스에서 메서드 선언 뿐 아니라, 기본적인 구현체까지 제공할 수 있다.
            - 기존의 인터페이스를 구현하는 클래스에 새로운 기능을 추가할 수 있다.
            - 예) `Comparator.reversed()`
        - 정적 메서드
            - 자바 9부터 private static 메서드도 가질 수 있다.
            - 단, private 필드는 아직도 선언할 수 없다.
+ 서비스 제공자 프레임워크
    - 확장 가능한 애플리케이션을 만드는 방법
        - 애플리케이션 코드를 수정하지 않고 외적인 요인만으로도 조작 가능 = 확장 가능
    - 주요 구성 요소
        - 서비스 제공자 인터페이스(SPI)와 서비스 제공자 (서비스 구현체)
        - 서비스 제공자 등록 API (서비스 인터페이스의 구현체를 등록하는 방법)
            - `@Configuration`, `@Bean`
        - 서비스 접근 API (서비스의 클라이언트가 서비스 인터페이스의 인스턴스를 가져올 때 사용하는 API)
            - `ApplicationContext.getBean()`, `@Autowired`
    - 다양한 변형
        - 브릿지 패턴
            - 구체적인 것과 추상적인 것을 나눠서 연결하는 패턴
            - 각각이 서로 영향을 주지 않으면서 각각 계층 구조로 발전시키는데 사용
        - 의존 객체 주입 프레임워크
        - java.util.ServiceLoader
+ 리플렉션 (reflection)
    - 클래스 로더를 통해 읽어온 클래스 정보(거울에 반사된 정보)를 사용하는 기술
    - 리플렉션을 사용해 클래스를 읽어오거나, 인스턴스를 생성하거나, 메서드를 실행하거나, 필드의 값을 가져오거나 변경하는 것이 가능하다.
    - 언제 사용?
        - 특정 애노테이션이 붙어있는 필드 또는 메서드 읽기 (JUnit, Spring)
        - 특정 이름 패턴에 해당하는 목록 가져와 호출하기 (getter, setter)
