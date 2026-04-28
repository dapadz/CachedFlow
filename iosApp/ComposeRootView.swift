import SwiftUI
import CachedFlowDemo
import Dispatch

@MainActor
final class DemoScreenModel: ObservableObject {
    @Published var platformName = ""
    @Published var storeLabel = ""
    @Published var overview = ""
    @Published var strategyStatus = ""
    @Published var cacheDump = ""
    @Published var logs = ""
    @Published var cachedAfterLoad = true
    @Published var busy = false

    private let bridge = IosDemoBridge()

    init() {
        apply(snapshot: bridge.snapshot())
    }

    func clearCache() {
        run { [self] in self.bridge.clearCache() }
    }

    func clearLog() {
        apply(snapshot: bridge.clearLog())
    }

    func runOnlyRequest() {
        run { [self] in self.bridge.runOnlyRequest(cachedAfterLoad: self.cachedAfterLoad) }
    }

    func runIfHave() {
        run { [self] in self.bridge.runIfHave(cachedAfterLoad: self.cachedAfterLoad) }
    }

    func runOnlyCache() {
        run { [self] in self.bridge.runOnlyCache(cachedAfterLoad: self.cachedAfterLoad) }
    }

    func runStringRoundTrip() {
        run { [self] in self.bridge.runStringRoundTrip() }
    }

    func runIntRoundTrip() {
        run { [self] in self.bridge.runIntRoundTrip() }
    }

    func runBooleanRoundTrip() {
        run { [self] in self.bridge.runBooleanRoundTrip() }
    }

    func runSerializableObject() {
        run { [self] in self.bridge.runSerializableObject() }
    }

    func runSerializableList() {
        run { [self] in self.bridge.runSerializableList() }
    }

    func runPolymorphicList() {
        run { [self] in self.bridge.runPolymorphicList() }
    }

    func runJsonResilience() {
        run { [self] in self.bridge.runJsonResilience() }
    }

    private func run(_ block: @escaping () -> IosDemoSnapshot) {
        guard !busy else { return }
        busy = true
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            let snapshot = block()
            DispatchQueue.main.async {
                self?.apply(snapshot: snapshot)
                self?.busy = false
            }
        }
    }

    private func apply(snapshot: IosDemoSnapshot) {
        platformName = snapshot.platformName
        storeLabel = snapshot.storeLabel
        overview = snapshot.overview
        strategyStatus = snapshot.strategyStatus
        cacheDump = snapshot.cacheDump
        logs = snapshot.logs
    }
}

struct ComposeRootView: View {
    @StateObject private var model = DemoScreenModel()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("CachedFlow iOS Demo")
                            .font(.title.bold())
                        HStack {
                            pill(model.platformName)
                            pill(model.storeLabel)
                        }
                        Text(model.overview)
                            .font(.body)
                            .foregroundStyle(.secondary)
                    }

                    VStack(alignment: .leading, spacing: 12) {
                        Text("General")
                            .font(.headline)
                        HStack {
                            actionButton("Clear cache", action: model.clearCache)
                            actionButton("Clear log", action: model.clearLog, prominent: false)
                        }
                    }

                    VStack(alignment: .leading, spacing: 12) {
                        Text("Strategies")
                            .font(.headline)
                        Toggle("cachedAfterLoad", isOn: $model.cachedAfterLoad)
                            .disabled(model.busy)
                        HStack {
                            actionButton("ONLY_REQUEST", action: model.runOnlyRequest)
                            actionButton("IF_HAVE", action: model.runIfHave, prominent: false)
                        }
                        HStack {
                            actionButton("ONLY_CACHE", action: model.runOnlyCache, prominent: false)
                        }
                        sectionText(model.strategyStatus)
                    }

                    VStack(alignment: .leading, spacing: 12) {
                        Text("Primitive keys")
                            .font(.headline)
                        HStack {
                            actionButton("String", action: model.runStringRoundTrip)
                            actionButton("Int", action: model.runIntRoundTrip, prominent: false)
                            actionButton("Boolean", action: model.runBooleanRoundTrip, prominent: false)
                        }
                    }

                    VStack(alignment: .leading, spacing: 12) {
                        Text("Serialization")
                            .font(.headline)
                        HStack {
                            actionButton("Object", action: model.runSerializableObject)
                            actionButton("List", action: model.runSerializableList, prominent: false)
                        }
                        HStack {
                            actionButton("Polymorphic", action: model.runPolymorphicList, prominent: false)
                            actionButton("Broken JSON", action: model.runJsonResilience, prominent: false)
                        }
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Cache dump")
                            .font(.headline)
                        sectionText(model.cacheDump)
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Event log")
                            .font(.headline)
                        sectionText(model.logs)
                    }
                }
                .padding(20)
            }
            .background(Color(uiColor: .systemGroupedBackground))
            .navigationTitle("CachedFlow")
            .toolbar {
                if model.busy {
                    ProgressView()
                }
            }
        }
    }

    @ViewBuilder
    private func actionButton(
        _ title: String,
        action: @escaping () -> Void,
        prominent: Bool = true
    ) -> some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline.weight(.semibold))
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .frame(maxWidth: .infinity)
        }
        .foregroundColor(prominent ? .white : .accentColor)
        .background(
            Group {
                if prominent {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color.accentColor)
                } else {
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.accentColor, lineWidth: 1)
                }
            }
        )
        .disabled(model.busy)
        .opacity(model.busy ? 0.6 : 1.0)
    }

    private func pill(_ text: String) -> some View {
        Text(text)
            .font(.caption.weight(.semibold))
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(.thinMaterial, in: Capsule())
    }

    private func sectionText(_ text: String) -> some View {
        Text(text)
            .font(.system(.footnote, design: .monospaced))
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(12)
            .background(Color(uiColor: .secondarySystemBackground), in: RoundedRectangle(cornerRadius: 16))
            .textSelection(.enabled)
    }
}
