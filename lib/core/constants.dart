import 'dart:io';

class AppConstants {
  // Local development / LAN backend addresses
  // Backend binds to 0.0.0.0 inside Docker — reachable at this LAN IP from any device
  static const String lanHost = '192.168.68.78';
  static const int port = 8080;

  /// Returns appropriate base URL depending on runtime platform:
  /// - Android Emulator: 10.0.2.2:8080 (Docker host from emulator)
  /// - Android Device on LAN: 192.168.68.78:8080
  /// - Windows / Desktop: 127.0.0.1:8080
  static String get defaultBaseUrl {
    if (Platform.isAndroid) {
      // Physical device on same Wi-Fi — Docker binds to 0.0.0.0 so any LAN IP works
      return 'http://$lanHost:$port';
    }
    return 'http://127.0.0.1:$port';
  }

  // Current client build defaults (aligned with pubspec.yaml v0.0.0.1+1)
  static const String currentVersionName = '0.0.0.1';
  static const int currentVersionCode = 1;
}

/// Central single source of truth for UI messages and assertions
/// Prevents test drift between production code and test suites
class UpdateStrings {
  static const String statusIdle = 'Ready';
  static const String statusChecking = 'Checking for updates...';
  static const String statusUpToDate = 'App is up to date';
  static const String statusUpdateAvailable = 'New update available';
  static const String statusDownloading = 'Downloading update...';
  static const String statusVerifying = 'Verifying SHA-256 package checksum...';
  static const String statusReadyToInstall = 'Ready to install';
  static const String statusErrorNetwork = 'Failed to connect to update server';
  static const String statusErrorCorrupt = 'Downloaded package failed SHA-256 integrity check';
  static const String statusErrorInstall = 'Unable to launch package installer';
}
