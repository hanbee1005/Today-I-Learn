# Back to the BASIC
해당 디렉토리는 기본이 되는 언어, 프레임워크 등에 대해 다시 공부한 내용을 정리하는 임시 디렉토리입니다. 기본부터 탄탄하게! 🧱

- 기간(3개월): 2024-03 ~ 2024-05

## Part 1. Java
### 변수
- Primitive Type / Reference Type
  + 기본형(Primitive Type) : ```int```, ```long```, ```double```, ```boolean```처럼 변수에 사용할 값을 직접 넣을 수 있는 데이터 타입
  + 참조형(Reference Type) : 객체(```Student```), 배열(```int[]```)와 같이 데이터에 접근하기 위한 참조(주소)를 저장하는 데이터 타입
  + ***String*** : String은 클래스로 참조형이지만 기본형처럼 문자값을 바로 대입
- 계산
  + 기본형은 연산이 가능하지만 참조형은 직접 연산이 불가능
- 대입
  + 자바에서 대입은 **항상 변수에 값을 복사해서 저장**하는 것!!!
  + 기본형이면 변수에 들어있는 실제 값을 복사해서 대입
    ```java
    int a = 10;
    int b = a;
    // 결과 a = 10, b = 10
    ```
  + 참조형이면 변수에 들어있는 참조값을 복사해서 대입
    ```java
    Student s1 = new Student();
    Student s2 = s1;
    // 결과 s1 = x001, s2 = x001 -> 객체는 하나 
    ```
- 메서드 호출
  + 기본형은 메서드 내부에서 파라미터의 값을 변경해도 호출자의 변수 값에는 영향이 없음
  + 참조형은 메서드 내부에서 파라미터로 전달된 객체의 멤버 변수를 변경하면 호출자의 객체도 변경됨
- 초기화
  + 멤버 변수(필드) : 자동 초기화, 인스턴스 생성 시 기본값(숫자(int)는 0, boolean은 false, 참조형은 null)으로 초기화되거나 개발자가 수동으로 초기화할 수 있음
  + 지역 변수 : 수동 초기화, 메서드 내에서 사용되는 지역 변수는 반드시 수동으로 초기화 후 사용해야 함
- null
  + 참조형 변수에 아직 가리키는 대상이 없는 경우
  + 아무도 참조하지 않는 대상은 GC에 의해 정리됨
  + ```NullPointerException```
    - null 에 .(dot)을 찍었을 때 발생

### 메모리 구조
- 스택 / 큐
- static
- JVM
- GC

### 클래스
- 왜 필요한가?
  + 하나의 개념을 만들고 연관된 속성을 같이 관리하기 위해 사용
- 용어
  + 클래스 : 사용자 정의 타입을 만들기 위한 설계도
  + 인스턴스(= 객체) : 실제 메모리에 만들어진 실체
- 구성
  + 생성자
  + 멤버변수, 필드
  + 메서드
- 상속
- 다형성
- 캡슐화

### 객체지향
- SOLID
  + SRP(Single Responsibility Principle: 단일 책임 원칙)
  + OCP(Open Close Principle: 개방 폐쇄 원칙)
  + LSP(Liskov Substitution Principle: 리스코프 치환 원칙)
  + ISP(Interface Segregation Principle: 인터페이스 분리 원칙)
  + DIP(Dependency Inversion Principle: 의존성 역전의 원칙)

### 참고
- [김영한의 실전 자바 - 기본편](https://www.inflearn.com/course/%EA%B9%80%EC%98%81%ED%95%9C%EC%9D%98-%EC%8B%A4%EC%A0%84-%EC%9E%90%EB%B0%94-%EA%B8%B0%EB%B3%B8%ED%8E%B8/dashboard)

## Part 2. Spring Boot
### 빈(Bean)
- 라이프사이클(Life Cycle)

### DI(Dependency Injection)

### AOP

