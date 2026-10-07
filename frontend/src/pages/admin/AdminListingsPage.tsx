import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { AdminLayout } from '../../components/AdminLayout'
import { EmptyState } from '../../components/EmptyState'
import { Pagination } from '../../components/Pagination'
import { Spinner } from '../../components/Spinner'
import { StatusBadge } from '../../components/StatusBadge'
import { ApiError, api } from '../../lib/api'
import { formatDate, formatPrice } from '../../lib/format'
import { useToast } from '../../lib/toast'
import type { ListingStatus, OwnedListing } from '../../lib/types'

const STATUS_TABS: { value: ListingStatus | undefined; label: string }[] = [
  { value: 'PENDING', label: 'Pending' },
  { value: 'APPROVED', label: 'Live' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'EXPIRED', label: 'Expired' },
  { value: undefined, label: 'All' },
]

export function AdminListingsPage() {
  const toast = useToast()
  const [status, setStatus] = useState<ListingStatus | undefined>('PENDING')
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<{ content: OwnedListing[]; totalPages: number } | null>(null)
  const [rejectingId, setRejectingId] = useState<number | null>(null)
  const [reason, setReason] = useState('')
  const [busyId, setBusyId] = useState<number | null>(null)

  function reload() {
    setResult(null)
    api.admin.listings
      .list(status, page, 20)
      .then((res) => setResult({ content: res.content, totalPages: res.totalPages }))
      .catch(() => setResult({ content: [], totalPages: 0 }))
  }

  useEffect(reload, [status, page])

  async function handleApprove(id: number) {
    setBusyId(id)
    try {
      await api.admin.listings.decide(id, 'APPROVED')
      toast.show('Listing approved')
      reload()
    } catch (err) {
      toast.show(err instanceof ApiError ? err.message : 'Could not approve', 'error')
    } finally {
      setBusyId(null)
    }
  }

  async function handleReject(id: number) {
    if (!reason.trim()) {
      toast.show('A reason is required to reject a listing', 'error')
      return
    }
    setBusyId(id)
    try {
      await api.admin.listings.decide(id, 'REJECTED', reason.trim())
      toast.show('Listing rejected')
      setRejectingId(null)
      setReason('')
      reload()
    } catch (err) {
      toast.show(err instanceof ApiError ? err.message : 'Could not reject', 'error')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <AdminLayout>
      <div className="flex flex-col gap-4">
        <div className="flex gap-2">
          {STATUS_TABS.map((t) => (
            <button
              key={t.label}
              type="button"
              onClick={() => {
                setStatus(t.value)
                setPage(0)
              }}
              className={`px-3 py-1.5 text-sm font-medium ${
                status === t.value ? 'bg-ink text-paper' : 'bg-paper-raised text-ink-soft'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>

        {result === null && <Spinner />}
        {result?.content.length === 0 && <EmptyState title="Nothing here" />}

        <ul className="flex flex-col divide-y divide-line">
          {result?.content.map((l) => (
            <li key={l.id} className="flex flex-col gap-2 py-4">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <Link to={`/listings/${l.id}`} className="font-medium text-ink hover:underline">
                    {l.title}
                  </Link>
                  <p className="text-sm text-ink-soft">
                    {formatPrice(l.price, l.priceUnit)} · {l.city} · {formatDate(l.createdAt)}
                  </p>
                  {l.rejectionReason && (
                    <p className="text-sm text-brick">Reason: {l.rejectionReason}</p>
                  )}
                </div>
                <div className="flex flex-shrink-0 items-center gap-2">
                  <StatusBadge status={l.status} />
                  {l.status === 'PENDING' && (
                    <>
                      <button
                        type="button"
                        onClick={() => handleApprove(l.id)}
                        disabled={busyId === l.id}
                        className="border border-line px-3 py-1.5 text-sm font-medium text-steel"
                      >
                        Approve
                      </button>
                      <button
                        type="button"
                        onClick={() => setRejectingId(rejectingId === l.id ? null : l.id)}
                        disabled={busyId === l.id}
                        className="border border-line px-3 py-1.5 text-sm font-medium text-brick"
                      >
                        Reject
                      </button>
                    </>
                  )}
                </div>
              </div>
              {rejectingId === l.id && (
                <div className="flex gap-2">
                  <input
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    placeholder="Reason for rejection"
                    className="flex-1 border border-line bg-white px-3 py-1.5 text-sm"
                  />
                  <button
                    type="button"
                    onClick={() => handleReject(l.id)}
                    disabled={busyId === l.id}
                    className="bg-brick px-3 py-1.5 text-sm font-medium text-paper"
                  >
                    Confirm reject
                  </button>
                </div>
              )}
            </li>
          ))}
        </ul>

        {result && <Pagination page={page} totalPages={result.totalPages} onChange={setPage} />}
      </div>
    </AdminLayout>
  )
}
