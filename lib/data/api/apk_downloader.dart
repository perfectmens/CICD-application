import 'dart:io';
import 'package:crypto/crypto.dart';
import 'package:http/http.dart' as http;
import 'package:path_provider/path_provider.dart';

typedef DownloadProgressCallback = void Function(int receivedBytes, int totalBytes, double percent);

class ApkDownloader {
  final http.Client _client;

  ApkDownloader({http.Client? client}) : _client = client ?? http.Client();

  /// Downloads APK from [url], calculates SHA-256, compares with [expectedSha256],
  /// and saves to a local file in app cache/downloads directory.
  Future<File> downloadAndVerifyApk({
    required String url,
    required String expectedSha256,
    required String targetFileName,
    DownloadProgressCallback? onProgress,
  }) async {
    final uri = Uri.parse(url);
    final request = http.Request('GET', uri);
    final response = await _client.send(request);

    if (response.statusCode != 200) {
      throw HttpException('Failed to download update package: HTTP ${response.statusCode}');
    }

    final totalBytes = response.contentLength ?? 0;
    int receivedBytes = 0;

    final dir = await getApplicationDocumentsDirectory();
    final file = File('${dir.path}/$targetFileName');
    if (await file.exists()) {
      await file.delete();
    }

    final sink = file.openWrite();
    try {
      await for (final chunk in response.stream) {
        receivedBytes += chunk.length;
        sink.add(chunk);

        if (totalBytes > 0 && onProgress != null) {
          final percent = (receivedBytes / totalBytes).clamp(0.0, 1.0);
          onProgress(receivedBytes, totalBytes, percent);
        }
      }
    } finally {
      await sink.flush();
      await sink.close();
    }

    // Verify SHA-256 hash integrity
    if (expectedSha256.isNotEmpty) {
      final digest = await sha256.bind(file.openRead()).first;
      final calculatedHash = digest.toString();
      if (calculatedHash.toLowerCase() != expectedSha256.trim().toLowerCase()) {
        await file.delete();
        throw HashMismatchException(
          'Package integrity check failed. Expected: $expectedSha256, Calculated: $calculatedHash',
        );
      }
    }

    return file;
  }
}

class HashMismatchException implements Exception {
  final String message;
  HashMismatchException(this.message);

  @override
  String toString() => message;
}

class HttpException implements Exception {
  final String message;
  HttpException(this.message);

  @override
  String toString() => message;
}
