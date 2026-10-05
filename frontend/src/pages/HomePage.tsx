import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { CategoryTabs } from '../components/CategoryTabs'
import { EmptyState } from '../components/EmptyState'
import { ListingCard } from '../components/ListingCard'
import { Spinner } from '../components/Spinner'
import { api } from '../lib/api'
import type { ListingSummary } from '../lib/types'

export function HomePage() {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [recent, setRecent] = useState<ListingSummary[] | null>(null)

  useEffect(() => {
    api.listings
      .browse({ page: 0, size: 8 })
      .then((res) => setRecent(res.content))
      .catch(() => setRecent([]))
  }, [])

  function goBrowse(params: { q?: string; categoryId?: number }) {
    const search = new URLSearchParams()
    if (params.q) search.set('q', params.q)
    if (params.categoryId) search.set('categoryId', String(params.categoryId))
    navigate(`/browse?${search.toString()}`)
  }

  return (
    <div className="flex flex-col gap-12">
      <section className="flex flex-col gap-5 border-b border-line pb-10">
        <h1 className="font-display text-3xl font-bold text-ink sm:text-4xl">
          Find what's near you.
        </h1>
        <form
          onSubmit={(e) => {
            e.preventDefault()
            goBrowse({ q: query })
          }}
          className="flex max-w-xl gap-2"
        >
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search listings — 2BHK, sofa, cement..."
            className="flex-1 border border-line bg-white px-4 py-2.5 text-ink placeholder:text-ink-soft focus:border-ochre-dark"
          />
          <button
            type="submit"
            className="bg-ochre px-5 py-2.5 font-semibold text-ink hover:bg-ochre-dark"
          >
            Search
          </button>
        </form>
        <CategoryTabs onSelect={(id) => goBrowse({ categoryId: id })} />
      </section>

      <section>
        <h2 className="mb-4 font-display text-xl font-semibold text-ink">Recently listed</h2>
        {recent === null && <Spinner />}
        {recent?.length === 0 && (
          <EmptyState title="No listings yet" body="Be the first to post one — it's free." />
        )}
        {recent && recent.length > 0 && (
          <div className="grid grid-cols-2 gap-x-6 gap-y-8 sm:grid-cols-3 lg:grid-cols-4">
            {recent.map((l) => (
              <ListingCard key={l.id} listing={l} />
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
