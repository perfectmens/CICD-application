import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:remote_update_demo/data/repositories/update_repository.dart';
import 'package:remote_update_demo/main.dart';
import 'package:remote_update_demo/presentation/viewmodels/home_viewmodel.dart';
import 'viewmodels/home_viewmodel_test.dart';

void main() {
  testWidgets('RemoteUpdateApp renders header and update card', (WidgetTester tester) async {
    final fakeRepo = FakeUpdateRepository();
    final viewModel = HomeViewModel(repository: fakeRepo);

    await tester.pumpWidget(
      MultiProvider(
        providers: [
          Provider<UpdateRepository>.value(value: fakeRepo),
          ChangeNotifierProvider<HomeViewModel>.value(value: viewModel),
        ],
        child: const RemoteUpdateApp(),
      ),
    );

    // Initial frame
    await tester.pumpAndSettle();

    // Verify main components render
    expect(find.text('Remote Update Demo'), findsOneWidget);
    expect(find.text('System Diagnostics'), findsOneWidget);
    expect(find.text('Release Governance'), findsOneWidget);
    expect(find.text('Check for Updates'), findsOneWidget);
  });
}
