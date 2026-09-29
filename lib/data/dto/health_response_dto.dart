import '../../domain/models/server_health.dart';

class HealthResponseDto {
  final String status;
  final String service;
  final String timestamp;
  final String version;
  final String lanIp;

  HealthResponseDto({
    required this.status,
    required this.service,
    required this.timestamp,
    required this.version,
    required this.lanIp,
  });

  factory HealthResponseDto.fromJson(Map<String, dynamic> json) {
    return HealthResponseDto(
      status: json['status'] as String? ?? 'unknown',
      service: json['service'] as String? ?? '',
      timestamp: json['timestamp'] as String? ?? '',
      version: json['version'] as String? ?? '',
      lanIp: json['lan_ip'] as String? ?? json['lanIp'] as String? ?? '',
    );
  }

  ServerHealth toDomain() {
    return ServerHealth(
      status: status,
      service: service,
      timestamp: timestamp,
      version: version,
      lanIp: lanIp,
    );
  }
}
