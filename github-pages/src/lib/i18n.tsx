import React, { createContext, useContext, useEffect, useMemo, useState } from "react"

export type Locale = "de" | "en"

type Translations = Record<string, string>

type I18nContextValue = {
  locale: Locale
  setLocale: (l: Locale) => void
  t: (key: string) => string
}

const I18N_STORAGE_KEY = "backloggd-app-locale"

const I18nContext = createContext<I18nContextValue | undefined>(undefined)

const de: Translations = {
  // Navigation
  "nav.brand": "Backloggd App",
  "nav.screenshots": "Screenshots",
  "nav.features": "Features",
  "nav.download": "Download",

  // Hero
  "hero.badge": "Inoffizielle Android App für Backloggd",
  "hero.title.line1": "Deine Backloggd-Bibliothek",
  "hero.title.line2": "als Android App",
  "hero.description": "Erweitere deine Backloggd-Erfahrung mit einer nativen Android App. Verwalte deine Spiele-Sammlung unterwegs, schnell und komfortabel.",
  "hero.disclaimer": "Diese App erweitert die Funktionen von Backloggd und ist nicht offiziell mit Backloggd verbunden.",
  "hero.cta.download_now": "Jetzt herunterladen",
  "hero.cta.github": "GitHub",

  // Screenshots section
  "screenshots.title": "App Screenshots",
  "screenshots.subtitle": "Sieh dir die App in Aktion an",

  // Features section
  "features.title": "App Features",
  "features.subtitle": "Erweiterte Funktionen für deine Backloggd-Bibliothek",
  "features.push.title": "Push Notifications",
  "features.push.desc": "Erhalte Benachrichtigungen, sobald neue Aktivitäten in deinem Backloggd-Konto auftauchen – etwa neue Follower, Likes oder Kommentare.",
  "features.release.title": "Spielerelease Benachrichtigungen",
  "features.release.desc": "Lass dich automatisch informieren, wenn ein Spiel aus deiner Wunschliste am aktuellen Tag erscheint. Kein Release mehr verpassen!",
  "features.interval.title": "Custom Update Interval ",
  "features.interval.desc": "Bestimme selbst, wie oft die App im Hintergrund nach neuen Benachrichtigungen sucht – ganz nach deinem Bedarf.",
  "features.fullscreen.title": "Vollbildmodus",
  "features.fullscreen.desc": "Erlebe Backloggd im immersiven Vollbildmodus, optimiert für Android und perfekt für den täglichen Gebrauch.",
  "features.icon.title": "Adaptives App Icon",
  "features.icon.desc": "Das App-Icon unterstützt adaptive Designs und fügt sich harmonisch in deinen Android-Launcher ein.",
  "features.private.title": "Leichtgewichtig & Privat",
  "features.private.desc": "Die App basiert auf einer WebView und speichert keine persönlichen Daten – leicht, schnell und sicher.",

  // Download section
  "download.title": "Bereit loszulegen?",
  "download.subtitle": "Lade die inoffizielle Backloggd Android App jetzt herunter und erweitere deine Backloggd-Erfahrung",
  "download.button.apk": "APK herunterladen",

  // Footer
  "footer.brand": "Backloggd App",
  "footer.tagline": "Inoffizielle Android App für Backloggd",
  "footer.disclaimer": "Nicht verbunden mit Backloggd",
  "footer.section.product": "Produkt",
  "footer.section.development": "Entwicklung",
  "footer.section.info": "Info",
  "footer.link.screenshots": "Screenshots",
  "footer.link.features": "Features",
  "footer.link.download": "Download",
  "footer.link.github": "GitHub",
  "footer.link.releases": "Releases",
  "footer.link.backloggd": "backloggd.com",
  "footer.extends": "Erweitert Backloggd",
  "footer.copyright": "© 2024 Backloggd App. Alle Rechte vorbehalten.",
  "footer.disclaimer.line": "Diese App ist nicht offiziell mit Backloggd verbunden."
}

const en: Translations = {
  // Navigation
  "nav.brand": "Backloggd App",
  "nav.screenshots": "Screenshots",
  "nav.features": "Features",
  "nav.download": "Download",

  // Hero
  "hero.badge": "Unofficial Android app for Backloggd",
  "hero.title.line1": "Your Backloggd library",
  "hero.title.line2": "as an Android app",
  "hero.description": "Enhance your Backloggd experience with a native Android app. Manage your game collection on the go—fast and conveniently.",
  "hero.disclaimer": "This app extends Backloggd’s functionality and is not officially affiliated with Backloggd.",
  "hero.cta.download_now": "Download now",
  "hero.cta.github": "GitHub",

  // Screenshots section
  "screenshots.title": "App Screenshots",
  "screenshots.subtitle": "See the app in action",

  // Features section
  "features.title": "App Features",
  "features.subtitle": "Advanced features for your Backloggd library",
  "features.push.title": "Push Notifications",
  "features.push.desc": "Get notified when new activity appears in your Backloggd account—new followers, likes, or comments.",
  "features.release.title": "Release Reminders",
  "features.release.desc": "Automatically get reminded when a game from your wishlist releases today. Never miss a launch!",
  "features.interval.title": "Custom Update Interval",
  "features.interval.desc": "Choose how often the app checks for new notifications in the background—tailored to your needs.",
  "features.fullscreen.title": "Fullscreen Experience",
  "features.fullscreen.desc": "Enjoy Backloggd in an immersive fullscreen mode, optimized for Android and everyday use.",
  "features.icon.title": "Adaptive App Icon",
  "features.icon.desc": "The app icon supports adaptive designs and integrates seamlessly with your Android launcher.",
  "features.private.title": "Lightweight & Private",
  "features.private.desc": "The app is based on a WebView and stores no personal data—light, fast, and secure.",

  // Download section
  "download.title": "Ready to get started?",
  "download.subtitle": "Download the unofficial Backloggd Android app now and enhance your Backloggd experience",
  "download.button.apk": "Download APK",

  // Footer
  "footer.brand": "Backloggd App",
  "footer.tagline": "Unofficial Android app for Backloggd",
  "footer.disclaimer": "Not affiliated with Backloggd",
  "footer.section.product": "Product",
  "footer.section.development": "Development",
  "footer.section.info": "Info",
  "footer.link.screenshots": "Screenshots",
  "footer.link.features": "Features",
  "footer.link.download": "Download",
  "footer.link.github": "GitHub",
  "footer.link.releases": "Releases",
  "footer.link.backloggd": "backloggd.com",
  "footer.extends": "Extends Backloggd",
  "footer.copyright": "© 2024 Backloggd App. All rights reserved.",
  "footer.disclaimer.line": "This app is not officially affiliated with Backloggd."
}

function detectDefaultLocale(): Locale {
  try {
    const saved = localStorage.getItem(I18N_STORAGE_KEY) as Locale | null
    if (saved === "de" || saved === "en") return saved
  } catch {}
  const lang = typeof navigator !== "undefined" ? navigator.language.toLowerCase() : "de"
  if (lang.startsWith("de")) return "de"
  return "en"
}

function getBundle(locale: Locale): Translations {
  return locale === "de" ? de : en
}

export function I18nProvider({ children }: { children: React.ReactNode }) {
  const [locale, setLocaleState] = useState<Locale>(detectDefaultLocale())

  useEffect(() => {
    try {
      localStorage.setItem(I18N_STORAGE_KEY, locale)
    } catch {}
  }, [locale])

  const bundle = useMemo(() => getBundle(locale), [locale])

  const value = useMemo<I18nContextValue>(() => ({
    locale,
    setLocale: (l) => setLocaleState(l),
    t: (key: string) => bundle[key] ?? key,
  }), [locale, bundle])

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>
}

export function useI18n() {
  const ctx = useContext(I18nContext)
  if (!ctx) throw new Error("useI18n must be used within I18nProvider")
  return ctx
}

