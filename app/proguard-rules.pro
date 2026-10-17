# StandBy Pro release rules.
# Compose, Coil, DataStore and AndroidX ship their own consumer rules; services and
# activities are kept through the manifest. Only app-specific extras live here.

# Keep enum names stable: settings persist enum constants by name.
-keepclassmembers enum com.standbypro.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Readable crash traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
