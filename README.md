# - COMMON-CORE
공통 ErrorType / response 데이터 / 각종 코어 Util 등을 처리하는 모듈 
``` GRADLE  
response 데이터 예시
{
  "code": 100,
  "message": null,
  "resultData": true
}
```


## Initial settings
해당 모듈을 정상적으로 사용하려면 다음과 같은 설정을 해야 함 - 추후작성
1.  메인 프로젝트의 `build.gradle` 파일에 모듈 추가  
	``` GRADLE  
	// build.gradle

	// ...
	dependencies {
		// ...
		// dependencies에 -common-util 프로젝트를 추가함으로써 해당 모듈을 사용 가능함
		implementation project(':common-util')
		// ...
	}
	// ...
	```


