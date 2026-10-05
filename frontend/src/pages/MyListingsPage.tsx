import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { EmptyState } from '../components/EmptyState'
import { Spinner } from '../components/Spinner'
import { StatusBadge } from '../components/StatusBadge'
import { ApiError, api } from '../lib/api'
import { formatPrice } from '../lib/format'
import { useToast } from '../lib/toast'
import type { OwnedListing } from '../lib/types'

export function MyListingsPage() {
  const toast = useToast()
  const [listings, setListings] = useState<OwnedListing[] | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)

  function reload() {
    api.listings.mine().then(setListings).catch(() => setListings([]))
  }

  useEffect(reload, [])

  async function handleDelete(id: number) {
    if (!confirm('Delete this listing? This cannot be undone.')) return
    setBusyId(id)
    try {
      await api.listings.remove(id)
      reload()
      toast.show('Listing deleted')
    } catch (err) {
      toast.show(err instanceof ApiError ? err.message : 'Could not delete listing', 'error')
    } finally {
      setBusyId(null)
    }
  }

  async function handleRenew(id: number) {
    setBusyId(id)
    try {
      await api.listings.renew(id)
      reload()
      toast.show('Listing renewed')
    } catch (err) {
      toast.show(err instanceof ApiError ? err.message : 'Could not renew listing', 'error')
    } finally {
      setBusyId(null)
    }
  }

  if (listings === null) return <Spinner />

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="font-display text-2xl font-bold text-ink">My listings</h1>
        <Link to="/listings/new" className="bg-ochre px-4 py-2 font-semibold text-ink hover:bg-ochre-dark">
          Post a listing
        </Link>
      </div>

      {listings.length === 0 && (
        <EmptyState title="You haven't posted anything yet" body="Posting is free." />
      )}

      <ul className="flex flex-col divide-y divide-line">
        {listings.map((l) => (
          <li key={l.id} className="flex items-center gap-4 py-4">
            <div className="h-16 w-16 flex-shrink-0 overflow-hidden rounded bg-paper-raised">
              {l.images[0] && (
                <img src={l.images[0].url} alt="" className="h-full w-full object-cover" />
              )}
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2">
                <span className="font-medium text-ink">{l.title}</span>
                <StatusBadge status={l.status} />
              </div>
              <p className="text-sm text-ink-soft">
                {formatPrice(l.price, l.priceUnit)} · {l.city}
              </p>
              {l.status === 'REJECTED' && l.rejectionReason && (
                <p className="text-sm text-brick">Reason: {l.rejectionReason}</p>
              )}
            </div>
            <div className="flex gap-2">
              {(l.status === 'APPROVED' || l.status === 'EXPIRED') && (
                <button
                  type="button"
                  onClick={() => handleRenew(l.id)}
                  disabled={busyId === l.id}
                  className="border border-line px-3 py-1.5 text-sm font-medium text-steel"
                >
                  Renew
                </button>
              )}
              <Link
                to={`/listings/${l.id}/edit`}
                className="border border-line px-3 py-1.5 text-sm font-medium text-steel"
              >
                Edit
              </Link>
              <button
                type="button"
                onClick={() => handleDelete(l.id)}
                disabled={busyId === l.id}
                className="border border-line px-3 py-1.5 text-sm font-medium text-brick"
              >
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}
