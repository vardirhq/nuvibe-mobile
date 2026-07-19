# Keep Room entities/DAOs metadata generated at build time.
-keepclassmembers class dev.nuvibe.player.data.local.** { *; }

# Media3 uses reflection for some session callbacks.
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
