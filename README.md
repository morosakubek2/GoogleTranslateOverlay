# TranslateOverlay
# Google Translate Overlay – Offline Translator Proxy

**Two versions in one repository – zero GUI, 100% offline translation**

> **Replace Google Translate with `offline-translator` – no internet, no ads, no account.**

---

## Two APK Versions

| Version | File | Purpose |
|-------|------|--------|
| **minimal** | `GoogleTranslateOverlay-minimal-vX.apk` | Select text → `!Translate` → `offline-translator` |
| **assistant** | `GoogleTranslateOverlay-assistant-vX.apk` | **Replaces default voice assistant** – works **only when text is selected** → `offline-translator` |

---

## Designed for **eBook Readers** (and phones)

> **Primary use case: eBook apps with limited translator integration**  
> Example: **AlReaderX** – only allows *Google Translate*, but **you can replace it with offline translation**.

### How it works on eBook readers:

1. Install `GoogleTranslateOverlay-minimal-vX.apk`
2. Install **[offline-translator](https://github.com/davidvdev/offline-translator)**
3. Open **AlReaderX** (or any eBook app)
4. **Long-press a word or select text**
5. Tap **"Translate"** (or three dots → Translate)
6. Instead of online Google Translate → opens **offline-translator** with selected text
7. **Translation works 100% offline**

> **No internet needed on your e-ink device!**

---

## Version `minimal` – Standard Mode

- Select any text in **any app** (eBook, browser, notes)
- Menu shows: `!Translate` → **first option**
- Tap → opens `offline-translator`
- Also works with **Share → text/image**

---

## Version `assistant` – Replaces Voice Assistant (Text-Only)

> **Does NOT respond to "OK Google"**  
> **Does NOT listen to voice**  
> **Activates only when text is selected**

### Setup:
1. Install `GoogleTranslateOverlay-assistant-vX.apk`
2. Go to:  
   **Settings → Apps → Default apps → Assist & voice input → Assist app**
3. Select **"Google Translate Overlay"**
4. Now:
   - **Select text** in any app
   - **Long-press Home button** (or swipe gesture)
   - Assistant screen appears (blank – no UI)
   - **Automatically** opens `offline-translator` with selected text

> **Perfect for eBook apps that trigger assistant on text selection**

---

## Why Better Than Google Translate?

| Feature | Google Translate | **Our Overlay** |
|--------|------------------|-----------------|
| Internet | Required | **Not required** |
| Ads | Yes | **None** |
| Account | Required | **None** |
| OCR (image to text) | Online | **Offline** |
| eBook reader support | Limited | **Full control** |
| **Assistant mode (offline)** | No | **Yes – text only** |

---

## Requirements

- **Android 6.0+** (`minimal`)  
- **Android 8.0+** (`assistant`)  
- **[offline-translator](https://github.com/davidvdev/offline-translator)** installed  
  → Download language models inside the app (first launch)

---

## How to Install

1. Download the desired APK from **Releases**
2. Enable **Install from unknown sources**
3. Install
4. *(For `assistant`)*: Set as default **Assist app** in Settings

---

## GitHub Actions Build

- Automatically builds **both versions**
- Signs with **random key in CI**
- No `res/` folder → **no AAPT errors**
- Versions: `minimal` + `assistant`

---

## Project Structure
