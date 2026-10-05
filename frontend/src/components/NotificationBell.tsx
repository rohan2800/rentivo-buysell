import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { api } from '../lib/api'
import { useAuth } from '../lib/auth'

export function NotificationBell() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()
  const [count, setCount] = useState(0)

  useEffect(() => {
    if (!isAuthenticated) {
      setCount(0)
      return
    }
    let cancelled = false
    api.notifications
      .unreadCount()
      .then((res) => {
        if (!cancelled) setCount(res.count)
      })
      .catch(() => undefined)
    return () => {
      cancelled = true
    }
    // Refetch whenever the route changes — simplest way to pick up new notifications
    // without a websocket, since visiting /notifications marks things read.
  }, [isAuthenticated, location.pathname])

  if (!isAuthenticated) return null

  return (
    <Link to="/notifications" className="relative text-sm font-medium text-steel">
      Notifications
      {count > 0 && (
        <span className="absolute -right-3 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-brick px-1 text-[10px] font-semibold text-paper">
          {count > 9 ? '9+' : count}
        </span>
      )}
    </Link>
  )
}
