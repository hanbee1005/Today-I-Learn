# Item02. 생성자에 매개변수가 많다면 빌더를 고려하라
### 핵심 정리
- 정적 팩터리와 생성자에 선택적 매개변수가 많을 때 고려할 수 있는 방안
    - 대안 1) 점층적 생성자 패턴 또는 생성자 체이닝
        - 매개변수가 늘어나면 클라이언트 코드를 작성하거나 읽기 어렵다.
    - 대안 2) 자바빈즈 패턴
        - 완전한 객체를 만들려면 메서드를 여러번 호출해야 한다. (일관성이 무너진 상태가 될 수 있다.)
        - 클래스를 불변으로 만들 수 없다.
- 권장하는 방법: 빌더 패턴
    - 플루언트 API 또는 메서드 체이닝을 한다.
    - 계층적으로 설계된 클래스와 함께 사용하기 좋다.
    - 점층적 생성자보다 클라이언트 코드를 읽고 쓰기가 훨씬 간결하고, 자바빈즈보다 훨씬 안전하다.
    - lombok @Builder
    - Annotation Processor
    - 단점
        - AllArgumentConstructor 가 노출 → private 설정으로 대처
        - 필수값 지정이 불가
    - 계층형 빌더
        - self() 구조… 확인!
### 완벽 공략
- 자바빈스, 게터, 세터
    - (주로 GUI에서) 재사용 가능한 소프트웨어 컴포넌트
    - java.beans 패키지 안에 있는 모든 것
    - 그 중에서도 자바빈이 지켜야 할 규약
        - argument 없는 기본 생성자 → 리플랙션 시 객체를 생성하기 좋게 하기 위해 규약을 정함, 반드시 명시해야 따르는 것은 아님! (아무것도 안썼을 때 자동으로 생기는 것도 포함)
        - getter와 setter 메소드 이름 규약 → boolean은 getXXX 대신 isXXX
        - Serializable 인터페이스 구현 → 객체의 값 그대로 직렬화를 통해 저장했다가 다시 쓰기 위한 의도가 있음
    - 하지만 실제로 오늘날 자바빈 스팩 중에서도 getter와 setter가 주로 쓰는 이유?
        - JPA나 스프링과 같은 여러 프레임워크에서 리플랙션을 통해 특정 객체의 값을 조회하거나 설정하기 때문에 일관적인 방식인 getter, setter 규약을 사용
    - 자바 17 레코드 추가로 공부해보기! → 어떤 형태로 getter, setter를 만들어주는지 확인!
- 객체 얼리기 (freezing)
    - 임의의 객체를 불변 객체로 만들어주는 기능
    - Object.freeze()에 전달한 객체는 그 뒤로 변경될 수 없다.
        - 새 프로퍼티를 추가하지 못함
        - 기존 프로퍼티를 제거하지 못함
        - 기존 프로퍼티 값을 변경하지 못함
        - 프로토타입을 변경하지 못함
    - strict 모드에서만 동작함
    - 비슷한 류의 function으로 Object.seal()과 Object.preventExtensions()가 있다.
        - Object.seal() → 추가, 삭제는 안되고 수정은 가능
        - Object.preventExtensions → 추가만 안되고 나머지는 가능

    ```jsx
    var keesun = {
      'name': 'keesun',
      'age': 40
    };

    delete keesun.name;

    console.log(keesun.name);  // undefined!!!

    keesun.kids = ['서연'];
    keesun.kids.put('지연');

    const someone = {
      'name': 'someone'
    }

    someone.name = 'newone'

    keesun = {
      'name': 'newsun'
    }

    // const는 객체 자체를 변경하는 것은 불가
    someone = {
      'name': 'new'
    }
    ```

    ```jsx
    'use strict';

    const keesun = {
      'name': 'keesun'
    }

    Object.freeze(keesun);

    keesun.kids = ['서연'];  // 에러 발생!!!!!!!
    ```

    - 자바에서 적용하려면
        - 객체가 freezing이 되었다는 마커가 있어야 함
        - 객체를 변경하려는 setter와 같은 부분에 이 flag를 확인하는 부분이 필요
        - 하지만 가변 객체인데 어떤 경우에 불변 객체가 된 것인지 확인하는게 너무 어려움 → 따라서 이런 경우를 자바에서 본 적은 없고 널리 쓰이는 기능은 아닐 것으로 예상
    - mutable, immutable 개념이 더 중요!
        - `final` 이 아닌 필드들은 지속적으로 객체 생성 후에도 변경이 가능! → mutable
        - `final` 을 붙이는 경우에는 값을 새로 할당할 수 없지만 `List<String>` 등과 같이 레퍼런스를 가지는 경우 내부 값은 변경할 수 있음 → immutable
        - 상속을 막기 위해 클래스 앞에도 `final`을 붙일 수 있음
- 빌더 패턴
    - 동일한 프로세스를 거쳐 다양한 구성의 인스턴스를 만드는 방법
    - 복잡한 객체를 만드는 프로세스를 독립적으로 분리할 수 있다.
        - 코드를 줄일 수 있고
        - 단일 책임 원칙을 적용해서 객체를 생성하는 과정을 별도의 클래스로 분리할 수 있는 장점
        - Builder 인터페이스, 그의 구현체인 ConcreteBuilder
        - Director는 빌더를 통해 만들어지는 객체들 중에 자주 만들어지는 형태의 객체가 있다면 그 객체를 Director에 위임을 해서 Director가 빌더를 사용해서 만들 수 있게 하는 구조 → 반드시 있어야 빌드 패턴인 것은 아님!!!
        - 예제 코드 확인해보기!!! `main → TourDirector → TourPlanBuilder → TourPlan`
- IllegalArgumentException
    - 잘못된 인자를 넘겨 받았을 때 사용할 수 있는 기본 런타임 예외
    - 어떤 인자에서 에러가 발생했는지 넘기는 것이 유용
        - `throw new IllegalArgumentException(”관련 메시지 : “ + argument_value);`
    - checked exception vs unchecked exception
        - checked exception
            - 예외를 다시 던지거나
            - try-catch로 예외를 잡아 처리가 필요!
            - 복구가 가능한 상황에서 던짐
        - unchecked exception
            - 복구가 불가능한 상황
        - 트랜잭션과는 아무 상관이 없음!!!!!!! 별도로 생각해야 함! (백기선 java 로 영상 찾아서 확인…!)
    - 간혹 메서드 선언부에 unchecked exception을 선언하는 이유?
        - `public void setXXX(String s) throws IllegalArgumentException { … }`
        - 클라이언트에 명시적으로 알려주기 위한 용도
        - 주로 선언하지 않는 경우는 모든 것들을 선언하는게 가독성이 떨어지기 때문에 unchecked exception은 잘 선언하지 않음. (checked exception 은 보통 명시)
    - checked exception은 왜 사용할까?
        - 해당 에러가 발생했을 때 클라이언트가 추가 액션을 해주길 바랄 때 강제성을 부여하려고 사용!
    - 과제1) 자바의 모든 RuntimeException 클래스 이름 한번씩 읽어보기
        - 기본 런타임 에러는 먼저 사용하고 없다면 만들어서 쓰는 것을 권장!!
    - 과제2) 이 링크의 글을 꼭 읽으세요!
- 재귀적인 타입 한정을 이용하는 제네릭 타입
- 가변인수(varargs) 매개변수를 여러 개 사용할 수 있다.
    - 여러 인자를 받을 수 있는 가변적인 argument (Var + args)

        ```java
        public void printNumbers(int... numbers) {
          System.out.println(numbers.getClass().getCanonicalName());  // 실제 어떤 타입일지 -> 배열
          System.out.println(numbers.getClass().getComponentType());  // 배열 내 포함된 타입이 뭔지
          Arrays.stream(numbers).forEach(System.out::println);
        }
        ```

    - 가변 인수는 메서드에 오직 하나만 선언할 수 있다.
    - 가변 인수는 메서드의 가장 마지막 매개변수가 되어야 한다.
    - 빌더를 이용하면 가변인수 매개변수를 여러 개 사용할 수 있다. 각각을 적절한 메서드로 나눠 선언하면 된다.
    - Heap polution 도 알아보기!
