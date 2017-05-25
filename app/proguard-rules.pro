# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\sdk\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-ignorewarning          #忽略警告
-optimizationpasses 5          # 指定代码的压缩级别
-dontusemixedcaseclassnames   # 是否使用大小写混合
-dontskipnonpubliclibraryclasses #不去忽略非公共的库类
-dontoptimize #优化 不优化输入的类文件
-dontpreverify           # 混淆时是否做预校验
-verbose                # 混淆时是否记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法
-keepattributes *Annotation*,Signature,Exceptions #保护注解
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity      # 保持哪些类不被混淆
-keep public class * extends android.app.Application   # 保持哪些类不被混淆
-keep public class * extends android.app.Service       # 保持哪些类不被混淆
-keep public class * extends android.content.BroadcastReceiver  # 保持哪些类不被混淆
-keep public class * extends android.content.ContentProvider    # 保持哪些类不被混淆
-keep public class * extends android.app.backup.BackupAgentHelper # 保持哪些类不被混淆
-keep public class * extends android.preference.Preference        # 保持哪些类不被混淆
-keep public class com.android.vending.licensing.ILicensingService    # 保持哪些类不被混淆

#如果有引用v4包可以添加下面这行
-keep public class * extends android.support.v4.app.Fragment

#如果有引用v4包可以添加下面这行
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

#如果引用了v4或者v7包，可以忽略警告，因为用不到android.support
-dontwarn android.support.**

-dontwarn android.support.**        #如果引用了v4或者v7包

#记录生成的日志数据,gradle build时在本项目根目录输出

-dump class_files.txt                       #apk 包内所有 class 的内部结构

-printseeds seeds.txt                       #未混淆的类和成员

-printusage unused.txt                      #列出从 apk 中删除的代码

-printmapping mapping.txt                   #混淆前后的映射

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
-keepclasseswithmembernames class * {  # 保持 native 方法不被混淆
    native <methods>;
}
-keepclasseswithmembers class * {   # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {# 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity { # 保持自定义控件类不被混淆
    public void *(android.view.View);
}
-keepclassmembers enum * {     # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * {
    public void *ButtonClicked(android.view.View);
}

-keep class * implements android.os.Parcelable { # 保持 Parcelable 不被混淆
    public static final android.os.Parcelable$Creator *;
}
#保持 Serializable 不被混淆
-keepnames class * implements java.io.Serializable


#保持 Serializable 不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#-keep class MyClass                      # 保持自己定义的类不被混淆

#不混淆资源类
 -keepclassmembers class **.R$* {
     public static <fields>;
 }

#-keep class com.uteamtec.dada.activity.**{*;}

#-keep class com.uteamtec.dada.adapter.** {*;}
#-keep class com.uteamtec.dada.fragment.** {*;}

##混淆保护自己项目的部分代码以及引用的第三方jar包library（想混淆去掉"#"）
#-libraryjars libs/umeng-analytics-v5.2.4.jar
#-libraryjars libs/alipaysecsdk.jar
#-libraryjars libs/alipayutdid.jar
#-libraryjars libs/weibosdkcore.jar


###-------- Gson 相关的混淆配置--------
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }


-keepattributes Signature #避免混淆泛型 如果混淆报错建议关掉

#移除log 测试了下没有用还是建议自己定义一个开关控制是否输出日志
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
