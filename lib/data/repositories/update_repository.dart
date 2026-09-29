import 'dart:io';
import '../../domain/models/greeting.dart';
import '../../domain/models/server_health.dart';
import '../../domain/models/update_info.dart';
import '../api/api_client.dart';
import '../api/apk_downloader.dart';

abstract class UpdateRepository {
  Future<ServerHealth> checkHealth();
  Future<UpdateInfo> checkForUpdate({
    required String currentVersion,
    required int versionCode,
  });
  Future<List<Greeting>> fetchGreetings({int count = 10});
  Future<File> downloadAndVerifyApk({
    required String url,
    required String sha256,
    required String fileName,
    DownloadProgressCallback? onProgress,
  });
  void updateServerUrl(String newUrl);
  String get serverUrl;
}

class UpdateRepositoryImpl implements UpdateRepository {
  final ApiClient _apiClient;
  final ApkDownloader _downloader;

  UpdateRepositoryImpl({
    required ApiClient apiClient,
    ApkDownloader? downloader,
  })  : _apiClient = apiClient,
        _downloader = downloader ?? ApkDownloader();

  @override
  String get serverUrl => _apiClient.baseUrl;

  @override
  void updateServerUrl(String newUrl) {
    _apiClient.updateBaseUrl(newUrl);
  }

  @override
  Future<ServerHealth> checkHealth() async {
    final dto = await _apiClient.getHealth();
    return dto.toDomain();
  }

  @override
  Future<UpdateInfo> checkForUpdate({
    required String currentVersion,
    required int versionCode,
  }) async {
    final dto = await _apiClient.checkVersion(
      currentVersion: currentVersion,
      versionCode: versionCode,
    );
    return dto.toDomain();
  }

  @override
  Future<List<Greeting>> fetchGreetings({int count = 10}) async {
    final dto = await _apiClient.getGreetings(count: count);
    return dto.toDomain();
  }

  @override
  Future<File> downloadAndVerifyApk({
    required String url,
    required String sha256,
    required String fileName,
    DownloadProgressCallback? onProgress,
  }) {
    return _downloader.downloadAndVerifyApk(
      url: url,
      expectedSha256: sha256,
      targetFileName: fileName,
      onProgress: onProgress,
    );
  }
}
