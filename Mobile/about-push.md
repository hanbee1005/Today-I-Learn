# Push 관련

## appToken(푸시 토큰) 발급
### iOS (APNs: Apple Push Notification service)
- 공식 문서
  + [Apple: Registering Your App with APNs](https://developer.apple.com/documentation/usernotifications/registering-your-app-with-apns)
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
### Android (FCM)