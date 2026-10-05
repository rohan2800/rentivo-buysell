import { useEffect, useState } from 'react'
import { EmptyState } from '../components/EmptyState'
import { ListingCard } from '../components/ListingCard'
import { Pagination } from '../components/Pagination'
import { Spinner } from '../components/Spinner'
import { api } from '../lib/api'
import type { ListingSummary } from '../lib/types'

export function FavoritesPage() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<{ content: ListingSummary[]; totalPages: number } | null>(null)

  useEffect(() => {
    setResult(null)
    api.favorites
      .mine(page, 12)
      .then((res) => setResult({ content: res.content, totalPages: res.totalPages }))
      .catch(() => setResult({ content: [], totalPages: 0 }))
  }, [page])

  return (
    <div className="flex flex-col gap-6">
      <h1 className="font-display text-2xl font-bold text-ink">Favorites</h1>
      {result === null && <Spinner />}
      {result?.content.length === 0 && (
        <EmptyState title="No favorites yet" body="Save listings you like while browsing." />
      )}
      {result && result.content.length > 0 && (
        <>
          <div className="grid grid-cols-2 gap-x-6 gap-y-8 sm:grid-cols-3 lg:grid-cols-4">
            {result.content.map((l) => (
              <ListingCard key={l.id} listing={l} />
            ))}
          </div>
          <Pagination page={page} totalPages={result.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  )
}
