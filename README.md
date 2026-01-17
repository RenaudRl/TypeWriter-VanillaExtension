# Vanilla Extension

![Java Version](https://img.shields.io/badge/Java-21-orange)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Target](https://img.shields.io/badge/Target-Paper%20/%20Folia%20/%20BTC--CORE-blue)

**Vanilla Extension** is a core integration module for **TypeWriter**, engineered for **BTC Studio** infrastructure. It bridges standard Minecraft mechanics with the TypeWriter engine, enabling triggering and configuration based on vanilla events.

---

## 🚀 Key Features

### 🔮 Enchantment Integration
- **Enchanting Table Triggers**: Hook into vanilla enchanting events to trigger TypeWriter actions.
- **Anvil Mechanics**: Detect and react to item combinations and repairs in anvils.

### 🛠️ Vanilla Feature Support
- **Native Events**: Seamlessly integrates with native Minecraft behaviors.
- **Configurable Triggers**: Use TypeWriter's powerful configuration system to define complex reactions to standard gameplay.

---

## ⚙️ Configuration

Vanilla Extension configuration is managed via TypeWriter's manifest system.

## 🛠 Building & Deployment

Requires **Java 21**.

```bash
# Clone the repository
git clone https://github.com/RenaudRl/TypeWriter-VanillaExtension.git
cd TypeWriter-VanillaExtension

# Build the project
./gradlew clean build
```

### Artifact Locations:
- `build/libs/Vanilla-[Version].jar`

---

## 🤝 Credits & Inspiration
- **[TypeWriter](https://github.com/gabber235/Typewriter)** - The engine this extension is built for.
- **[BTC Studio](https://github.com/RenaudRl)** - Maintenance and specialized optimizations.

---

## 📜 License
Licensed under the **MIT License**.
