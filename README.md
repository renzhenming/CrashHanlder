# CrashHanlder
抓取异常信息的小工具

一行代码，捕获未经catch的异常信息

1.Add it in your root build.gradle at the end of repositories:

allprojects {
    repositories {
	maven { url 'https://jitpack.io' }
    }
}	

2.Add the dependency:

dependencies {
    implementation 'com.github.renzhenming:CrashHanlder:1.0.0'
}

3.在应用启动后注册，建议在Application中
```
UncaughtCrashHandler.getInstance().init(this);
```
