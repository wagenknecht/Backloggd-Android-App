import { useI18n } from "@/lib/i18n"
import { Button } from "@/components/ui/button"
import { Languages } from "lucide-react"

export function LanguageToggle() {
  const { locale, setLocale } = useI18n()

  const nextLocale = locale === "de" ? "en" : "de"
  const label = locale === "de" ? "DE" : "EN"

  return (
    <div className="flex items-center gap-2">
      <Button
        variant="ghost"
        size="sm"
        aria-label="Toggle language"
        onClick={() => setLocale(nextLocale)}
        className="px-2"
      >
        <Languages className="mr-2 h-4 w-4" />
        {label}
      </Button>
    </div>
  )
}

