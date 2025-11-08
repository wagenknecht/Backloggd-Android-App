# Backloggd Landing Page

Eine moderne Landing Page für die inoffizielle Backloggd Android App, gebaut mit React, TypeScript, Vite und ShadCN UI.

## 🚀 Features

- **Moderne UI**: Erstellt mit ShadCN UI Komponenten
- **Responsive Design**: Optimiert für alle Geräte
- **Screenshots Galerie**: Zeige deine App-Screenshots in einer schönen Galerie
- **Schnell**: Gebaut mit Vite für optimale Performance
- **Type-Safe**: Vollständig in TypeScript geschrieben

## 🛠️ Technologien

- [React](https://react.dev/)
- [TypeScript](https://www.typescriptlang.org/)
- [Vite](https://vitejs.dev/)
- [Tailwind CSS](https://tailwindcss.com/)
- [ShadCN UI](https://ui.shadcn.com/)
- [Lucide React](https://lucide.dev/) (Icons)

## 📦 Installation

```bash
# Dependencies installieren
npm install --ignore-scripts

# Entwicklungsserver starten
npm run dev

# Production Build erstellen
npm run build

# Preview des Production Builds
npm run preview
```

## 📸 Screenshots hinzufügen

1. Lege deine App-Screenshots im `public/` Ordner ab
2. Benenne sie: `screenshot1.png`, `screenshot2.png`, etc. (bis zu 6 Screenshots)
3. Die Screenshots sollten im Portrait-Format (9:16) sein für beste Ergebnisse
4. Optimale Größe: ~1080x1920px oder ähnliches Seitenverhältnis

Die Screenshots werden automatisch in der Galerie angezeigt. Falls ein Screenshot fehlt, wird ein Platzhalter angezeigt.

## 🔗 GitHub Links konfigurieren

Aktualisiere die GitHub-Konfiguration in `src/config.ts`:

```typescript
export const config = {
  github: {
    username: "DEIN-USERNAME",  // Dein GitHub Username
    repository: "DEIN-REPO",     // Dein Repository Name
  }
}
```

Alle GitHub-Links werden automatisch basierend auf dieser Konfiguration generiert.

## 🚀 Deployment auf GitHub Pages

Die Seite wird automatisch auf GitHub Pages deployed, wenn Code zum `github-pages` Branch gepusht wird.

### Manuelles Setup

1. Erstelle einen Branch namens `github-pages`:
   ```bash
   git checkout -b github-pages
   git push -u origin github-pages
   ```

2. Gehe zu deinem GitHub Repository
3. Navigiere zu **Settings** → **Pages**
4. Unter **Source** wähle **GitHub Actions**
5. Der Workflow wird automatisch beim Push zum `github-pages` Branch ausgeführt

### Workflow-Verhalten

- Der Deployment-Workflow läuft **nur** auf dem `github-pages` Branch
- Du kannst den Code auf `main` entwickeln und nur die Landing Page auf `github-pages` haben
- Optional: Der Sync-Workflow (`.github/workflows/sync-to-pages.yml`) kann automatisch Änderungen von `main` auf `github-pages` pushen
  - Der Sync-Workflow ignoriert Änderungen an `README.md` (README bleibt auf `main`)
  - Um den Sync-Workflow zu deaktivieren, benenne die Datei um oder lösche sie

### Workflow-Optionen

**Option 1: Manuelles Deployment** (Empfohlen)
- Entwickle auf `main`
- Pushe manuell zum `github-pages` Branch, wenn du die Landing Page aktualisieren willst
- README bleibt auf `main`

**Option 2: Automatisches Sync** (Optional)
- Der Sync-Workflow pusht automatisch alle Änderungen (außer README) von `main` zu `github-pages`
- Aktiviert den Sync-Workflow in `.github/workflows/sync-to-pages.yml` (ist bereits aktiviert)
- Deaktiviere ihn, indem du den `on:` Block auskommentierst oder die Datei umbenennst

### Wichtiger Hinweis

Die `base` URL in `vite.config.ts` ist auf `/Backloggd-Shadcn/` eingestellt. Falls dein Repository einen anderen Namen hat, passe diesen Wert entsprechend an.

## 📁 Projektstruktur

```
├── public/
│   ├── screenshot1.png    # App Screenshots (optional)
│   ├── screenshot2.png
│   └── ...
├── src/
│   ├── components/
│   │   └── ui/          # ShadCN UI Komponenten
│   ├── lib/
│   │   └── utils.ts     # Utility-Funktionen
│   ├── App.tsx          # Hauptkomponente
│   ├── main.tsx         # Einstiegspunkt
│   └── index.css        # Globale Styles
├── .github/
│   └── workflows/
│       └── deploy.yml   # GitHub Actions Workflow
└── public/              # Statische Assets
```

## 🎨 Anpassungen

### Farben und Themes

Die Farben können in `src/index.css` angepasst werden. Die CSS-Variablen verwenden HSL-Werte.

### Inhalte

Die Landing Page Inhalte können direkt in `src/App.tsx` angepasst werden.

### Screenshots

- Screenshots im `public/` Ordner ablegen
- Benennung: `screenshot1.png` bis `screenshot6.png`
- Falls weniger Screenshots vorhanden sind, werden Platzhalter angezeigt

## ⚠️ Wichtiger Hinweis

Diese App ist **nicht offiziell** mit backloggd.com verbunden. Stelle sicher, dass du dies klar auf der Landing Page kommunizierst (ist bereits implementiert).

## 📝 License

MIT
