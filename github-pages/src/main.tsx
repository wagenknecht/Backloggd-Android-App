import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './index.css'
import { I18nProvider } from '@/lib/i18n'

// Initiales Theme anwenden, bevor die App rendert
(() => {
  try {
    const stored = localStorage.getItem('theme')
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    const useDark = stored ? stored === 'dark' : prefersDark
    const cls = document.documentElement.classList
    if (useDark) cls.add('dark')
    else cls.remove('dark')
  } catch (_) {
    // Falls localStorage nicht verfügbar ist, bleibt Standard (light)
  }
})()

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <I18nProvider>
      <App />
    </I18nProvider>
  </React.StrictMode>,
)



