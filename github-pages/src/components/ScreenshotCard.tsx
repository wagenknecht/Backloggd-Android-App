import { Card } from "@/components/ui/card"
import { Smartphone } from "lucide-react"
import { useState } from "react"

interface ScreenshotCardProps {
  src: string
  alt: string
  caption?: string
  compact?: boolean
}

const isVideoSrc = (src: string) => /\.(webm|mp4|mov)(\?.*)?$/i.test(src)

export function ScreenshotCard({ src, alt, caption, compact = false }: ScreenshotCardProps) {
  const [hasError, setHasError] = useState(false)
  const [aspectRatio, setAspectRatio] = useState<string | null>(null)
  const isVideo = isVideoSrc(src)

  return (
    <Card className={`overflow-hidden group hover:shadow-lg transition-shadow ${compact ? 'border' : 'border-2'}`}>
      <div
        className="bg-gradient-to-br from-primary/10 to-secondary/10 flex items-center justify-center relative overflow-hidden"
        style={aspectRatio ? { aspectRatio } : undefined}
      >
        {hasError ? (
          <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-primary/20 to-secondary/20">
            <Smartphone className={`${compact ? 'h-8 w-8' : 'h-12 w-12 sm:h-16 sm:w-16'} text-primary/50`} />
          </div>
        ) : isVideo ? (
          <video
            src={src}
            className="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300"
            onError={() => setHasError(true)}
            onLoadedMetadata={(e) => {
              const v = e.currentTarget
              if (v.videoWidth > 0 && v.videoHeight > 0) {
                setAspectRatio(`${v.videoWidth} / ${v.videoHeight}`)
              }
            }}
            autoPlay
            loop
            muted
            playsInline
            preload="metadata"
            aria-label={alt}
          />
        ) : (
          <img
            src={src}
            alt={alt}
            className="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300"
            onError={() => setHasError(true)}
            onLoad={(e) => {
              const img = e.currentTarget
              if (img.naturalWidth > 0 && img.naturalHeight > 0) {
                setAspectRatio(`${img.naturalWidth} / ${img.naturalHeight}`)
              }
            }}
            loading="lazy"
          />
        )}
      </div>
      {caption && (
        <div className={`${compact ? 'px-2 py-1.5 text-xs' : 'px-3 py-2 text-xs sm:text-sm'} text-center text-muted-foreground border-t`}>
          {caption}
        </div>
      )}
    </Card>
  )
}
