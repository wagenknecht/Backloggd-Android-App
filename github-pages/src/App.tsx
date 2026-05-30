import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { ScreenshotCard } from "@/components/ScreenshotCard"
import { MobileNav } from "@/components/MobileNav"
import { githubUrls, config } from "@/config"
import { RepoStats } from "@/components/RepoStats"
import { Smartphone, Download, Github, ExternalLink, Star, BellRing, CalendarCheck, Clock, Maximize, AppWindow, Shield } from "lucide-react"
import { ThemeToggle } from "@/components/ThemeToggle"
import { useI18n } from "@/lib/i18n"
import { LanguageToggle } from "@/components/LanguageToggle"

const screenshots = [
  { src: "navbar.webm", captionKey: "screenshots.caption.navbar" },
  { src: "sidebar.webm", captionKey: "screenshots.caption.sidebar" },
  { src: "quick-search.webm", captionKey: "screenshots.caption.quicksearch" },
  { src: "screenshot2.png", captionKey: "screenshots.caption.notifications" },
] as const

function App() {
  const { t } = useI18n()
  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-secondary/20">
      {/* Navigation - Mobile First */}
      <nav className="sticky top-0 z-30 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container mx-auto px-4 py-2 sm:py-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Smartphone className="h-5 w-5 sm:h-6 sm:w-6 text-primary" />
              <span className="text-lg sm:text-xl font-bold">{t("nav.brand")}</span>
            </div>
            {/* Desktop Navigation */}
            <div className="hidden md:flex items-center space-x-4">
              <a href="#screenshots">
                <Button variant="ghost" size="sm">{t("nav.screenshots")}</Button>
              </a>
              <a href="#features">
                <Button variant="ghost" size="sm">{t("nav.features")}</Button>
              </a>
              <a href="#download">
                <Button size="sm">{t("nav.download")}</Button>
              </a>
              <ThemeToggle />
              <LanguageToggle />
            </div>
            {/* Mobile Navigation */}
            <MobileNav />
          </div>
        </div>
      </nav>

      {/* Hero Section - Mobile First */}
      <section className="container mx-auto px-4 pt-8 sm:pt-10 md:pt-12 pb-4 sm:pb-6 md:pb-8">
        <div className="max-w-3xl mx-auto space-y-3 sm:space-y-4 text-center">
          <div className="inline-flex items-center rounded-full border px-2 py-0.5 text-xs sm:text-sm">
            <Star className="mr-1.5 h-3 w-3 sm:h-4 sm:w-4 fill-primary text-primary" />
            <span>{t("hero.badge")}</span>
          </div>
          <h1 className="text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-bold tracking-tight leading-tight">
            {t("hero.title.line1")}
            <br />
            <span className="text-primary">{t("hero.title.line2")}</span>
          </h1>
          <p className="text-base sm:text-lg md:text-xl text-muted-foreground max-w-2xl mx-auto px-4">
            {t("hero.description")}
          </p>
          <p className="text-xs sm:text-sm text-muted-foreground/80 max-w-2xl mx-auto px-4">
            {t("hero.disclaimer")}
          </p>
          <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-center pt-4 sm:pt-6 px-4">
            <a href={githubUrls.latestRelease} className="w-full sm:w-auto">
              <Button size="lg" className="w-full sm:w-auto text-base sm:text-lg px-6 sm:px-8">
                <Download className="mr-2 h-4 w-4 sm:h-5 sm:w-5" />
                {t("hero.cta.download_now")}
              </Button>
            </a>
            <a href={githubUrls.repository} target="_blank" rel="noopener noreferrer" className="w-full sm:w-auto">
              <Button size="lg" variant="outline" className="w-full sm:w-auto text-base sm:text-lg px-6 sm:px-8">
                <Github className="mr-2 h-4 w-4 sm:h-5 sm:w-5" />
                {t("hero.cta.github")}
              </Button>
            </a>
          </div>
        </div>
      </section>

      {/* Screenshots Section - Mobile First */}
      <section id="screenshots" className="container mx-auto px-4 py-12 sm:py-16 md:py-20">
        <div className="text-center mb-8 sm:mb-12 md:mb-16">
          <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold mb-2 sm:mb-4">{t("screenshots.title")}</h2>
          <p className="text-sm sm:text-base md:text-lg text-muted-foreground max-w-2xl mx-auto px-4">
            {t("screenshots.subtitle")}
          </p>
        </div>
        
        {/* Mobile: Horizontal Scroll */}
        <div className="sm:hidden">
          <div className="flex gap-3 overflow-x-auto pb-4 -mx-4 px-4 snap-x snap-mandatory scrollbar-hide">
            {screenshots.map(({ src, captionKey }) => (
              <div key={src} className="flex-shrink-0 w-[260px] snap-center">
                <ScreenshotCard
                  src={`${import.meta.env.BASE_URL}${src}`}
                  alt={t(captionKey)}
                  caption={t(captionKey)}
                  compact={true}
                />
              </div>
            ))}
          </div>
        </div>

        {/* Desktop: Grid */}
        <div className="hidden sm:grid sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 max-w-6xl mx-auto">
          {screenshots.map(({ src, captionKey }) => (
            <ScreenshotCard
              key={src}
              src={`${import.meta.env.BASE_URL}${src}`}
              alt={t(captionKey)}
              caption={t(captionKey)}
            />
          ))}
        </div>
      </section>

      {/* Features Section - Mobile First - App Features */}
      <section id="features" className="container mx-auto px-4 py-12 sm:py-16 md:py-20">
        <div className="text-center mb-8 sm:mb-12 md:mb-16">
          <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold mb-2 sm:mb-4">{t("features.title")}</h2>
          <p className="text-sm sm:text-base md:text-lg text-muted-foreground max-w-2xl mx-auto px-4">
            {t("features.subtitle")}
          </p>
        </div>
        
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6 max-w-5xl mx-auto">
          <Card>
            <CardHeader className="pb-4">
              <div className="h-10 w-10 sm:h-12 sm:w-12 rounded-lg bg-primary/10 flex items-center justify-center mb-3 sm:mb-4">
                <BellRing className="h-5 w-5 sm:h-6 sm:w-6 text-primary" />
              </div>
              <CardTitle className="text-lg sm:text-xl">{t("features.push.title")}</CardTitle>
              <CardDescription className="text-sm sm:text-base">
                {t("features.push.desc")}
              </CardDescription>
            </CardHeader>
          </Card>

          <Card>
            <CardHeader className="pb-4">
              <div className="h-10 w-10 sm:h-12 sm:w-12 rounded-lg bg-primary/10 flex items-center justify-center mb-3 sm:mb-4">
                <CalendarCheck className="h-5 w-5 sm:h-6 sm:w-6 text-primary" />
              </div>
              <CardTitle className="text-lg sm:text-xl">{t("features.release.title")}</CardTitle>
              <CardDescription className="text-sm sm:text-base">
                {t("features.release.desc")}
              </CardDescription>
            </CardHeader>
          </Card>

          <Card>
            <CardHeader className="pb-4">
              <div className="h-10 w-10 sm:h-12 sm:w-12 rounded-lg bg-primary/10 flex items-center justify-center mb-3 sm:mb-4">
                <Clock className="h-5 w-5 sm:h-6 sm:w-6 text-primary" />
              </div>
              <CardTitle className="text-lg sm:text-xl">{t("features.interval.title")}</CardTitle>
              <CardDescription className="text-sm sm:text-base">
                {t("features.interval.desc")}
              </CardDescription>
            </CardHeader>
          </Card>

          <Card>
            <CardHeader className="pb-4">
              <div className="h-10 w-10 sm:h-12 sm:w-12 rounded-lg bg-primary/10 flex items-center justify-center mb-3 sm:mb-4">
                <Maximize className="h-5 w-5 sm:h-6 sm:w-6 text-primary" />
              </div>
              <CardTitle className="text-lg sm:text-xl">{t("features.fullscreen.title")}</CardTitle>
              <CardDescription className="text-sm sm:text-base">
                {t("features.fullscreen.desc")}
              </CardDescription>
            </CardHeader>
          </Card>

          <Card>
            <CardHeader className="pb-4">
              <div className="h-10 w-10 sm:h-12 sm:w-12 rounded-lg bg-primary/10 flex items-center justify-center mb-3 sm:mb-4">
                <AppWindow className="h-5 w-5 sm:h-6 sm:w-6 text-primary" />
              </div>
              <CardTitle className="text-lg sm:text-xl">{t("features.icon.title")}</CardTitle>
              <CardDescription className="text-sm sm:text-base">
                {t("features.icon.desc")}
              </CardDescription>
            </CardHeader>
          </Card>

          <Card>
            <CardHeader className="pb-4">
              <div className="h-10 w-10 sm:h-12 sm:w-12 rounded-lg bg-primary/10 flex items-center justify-center mb-3 sm:mb-4">
                <Shield className="h-5 w-5 sm:h-6 sm:w-6 text-primary" />
              </div>
              <CardTitle className="text-lg sm:text-xl">{t("features.private.title")}</CardTitle>
              <CardDescription className="text-sm sm:text-base">
                {t("features.private.desc")}
              </CardDescription>
            </CardHeader>
          </Card>
        </div>
      </section>

      {/* Download Section - Mobile First */}
      <section id="download" className="container mx-auto px-4 py-12 sm:py-16 md:py-20">
        <div className="max-w-4xl mx-auto">
          <Card className="bg-gradient-to-r from-primary/10 to-secondary/10 border-primary/20">
            <CardHeader className="text-center pb-4 sm:pb-6">
              <CardTitle className="text-2xl sm:text-3xl mb-3 sm:mb-4">{t("download.title")}</CardTitle>
              <CardDescription className="text-sm sm:text-base md:text-lg px-4">
                {t("download.subtitle")}
              </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-center pb-4 sm:pb-6 px-4">
              <a href={githubUrls.latestRelease} target="_blank" rel="noopener noreferrer" className="w-full sm:w-auto">
                <Button size="lg" className="w-full sm:w-auto text-base sm:text-lg px-6 sm:px-8">
                  <Download className="mr-2 h-4 w-4 sm:h-5 sm:w-5" />
                  {t("download.button.apk")}
                </Button>
              </a>
              <a href={githubUrls.repository} target="_blank" rel="noopener noreferrer" className="w-full sm:w-auto">
                <Button size="lg" variant="outline" className="w-full sm:w-auto text-base sm:text-lg px-6 sm:px-8">
                  <Github className="mr-2 h-4 w-4 sm:h-5 sm:w-5" />
                  {t("hero.cta.github")}
                  <ExternalLink className="ml-2 h-3 w-3 sm:h-4 sm:w-4" />
                </Button>
              </a>
            </CardContent>
            <CardFooter className="justify-center">
              <RepoStats owner={config.github.username} repo={config.github.repository} />
            </CardFooter>
          </Card>
        </div>
      </section>

      {/* Footer - Mobile First */}
      <footer className="border-t bg-background">
        <div className="container mx-auto px-4 py-8 sm:py-12">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 sm:gap-8">
            <div className="space-y-3 sm:space-y-4">
              <div className="flex items-center space-x-2">
                <Smartphone className="h-5 w-5 sm:h-6 sm:w-6 text-primary" />
                <span className="text-lg sm:text-xl font-bold">{t("footer.brand")}</span>
              </div>
              <p className="text-xs sm:text-sm text-muted-foreground">{t("footer.tagline")}</p>
              <p className="text-xs text-muted-foreground/80">{t("footer.disclaimer")}</p>
            </div>
            
            <div>
              <h3 className="font-semibold mb-3 sm:mb-4 text-sm sm:text-base">{t("footer.section.product")}</h3>
              <ul className="space-y-2 text-xs sm:text-sm text-muted-foreground">
                <li><a href="#screenshots" className="hover:text-foreground">{t("footer.link.screenshots")}</a></li>
                <li><a href="#features" className="hover:text-foreground">{t("footer.link.features")}</a></li>
                <li><a href="#download" className="hover:text-foreground">{t("footer.link.download")}</a></li>
              </ul>
            </div>
            
            <div>
              <h3 className="font-semibold mb-3 sm:mb-4 text-sm sm:text-base">{t("footer.section.development")}</h3>
              <ul className="space-y-2 text-xs sm:text-sm text-muted-foreground">
                <li>
                  <a href={githubUrls.repository} target="_blank" rel="noopener noreferrer" className="hover:text-foreground flex items-center">
                    {t("footer.link.github")}
                    <ExternalLink className="ml-1 h-3 w-3" />
                  </a>
                </li>
                <li>
                  <a href={githubUrls.releases} target="_blank" rel="noopener noreferrer" className="hover:text-foreground flex items-center">
                    {t("footer.link.releases")}
                    <ExternalLink className="ml-1 h-3 w-3" />
                  </a>
                </li>
              </ul>
            </div>
            
            <div>
              <h3 className="font-semibold mb-3 sm:mb-4 text-sm sm:text-base">{t("footer.section.info")}</h3>
              <ul className="space-y-2 text-xs sm:text-sm text-muted-foreground">
                <li>
                  <a href="https://www.backloggd.com" target="_blank" rel="noopener noreferrer" className="hover:text-foreground flex items-center">
                    {t("footer.link.backloggd")}
                    <ExternalLink className="ml-1 h-3 w-3" />
                  </a>
                </li>
                <li className="text-muted-foreground/80">{t("footer.extends")}</li>
              </ul>
            </div>
          </div>

          <div className="mt-8 sm:mt-12 pt-6 sm:pt-8 border-t text-center text-xs sm:text-sm text-muted-foreground">
            <p>{t("footer.copyright")}</p>
            <p className="mt-1">{t("footer.disclaimer.line")}</p>
          </div>
        </div>
      </footer>
    </div>
  )
}

export default App
