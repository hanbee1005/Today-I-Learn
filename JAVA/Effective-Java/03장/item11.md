# Item11. equals를 재정의하려거든 hashCode도 재정의하라
### 핵심 정리
- hashCode 규약
    - equals 비교에 사용하는 정보가 변경되지 않았다면 hashCode는 매번 같은 값을 리턴해야 한다. (변경되거나 애플리케이션을 다시 실행했다면 달라질 수 있다.)
    - 두 객체에 대한 equals가 같다면 hashCode의 값도 같아야 한다.
        - HashMap에 값을 넣을 때 hashCode 값을 가지고 어느 버킷에 넣을지 확인하고 꺼낼 때도 hashCode를 가지고 해당 버킷을 찾아서  그 버킷에서 객체를 조회
    - 두 객체에 대한 equals가 다르더라도, hashCode의 값은 같을 수 있지만 해시 테이블 성능을 고려해 다른 값을 리턴하는 것이 좋다.
        - hashCode 값이 같으면 해시 충돌!!!
        - hashCode의 버킷은 LinkedList로 구현되어 있고 hashCode 값이 같은 경우 같은 LinkedList에서 equals로 맞는 객체를 조회
        - 알고리즘 효율성이 떨어짐…
- hashCode 구현 방법

    ```java
    @Override
    public int hashCode() {
      int result = Short.hashCode(areaCode);  // 1
      result = 31 * result + Short.hashCode(prefix); // 2
      result = 31 * result + Short.hashCode(lineNum); // 3
      return result;
    }
    ```

    1. 핵심 필드 하나의 값의 해시값을 계산해서 result 값을 초기화한다.
    2. 기본 타입은 Type.hashCode, 참조 타입은 해당 필드의 hashCode, 배열은 모든 원소를 재귀적으로 앞의 로직을 적용하나 Arrays.hashCode

        result = 31 * result + 해당 필드의 hashCode 계산값

    3. result를 리턴한다.
    - 31을 곱하는 것은 홀수여야 하고 해싱을 할 때 충돌이 가장 적게 나는 수이기 때문! (연구 결과)
    - 불변 객체라면 필드로 빼서 저장해둘 수 있다. but. 멀티 스레드 환경에서 스레드 안전성을 고려해야 한다.
    - lombok이 제공하는 `@EqualsAndHashCode` 사용!
### 완벽 공략
- p68. 해시맵 내부의 연결 리스트
    - 내부 구현은 언제든지 바뀔 수 있다.
    - 자바 8에서 해시 충돌 시 성능 개선을 위해 내부적으로 동일한 버킷에 일정 개수 이상의 엔트리가 추가되면, 연결 리스트 대신 이진 트리(red-black tree)를 사용하도록 바뀌었다.
        - [https://dzone.com/articles/hashmap-performance](https://dzone.com/articles/hashmap-performance)
    - 연결 리스트에서 어떤 값을 찾는데 걸리는 시간은?
    - 이진 트리에서 어떤 값을 찾는데 걸리는 시간은?
- p70. 해시 충돌이 더욱 적은 방법을 꼭 써야 한다면…
- p71. 클래스를 스레드 안전하게 만들도록 신경 써야 한다.
    - 멀티 스레드 환경에서 안전한 코드, Thread-safety
    - 가장 안전한 방법은 여러 스레드 간에 공유하는 데이터가 없는 것
    - 공유하는 데이터가 있다면
        - Synchronization

            ```java
            @Override
            public synchronized int hashCode() { ... }

            // Double checked locking
            private volatile int hashCode;  // volatile 은 메인 메모리에 저장된 데이터를 읽어옴 (cach된 데이터 x)

            @Override
            public int hashCode() {
              if (hashCode != 0) {
                return hashCode;
              }

              synchronized (this) {
                int result = hashCode;
                if (result == 0) {
                  ...
                }
                return result;
              }
            }
            ```

        - ThreadLocal
            - 한 스레드 내에서만 공유할 수 있는 데이터
            - `@Transactional` 애노테이션을 사용할 때 활용됨
        - 불변 객체 사용
        - Synchronized 데이터 사용
            - HashMap과 HashTable(스레드 세이프함)의 차이
        - Concurrent 데이터 사용
