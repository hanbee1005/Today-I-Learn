# Item05. 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라
### 핵심 정리 
```java
public class SpellChecker {
  private final Dictionary dictionary = new Dictionary(); // 이렇게 작성하지 x

  ...
}

------------------------------------------------------------------------
public class SpellChecker {
  private final Dictionary dictionary;

  public SpellChecker(Dictionary dictionary) { // 외부에서 객체 생성 시 받을 수 있도록
    this.dictionary = dictionary;
  }

  ...
}
```

- 사용하는 자원에 따라 동작이 달라지는 클래스는 정적 유틸리티 클래스나 싱글턴 방식이 적합하지 않다.
- 의존 객체 주입이란 인스턴스를 생성할 때 필요한 자원을 넘겨주는 방식이다.
- 이 방식의 변형으로 생성자에 자원 팩터리를 넘겨줄 수 있다.
- 의존 객체 주입을 사용하면 클래스의 유연성, 재사용성, 테스트 용이성을 개선할 수 있다.
### 완벽 공략
- p29. 이 패턴의 쓸만한 변형으로 생성자에 자원 팩터리를 넘겨주는 방식이 있다.
- p29. 자바 8에 소개한 Supplier<T> 인터페이스가 팩터리를 표현한 완벽한 예다.

    ```java
    public class SpellChecker {
      private final Dictionary dictionary;

      public SpellChecker(Supplier<Dictionary> dictionarySupplier) {
        this.dictionary = dictionarySupplier.get();
      }

      ...
    }

    -------------------------------------------------------------------
    ...
    SpellChecker spellChecker = new SpellChecker(() -> new DefaultDictionary());

    or

    SpellChecker spellChecker = new SpellChecker(DefaultDictionary::new);

    or

    SpellChecker spellChecker = new SpellChecker(DictionaryFactory::new);
    ```

- p29. 한정적 와일드카드 타입을 사용해서 팩터리 타입의 매개변수를 제한해야 한다.

    ```java
    public class SpellChecker {
      private final Dictionary dictionary;

      // 직접적인 클래스 타입을 받지 말고!!!
      public SpellChecker(Supplier<DefaultDictionary> dictionarySupplier) {
        this.dictionary = dictionarySupplier.get();
      }

      // 인터페이스 하위 클래스는 모두 받을 수 있도록!!!
      public SpellChecker(Supplier<? extends Dictionary> dictionarySupplier) {
        this.dictionary = dictionarySupplier.get();
      }

      ...
    }
    ```

- p29. 팩터리 메서드 패턴
    - 객체를 생성할 때 만드는 부분이 비슷한 부분은 공장을 통해 만들고 다른 부분은 각자 구현할 수 있도록!
    - 구체적으로 어떤 인스턴스를 만들지는 서브 클래스가 정한다.
    - 새로운 Product를 제공하는 팩터리를 추가하더라도 팩터리를 사용하는 클라이언트 코드는 변경할 필요가 없다.
    - OCP 원칙을 잘 구현한 내용!
        - 어떤 팩터리를 사용하든 무슨 딕셔너리가 생기든 SpellChecker는 변경 x

        ```java
        public interface DictionaryFactory {
          Dictionary getDictionary();
        }

        public class DefaultDictionaryFactory implements DictionaryFactory {
          public Dictionary getDictionary() {
            return new DefaultDictionary();
          }
        }

        public class MockDictionaryFactory implements DictionaryFactory {
          public Dictionary getDictionary() {
            return new MockDictionary();
          }
        }

        ------------------------------------------------------------------

        public class SpellChecker {
          private final Dictionary dictionary;

          public SpellChecker(DictionaryFactory factory) {
            this.dictionary = factory.getDictionary();
          }

          ...
        }
        ```

- p30. 의존 객체가 많은 경우에 Dagger, Guice, 스프링 같은 의존 객체 주입 프레임워크 도입을 고려할 수 있다.
    - BeanFactory 또는 ApplicationContext
    - InversionOfControl - 뒤짚어진 제어권
        - 자기 코드에 대한 제어권을 자기 자신이 가지고 있지 않고 외부에서 제어하는 경우
        - 제어권? 인스턴스를 만들거나, 어떤 메소드를 실행하거나, 필요로 하는 의존성을 주입받는 등…
    - 스프링 IoC 컨테이너 사용 장점
        - 수많은 개발자들에게 검증되었으며 자바 표준 스팩(@Inject)도 지원한다.
        - 손쉽게 싱글톤 Scope을 사용할 수 있다.
        - 객체 생성 (Bean) 관련 라이프사이클 인터페이스를 제공한다. → ex) 스프링 AOP

        ```java
        public class SpellChecker {
          private Dictionary dictionary;

          public SpellChecker(Dictionary dictionary) {
            this.dictionary = dictionary;
          }

          ...
        }

        public class SpringDictionary implements Dictionary {
          ...
        }

        -----------------------------------------------------------------
        @Configuration
        public class AppConfig {
          @Bean
          public SpellChecker spellChecker() {
            return new SpellChecker(dictionary);
          }

          @Bean
          public Dictionary dictionary() {
            return new SpringDictionary();
          }
        }

        -----------------------------------------------------------------
        public class App {
          public static void main(String[] args) {
            ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

            // 아래 SpellChecker는 스프링이 만들어준 싱글톤 Bean
            // SpellChecker는 스프링이 주입한 Dictionary를 가짐
            SpellChecker spellChecker = applicationContext.getBean(SpellChecker.class);
          }
        }
        ```

        - 빈으로 사용할 클래스들에 `@Component`라는 애노테이션을 추가해 ComponentScan 방식으로 빈을 등록

            ```java
            @Component
            public class SpellChecker {
              private Dictionary dictionary;

              public SpellChecker(Dictionary dictionary) {
                this.dictionary = dictionary;
              }

              ...
            }

            @Component
            public class SpringDictionary implements Dictionary {
              ...
            }

            --------------------------------------------------------------
            @Configuration
            @ComponentScan(basePackageClasses = AppConfig.class) // AppConfig 가 있는 패키지부터 스캔 시작!!
            public class AppConfig {
            }
            ```
