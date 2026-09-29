class ServerHealth {
  final String status;
  final String service;
  final String timestamp;
  final String version;
  final String lanIp;

  const ServerHealth({
    required this.status,
    required this.service,
    required this.timestamp,
    required this.version,
    required this.lanIp,
  });

  bool get isHealthy => status.toLowerCase() == 'ok';
}
