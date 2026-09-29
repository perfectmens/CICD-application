import '../../domain/models/update_info.dart';

class UpdateCheckResponseDto {
  final String latestVersionName;
  final int latestVersionCode;
  final bool hasUpdate;
  final bool isMandatory;
  final String downloadUrl;
  final String sha256;
  final String releaseNotes;
  final String publishedAt;

  UpdateCheckResponseDto({
    required this.latestVersionName,
    required this.latestVersionCode,
    required this.hasUpdate,
    required this.isMandatory,
    required this.downloadUrl,
    required this.sha256,
    required this.releaseNotes,
    required this.publishedAt,
  });

  factory UpdateCheckResponseDto.fromJson(Map<String, dynamic> json) {
    return UpdateCheckResponseDto(
      latestVersionName: json['latestVersionName'] as String? ?? '',
      latestVersionCode: json['latestVersionCode'] as int? ?? 1,
      hasUpdate: json['hasUpdate'] as bool? ?? false,
      isMandatory: json['isMandatory'] as bool? ?? false,
      downloadUrl: json['downloadUrl'] as String? ?? '',
      sha256: json['sha256'] as String? ?? '',
      releaseNotes: json['releaseNotes'] as String? ?? '',
      publishedAt: json['publishedAt'] as String? ?? '',
    );
  }

  UpdateInfo toDomain() {
    return UpdateInfo(
      latestVersionName: latestVersionName,
      latestVersionCode: latestVersionCode,
      hasUpdate: hasUpdate,
      isMandatory: isMandatory,
      downloadUrl: downloadUrl,
      sha256: sha256,
      releaseNotes: releaseNotes,
      publishedAt: publishedAt,
    );
  }
}
