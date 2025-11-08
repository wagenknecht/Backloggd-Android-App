import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Menu, X } from "lucide-react"

export function MobileNav() {
  const [isOpen, setIsOpen] = useState(false)

  const navItems = [
    { href: "#screenshots", label: "Screenshots" },
    { href: "#features", label: "Features" },
    { href: "#download", label: "Download" },
  ]

  return (
    <>
      <Button
        variant="ghost"
        size="icon"
        className="md:hidden"
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Toggle menu"
      >
        {isOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
      </Button>

      {isOpen && (
        <>
          <div
            className="fixed inset-0 bg-black/50 z-40 md:hidden animate-in fade-in"
            onClick={() => setIsOpen(false)}
          />
          <nav className="fixed top-[57px] sm:top-[65px] left-0 right-0 bg-background border-b z-50 md:hidden shadow-lg">
            <div className="container mx-auto px-4 py-4 space-y-2">
              {navItems.map((item) => (
                <a
                  key={item.href}
                  href={item.href}
                  onClick={() => setIsOpen(false)}
                  className="block py-3 px-4 rounded-md hover:bg-accent transition-colors text-base font-medium"
                >
                  {item.label}
                </a>
              ))}
              <a
                href="#download"
                onClick={() => setIsOpen(false)}
                className="block mt-4"
              >
                <Button className="w-full">Download</Button>
              </a>
            </div>
          </nav>
        </>
      )}
    </>
  )
}

