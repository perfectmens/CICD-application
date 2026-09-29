import 'dart:io';
import '../../core/constants.dart';
import '../../domain/models/update_info.dart';

sealed class UpdateProcessStatus {
  const UpdateProcessStatus();
}

class UpdateStatusIdle extends UpdateProcessStatus {
  final String message;
  const UpdateStatusIdle([this.message = UpdateStrings.statusIdle]);
}

class UpdateStatusChecking extends UpdateProcessStatus {
  final String message;
  const UpdateStatusChecking([this.message = UpdateStrings.statusChecking]);
}

class UpdateStatusUpToDate extends UpdateProcessStatus {
  final String message;
  const UpdateStatusUpToDate([this.message = UpdateStrings.statusUpToDate]);
}

class UpdateStatusAvailable extends UpdateProcessStatus {
  final UpdateInfo info;
  final String message;
  const UpdateStatusAvailable(this.info, [this.message = UpdateStrings.statusUpdateAvailable]);
}

class UpdateStatusDownloading extends UpdateProcessStatus {
  final double progress; // 0.0 to 1.0
  final int receivedBytes;
  final int totalBytes;
  final String message;
  const UpdateStatusDownloading({
    required this.progress,
    required this.receivedBytes,
    required this.totalBytes,
    this.message = UpdateStrings.statusDownloading,
  });
}

class UpdateStatusVerifying extends UpdateProcessStatus {
  final String message;
  const UpdateStatusVerifying([this.message = UpdateStrings.statusVerifying]);
}

class UpdateStatusReadyToInstall extends UpdateProcessStatus {
  final File apkFile;
  final String message;
  const UpdateStatusReadyToInstall(this.apkFile, [this.message = UpdateStrings.statusReadyToInstall]);
}

class UpdateStatusError extends UpdateProcessStatus {
  final String error;
  const UpdateStatusError(this.error);
}
