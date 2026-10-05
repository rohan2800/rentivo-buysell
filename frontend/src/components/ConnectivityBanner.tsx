import { useEffect, useState } from 'react'
import { isReachable, subscribeConnectivity } from '../lib/connectivity'

export function ConnectivityBanner() {
  const [reachable, setReachable] = useState(isReachable())

  useEffect(() => subscribeConnectivity(setReachable), [])

  if (reachable) return null

  return (
    <div className="bg-brick px-4 py-2 text-center text-sm font-medium text-paper">
      Can't reach the server. Check that the backend is running and try again.
    </div>
  )
}
