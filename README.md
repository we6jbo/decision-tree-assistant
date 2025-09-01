<!-- MARK: JBO1|actor=Jeremiah ONeal|ts=2025-09-01T10:46-07:00|note=All changes from T14 committed to Github on this date.|license=GPL-3.0-or-later|deliver_to=Lenovo ideacentre|deliver_by=2025-09-01T13:00-07:00|verification=unverified | CARD=A8E9FF8891C.json -->
# Decision Tree Assistant

Decision Tree Assistant is an Android + Wear OS app that helps you create, track, and improve decision trees for personal and professional goals.  
The app is designed to pair with a small backend (such as a Raspberry Pi running an Ollama/LLaMA server) where your decision data can be processed and improved with AI.

---

## ✨ Features

- **Goal tracking**: Enter a goal (e.g. “Get a VA IT role via Schedule A”) and generate a decision tree of steps.
- **Constraints & facts**: Add conditions (like location preference) and relevant facts (like education or experience).
- **Offline-first**: Trees are stored on your phone/watch and synchronized with your backend when connected.
- **Health reporting**: The mobile app can send heartbeat pings to confirm it’s running correctly.
- **Extendable**: New features or UI updates can be added and deployed through GitHub CI.

---

## 📱 Platforms

- **Mobile (Android)**: core app for goal entry, syncing, and reviewing decision trees.  
- **Wear OS (Samsung Watch)**: lightweight companion for quick input and status checks.

---

## 🛠️ Development Workflow

1. Edit the app modules under:
   - `mobile/src/main/...`
   - `wear/src/main/...` (if enabled)
2. Push changes to `main`.
3. GitHub Actions (`.github/workflows/android.yml`) builds the APKs.
4. Built artifacts are downloaded and uploaded to [DeployGate](https://deploygate.com/) for distribution.

---

## 🚀 Installation

- The current testing distribution uses [DeployGate](https://deploygate.com/).  
- Install the DeployGate app on your device and subscribe to the app project.  
- When new builds are uploaded, you’ll be prompted to install/update.

---

## 🔮 Roadmap

- [ ] Improved UI for browsing saved decision trees  
- [ ] Automatic sync when reconnected to backend  
- [ ] Play Store internal testing channel (future)  
- [ ] Wear OS richer interface  

---

## 📄 License

This project is licensed under the [Unlicense](LICENSE) — free and open, dedicated to the public domain.

