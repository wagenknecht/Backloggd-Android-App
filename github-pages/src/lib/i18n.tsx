import React, { createContext, useContext, useEffect, useMemo, useState } from "react"

export type Locale = "de" | "en" | "es" | "fr"

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
  "features.release.title": "Benachrichtigungen zu Spiele-Releases",
  "features.release.desc": "Lass dich automatisch informieren, wenn ein Spiel aus deiner Wunschliste am aktuellen Tag erscheint. Kein Release mehr verpassen!",
  "features.interval.title": "Benutzerdefiniertes Update-Intervall ",
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
  "features.release.title": "Game Release Notifications",
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

const es: Translations = {
  "nav.brand": "Backloggd App",
  "nav.screenshots": "Capturas",
  "nav.features": "Características",
  "nav.download": "Descargar",

  "hero.badge": "Aplicación Android no oficial para Backloggd",
  "hero.title.line1": "Tu biblioteca de Backloggd",
  "hero.title.line2": "como una app de Android",
  "hero.description": "Mejora tu experiencia en Backloggd con una app nativa para Android. Administra tu colección de juegos donde estés—rápida y cómodamente.",
  "hero.disclaimer": "Esta app amplía la funcionalidad de Backloggd y no está afiliada oficialmente a Backloggd.",
  "hero.cta.download_now": "Descargar ahora",
  "hero.cta.github": "GitHub",

  "screenshots.title": "Capturas de la app",
  "screenshots.subtitle": "Mira la app en acción",

  "features.title": "Funciones de la app",
  "features.subtitle": "Funciones avanzadas para tu biblioteca de Backloggd",
  "features.push.title": "Notificaciones push",
  "features.push.desc": "Recibe avisos cuando haya nueva actividad en tu cuenta de Backloggd—nuevos seguidores, likes o comentarios.",
  "features.release.title": "Recordatorios de lanzamientos",
  "features.release.desc": "Recibe automáticamente un aviso cuando un juego de tu lista de deseos se lance hoy. ¡No te pierdas ningún lanzamiento!",
  "features.interval.title": "Intervalo de actualización personalizado",
  "features.interval.desc": "Elige con qué frecuencia la app busca nuevas notificaciones en segundo plano—adaptado a tus necesidades.",
  "features.fullscreen.title": "Experiencia a pantalla completa",
  "features.fullscreen.desc": "Disfruta Backloggd en un modo inmersivo de pantalla completa, optimizado para Android y uso diario.",
  "features.icon.title": "Icono de app adaptable",
  "features.icon.desc": "El icono de la app admite diseños adaptables e integra a la perfección con tu lanzador de Android.",
  "features.private.title": "Ligera y privada",
  "features.private.desc": "La app se basa en una WebView y no almacena datos personales—ligera, rápida y segura.",

  "download.title": "¿Listo para empezar?",
  "download.subtitle": "Descarga ahora la app Android no oficial de Backloggd y mejora tu experiencia en Backloggd",
  "download.button.apk": "Descargar APK",

  "footer.brand": "Backloggd App",
  "footer.tagline": "App Android no oficial para Backloggd",
  "footer.disclaimer": "No afiliada con Backloggd",
  "footer.section.product": "Producto",
  "footer.section.development": "Desarrollo",
  "footer.section.info": "Info",
  "footer.link.screenshots": "Capturas",
  "footer.link.features": "Características",
  "footer.link.download": "Descargar",
  "footer.link.github": "GitHub",
  "footer.link.releases": "Lanzamientos",
  "footer.link.backloggd": "backloggd.com",
  "footer.extends": "Amplía Backloggd",
  "footer.copyright": "© 2024 Backloggd App. Todos los derechos reservados.",
  "footer.disclaimer.line": "Esta app no está afiliada oficialmente a Backloggd."
}

const fr: Translations = {
  "nav.brand": "Backloggd App",
  "nav.screenshots": "Captures",
  "nav.features": "Fonctionnalités",
  "nav.download": "Télécharger",

  "hero.badge": "Application Android non officielle pour Backloggd",
  "hero.title.line1": "Votre bibliothèque Backloggd",
  "hero.title.line2": "en application Android",
  "hero.description": "Améliorez votre expérience Backloggd avec une application Android native. Gérez votre collection de jeux en déplacement—rapidement et facilement.",
  "hero.disclaimer": "Cette application étend les fonctionnalités de Backloggd et n’est pas officiellement affiliée à Backloggd.",
  "hero.cta.download_now": "Télécharger maintenant",
  "hero.cta.github": "GitHub",

  "screenshots.title": "Captures d’écran de l’app",
  "screenshots.subtitle": "Voir l’app en action",

  "features.title": "Fonctionnalités de l’app",
  "features.subtitle": "Fonctionnalités avancées pour votre bibliothèque Backloggd",
  "features.push.title": "Notifications push",
  "features.push.desc": "Recevez des notifications lorsque de nouvelles activités apparaissent sur votre compte Backloggd—nouveaux abonnés, likes ou commentaires.",
  "features.release.title": "Rappels de sorties",
  "features.release.desc": "Recevez automatiquement un rappel lorsqu’un jeu de votre liste de souhaits sort aujourd’hui. Ne manquez plus aucun lancement !",
  "features.interval.title": "Intervalle de mise à jour personnalisé",
  "features.interval.desc": "Choisissez la fréquence à laquelle l’application vérifie les nouvelles notifications en arrière-plan—adaptée à vos besoins.",
  "features.fullscreen.title": "Expérience plein écran",
  "features.fullscreen.desc": "Profitez de Backloggd dans un mode immersif plein écran, optimisé pour Android et l’usage quotidien.",
  "features.icon.title": "Icône d’application adaptative",
  "features.icon.desc": "L’icône de l’application prend en charge des designs adaptatifs et s’intègre parfaitement à votre lanceur Android.",
  "features.private.title": "Légère et privée",
  "features.private.desc": "L’application est basée sur une WebView et n’enregistre aucune donnée personnelle—légère, rapide et sécurisée.",

  "download.title": "Prêt à commencer ?",
  "download.subtitle": "Téléchargez dès maintenant l’application Android non officielle Backloggd et améliorez votre expérience Backloggd",
  "download.button.apk": "Télécharger l’APK",

  "footer.brand": "Backloggd App",
  "footer.tagline": "Application Android non officielle pour Backloggd",
  "footer.disclaimer": "Non affiliée à Backloggd",
  "footer.section.product": "Produit",
  "footer.section.development": "Développement",
  "footer.section.info": "Info",
  "footer.link.screenshots": "Captures",
  "footer.link.features": "Fonctionnalités",
  "footer.link.download": "Télécharger",
  "footer.link.github": "GitHub",
  "footer.link.releases": "Versions",
  "footer.link.backloggd": "backloggd.com",
  "footer.extends": "Étend Backloggd",
  "footer.copyright": "© 2024 Backloggd App. Tous droits réservés.",
  "footer.disclaimer.line": "Cette application n’est pas officiellement affiliée à Backloggd."
}

function detectDefaultLocale(): Locale {
  try {
    const saved = localStorage.getItem(I18N_STORAGE_KEY) as Locale | null
    if (saved === "de" || saved === "en" || saved === "es" || saved === "fr") return saved
  } catch {}
  const lang = typeof navigator !== "undefined" ? navigator.language.toLowerCase() : "en"
  if (lang.startsWith("de")) return "de"
  return "en"
}

function getBundle(locale: Locale): Translations {
  switch (locale) {
    case "de":
      return de
    case "es":
      return es
    case "fr":
      return fr
    case "en":
    default:
      return en
  }
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

