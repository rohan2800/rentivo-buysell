import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { CategoryTabs } from '../components/CategoryTabs'
import { EmptyState } from '../components/EmptyState'
import { ListingCard } from '../components/ListingCard'
import { Pagination } from '../components/Pagination'
import { Spinner } from '../components/Spinner'
import { api } from '../lib/api'
import type { ListingSummary, ListingType } from '../lib/types'

export function BrowsePage() {
  const [params, setParams] = useSearchParams()
  const [result, setResult] = useState<{ content: ListingSummary[]; totalPages: number } | null>(null)

  const categoryId = params.get('categoryId') ? Number(params.get('categoryId')) : undefined
  const q = params.get('q') ?? ''
  const city = params.get('city') ?? ''
  const type = (params.get('type') as ListingType | null) ?? undefined
  const minPrice = params.get('minPrice') ? Number(params.get('minPrice')) : undefined
  const maxPrice = params.get('maxPrice') ? Number(params.get('maxPrice')) : undefined
  const page = params.get('page') ? Number(params.get('page')) : 0

  useEffect(() => {
    setResult(null)
    api.listings
      .browse({
        categoryId,
        q: q || undefined,
        city: city || undefined,
        type,
        minPrice,
        maxPrice,
        page,
        size: 12,
      })
      .then((res) => setResult({ content: res.content, totalPages: res.totalPages }))
      .catch(() => setResult({ content: [], totalPages: 0 }))
  }, [categoryId, q, city, type, minPrice, maxPrice, page])

  function update(next: Record<string, string | undefined>) {
    const merged = new URLSearchParams(params)
    for (const [key, value] of Object.entries(next)) {
      if (value) merged.set(key, value)
      else merged.delete(key)
    }
    merged.delete('page')
    setParams(merged)
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-4 border-b border-line pb-6">
        <CategoryTabs
          selectedId={categoryId}
          onSelect={(id) => update({ categoryId: id ? String(id) : undefined })}
        />
        <div className="flex flex-wrap gap-3">
          <input
            defaultValue={q}
            onBlur={(e) => update({ q: e.target.value })}
            placeholder="Search"
            className="border border-line bg-white px-3 py-2 text-sm"
          />
          <input
            defaultValue={city}
            onBlur={(e) => update({ city: e.target.value })}
            placeholder="City"
            className="border border-line bg-white px-3 py-2 text-sm"
          />
          <select
            value={type ?? ''}
            onChange={(e) => update({ type: e.target.value || undefined })}
            className="border border-line bg-white px-3 py-2 text-sm"
          >
            <option value="">Rent or sale</option>
            <option value="RENT">For rent</option>
            <option value="SALE">For sale</option>
          </select>
          <input
            type="number"
            min={0}
            defaultValue={minPrice ?? ''}
            onBlur={(e) => update({ minPrice: e.target.value || undefined })}
            placeholder="Min price"
            className="w-28 border border-line bg-white px-3 py-2 text-sm"
          />
          <input
            type="number"
            min={0}
            defaultValue={maxPrice ?? ''}
            onBlur={(e) => update({ maxPrice: e.target.value || undefined })}
            placeholder="Max price"
            className="w-28 border border-line bg-white px-3 py-2 text-sm"
          />
        </div>
      </div>

      {result === null && <Spinner />}
      {result?.content.length === 0 && (
        <EmptyState title="No listings match" body="Try a different search or city." />
      )}
      {result && result.content.length > 0 && (
        <>
          <div className="grid grid-cols-2 gap-x-6 gap-y-8 sm:grid-cols-3 lg:grid-cols-4">
            {result.content.map((l) => (
              <ListingCard key={l.id} listing={l} />
            ))}
          </div>
          <Pagination
            page={page}
            totalPages={result.totalPages}
            onChange={(p) => update({ page: String(p) })}
          />
        </>
      )}
    </div>
  )
}
