# 객체 지향 쿼리 언어(JPQL) - 기본
- 가장 단순한 조회 방식
  - `em.find()`
  - 객체 그래프 탐색
- JPA를 사용하면 엔티티 객체를 중심으로 개발하는데 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색해야 함
- 모든 DB 데이터를 객체로 변환하여 검색하는 것은 불가능하기 때문에 애플리케이션에서 필요한 데이터만 DB에서 불러오기 위해 결국 검색 조건이 포함된 SQL이 필요
- JPA는 SQL을 추상화한 JPQL이라는 객체 지향 쿼리 언어 제공
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원
- JPQL은 엔티티 객체를 대상으로 쿼리 / SQL은 데이터베이스 테이블을 대상으로 쿼리

- 검색
    
    ```java
    List<Member> members = em.createQuery(
      "select m from Member m where m.username like '%kim%'"
      , Member.class
    ).getResultList();
    ```
    
- 다른 방법
    - Criteria
        
        ```java
        // Criteria 사용 준비
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Member> query = cb.createQuery(Member.class);
        
        // 루트 클래스 (조회를 시작할 클래스)
        Root<Member> m = query.from(Member.class);
        
        // 쿼리 생성
        CriteriaQuery<Member> cq = query.select(m).where(cb.equals(m.get("username"), "kim"));
        List<Member> members = em.createQuery(cq).getResultList();
        ```
        
        - 문자가 아닌 자바 코드로 JPQL을 작성할 수 있는 JPQL 빌더 역할을 하는 JPA 공식 기능이지만 너무 복잡하고 실용성이 없어서 **QueryDSL** 사용 권장!
    - QueryDSL
        
        ```java
        JPAFactoryQuery query = new JPAFactoryQuery(em);
        QMember m = QMember.member;
        
        List<Member> members = query.selectFrom(m)
                              .where(m.name.like("kim"))
                              .orderBy(m.name.desc())
                              .fetch();
        ```
        
        - 문자가 아닌 자바 코드로 JPQL을 작성할 수 있는 JPQL 빌더 역할을 하고 컴파일 시 문법 오류를 잡을 수 있으며 동적 쿼리 작성이 편리함 → 단순하고 쉬워서 실무 환경에서 추천
    - 네이티브 SQL
        
        ```java
        String query = "SELECT id, age, team_id FROM member WHERE name = 'kim'";
        List<Member> members = em.createNativeQuery(query, Member.class).getResultList();
        ```
        
        - JPA가 직접 SQL을 사용할 수 있도록 제공하는 기능
        - JPQL로 해결할 수 없는 특정 데이터베이스에 의존적인 기능
        - 예) 오라클의 CONNECT BY, 특정 DB만 사용하는 SQL 힌트
    - JDBC 직접 사용, SpringJdbcTemplate 등
        - JPA를 사용하면서 JDBC 커넥션을 직접 사용하거나, 스프링 JdbcTemplate, 마이바티스 등을 함께 사용 가능
        - 단, 영속성 컨텍스트를 적절한 시점에 강제로 플러시 필요
        - 예) JPA를 우회해서 SQL을 실행하기 직전에 영속성 컨텍스트 수동 플러시

### JPQL (Java Persistence Query Language)
- JPQL은 객체 지향 쿼리 언어다. 따라서 테이블을 대상으로 쿼리하는 것이 아니라 엔티티 객체를 대상으로 쿼리한다.
- JPQL은 SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않는다.
- JPQL은 결국 SQL로 변환된다.
- 문법
    - `select m from Member as m where m.age > 18`
    - 엔티티와 속성은 대소문자 구분 o (Member, age)
    - JPQL 키워드는 대소문자 구분 x (SELECT, FROM, where)
    - 엔티티 이름 사용 (테이블 이름 x)
    - 별칭은 필수 (as는 생략 가능)
- 집합과 정렬

    ```java
    select
      COUNT(m),    // 회원 수
      SUM(m.age),  // 나이 합
      AVG(m.age),  // 평균 나이
      MAX(m.age),  // 최대 나이
      MIN(m.age)   // 최소 나이
    from Member m
    ```

    - GROUP BY, HAVING
    - ORDER BY
- TypeQuery : 반환 타입이 명확할 때 사용
- Query : 반환 타입이 명확하지 않을 때 사용

    ```java
    TypeQuery<Member> query = em.createQuery("SELECT m FROM Member m", Member.class);

    Query<Member> query = em.createQuery("SELECT m.username, m.age FROM Member m");
    ```

- 결과 조회 API
    - query.getResultList() : 결과가 하나 이상일 때, 리스트 반환 (결과가 없으면 빈 리스트 반환)
    - query.getSingleResult() : 결과가 정확히 하나, 단일 객체 반환
        - 결과가 없으면 javax.persistence.NoResultException
        - 둘 이상이면 javax.persistence.NonUniqueResultException
- 파라미터 바인딩 - 이름 기준, 위치 기준

    ```java
    SELECT m FROM Member m where m.username=:username
    query.setParameter("username", usernameParam);

    SELECT m FROM Member m where m.username=?1
    query.setParameter(1, usernameParam);
    ```

    - 위치 기준은 안쓰는게 좋음! 이름 기준으로 사용할 것!!!
- 프로젝션(SELECT)
    - SELECT 절에 조회할 대상을 지정하는 것
    - 프로젝션 대상 : 엔티티, 임베디드 타입, 스칼라 타입 (숫자, 문자 등 기본 데이터 타입)
    - `SELECT m FROM Member m` → 엔티티 프로젝션
    - `SELECT m.team FROM Member m` → 엔티티 프로젝션
    - `SELECT m.address FROM Member m` → 임베디드 타입 프로젝션 (소속 엔티티로부터 시작)
    - `SELECT m.username, m.age FROM Member m` → 스칼라 타입 프로젝션
    - DISTINCT 로 중복 제거
    - 여러 값 조회 - `SELECT m.username, m.age FROM Member m`
        - Query 타입으로 조회

            ```java
            List resultList = em.createQuery("select m.username, m.age from Member m");
            Object o = resultList.get(0);
            Object[] result = (Object[]) o;
            System.out.println("username = " + result[0]);
            System.out.println("age = " + result[1]);
            ```

        - Object[] 타입으로 조회

            ```java
            List<Object[]> resultList = em.createQuery("select m.username, m.age from Member m");
            Object[] result = resultList.get(0);
            System.out.println("username = " + result[0]);
            System.out.println("age = " + result[1]);
            ```

        - new 명령어로 조회
            - 단순 값을 DTO로 바로 조회
            - `SELECT new jpabook.jpql.UserDTO(m.username, m.age) FROM Member m`
            - 패키지명을 포함한 전체 클래스명 입력
            - 순서와 타입이 일치하는 생성자 필요
- 페이징
    - setFirstResult(int startPosition) : 조회 시작 위치 (0부터 시작)
    - setMaxResults(int maxResult) : 조회할 데이터 수

    ```java
    List<Member> result = em.createQuery("select m from Member m order by m.age desc", Member.class)
                            .setFirstResult(0)
                            .setMaxResult(10)
                            .getResultList();
    ```

- 조인
    - 내부 조인 : `SELECT m FROM Member m [INNER] JOIN m.team t`
    - 외부 조인 : `SELECT m FROM Member m LEFT [OUTER] JOIN m.team t`
    - 세타 조인 : `SELECT count(m) FROM Member m, Team t where m.username = t.name`
    - ON 절을 활용한 조인
        - 조인 대상 필터링 (ex. 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인)

            ```java
            SELECT m, t FROM Member m LEFT JOIN m.team t ON t.name = 'A'
            ```

        - 연관관계 없는 엔티티 외부 조인 (ex. 회원의 이름과 팀의 이름이 같은 대상 외부 조인)

            ```java
            SELECT m, t FROM Member m LEFT JOIN Team t ON m.username = t.name
            ```

- 서브 쿼리
    - 나이가 평균보다 많은 회원

        ```java
        SELECT m FROM Member m WHERE m.age > (SELECT avg(m2.age) FROM Member m2)
        ```

    - 한 건이라도 주문한 고객

        ```java
        SELECT m FROM Member m WHERE (SELECT count(o) FROM Order o WHERE m = o.member) > 0
        ```

    - 지원 함수
        - [NOT] EXIST (subquery): 서브쿼리에 결과가 존재하면 참
            - {ALL | ANY | SOME} (subquery)
            - ALL 모두 만족하면 참
            - ANY, SOME : 같은 의미로 조건을 하나라도 만족하면 참
        - [NOT] IN (subquery): 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참

        ```java
        // 팀A 소속인 회원
        SELECT m FROM Member m WHERE EXIST (SELECT t FROM m.team WHERE t.name = '팀A')

        // 전체 상품 각각의 재고보다 주문량이 더 많은 주문들
        SELECT o FROM Order o WHERE o.orderAmount > ALL (SELECT p.stockAmount FROM Product p))

        // 어느 팀이든 팀에 소속된 회원
        SELECT m FROM Member m WHERE m.team = ANY (SELECT t FROM Team t)
        ```

    - 한계
        - JPA는 WHERE, HAVING 절에서만 서브 쿼리 사용 가능
        - SELECT 절도 가능 (하이버에니트에서 지원)
        - FROM 절의 서브 쿼리는 현재 JPQL에서는 불가능 → 조인으로 풀어서 해결
- JPQL 타입 표현과 기타식
    - 문자 : ‘Hello’, She”s’
    - 숫자 : 10L(Long), 10D(Double), 10F(Float)
    - Boolean : TRUE, FALSE
    - ENUM : japbook.MemberType.Admin (패키지명 포함)
    - 엔티티 타입 : TYPE(m) = Member (상속 관계에서 사용)

        `SELECT i FROM Item i WHERE type(i) = Book`

    - 기타
        - SQL과 문법이 같은 식
        - EXISTS, IN
        - AND, OR, NOT
        - =, <, <=, >, >=, <>
        - BETWEEN, LIKE, IS (NOT) NULL
- 조건식
    - 기본 CASE 식

        ```java
        select
          case when m.age <= 10 then '학생 요금'
               when m.age >= 60 then '경로 요금'
               else '일반 요금'
          end
        from Member m
        ```

    - 단순 case 식

        ```java
        select
          case t.name
            when '팀A' then '인센티브110%'
            when '팀B' then '인센티브120%'
            else '인센티브105%'
          end
        from Team t
        ```

    - COALESCE : 하나씩 조회해서 null이 아니면 반환

        `SELECT COALESCE(m.username, ‘이름 없는 회원’) FROM Member m`

    - NULLIF : 두 값이 같으면 null 반환, 다르면 첫번째 값 반환

        `SELECT NULLIF(m.username, ‘관리자’) FROM Member m`

- 기본 함수
    - CONCAT
    - SUBSTRING
    - TRIM
    - LOWER, UPPER
    - LENGTH
    - LOCATE
    - ABS, SQRT, MOD
    - SIZE, INDEX(JPA 용도)
    - 사용자 정의 함수
        - 하이버네이트는 사용전 방언에 추가해야 한다.
        - 사용하는 DB 방언을 상속받고 사용자 정의 함수를 등록한다.

            `SELECT function(’group_concat’, i.name) FROM Item i`
                
- 경로 표현식
    - .(점)을 찍어서 객체 그래프를 탐색하는 것

    ```java
    SELECT m.username -- 상태 필드
    FROM Member m
      JOIN m.team t   -- 단일 값 연관 필드
      JOIN m.orders o -- 컬렉션 값 연관 필드
    WHERE t.name = '팀A'
    ```

    - 상태 필드(state field): 단순히 값을 저장하기 위한 필드 (ex. m.username)
        - 경로 탐색의 끝. 탐색 x
    - 연관 필드(association field): 연관관계를 위한 필드
        - 단일 값 연관 필드 : @ManyToOne, @OneToOne 대상이 엔티티 (ex. m.team)
            - 묵시적 내부 조인(inner join) 발생. 탐색 o
        - 컬렉션 값 연관 필드 : @OneToMany, @ManyToMany 대상이 컬렉션 (ex. m.orders)
            - 묵시적 내부 조인(inner join) 발생, 탐색 x
            - From 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능

                `SELECT m FROM Team t JOIN t.members m`

    - 묵시적 조인 x, 명시적 조인을 쓸 것!!!!!!
- 페치 조인 (fetch join)
    - SQL 조인 종류가 아님
    - JPQL에서 성능 최적화를 위해 제공하는 기능
    - 연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회하는 기능
    - join fetch 명령어 사용
    - 페치 조인 ::= `[LEFT [OUTER] | INNER] JOIN FETCH 조인 경로`
    - 엔티티 페치 조인
        - 회원을 조회하면서 연관된 팀도 함께 조회 (SQL 한번에)
        - SQL을 보면 회원 뿐만 아니라 팀(T.*)도 함께 SELECT
        - [JPQL] `SELECT m FROM Member m JOIN FETCH m.team`
        - [SQL] `SELECT m.*, t.* FROM Member m INNER JOIN Team t ON m.team_id=t.id`
        - 지연 로딩 설정이 되어 있어도 fetch join 우선!
    - 컬렉션 페치 조인
        - 일대다 관계, 컬렉션 페치 조인
        - [JPQL] `SELECT t FROM Team t JOIN FETCH t.members WHERE t.name=’팀A’`
        - [SQL] `SELECT t.*, m.* FROM Team t INNER JOIN Member m ON t.id=m.team_id WHERE t.name=’팀A’`
    - 페치 조인과 DISTINCT
        - SQL의 DISTINCT는 중복된 결과를 제거하는 명령
        - JPQL의 DISTINCT는 2가지 기능 제공
            1. SQL에 DISTINCT를 추가
            2. 애플리케이션에서 엔티티 중복 제거
    - 페치 조인과 일반 조인의 차이
        - 일반 조인 실행 시 연관된 엔티티를 함께 조회하지 않음
        - JPQL은 결과를 반환할 때 연관관계 고려 x
        - 단지 SELECT 절에 지정한 엔티티만 조회할 뿐
        - 페치 조인을 사용할 때만 연관된 엔티티도 함께 조회 (즉시 로딩)
        - 페치 조인은 객체 그래프를 SQL 한번에 조회하는 개념
    - 페치 조인의 특징과 한계
        - 페치 조인 대상에는 별칭을 줄 수 없음
            - 하이버네이트는 가능하지만 가급적 사용하지 않는 것이 좋음
        - 둘 이상의 컬렉션은 페치 조인 할 수 없다.
        - 컬렉션을 페치 조인하면 페이징 API(setFirstResult, setMaxResults)를 사용할 수 없다.
            - 일대일, 다대일 같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
            - 하이버네이트는 경고 로그를 남기고 메모리에서 페이징(매우 위험)
        - grobal setting 으로 hibernate.default_batch_fetch_size를 1000 이하로 보통 지정
        - 연관된 엔티티들을 SQL 한번으로 조회 - 성능 최적화
        - 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선함
            - @OneToMany(fetch = FetchType.LAZY) // 글로벌 로딩 전략
        - 실무에서 글로벌 로딩 전략은 모두 지연 로딩으로
        - 최적화가 필요한 곳은 페치 조인 적용
- 다형성 쿼리
    - Type
        - 조회 대상을 특정 자식으로 한정지을 수 있음
        - 예) Item 중에 Book, Movie 를 조회해라
        - [JPQL] `SELECT i FROM Item i WHERE TYPE(i) IN (Book, Movie)`
        - [SQL] `SELECT i.* FROM Item i WHERE i.DTYPE IN (’B’, ‘M’)`
    - TREAT (JPA 2.1)
        - 자바의 타입 캐스팅과 유사
        - 상속 구조에서 부모 타입을 특정 자식 타입으로 다룰 때 사용
        - FROM, WHERE, SELECT(하이버네이트 지원) 사용
        - [JPQL] `SELECT i FROM Item i WHERE TREAT(i as Book).author = ‘kim’`
        - [SQL] `SELECT i.* FROM Item i WHERE i.DTYPE = ‘B’ AND i.author = ‘kim’`
- 엔티티 직접 사용
    - 기본 키 값
        - JPQL에서 엔티티를 직접 사용하면 SQL에서 해당 엔티티의 기본 키 타입을 사용
        - [JPQL] `SELECT count(m.id) FROM Member m` // 엔티티의 id를 사용
        - [JPQL] `SELECT count(m) FROM Member m` // 엔티티를 직접 사용
        - [SQL] `SELECT COUNT(m.id) FROM Member m`
        - 엔티티를 파라미터로 사용
        - [JPQL] `SELECT m FROM Member m WHERE m = :member`
        - [SQL] `SELECT m.* FROM Member m WHERE m.id = ?`
    - 외래키 값
        - [JPQL] `SELECT m FROM Member m WHERE m.team = :team`
        - [SQL] `SELECT m.* FROM Member m WHERE m.team_id = ?`
- Named 쿼리
    - 미리 정의해서 이름을 부여해두고 사용하는 JPQL
    - 정적 쿼리
    - 애노테이션, XML에 정의
    - 애플리케이션 로딩 시점에 초기화 후 재사용
    - 애플리케이션 로딩 시점에 쿼리를 검증

    ```java
    @Entity
    @NamedQuery(
      name = "Member.findByUsername",
      query = "select m from Member m where m.username = :username"
    )
    public class Member {
      ...
    }

    -------------

    List<Member> members = em.createNamedQuery("Member.findByUsername", Member.class)
                              .setParameter("username1", "회원1")
                              .getResultList();
    ```

    - XML이 우선권을 가짐
    - Spring Data JPA 에서 `@Query` 애노테이션이 바로 Named 쿼리!
- 벌크 연산
    - 쿼리 한번으로 여러 테이블 로우 변경(엔티티)
    - executeUpdate()의 결과는 영향받는 엔티티 수 반환
    - UPDATE, DELETE 지원
    - INSERT (insert into … select, 하이버네이트 지원)

    ```java
    String sqlString = "UPDATE Product p "
                        + "SET p.price = p.price * 1.1 "
                        + "WHERE p.stockAmount < :stockAmount";

    int resultCount = em.createQuery(sqlString)
                        .setParameter("stockAmount", 10)
                        .executeUpdate();
    ```

    - 주의
        - 벌크 연산은 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리
        - 벌크 연산을 먼저 실행하거나 벌크 연산 수행 후 영속성 컨텍스트 초기화
    - Spring Data JPA 에서 벌크 연산 날릴때 영속성 컨텍스트 초기화 내용이 있음! (확인 필요!)

        ```java
        @Modifying
        @Query("update User u set u.name = :name where u.age = :age"
        int setFixedNameFor(String name, int age);
        ```
