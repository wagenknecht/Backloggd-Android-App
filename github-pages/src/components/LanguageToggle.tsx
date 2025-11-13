import { useI18n } from "@/lib/i18n"
import { Languages } from "lucide-react"

export function LanguageToggle() {
  const { locale, setLocale } = useI18n()

  return (
    <div className="flex items-center gap-2">
      <Languages className="h-4 w-4" />
      <select
        aria-label="Select language"
        className="bg-transparent border rounded px-2 py-1 text-sm"
        value={locale}
        onChange={(e) => setLocale(e.target.value as any)}
      >
        <option value="en">EN</option>
        <option value="de">DE</option>
        <option value="es">ES</option>
        <option value="fr">FR</option>
      </select>
    </div>
  )
}

