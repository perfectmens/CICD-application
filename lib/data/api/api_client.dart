import 'dart:convert';
import 'package:http/http.dart' as http;
import '../dto/greetings_response_dto.dart';
import '../dto/health_response_dto.dart';
import '../dto/update_check_response_dto.dart';

class ApiClient {
  final http.Client _client;
  String _baseUrl;

  ApiClient({http.Client? client, required String baseUrl})
      : _client = client ?? http.Client(),
        _baseUrl = baseUrl.endsWith('/') ? baseUrl.substring(0, baseUrl.length - 1) : baseUrl;

  String get baseUrl => _baseUrl;

  void updateBaseUrl(String newBaseUrl) {
    _baseUrl = newBaseUrl.endsWith('/') ? newBaseUrl.substring(0, newBaseUrl.length - 1) : newBaseUrl;
  }

  /// GET /api/v1/health (system.health.check)
  Future<HealthResponseDto> getHealth() async {
    final uri = Uri.parse('$_baseUrl/api/v1/health');
    final response = await _client.get(uri).timeout(const Duration(seconds: 8));

    if (response.statusCode == 200) {
      final json = jsonDecode(response.body) as Map<String, dynamic>;
      return HealthResponseDto.fromJson(json);
    } else {
      throw HttpException('Server responded with status ${response.statusCode}: ${response.body}');
    }
  }

  /// GET /api/v1/version/check (version.update.check)
  Future<UpdateCheckResponseDto> checkVersion({
    required String currentVersion,
    required int versionCode,
  }) async {
    final uri = Uri.parse('$_baseUrl/api/v1/version/check').replace(
      queryParameters: {
        'current_version': currentVersion,
        'version_code': versionCode.toString(),
      },
    );
    final response = await _client.get(uri).timeout(const Duration(seconds: 8));

    if (response.statusCode == 200) {
      final json = jsonDecode(response.body) as Map<String, dynamic>;
      return UpdateCheckResponseDto.fromJson(json);
    } else {
      throw HttpException('Server responded with status ${response.statusCode}: ${response.body}');
    }
  }

  /// GET /api/v1/messages/random (messages.greetings.random)
  Future<GreetingsResponseDto> getGreetings({int count = 10}) async {
    final uri = Uri.parse('$_baseUrl/api/v1/messages/random').replace(
      queryParameters: {'count': count.toString()},
    );
    final response = await _client.get(uri).timeout(const Duration(seconds: 8));

    if (response.statusCode == 200) {
      final json = jsonDecode(response.body) as Map<String, dynamic>;
      return GreetingsResponseDto.fromJson(json);
    } else {
      throw HttpException('Server responded with status ${response.statusCode}: ${response.body}');
    }
  }
}

class HttpException implements Exception {
  final String message;
  HttpException(this.message);

  @override
  String toString() => message;
}
