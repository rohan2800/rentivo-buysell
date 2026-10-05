// A minimal pub-sub so api.ts can report network reachability and any component (just Layout,
// in practice) can subscribe without needing a React context wired through every call site.

type Listener = (reachable: boolean) => void

let reachable = true
const listeners = new Set<Listener>()

export function reportReachable() {
  if (!reachable) {
    reachable = true
    listeners.forEach((l) => l(true))
  }
}

export function reportUnreachable() {
  if (reachable) {
    reachable = false
    listeners.forEach((l) => l(false))
  }
}

export function subscribeConnectivity(listener: Listener): () => void {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export function isReachable(): boolean {
  return reachable
}
