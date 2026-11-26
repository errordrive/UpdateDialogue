# No keep rules for the dialogue classes: R8 shrinks and obfuscates
# them (class and method names become short generated names).
# The only stable entry point is the generated bootstrap bridge,
# kept under its generated name in proguard-generated.pro
# (written by tools/generate_secure_link.py on every generate).

# Activity is referenced from the manifest.
-keep class com.error.updatedialogue.MainActivity { *; }

# R8 full-mode safety.
-dontwarn org.json.**
-dontwarn org.jf.dexlib2.**
-dontwarn com.google.common.**
-dontwarn javax.annotation.**
