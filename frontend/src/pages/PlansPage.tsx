import { useEffect, useState } from 'react'
import { Spinner } from '../components/Spinner'
import { ApiError, api } from '../lib/api'
import { formatDate, formatPrice } from '../lib/format'
import type { SubscriptionPlan, SubscriptionStatus } from '../lib/types'

export function PlansPage() {
  const [plans, setPlans] = useState<SubscriptionPlan[] | null>(null)
  const [status, setStatus] = useState<SubscriptionStatus | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  function reload() {
    api.subscriptions.plans().then(setPlans).catch(() => setPlans([]))
    api.subscriptions.status().then(setStatus).catch(() => setStatus(null))
  }

  useEffect(reload, [])

  async function handleActivate(planId: number) {
    setBusyId(planId)
    setNotice(null)
    try {
      await api.subscriptions.activateDev(planId)
      reload()
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) {
        setNotice('Online payments are coming soon — plan activation is not open yet.')
      } else {
        setNotice(err instanceof ApiError ? err.message : 'Could not activate plan')
      }
    } finally {
      setBusyId(null)
    }
  }

  if (plans === null) return <Spinner />

  return (
    <div className="flex flex-col gap-8">
      <h1 className="font-display text-2xl font-bold text-ink">Plans</h1>

      {status && (
        <div className="border border-line p-4">
          <p className="text-sm text-ink-soft">Current status</p>
          {status.active ? (
            <p className="text-ink">
              <span className="font-semibold">{status.planName}</span> — {status.contactsRemaining}{' '}
              of {status.contactLimit} contacts left, valid until{' '}
              {status.endAt && formatDate(status.endAt)}
            </p>
          ) : (
            <p className="text-ink-soft">{status.message}</p>
          )}
        </div>
      )}

      {notice && <p className="bg-steel-soft px-3 py-2 text-sm text-steel">{notice}</p>}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {plans.map((p) => (
          <div key={p.id} className="flex flex-col gap-2 border border-line p-5">
            <span className="font-display text-lg font-semibold text-ink">{p.name}</span>
            <span className="font-display text-2xl font-bold text-ochre-dark">
              {formatPrice(p.price, 'TOTAL')}
            </span>
            <span className="text-sm text-ink-soft">
              {p.contactLimit} contacts · valid {p.validityDays} days
            </span>
            {p.description && <p className="text-sm text-ink-soft">{p.description}</p>}
            <button
              type="button"
              onClick={() => handleActivate(p.id)}
              disabled={busyId === p.id}
              className="mt-2 bg-ochre px-4 py-2 font-semibold text-ink hover:bg-ochre-dark disabled:opacity-60"
            >
              {busyId === p.id ? 'Activating…' : 'Activate'}
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}
