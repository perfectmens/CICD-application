import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:open_filex/open_filex.dart';
import '../../core/constants.dart';
import '../../data/repositories/update_repository.dart';
import '../../domain/models/greeting.dart';
import '../../domain/models/server_health.dart';
import '../../domain/models/update_info.dart';
import 'update_state.dart';

class HomeViewModel extends ChangeNotifier {
  final UpdateRepository _repository;

  // Client version tracking
  String _currentVersionName = AppConstants.currentVersionName;
  int _currentVersionCode = AppConstants.currentVersionCode;

  // Server health state
  ServerHealth? _health;
  bool _isHealthLoading = false;
  String? _healthError;

  // Update engine state
  UpdateProcessStatus _updateStatus = const UpdateStatusIdle();
  UpdateInfo? _latestUpdateInfo;
  File? _downloadedApk;

  // Broadcast greetings state
  List<Greeting> _greetings = [];
  bool _isGreetingsLoading = false;
  String? _greetingsError;

  HomeViewModel({required UpdateRepository repository}) : _repository = repository;

  // Getters
  String get currentVersionName => _currentVersionName;
  int get currentVersionCode => _currentVersionCode;
  String get serverUrl => _repository.serverUrl;

  ServerHealth? get health => _health;
  bool get isHealthLoading => _isHealthLoading;
  String? get healthError => _healthError;
  bool get isServerOnline => _health?.isHealthy == true;

  UpdateProcessStatus get updateStatus => _updateStatus;
  UpdateInfo? get latestUpdateInfo => _latestUpdateInfo;
  File? get downloadedApk => _downloadedApk;

  List<Greeting> get greetings => _greetings;
  bool get isGreetingsLoading => _isGreetingsLoading;
  String? get greetingsError => _greetingsError;

  void setClientVersion(String name, int code) {
    _currentVersionName = name;
    _currentVersionCode = code;
    notifyListeners();
  }

  void updateServerUrl(String newUrl) {
    _repository.updateServerUrl(newUrl);
    notifyListeners();
    checkHealth();
  }

  /// Initial load or pull-to-refresh
  Future<void> loadInitialData() async {
    await Future.wait([
      checkHealth(),
      fetchGreetings(),
    ]);
  }

  /// Check backend health status (system.health.check)
  Future<void> checkHealth() async {
    _isHealthLoading = true;
    _healthError = null;
    notifyListeners();

    try {
      _health = await _repository.checkHealth();
      _healthError = null;
    } catch (e) {
      _health = null;
      _healthError = e.toString();
    } finally {
      _isHealthLoading = false;
      notifyListeners();
    }
  }

  /// Check for application updates (version.update.check)
  Future<void> checkForUpdates() async {
    _updateStatus = const UpdateStatusChecking();
    notifyListeners();

    try {
      final info = await _repository.checkForUpdate(
        currentVersion: _currentVersionName,
        versionCode: _currentVersionCode,
      );
      _latestUpdateInfo = info;

      if (info.hasUpdate && info.latestVersionCode > _currentVersionCode) {
        _updateStatus = UpdateStatusAvailable(info);
      } else {
        _updateStatus = const UpdateStatusUpToDate();
      }
    } catch (e) {
      _updateStatus = UpdateStatusError(
        '${UpdateStrings.statusErrorNetwork}: ${e.toString()}',
      );
    } finally {
      notifyListeners();
    }
  }

  /// Download available APK with streaming SHA-256 validation
  Future<void> startDownload() async {
    final info = _latestUpdateInfo;
    if (info == null || info.downloadUrl.isEmpty) {
      _updateStatus = const UpdateStatusError('No valid download URL available');
      notifyListeners();
      return;
    }

    // If downloadUrl is relative path from backend proxy (e.g. /api/v1/app/download), prepend serverUrl
    String resolvedUrl = info.downloadUrl;
    if (resolvedUrl.startsWith('/')) {
      resolvedUrl = '${_repository.serverUrl}$resolvedUrl';
    }

    final fileName = 'update-v${info.latestVersionName}.apk';

    _updateStatus = const UpdateStatusDownloading(
      progress: 0.0,
      receivedBytes: 0,
      totalBytes: 0,
    );
    notifyListeners();

    try {
      final file = await _repository.downloadAndVerifyApk(
        url: resolvedUrl,
        sha256: info.sha256,
        fileName: fileName,
        onProgress: (received, total, percent) {
          _updateStatus = UpdateStatusDownloading(
            progress: percent,
            receivedBytes: received,
            totalBytes: total,
          );
          notifyListeners();
        },
      );

      _downloadedApk = file;
      _updateStatus = UpdateStatusReadyToInstall(file);
    } catch (e) {
      _downloadedApk = null;
      _updateStatus = UpdateStatusError(e.toString());
    } finally {
      notifyListeners();
    }
  }

  /// Launch package installer
  Future<void> installUpdate() async {
    final file = _downloadedApk;
    if (file == null || !await file.exists()) {
      _updateStatus = const UpdateStatusError('Installer package file not found');
      notifyListeners();
      return;
    }

    try {
      final result = await OpenFilex.open(file.path, type: 'application/vnd.android.package-archive');
      if (result.type != ResultType.done) {
        _updateStatus = UpdateStatusError('${UpdateStrings.statusErrorInstall}: ${result.message}');
        notifyListeners();
      }
    } catch (e) {
      _updateStatus = UpdateStatusError('${UpdateStrings.statusErrorInstall}: $e');
      notifyListeners();
    }
  }

  /// Retrieve broadcast greetings (messages.greetings.random)
  Future<void> fetchGreetings() async {
    _isGreetingsLoading = true;
    _greetingsError = null;
    notifyListeners();

    try {
      _greetings = await _repository.fetchGreetings(count: 10);
      _greetingsError = null;
    } catch (e) {
      _greetings = [];
      _greetingsError = e.toString();
    } finally {
      _isGreetingsLoading = false;
      notifyListeners();
    }
  }

  void resetUpdateState() {
    _updateStatus = const UpdateStatusIdle();
    notifyListeners();
  }
}
