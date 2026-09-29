class UpdateInfo {
  final String latestVersionName;
  final int latestVersionCode;
  final bool hasUpdate;
  final bool isMandatory;
  final String downloadUrl;
  final String sha256;
  final String releaseNotes;
  final String publishedAt;

  const UpdateInfo({
    required this.latestVersionName,
    required this.latestVersionCode,
    required this.hasUpdate,
    required this.isMandatory,
    required this.downloadUrl,
    required this.sha256,
    required this.releaseNotes,
    required this.publishedAt,
  });
}
