# Item06. 불필요한 객체 생성을 피하라
### 핵심 정리
- 문자열
    - 사실상 동일한 객체라서 매번 새로 만들 필요가 없다. (JVM 내에서 풀에 담아서 캐싱하고 있기 때문에!)
    - `new String(”자바”)`을 사용하지 않고 문자열 리터럴 `“자바”` 를 사용해 기존에 동일한 문자열을 재사용하는 것이 좋다.
      ```java
      String hello = "hello";
      String hello2 = new String("hello");
      String hello3 = "hello";

      System.out.println(hello == hello2); // false
      System.out.println(hello.equals(hello2)); // true - 문자열 비교는 무조건 이렇게!!
      System.out.println(hello == hello3); // true
      ```

- 정규식, Pattern
    - 생성 비용이 비싼 객체라서 반복해서 생성하기 보다 캐싱하여 재사용하는 것이 좋다.
      ```java
      public class RomanNumerals {
        static boolean isRomanNumeralSlow(String s) {
          return s.matches("^(?=.)M*(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
        }

        // 재사용해서 성능을 개선
        private static final Pattern ROMAN = Pattern.compile("^(?=.)M*(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");

        static boolean isRomanNumeralFast(String s) {
          return ROMAN.matcher(s).matches();
        }
      }

      -----------------------------------------------------------------------------------------------------------------------
      public class Main {
        public static void main(String[] args) {
          boolean result = false;
          long start = System.nanoTime();
          for (int i = 0; i < 100; i++) {
            result = RomanNumerals.isRomanNumeralSlow("MCMLXXVI");
          }
          long end = System.nanoTime();
          System.out.println(end - start);
          System.out.println(result);
        }
      }
      ```

- 오토 박싱(AutoBoxing)
    - 기본 타입(int)에 상응하는 박싱된 기본 타입(Integer)으로 상호 변환해주는 기술
    - 기본 타입과 박싱된 기본 타입을 섞어 사용하면 변환하는 과정에서 불필요한 객체가 생성될 수 있다.
      ```java
      public class Sum {
        private static long sum() {
          Long sum = 0L; // long 으로 변경하는게 좋음!!!
          for (long i = 0; i < Integer.MAX_VALUE; i++) {
            sum += i; // 오토박싱이 불필요하게 일어남!!! (계속 Long 객체 생성)
          }
          return sum;
        }

        public static void main(String[] args) {
          long start = System.nanoTime();
          long x = sum();
          long end = System.nanoTime();
          System.out.println((end - start) / 1_000_000. + "ms.");
          System.out.println(x);
        }
      }
      ```

- “객체 생성은 비싸니 피하라”는 의미가 아님!!!
### 완벽 공략
- p31. 사용 자제 API (Deprecation)
    - 클라이언트가 사용하지 않길 바라는 코드가 있다면…
    - 사용 자제를 권장하고 대안을 제시하는 방법이 있다.
    - @Deprecated
        - 컴파일 시 경고 메시지를 통해 사용 자제를 권장하는 API라는 것을 클라이언트에 알려줄 수 있다.
        - `forRemoval = true` 옵션을 사용해 삭제될 것이라고 강력하게 알려줄 수 있음 (Java 9부터)
    - @deprecated (주석 내 사용)
        - 문서화(Javadoc)에 사용해 왜 해당 API 사용을 지양하며 그 대신 권장하는 API가 어떤 것인지 표기할 수 있다.
- p32. 정규 표현식
    - 내부적으로 Pattern이 쓰이는 곳
    - String.matches(String regex)
    - String.split(String regex) → 대안, Pattern.compile(regex).split(str);
    - String.replace*(String regex, String replacement) → 대안, Pattern.compile(regex).matcher(str).replaceAll(repl)
    - 참고 - [https://regex101.com/](https://regex101.com/)
- p32. 한번 쓰고 버려져서 가비지 컬렉션 대상이 된다.
    - Mark(더이상 참고가 없는것인지 확인하는 객체), Sweep(필요없는 오브젝트를 실제 힙에서 날리는 역할), Compact(파편화된 공간을 확인해 줄여서 큰 공간을 만드는 역할)
    - Young Generation(Eden, S0, S1), Old Generation
    - Minor GC(Young Generation에서 일어나는 청소작업), Full GC(모든 공간에서)
    - FullGC 알고리즘 - Serial, Parallel(좀더 많은 쓰레드 사용, 자바 8의 기본), CMS(자바 9부터 deprecated 되었음…), G1(자바 11의 기본), ZGC, Shenandoah
    - CG 로직을 보는 관점 - Throughput(애플리케이션 리소스 사용량), Latency (Stop-The-World)(GC 처리가 얼마나 걸리는가), Footprint(GC 알고리즘 때문에 사용하는 메모리 공간이 얼마나 되는가)
    - 참고) [How to choose the best Java garbage collector](https://developers.redhat.com/articles/2021/11/02/how-choose-best-java-garbage-collector)
- p33. 초기화 지연 기법 (아이템 83에서 다룸)
- p34. 방어적 복사 (아이템 50에서 다룸)
