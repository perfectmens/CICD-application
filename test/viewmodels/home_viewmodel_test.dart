import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:remote_update_demo/data/api/apk_downloader.dart';
import 'package:remote_update_demo/data/repositories/update_repository.dart';
import 'package:remote_update_demo/domain/models/greeting.dart';
import 'package:remote_update_demo/domain/models/server_health.dart';
import 'package:remote_update_demo/domain/models/update_info.dart';
import 'package:remote_update_demo/presentation/viewmodels/home_viewmodel.dart';
import 'package:remote_update_demo/presentation/viewmodels/update_state.dart';

class FakeUpdateRepository implements UpdateRepository {
  ServerHealth? mockHealth;
  UpdateInfo? mockUpdateInfo;
  List<Greeting> mockGreetings = [];
  bool shouldThrow = false;
  String _serverUrl = 'http://127.0.0.1:8080';

  @override
  String get serverUrl => _serverUrl;

  @override
  void updateServerUrl(String newUrl) {
    _serverUrl = newUrl;
  }

  @override
  Future<ServerHealth> checkHealth() async {
    if (shouldThrow) throw Exception('Network timeout');
    return mockHealth ??
        const ServerHealth(
          status: 'ok',
          service: 'remote-update-server',
          timestamp: '2026-09-29T10:00:00Z',
          version: '1.0.0',
          lanIp: '192.168.68.64',
        );
  }

  @override
  Future<UpdateInfo> checkForUpdate({required String currentVersion, required int versionCode}) async {
    if (shouldThrow) throw Exception('Connection refused');
    return mockUpdateInfo ??
        const UpdateInfo(
          latestVersionName: '0.0.5',
          latestVersionCode: 12,
          hasUpdate: true,
          isMandatory: false,
          downloadUrl: 'https://example.com/app.apk',
          sha256: 'abc123hash',
          releaseNotes: 'Performance fixes',
          publishedAt: '2026-09-29T10:00:00Z',
        );
  }

  @override
  Future<List<Greeting>> fetchGreetings({int count = 10}) async {
    if (shouldThrow) throw Exception('API error');
    return mockGreetings;
  }

  @override
  Future<File> downloadAndVerifyApk({
    required String url,
    required String sha256,
    required String fileName,
    DownloadProgressCallback? onProgress,
  }) async {
    return File('/dummy/path/$fileName');
  }
}

void main() {
  late FakeUpdateRepository fakeRepo;
  late HomeViewModel viewModel;

  setUp(() {
    fakeRepo = FakeUpdateRepository();
    viewModel = HomeViewModel(repository: fakeRepo);
  });

  group('HomeViewModel Tests', () {
    test('initial state has idle update status and default versions', () {
      expect(viewModel.updateStatus, isA<UpdateStatusIdle>());
      expect(viewModel.currentVersionName, equals('0.0.1'));
      expect(viewModel.currentVersionCode, equals(1));
    });

    test('checkHealth sets healthy state on success', () async {
      await viewModel.checkHealth();
      expect(viewModel.health, isNotNull);
      expect(viewModel.health?.isHealthy, isTrue);
      expect(viewModel.isServerOnline, isTrue);
      expect(viewModel.healthError, isNull);
    });

    test('checkHealth sets error state on network failure', () async {
      fakeRepo.shouldThrow = true;
      await viewModel.checkHealth();
      expect(viewModel.health, isNull);
      expect(viewModel.isServerOnline, isFalse);
      expect(viewModel.healthError, contains('Network timeout'));
    });

    test('checkForUpdates emits UpdateStatusAvailable when newer versionCode found', () async {
      viewModel.setClientVersion('0.0.1', 1);
      fakeRepo.mockUpdateInfo = const UpdateInfo(
        latestVersionName: '0.0.5',
        latestVersionCode: 12,
        hasUpdate: true,
        isMandatory: false,
        downloadUrl: 'http://test.com/apk',
        sha256: 'fakehash',
        releaseNotes: 'Notes',
        publishedAt: '2026',
      );

      await viewModel.checkForUpdates();
      expect(viewModel.updateStatus, isA<UpdateStatusAvailable>());
      final state = viewModel.updateStatus as UpdateStatusAvailable;
      expect(state.info.latestVersionName, equals('0.0.5'));
    });

    test('checkForUpdates emits UpdateStatusUpToDate when code is equal or lower', () async {
      viewModel.setClientVersion('0.0.5', 12);
      fakeRepo.mockUpdateInfo = const UpdateInfo(
        latestVersionName: '0.0.5',
        latestVersionCode: 12,
        hasUpdate: true,
        isMandatory: false,
        downloadUrl: 'http://test.com/apk',
        sha256: 'fakehash',
        releaseNotes: 'Notes',
        publishedAt: '2026',
      );

      await viewModel.checkForUpdates();
      expect(viewModel.updateStatus, isA<UpdateStatusUpToDate>());
    });

    test('checkForUpdates emits UpdateStatusError on network failure', () async {
      fakeRepo.shouldThrow = true;
      await viewModel.checkForUpdates();
      expect(viewModel.updateStatus, isA<UpdateStatusError>());
    });

    test('fetchGreetings populates greetings list', () async {
      fakeRepo.mockGreetings = [
        const Greeting(id: 1, text: 'Hello CI/CD', category: 'DevOps', emoji: '🚀'),
      ];

      await viewModel.fetchGreetings();
      expect(viewModel.greetings.length, equals(1));
      expect(viewModel.greetings.first.text, equals('Hello CI/CD'));
      expect(viewModel.greetingsError, isNull);
    });
  });
}
