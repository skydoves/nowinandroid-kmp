import NiaKit
import SwiftUI
import UIKit

/// Hosts the shared Compose Multiplatform UI. `MainViewController()` is the Kotlin entry point
/// exported by the `NiaKit` framework built from `:app:shared`.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(edges: .all)
    }
}
