import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/theme.dart';
import '../viewmodels/home_viewmodel.dart';
import '../viewmodels/update_state.dart';
import '../widgets/neumorphic_widgets.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<HomeViewModel>().loadInitialData();
    });
  }

  void _showServerSettingsDialog(BuildContext context, HomeViewModel vm) {
    final controller = TextEditingController(text: vm.serverUrl);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: NeumorphicColors.surface,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text(
          'Server Endpoint',
          style: TextStyle(color: NeumorphicColors.textPrimary, fontWeight: FontWeight.bold),
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Enter LAN IP or backend base URL:',
              style: TextStyle(fontSize: 13, color: NeumorphicColors.steelGray),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: controller,
              decoration: InputDecoration(
                filled: true,
                fillColor: NeumorphicColors.background,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: BorderSide.none,
                ),
                hintText: 'http://192.168.68.64:8080',
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel', style: TextStyle(color: NeumorphicColors.steelGray)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: NeumorphicColors.orange,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            ),
            onPressed: () {
              vm.updateServerUrl(controller.text.trim());
              Navigator.pop(ctx);
            },
            child: const Text('Save & Reconnect', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<HomeViewModel>();

    return Scaffold(
      backgroundColor: NeumorphicColors.background,
      body: SafeArea(
        child: RefreshIndicator(
          color: NeumorphicColors.orange,
          backgroundColor: NeumorphicColors.surface,
          onRefresh: vm.loadInitialData,
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Top Header Bar
                _buildHeader(context, vm),
                const SizedBox(height: 20),

                // Card 1: System Health & Infrastructure
                _buildHealthCard(context, vm),
                const SizedBox(height: 16),

                // Card 2: Version & Release Governance (Update Engine)
                _buildUpdateEngineCard(context, vm),
                const SizedBox(height: 16),

                // Card 3: Backend Broadcast Messages
                _buildBroadcastCard(context, vm),
                const SizedBox(height: 24),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildHeader(BuildContext context, HomeViewModel vm) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Remote Update Demo',
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.w800,
                color: NeumorphicColors.textPrimary,
                letterSpacing: -0.5,
              ),
            ),
            const SizedBox(height: 4),
            Row(
              children: [
                NeumorphicStatusDot(isOnline: vm.isServerOnline),
                const SizedBox(width: 6),
                Text(
                  vm.isServerOnline ? 'Server Online' : 'Connecting...',
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w500,
                    color: vm.isServerOnline ? NeumorphicColors.teal : NeumorphicColors.steelGray,
                  ),
                ),
              ],
            ),
          ],
        ),
        Row(
          children: [
            NeumorphicBadge(
              label: 'v${vm.currentVersionName}',
              color: NeumorphicColors.teal,
              icon: Icons.verified_outlined,
            ),
            const SizedBox(width: 8),
            GestureDetector(
              onTap: () => _showServerSettingsDialog(context, vm),
              child: Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: NeumorphicColors.surface,
                  shape: BoxShape.circle,
                  boxShadow: NeumorphicDepth.subtleRaised(),
                ),
                child: const Icon(
                  Icons.settings_outlined,
                  size: 20,
                  color: NeumorphicColors.steelGray,
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildHealthCard(BuildContext context, HomeViewModel vm) {
    final health = vm.health;

    return NeumorphicCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Row(
                children: [
                  Icon(Icons.dns_outlined, color: NeumorphicColors.teal, size: 20),
                  SizedBox(width: 8),
                  Text(
                    'System Diagnostics',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w700,
                      color: NeumorphicColors.textPrimary,
                    ),
                  ),
                ],
              ),
              IconButton(
                icon: vm.isHealthLoading
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2, color: NeumorphicColors.teal),
                      )
                    : const Icon(Icons.refresh, size: 18, color: NeumorphicColors.steelGray),
                onPressed: vm.isHealthLoading ? null : vm.checkHealth,
              ),
            ],
          ),
          const SizedBox(height: 12),
          if (vm.healthError != null) ...[
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: NeumorphicColors.error.withOpacity(0.08),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Row(
                children: [
                  const Icon(Icons.error_outline, color: NeumorphicColors.error, size: 18),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Connection Error: ${vm.healthError}',
                      style: const TextStyle(fontSize: 12, color: NeumorphicColors.error),
                    ),
                  ),
                ],
              ),
            ),
          ] else if (health != null) ...[
            _buildDetailRow('Service', health.service),
            _buildDetailRow('LAN Endpoint', '${health.lanIp}:8080'),
            _buildDetailRow('Server Version', health.version),
            _buildDetailRow('Status', health.status.toUpperCase(), isSuccess: health.isHealthy),
          ] else ...[
            const Center(
              child: Text(
                'No health telemetry available',
                style: TextStyle(fontSize: 13, color: NeumorphicColors.steelGray),
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildUpdateEngineCard(BuildContext context, HomeViewModel vm) {
    final status = vm.updateStatus;
    final info = vm.latestUpdateInfo;

    return NeumorphicCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Row(
                children: [
                  Icon(Icons.system_update_alt_outlined, color: NeumorphicColors.orange, size: 20),
                  SizedBox(width: 8),
                  Text(
                    'Release Governance',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w700,
                      color: NeumorphicColors.textPrimary,
                    ),
                  ),
                ],
              ),
              NeumorphicBadge(
                label: 'Code: ${vm.currentVersionCode}',
                color: NeumorphicColors.cyanBlue,
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Status Banner Box
          _buildUpdateStatusBanner(status),
          const SizedBox(height: 16),

          // Release Notes if available
          if (info != null && info.releaseNotes.isNotEmpty) ...[
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: const Color(0xFFF9F9FA),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: NeumorphicColors.divider),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Release Notes:',
                    style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: NeumorphicColors.textSecondary),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    info.releaseNotes,
                    style: const TextStyle(fontSize: 13, color: NeumorphicColors.textPrimary),
                  ),
                  if (info.sha256.isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Text(
                      'SHA-256: ${info.sha256.substring(0, 16)}...',
                      style: const TextStyle(fontSize: 11, fontFamily: 'monospace', color: NeumorphicColors.steelGray),
                    ),
                  ],
                ],
              ),
            ),
            const SizedBox(height: 16),
          ],

          // Action Button depending on status
          _buildActionButton(vm),
        ],
      ),
    );
  }

  Widget _buildUpdateStatusBanner(UpdateProcessStatus status) {
    String message = 'Ready to scan for remote releases';
    IconData icon = Icons.info_outline;
    Color color = NeumorphicColors.steelGray;
    Widget? extra;

    switch (status) {
      case UpdateStatusIdle s:
        message = s.message;
        icon = Icons.radio_button_checked;
        color = NeumorphicColors.steelGray;
      case UpdateStatusChecking s:
        message = s.message;
        icon = Icons.sync;
        color = NeumorphicColors.teal;
      case UpdateStatusUpToDate s:
        message = s.message;
        icon = Icons.check_circle_outline;
        color = NeumorphicColors.teal;
      case UpdateStatusAvailable s:
        message = '${s.message}: v${s.info.latestVersionName} (Build ${s.info.latestVersionCode})';
        icon = Icons.new_releases_outlined;
        color = NeumorphicColors.orange;
      case UpdateStatusDownloading s:
        message = '${s.message} ${(s.progress * 100).toStringAsFixed(0)}%';
        icon = Icons.downloading_outlined;
        color = NeumorphicColors.orange;
        extra = Padding(
          padding: const EdgeInsets.only(top: 8.0),
          child: NeumorphicProgress(progress: s.progress),
        );
      case UpdateStatusVerifying s:
        message = s.message;
        icon = Icons.security;
        color = NeumorphicColors.cyanBlue;
      case UpdateStatusReadyToInstall s:
        message = s.message;
        icon = Icons.install_mobile;
        color = NeumorphicColors.teal;
      case UpdateStatusError s:
        message = s.error;
        icon = Icons.warning_amber_rounded;
        color = NeumorphicColors.error;
    }

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: color.withOpacity(0.06),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withOpacity(0.2)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, size: 18, color: color),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  message,
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: color,
                  ),
                ),
              ),
            ],
          ),
          ?extra,
        ],
      ),
    );
  }

  Widget _buildActionButton(HomeViewModel vm) {
    final status = vm.updateStatus;

    if (status is UpdateStatusDownloading) {
      return const NeumorphicButton(
        onPressed: null,
        isPrimary: false,
        child: Text('Downloading Package...'),
      );
    }

    if (status is UpdateStatusReadyToInstall) {
      return NeumorphicButton(
        accentColor: NeumorphicColors.teal,
        onPressed: vm.installUpdate,
        child: const Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.install_mobile, size: 18, color: Colors.white),
            SizedBox(width: 8),
            Text('Install Update Now'),
          ],
        ),
      );
    }

    if (status is UpdateStatusAvailable) {
      return NeumorphicButton(
        accentColor: NeumorphicColors.orange,
        onPressed: vm.startDownload,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.download, size: 18, color: Colors.white),
            const SizedBox(width: 8),
            Text('Download v${status.info.latestVersionName}'),
          ],
        ),
      );
    }

    return NeumorphicButton(
      accentColor: NeumorphicColors.orange,
      onPressed: status is UpdateStatusChecking ? null : vm.checkForUpdates,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          if (status is UpdateStatusChecking) ...[
            const SizedBox(
              width: 16,
              height: 16,
              child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
            ),
            const SizedBox(width: 10),
          ] else ...[
            const Icon(Icons.cloud_sync_outlined, size: 18, color: Colors.white),
            const SizedBox(width: 8),
          ],
          const Text('Check for Updates'),
        ],
      ),
    );
  }

  Widget _buildBroadcastCard(BuildContext context, HomeViewModel vm) {
    final list = vm.greetings;

    return NeumorphicCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Row(
                children: [
                  Icon(Icons.chat_bubble_outline, color: NeumorphicColors.cyanBlue, size: 20),
                  SizedBox(width: 8),
                  Text(
                    'Backend Broadcasts',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w700,
                      color: NeumorphicColors.textPrimary,
                    ),
                  ),
                ],
              ),
              IconButton(
                icon: vm.isGreetingsLoading
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2, color: NeumorphicColors.cyanBlue),
                      )
                    : const Icon(Icons.refresh, size: 18, color: NeumorphicColors.steelGray),
                onPressed: vm.isGreetingsLoading ? null : vm.fetchGreetings,
              ),
            ],
          ),
          const SizedBox(height: 12),
          if (vm.greetingsError != null) ...[
            Text(
              'Could not load broadcasts: ${vm.greetingsError}',
              style: const TextStyle(fontSize: 12, color: NeumorphicColors.error),
            ),
          ] else if (list.isEmpty && !vm.isGreetingsLoading) ...[
            const Text(
              'No broadcasts found',
              style: TextStyle(fontSize: 13, color: NeumorphicColors.steelGray),
            ),
          ] else ...[
            ListView.separated(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: list.length > 5 ? 5 : list.length,
              separatorBuilder: (context, index) => const Divider(color: NeumorphicColors.divider, height: 16),
              itemBuilder: (context, idx) {
                final item = list[idx];
                return Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(item.emoji, style: const TextStyle(fontSize: 18)),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            item.text,
                            style: const TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w500,
                              color: NeumorphicColors.textPrimary,
                            ),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            item.category,
                            style: const TextStyle(
                              fontSize: 11,
                              color: NeumorphicColors.steelGray,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                );
              },
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildDetailRow(String label, String value, {bool? isSuccess}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: const TextStyle(fontSize: 13, color: NeumorphicColors.steelGray),
          ),
          Text(
            value,
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: isSuccess == true
                  ? NeumorphicColors.teal
                  : (isSuccess == false ? NeumorphicColors.error : NeumorphicColors.textPrimary),
            ),
          ),
        ],
      ),
    );
  }
}
