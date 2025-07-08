# Push 관련

## appToken(푸시 토큰) 발급
### iOS (APNs: Apple Push Notification service)
- 공식 문서
  + [Apple: Registering Your App with APNs](https://developer.apple.com/documentation/usernotifications/registering-your-app-with-apns)
- ✅ appToken이란?
  + APNs가 발급한 device token (hex string)
  + `앱 번들 ID + 디바이스 식별 정보 + APNs 인증서` 를 기반으로 생성
  + 즉, 같은 앱(같은 Bundle ID)을 동일한 디바이스에 설치하면 deviceToken이 바뀌지 않을 수 있습니다.
  + 그러나 실제 앱 삭제 후 다시 설치하는 경우, registerForRemoteNotifications() 호출 시 내부적으로 새롭게 token을 발급받는 동작을 할 수도 있고 안 할 수도 있습니다. Apple은 이 동작이 불확정적이라고 명시합니다.
- 앱 등록 및 appToken 발급 흐름
  + 앱에서 사용자 알림 권한 요청
    	```swift
    	UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            // granted 여부 확인
        }
    	```
  + 디바이스 토큰 등록 요청
        ```swift
    	DispatchQueue.main.async {
            UIApplication.shared.registerForRemoteNotifications()
        }
    	```
  + APNs에서 발급한 디바이스 토큰(appToken) 수신
    - 앱 델리게이트에서 콜백 메서드로 수신됨:
        ```swift
    	func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
            let tokenString = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
            // 이 토큰을 서버에 전달 (appToken)
        }
    	```
  + 서버에 전달
    - 위에서 수신한 `tokenString`을 앱 서버에 전달하여 해당 디바이스에 푸시 발송 가능하도록 설정합니다.

### Android (FCM: Firebase Cloud Messaging)
- 공식 문서
  + [Firebase: FCM - Client Setup](https://firebase.google.com/docs/cloud-messaging/android/client)
- ✅ appToken이란?
  + FCM이 발급한 registration token (JWT-like string)
  + `앱 패키지명 + Firebase 앱 인스턴스 ID + 디바이스 정보` 를 기반으로 생성
  + `google-services.json`의 project_number, mobilesdk_app_id 등을 기반으로 Firebase 서버에 등록 요청
  + 앱이 삭제되어도, Firebase Instance ID를 캐시로 유지하고 있으면 같은 토큰을 받을 수도 있습니다.
  + 하지만 일반적으로 앱 삭제 → 재설치 시 FirebaseInstanceId가 새로 발급되고, 이에 따라 토큰도 변경됩니다.
- 앱 등록 및 appToken 발급 흐름
  + Firebase SDK 설정
    - Firebase 프로젝트 생성 → google-services.json 다운로드 → app/에 추가
    - build.gradle에 Firebase 및 FCM 종속성 추가
      ```groovy
      implementation 'com.google.firebase:firebase-messaging:24.0.0'
      ```
  + FCM 토큰(appToken) 발급 요청
    ```kotlin
    FirebaseMessaging.getInstance().token
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val token = task.result
            // 이 토큰을 서버에 전달 (appToken)
        }
    }
    ```
  + 서버에 전달
  	- 위에서 받은 token을 서버에 저장하여, 해당 디바이스로 푸시 알림 전송 가능

### 정리
✅ appToken (디바이스 푸시 토큰)의 정의
- APNs (iOS): deviceToken
- FCM (Android): registrationToken                  

이 토큰은 푸시 메시지를 수신할 디바이스+앱 조합을 식별하기 위한 고유 값입니다.
APNs 또는 FCM 서버는 해당 앱 인스턴스를 등록하고, 이를 통해 푸시 메시지를 보낼 수 있는 주소 같은 역할을 하는 appToken을 발급합니다.

✅ 발급 기준 (기본적으로는 조합 개념)
| 항목           | iOS (APNs)                                         | Android (FCM)                                               |
|----------------|----------------------------------------------------|-------------------------------------------------------------|
| 앱 식별자      | `Bundle ID`                                        | `Package name`                                              |
| 디바이스 정보  | Apple 내부 디바이스 ID 기반 (비공개)               | Firebase Installation ID (앱 인스턴스 식별자)               |
| 인증 정보      | APNs 인증서 또는 키 (서버 연동 시 사용)            | Firebase 프로젝트 키 (`google-services.json` 내 포함됨)     |
| 발급 시점      | `registerForRemoteNotifications()` 호출 시         | `FirebaseMessaging.getInstance().token` 호출 시             |

즉, "앱 식별자 + 디바이스 식별 정보 + 인증 정보" 조합으로 생성된다고 이해하시면 거의 정확합니다.

✅ 토큰(appToken)이 변경되는 주요 조건
| 변경 요소              | 토큰 변경 가능성             | 설명                                                                 |
|------------------------|-------------------------------|----------------------------------------------------------------------|
| 앱을 삭제 후 재설치    | 높음 (Android), 중간~높음 (iOS) | 앱 인스턴스 ID 또는 디바이스 토큰이 갱신될 수 있음                   |
| 디바이스 초기화        | 무조건 바뀜                   | 하드웨어 정보, 앱 설치 정보 초기화                                   |
| Firebase 프로젝트 변경 | 무조건 바뀜 (Android)         | `google-services.json`이 바뀌면 프로젝트가 달라지므로 재발급 필요     |
| Bundle ID / 패키지명 변경 | 무조건 바뀜                | 앱 식별자가 바뀌므로 새로운 앱으로 간주되어 새로운 토큰 발급          |
| 사용자 계정 변경       | 변경 없음                     | 토큰은 계정과 무관하게 앱+디바이스 단위로 발급됨                     |







