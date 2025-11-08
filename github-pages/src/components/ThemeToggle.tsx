import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Moon, Sun } from "lucide-react"

export function ThemeToggle() {
  const [isDark, setIsDark] = useState(false)

  useEffect(() => {
    const stored = localStorage.getItem("theme")
    const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches
    const initialDark = stored ? stored === "dark" : prefersDark
    setIsDark(initialDark)
  }, [])

  useEffect(() => {
    const root = document.documentElement.classList
    if (isDark) {
      root.add("dark")
      localStorage.setItem("theme", "dark")
    } else {
      root.remove("dark")
      localStorage.setItem("theme", "light")
    }
  }, [isDark])

  return (
    <Button
      variant="ghost"
      size="icon"
      aria-label={isDark ? "Wechsel zu Hell" : "Wechsel zu Dunkel"}
      onClick={() => setIsDark((v) => !v)}
    >
      {isDark ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
    </Button>
  )
}

