# Item09. try-finally 보다는 try-with-resources를 사용하라
### 핵심 정리
- try-finally는 더 이상 최선의 방법이 아니다. (자바 7부터)
- try-with-resources 를 사용하면 코드가 더 짧고 분명하다.
- 만들어지는 예외 정보도 훨씬 유용하다.
    - try-finally 의 경우 가장 마지막에 발생한 에러만 보일 수 있기 때문에 처음에 뭐가 발생했는지 알 수 없는 경우가 생김
    - try-with-resources 는 처음 발생하는 에러가 보이고 후속으로 발생하는 에러도 같이 보임!

```java
static String firstLineOfFile(String path) throws IOException {
  try(BufferedReader br = new BufferedReader(new FileReader(path));
      // 다른 리소스 더 추가해도 됨
  ) {
    return br.readLine();
  }
}
```
    
### 완벽 공략
- p48. 자바 퍼즐러(책) 예외 처리 코드의 실수

    ```java
    try {
      ...
    } finally {
      try {
        output.close();  // IOException 이 아닌 다른게 발생하면 아래는 그대로 무시됨... 안전하지 않음!
      } catch(IOException e) {
      }

      try {
        input.close();
      } catch(IOException e) {
      }
    }
    ```

- p49. try-with-resources 바이트 코드
    - 중첩된 try-catch를 사용하고 finally는 쓰지 않고 있으며 에러가 발생하면 그대로 던지고 후속 에러는 addSuppressed 로 추가
