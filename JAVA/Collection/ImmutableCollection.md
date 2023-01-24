# 불변 컬렉션(Immutable Collection)
Java 9 부터 불변 컬렉션을 생성할 수 있는 정적 팩터리 메서드가 추가되었습니다.

- **정의**
  + 불변(Immutable) 컬렉션(Collection)은 아이템 추가, 수정, 제거가 불가능한 컬렉션으로 신규 아이템을 추가하거나 기존 아이템을 수정 또는 제거하려고 하면  ```java.lang.UnsupportedOperationException```이 발생합니다. 
  + 컬렉션이 생성된 후에 변경되기를 원하지 않는 경우에 사용하며, 의도치 않은 컬렉션 변경을 예방에 도움이 됩니다.

- **불변 컬렉션을 만드는 번거로움**
  + Java8까지는 불변(Immutable) 리스트를 만들기 위해서는 가변(mutalbe) 리스트를 먼저 만들고 Collections 클래스의 ```unmodifiableList()``` 정적 메서드를 사용하여 불변 리스트로 변환시켜줘야 했었습니다.
    ```java
    List<String> fruits = new ArrayList<>();

    fruits.add("Apple");
    fruits.add("Banana");
    fruits.add("Cherry");
    fruits = Collections.unmodifiableList(fruits);

    fruits.add("Kiwi"); // UnsupportedOperationException
    ```

- **Java 8 까지의 대체 방안**
  + Arrays API
    ```java
    List<String> fruits = Arrays.asList("Apple", "Banana", "Cherry");
    ```
  + Stream API
    ```java
    List<String> fruits = Stream
    	.of("Apple", "Banana", "Cherry")
    	.collect(collectingAndThen(toList(), Collections::unmodifiableList));
    ```
  + Guava 라이브러리
    ```java
    import com.google.common.collect.ImmutableList;

    List<String> fruits = ImmutableList.of("Apple", "Banana", "Cherry");

    fruits.add("Lemon"); // UnsupportedOperationException
    ```
- **Java 9 에 도입된 메서드**
  + List
    ```java
    List<String> fruits = List.of("Apple", "Banana", "Cherry"); // [Apple, Banana, Cherry]
    fruits.add("Lemon"); // UnsupportedOperationException
    ```
  + Set
    ```java
    Set<String> fruits1 = Set.of("Apple", "Banana", "Cherry"); // [Banana, Apple, Cherry]
    
    Set<String> fruits2 = Set.of("Apple", "Banana", "Cherry", "Apple"); // IllegalArgumentException
    ```
  + Map 
    ```java
    Map<Integer, String> fruits1 = Map.of(1, "Apple", 2, "Banana", 3, "Cherry"); // {1=Apple, 2=Banana, 3=Cherry}
    fruits1.put(4, "Lemon"); // UnsupportedOperationException
    
    Map<Integer, String> fruits2 = Map.ofEntries(Map.entry(1, "Apple"), Map.entry(2, "Banana"), Map.entry(3, "Cherry"));
    ```
