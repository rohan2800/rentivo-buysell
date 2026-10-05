import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { EmptyState } from '../components/EmptyState'
import { Spinner } from '../components/Spinner'
import { api } from '../lib/api'
import { formatRelative } from '../lib/format'
import type { AppNotification } from '../lib/types'

export function NotificationsPage() {
  const [items, setItems] = useState<AppNotification[] | null>(null)

  function reload() {
    api.notifications
      .mine(0, 50)
      .then((res) => setItems(res.content))
      .catch(() => setItems([]))
  }

  useEffect(reload, [])

  async function handleMarkRead(id: number) {
    await api.notifications.markRead(id)
    setItems((prev) => prev?.map((n) => (n.id === id ? { ...n, read: true } : n)) ?? null)
  }

  async function handleMarkAll() {
    await api.notifications.markAllRead()
    setItems((prev) => prev?.map((n) => ({ ...n, read: true })) ?? null)
  }

  if (items === null) return <Spinner />

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="font-display text-2xl font-bold text-ink">Notifications</h1>
        {items.some((n) => !n.read) && (
          <button type="button" onClick={handleMarkAll} className="text-sm font-medium text-steel">
            Mark all read
          </button>
        )}
      </div>

      {items.length === 0 && <EmptyState title="Nothing here yet" />}

      <ul className="flex flex-col divide-y divide-line">
        {items.map((n) => (
          <li
            key={n.id}
            className={`flex items-start justify-between gap-4 py-4 ${n.read ? '' : 'bg-ochre/5'}`}
          >
            <div>
              <p className="font-medium text-ink">{n.title}</p>
              <p className="text-sm text-ink-soft">{n.body}</p>
              <div className="mt-1 flex gap-3 text-xs text-ink-soft">
                <span>{formatRelative(n.createdAt)}</span>
                {n.listingId && (
                  <Link to={`/listings/${n.listingId}`} className="text-steel underline">
                    View listing
                  </Link>
                )}
              </div>
            </div>
            {!n.read && (
              <button
                type="button"
                onClick={() => handleMarkRead(n.id)}
                className="flex-shrink-0 text-xs font-medium text-steel"
              >
                Mark read
              </button>
            )}
          </li>
        ))}
      </ul>
    </div>
  )
}
