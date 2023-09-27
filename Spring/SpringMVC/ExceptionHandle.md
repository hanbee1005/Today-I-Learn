# Exception Handler Sample

## @RestControllerAdvice 와 @ExceptionHandler

## 404 에러 처리
- 기본적으로 404 에러 발생 시 응답 데이터는 다음과 같습니다.
  ```json
  {
    "timestamp": "2023-09-26T22:37:35.444+00:00",
    "status": 404,
    "error": "Not Found",
    "path": "/404"
  }
  ```
- Spring Boot 는 404 에러를 다음 부분에서 잡아서 미리 처리합니다.
  + org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController#error
  ```java
  @RequestMapping
  public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
      HttpStatus status = getStatus(request);
      if (status == HttpStatus.NO_CONTENT) {
          return new ResponseEntity<>(status);
      }
      Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
      return new ResponseEntity<>(body, status);
  }
  ```
  - 위 부분에서 처리하지 않고 커스텀 하는 부분은 다음과 같습니다.
  - 먼저 application.yml 에 다음과 같음 설정을 추가합니다.
    ```yaml
    spring:
        mvc:
          throw-exception-if-no-handler-found: true
        web:
          resources:
            add-mappings: false
    ```
    + throw-exception-if-no-handler-found 를 true 로 설정하게 되면
      - dispatcher servlet 에서 요청에 대한 핸들러를 찾을 때 요청을 처리할 수 있는 핸들러가 없다면 즉, mappedHandler 가 null 이라면 NoHandlerFoundException 예외를 던집니다.
      1. 요청에 대한 핸들러를 찾는 코드
        - org.springframework.web.servlet.DispatcherServlet#doDispatch
          ```java
          mappedHandler = getHandler(processedRequest);
          if (mappedHandler == null) {
            noHandlerFound(processedRequest, response);
            return;
          }
          ```
        - getHandler 함수에 의해 핸들러를 찾습니다. 이 함수는 해당되는 핸들러가 없으면 null 을 반환합니다.
        - 요청을 처리할 수 있는 핸들러가 없다면 noHandlerFound 메서드를 호출합니다.
      2. 핸들러가 없을 때 어떻게 처리할지 결정하는 함수
        - org.springframework.web.servlet.DispatcherServlet#noHandlerFound
        ```java
        protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
          if (pageNotFoundLogger.isWarnEnabled()) {
           pageNotFoundLogger.warn("No mapping for " + request.getMethod() + " " + getRequestUri(request));
          }
          if (this.throwExceptionIfNoHandlerFound) {
           throw new NoHandlerFoundException(request.getMethod(), getRequestUri(request),
           new ServletServerHttpRequest(request).getHeaders());
          }
          else {
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
          }
        }
        ```
        - throwExceptionIfNoHandlerFound 가 true 라면 NoHandlerFoundException 예외를 던집니다.
        - throwExceptionIfNoHandlerFound 값은 프로퍼티파일에서 설정할 수 있습니다.(다른방법으로도 가능합니다.)
        - throwExceptionIfNoHandlerFound 가 false 라면 최종적으로 BasicErrorController 에서 응답을 반환합니다.
    + add-mappings 를 false 로 설정하게 되면
      - 기존 핸들러를 찾을 때 RequestMappingHandlerMapping 으로 확인하여 없는 경우 위에서와 같이 에러를 발생
      - 이후에 다시 SimpleUrlHandlerMapping 으로 정적 url 인지 확인을 하게 되어 앞에서 던진 에러가 처리되지 않음
      - 따라서 add-mappings 를 false 로 설정해 SimpleUrlHandlerMapping 이 실행되지 않도록 처리해야 합니다.
    + 그리고 나서 @RestControllerAdvice 와 @ExceptionHandler 를 사용하여 처리하면 됩니다.
      - 구체적으로 명시하여 처리한 코드
        ```java
        @RestControllerAdvice
        public class NotFoundHandler {
  
          @ResponseStatus(HttpStatus.NOT_FOUND)
          @ExceptionHandler(NoHandlerFoundException.class)
          public ErrorResponse handle404(NoHandlerFoundException exception) {
            String message = "존재하지 않는 URL입니다. : " +  exception.getRequestURL();
            return new ErrorResponse(HttpStatus.NOT_FOUND, message);
          }
        }
        ```
      - 4xx 대 에러를 모두 그대로 반환할 수 있게 처리한 코드
        ```java
        @Slf4j
        @RestControllerAdvice
        public class RestExceptionHandler {

          @ExceptionHandler(Exception.class)
          public ResponseEntity<CommonResponseDto<String>> exceptionHandler(Exception e) throws Exception {
            ResponseEntity<CommonResponseDto<String>> result = null;

            if (e instanceof ErrorResponse errorResponse) {
              if (errorResponse.getStatusCode().is4xxClientError()) {
                throw e;
              }
            } else {
              log.error("The exception is occurred. The exception message is {} " , e.getMessage());
              result = ResponseUtil.createFailResponse(StatusCodeConstants.INTERNAL_SERVER_ERROR, null, HttpStatus.OK);
            }

            return result;
          }
        }
        ```
- 참고: https://velog.io/@ydh6226/Spring-404-NotFound-Response-Custom