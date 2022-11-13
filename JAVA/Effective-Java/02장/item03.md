# Item03. private 생성자나 열거 타입으로 싱글턴임을 보증하라
### 핵심 정리
- 애플리케이션 내에서 꼭 하나만 유지해야하는 경우, 하나만 있는 것이 더 좋은 경우 싱글톤 사용
- 첫번째 방법 : private 생성자 + public static final 필드      
  ```java
  public class Elvis {
    public static final Elvis INSTANCE = new Elvis();

    private Elvis() {}

    ...
  }
  ```
        
  - 장점 : 간결하고 싱글턴임을 API에 드러낼 수 있다. (javadoc을 만들었을 때)
  - 단점
      - 싱글턴을 사용하는 클라이언트 코드를 테스트하기 어려워진다. (인터페이스 없이 정의한 경우에는 더…)

          ```java
          public class Concert {
            ...

            private Elvis elvis;

            public Concert(Elvis elvis) {
              this.elvis = elvis;
            }

            ...
          }

          // Concert 객체를 테스트하려면 아래와 같이 직접 싱글턴 객체를 넣어주어야 함
          Concert concert = new Concert(Elvis.INSTANCE);
          concert.XXX();

          // 대안 : Mock 객체를 사용하고 싶다면 인터페이스 등을 구현한 뒤 별도 Mock 객체를 생성해서 전달
          public interface IElvis { ... }

          public class Elvis implements IElvis { ... }
          public class MockElvis implements IElvis { ... }

          public class Concert {
            ...

            private IElvis elvis;

            public Concert(IElvis elvis) {
              this.elvis = elvis;
            }

            ...
          }

          Concert concert = new Concert(new MockElvis());
          concert.XXX();
          ```

      - 리플렉션으로 private 생성자를 호출할 수 있다.

          ```java
          public class ElvisReflection {
            public static void main(String[] args) {
              try {
                Constructor<Elvis> defaultConstructor = Elvis.class.getDeclaredConstructor();
                defaultConstructor.setAccessible(true); // private 생성자를 호출하기 위함
                Elvis elvis1 = defaultConstructor.newInstance();
                Elvis elvis2 = defaultConstructor.newInstance();
                System.out.println(elvis1 == elvis2);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }

          // 결과 : false
          ```

          - getConstructor() : 일반 기본 생성자 (public)
          - getDeclaredConstructor() : 선언되어 있는 기본 생성자 (접근 지정자 상관 x)
          - 대안 : 별도 flag를 만들어서 재호출 되었을 때는 생성을 막는 방식으로 처리 가능

              ```java
              public class Elvis {
                public static final Elvis INSTANCE = new Elvis();
                private static boolean created;

                private Elvis() {
                  if (created) {
                    throw new UnsupportedOperationException("can't be created by constructor.");
                  }
                  created = true;
                }

                ...
              }
              ```

      - 역직렬화할 때 새로운 인스턴스가 생길 수 있다.

          ```java
          public class Elvis implements IElvis, Serializable {
            ...

            private Elvis() {}

            ...
          }

          ---

          public class ElvisSerialization {
            public static void main(String[] args) {
              try (ObjectOutput out = new ObjectOutputStream(new FileOutputStream("elvis.obj"))) {
                out.writeObject(Elvis.INSTANCE);
              } catch (Exception e) {
                e.printStackTrace();
              }

              try (ObjectInput in = new ObjectInputStream(new FileInputStream("elvis.obj"))) {
                Elvis elvis = (Elvis) in.readObject();
                System.out.println(elvis == Elvis.INSTANCE);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }

          // 결과 : false
          ```

          - 대안 : 역직렬화를 할 때 호출하는 메서드를 오버라이딩(?) 하는 방법이 있는데 이때 생성되어 있는 객체를 반환하도록 할 수는 있음…

              ```java
              public class Elvis implements IElvis, Serializable {
                ...

                public static final Elvis INSTANCE = new Elvis();

                private Elvis() {}

                private Object readResolve() { // 이 메서드가 호출!
                  return INSTANCE;
                }

                ...
              }
              ```

  - 단점을 커버하기 위해 복잡도가 올라가기 때문에 스프링을 쓴다면 빈으로 등록해서 싱글톤을 보장하며 사용할 수 있음 (현실적)
- 두번째 방법 : private 생성자 + 정적 팩터리 메서드

    ```java
    public class Elvis {
      private static final Elvis INSTANCE = new Elvis();
      private Elvis() {}

      public static Elvis getInstance() {
        return INSTANCE;
      }
    }
    ```

    - 장점
        - API를 바꾸지 않고도 싱글턴이 아니게 변경할 수 있다.
            - `getInctance()` 메서드 내부를 변경하면 되고 이를 사용하는 쪽은 변경할 필요 x
        - 정적 팩터리를 제네릭 싱글턴 팩터리로 만들 수 있다.

            ```java
            public class MetaElvis<T> {
              private static final MetaElvis<Object> INSTANCE = new MetaElvis();
              private MetaElvis() {}

              @SuppressWarnings("unchecked")
              public static <T> MetaElvis<T> getInstance() {
                return (MetaElvis<T>) INSTANCE;
              }
            }

            ---

            public static void main(String[] args) {
              MetaElvis<String> elvis1 = MetaElvis.getInstance();
              MetaElvis<Integer> elvis1 = MetaElvis.getInstance();
            }
            ```

            - 두 인스턴스는 같아서 equals 비교하면 같지만 타입이 다르기 때문에 == 비교는 안됨
            - 원래 가지고 있던 싱글턴 인스턴스를 원하는 타입으로 변환해주는 일을 함.
        - 정적 팩터리의 메서드 참조를 공급자(Supplier)로 사용할 수 있다.

            ```java
            public interface Singer {
              void sing();
            }

            ---

            public class Elvis implements Singer {
              private static final Elvis INSTANCE = new Elvis();
              private Elvis() {}
              public static Elvis getInstance() {
                return INSTNACE;
              }

              ...

              @Overrride
              public void sing() {
                System.out.println("my way~~~");
              }
            }

            ---

            public class Concert {
              public void start(Supplier<Singer> singerSupplier) {
                Singer singer = singerSupplier.get();
                singer.sing();
              }
            }

            ---

            public static void main(String[] args) {
              Concert concert = new Concert();
              concert.start(Elvis::getInstance);
            }

            // 결과 : my way~~~
            ```

    - 단점은 첫번째 방법과 동일…ㅠ
- 세번째 방법 : 열거 타입

    ```java
    public enum Elvis {
      INSTANCE;

      public void leaveTheBuilding() {
        System.out.println("Bye bye~~~~");
      }
    }
    ```

    - 가장 간단한 방법이며 직렬화와 리플렉션에도 안전하다.
        - enum은 애초에 `Elvis.class.getDeclaredConstructor();`를 호출해 생성자를 가져오려고 하면 에러 발생!!! (있는데 없다고 함…)
    - 테스트가 불편하다는 단점에 대해서는 동일하게 인터페이스를 구현하는 방식으로 해결 가능!
    - 대부분의 상황에서는 원소가 하나뿐인 열거 타입이 싱글톤을 만드는 가장 좋은 방법이다.
### 완벽 공략
- 메소드 참조
    - 메소드 하나만 호출하는 람다 표현식을 줄여 쓰는 방법
    - 스태틱 메서드 레퍼런스 `ClassName::StaticMethodName`
    - 인스턴스 메서드 레퍼런스 `InstanceName::MethodName`
    - 임의 객체의 인스턴스 메서드 레퍼런스 `ClassName::MethodName`
        - 호환 가능하게 만들어진 메서드일 때 (첫번째 인자는 자기 자신!!!)

        ```java
        public class Person {
          LocalDateTime birthday;

          public int compareByAge(Person p2) {
            return this.birthday.compareTo(p2.birthday);
          }

          ...
        }

        ---

        public static void main(String[] args) {
          List<Person> people = List.of(new Person(13), new Person(5), new Person(20));
          people.sort(Person::compareByAge);
        }
        ```

    - 생성자 레퍼런스 `ClassName::new`
- 함수형 인터페이스
    - 인터페이스 안에 메서드 선언이 하나만! `@FunctionalInterface` 없어도 가능 (but. 컴파일 타임에 체크!!)

        ```java
        @FunctionalInterface
        public interface MyFunction {
          String valueOf(Integer i);  // 선언은 무조건 하나만!!!

          // 자바 8 이후부터 가능
          static String hello() {
            return "hello";
          }
        }
        ```

    - 함수형 인터페이스는 람다 표현식과 메서드 참조에 대한 “타겟 타입”을 제공한다.
    - 타겟 타입은 변수 할당, 메서드 호출, 타입 변환에 활용할 수 있다.
    - 자바에서 제공하는 기본 함수형 인터페이스 익혀둘 것! (java.util.function 패키지)
        - Function<InputType, OutputType>
            - `Function<Integer, String> function = (i) → “hello”;`
        - Supplier<OutputType>
            - `Supplier<Person> supplier = Person::new;` // 기본 생성자 호출
        - Consumer<InputType>
            - `Consumer<String> consumer = System.out::println;`
        - Predicate<InputType> : 무조건 boolean 리턴
    - 함수형 인터페이스를 만드는 방법
        - 심화 학습 1) Understanding java method invocation with invokedynamic
        - 심화 학습 2) LambdaMetaFactory
- 직렬화
    - 객체를 바이트스트림으로 상호 변환하는 기술
    - 바이트스트림으로 변환된 객체를 파일로 저장하거나 네트워크를 통해 다른 시스템으로 전송할 수 있다.
    - Serializable 인터페이스 구현 `public class XXX implements Serializable { … }`
    - transient를 사용해서 직렬화하지 않을 필드 선언하기 `private transient String name;`
    - static 한 필드는 직렬화되지 않음
    - 직렬화를 한 상태에서 클래스를 변경(필드 추가, 삭제 등)한 뒤 이미 직렬화된 내용을 역직렬화하면 어떻게 될까? → 역직렬화 실패 (serialVersionUID가 변경되기 때문)
    - serialVersionUID는 언제 왜 사용하는가?
        - `private static final long serialVersionUID = 1L;`
        - 직접 serialVersionUID를 관리하는 방식으로 사용하면 직렬화 후 클래스를 변경해도 역직렬화가 가능하다.
    - 심화 학습 1) 객체 직렬화 스팩
    - 심화 학습 2) Externalizable
