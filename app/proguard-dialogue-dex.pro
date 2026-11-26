# Standalone dialogue dex safety.
# R8 can otherwise drop trivial anonymous-class constructors while leaving
# invoke-direct <init> references behind; keeping constructors prevents
# NoSuchMethodError/VerifyError when the dex is merged into a host app.
-keepclassmembers,allowobfuscation class * {
    <init>(...);
}
-repackageclasses 'a'
