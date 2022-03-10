# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# typical library config from http://proguard.sourceforge.net/manual/examples.html#library
-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated, SourceFile,LineNumberTable,*Annotation*,EnclosingMetho

# These methods are inserted by the javac compiler and the jikes compiler respectively, in JDK 1.2 and older, to implement the .class construct.
# ProGuard will automatically detect them and deal with them, even when their names have been obfuscated. However, other obfuscators may rely on the original method names.
# It may therefore be helpful to preserve them, in case these other obfuscators are ever used for further obfuscation of the library.
-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

# need to preserve some methods for enum classes
-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# skip public classes + its protected and public methods, fields etc.
-keep public class * {
    public protected *;
}

# keep @EditableProperty methods
-keepclassmembers class ** {
  @com.scitrader.finance.edit.annotations.EditableProperty public *;
}
