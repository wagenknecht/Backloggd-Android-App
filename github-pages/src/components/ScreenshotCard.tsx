import { Card } from "@/components/ui/card"
import { Smartphone } from "lucide-react"
import { useState } from "react"

interface ScreenshotCardProps {
  src: string
  alt: string
  compact?: boolean
}

export function ScreenshotCard({ src, alt, compact = false }: ScreenshotCardProps) {
  const [hasError, setHasError] = useState(false)

  return (
    <Card className={`overflow-hidden group hover:shadow-lg transition-shadow ${compact ? 'border' : 'border-2'}`}>
      <div className="aspect-[9/16] bg-gradient-to-br from-primary/10 to-secondary/10 flex items-center justify-center relative overflow-hidden">
        {!hasError ? (
          <img 
            src={src} 
            alt={alt}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            onError={() => setHasError(true)}
            loading="lazy"
          />
        ) : (
          <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-primary/20 to-secondary/20">
            <Smartphone className={`${compact ? 'h-8 w-8' : 'h-12 w-12 sm:h-16 sm:w-16'} text-primary/50`} />
          </div>
        )}
      </div>
    </Card>
  )
}
