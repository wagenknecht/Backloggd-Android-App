import { useEffect, useState } from "react"
import { Download, Tag } from "lucide-react"

interface RepoStatsProps {
  owner: string
  repo: string
}

type GitHubTag = {
  name: string
}

type GitHubRelease = {
  assets: { download_count: number }[]
}

export function RepoStats({ owner, repo }: RepoStatsProps) {
  const [latestTag, setLatestTag] = useState<string | null>(null)
  const [totalDownloads, setTotalDownloads] = useState<number | null>(null)
  const [error, setError] = useState<boolean>(false)

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch tags (latest is first)
        const tagsRes = await fetch(`https://api.github.com/repos/${owner}/${repo}/tags?per_page=1`)
        if (!tagsRes.ok) throw new Error("Failed to fetch tags")
        const tags: GitHubTag[] = await tagsRes.json()
        setLatestTag(tags[0]?.name ?? null)

        // Fetch releases with up to 100 items and sum asset download counts
        const releasesRes = await fetch(`https://api.github.com/repos/${owner}/${repo}/releases?per_page=100`)
        if (!releasesRes.ok) throw new Error("Failed to fetch releases")
        const releases: GitHubRelease[] = await releasesRes.json()
        const total = releases.reduce((sum, rel) => sum + rel.assets.reduce((s, a) => s + a.download_count, 0), 0)
        setTotalDownloads(total)
      } catch (e) {
        setError(true)
      }
    }
    fetchData()
  }, [owner, repo])

  return (
    <div className="flex items-center gap-4 flex-wrap justify-center">
      <div className="inline-flex items-center rounded-full border px-2.5 py-1 text-xs sm:text-sm">
        <Tag className="mr-1.5 h-3 w-3 sm:h-4 sm:w-4 text-primary" />
        <span>
          Aktuelle Version: {latestTag ?? (error ? "–" : "lädt…")}
        </span>
      </div>
      <div className="inline-flex items-center rounded-full border px-2.5 py-1 text-xs sm:text-sm">
        <Download className="mr-1.5 h-3 w-3 sm:h-4 sm:w-4 text-primary" />
        <span>
          Downloads insgesamt: {totalDownloads ?? (error ? "–" : "lädt…")}
        </span>
      </div>
    </div>
  )
}

