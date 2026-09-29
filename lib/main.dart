import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/constants.dart';
import 'core/theme.dart';
import 'data/api/api_client.dart';
import 'data/api/apk_downloader.dart';
import 'data/repositories/update_repository.dart';
import 'presentation/viewmodels/home_viewmodel.dart';
import 'presentation/views/home_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  // Instantiate Data & Infrastructure dependencies
  final apiClient = ApiClient(baseUrl: AppConstants.defaultBaseUrl);
  final apkDownloader = ApkDownloader();
  final updateRepository = UpdateRepositoryImpl(
    apiClient: apiClient,
    downloader: apkDownloader,
  );

  runApp(
    MultiProvider(
      providers: [
        Provider<UpdateRepository>.value(value: updateRepository),
        ChangeNotifierProvider<HomeViewModel>(
          create: (_) => HomeViewModel(repository: updateRepository),
        ),
      ],
      child: const RemoteUpdateApp(),
    ),
  );
}

class RemoteUpdateApp extends StatelessWidget {
  const RemoteUpdateApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Remote Update Demo',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        scaffoldBackgroundColor: NeumorphicColors.background,
        colorScheme: ColorScheme.fromSeed(
          seedColor: NeumorphicColors.orange,
          primary: NeumorphicColors.orange,
          surface: NeumorphicColors.surface,
        ),
        useMaterial3: true,
        fontFamily: 'Roboto',
      ),
      home: const HomeScreen(),
    );
  }
}
