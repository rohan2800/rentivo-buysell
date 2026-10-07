import { useEffect, useState } from 'react'
import { AdminLayout } from '../../components/AdminLayout'
import { Spinner } from '../../components/Spinner'
import { ApiError, api } from '../../lib/api'
import { formatPrice } from '../../lib/format'
import { useToast } from '../../lib/toast'
import type { PlanRequest, SubscriptionPlan } from '../../lib/types'

const EMPTY: PlanRequest = { name: '', price: 0, validityDays: 30, contactLimit: 10, description: '' }

function PlanForm({
  initial,
  onSaved,
  onCancel,
}: {
  initial?: SubscriptionPlan
  onSaved: () => void
  onCancel: () => void
}) {
  const toast = useToast()
  const [form, setForm] = useState<PlanRequest>(
    initial
      ? {
          name: initial.name,
          price: initial.price,
          validityDays: initial.validityDays,
          contactLimit: initial.contactLimit,
          description: initial.description ?? '',
        }
      : EMPTY,
  )
  const [saving, setSaving] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    try {
      if (initial) {
        await api.admin.plans.update(initial.id, { ...form, active: initial.active })
      } else {
        await api.admin.plans.create(form)
      }
      toast.show('Plan saved')
      onSaved()
    } catch (err) {
      toast.show(err instanceof ApiError ? err.message : 'Could not save plan', 'error')
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-3 border border-line p-4">
      <div className="grid grid-cols-2 gap-3">
        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Name
          <input
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
            className="border border-line bg-white px-3 py-2"
          />
        </label>
        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Price (₹)
          <input
            type="number"
            min={0}
            value={form.price}
            onChange={(e) => setForm({ ...form, price: Number(e.target.value) })}
            required
            className="border border-line bg-white px-3 py-2"
          />
        </label>
        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Validity (days)
          <input
            type="number"
            min={1}
            value={form.validityDays}
            onChange={(e) => setForm({ ...form, validityDays: Number(e.target.value) })}
            required
            className="border border-line bg-white px-3 py-2"
          />
        </label>
        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Contact limit
          <input
            type="number"
            min={1}
            value={form.contactLimit}
            onChange={(e) => setForm({ ...form, contactLimit: Number(e.target.value) })}
            required
            className="border border-line bg-white px-3 py-2"
          />
        </label>
      </div>
      <label className="flex flex-col gap-1 text-sm font-medium text-ink">
        Description
        <textarea
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
          rows={2}
          className="border border-line bg-white px-3 py-2"
        />
      </label>
      <div className="flex gap-2">
        <button
          type="submit"
          disabled={saving}
          className="bg-ochre px-4 py-2 font-semibold text-ink hover:bg-ochre-dark disabled:opacity-60"
        >
          {saving ? 'Saving…' : 'Save plan'}
        </button>
        <button type="button" onClick={onCancel} className="px-4 py-2 text-sm font-medium text-steel">
          Cancel
        </button>
      </div>
    </form>
  )
}

export function AdminPlansPage() {
  const toast = useToast()
  const [plans, setPlans] = useState<SubscriptionPlan[] | null>(null)
  const [editing, setEditing] = useState<SubscriptionPlan | 'new' | null>(null)

  function reload() {
    api.admin.plans.list().then(setPlans).catch(() => setPlans([]))
  }

  useEffect(reload, [])

  async function toggleActive(p: SubscriptionPlan) {
    try {
      await api.admin.plans.setActive(p.id, !p.active)
      toast.show(p.active ? 'Plan deactivated' : 'Plan activated')
      reload()
    } catch (err) {
      toast.show(err instanceof ApiError ? err.message : 'Could not update plan', 'error')
    }
  }

  if (!plans) return <AdminLayout><Spinner /></AdminLayout>

  return (
    <AdminLayout>
      <div className="flex flex-col gap-4">
        {editing === null && (
          <button
            type="button"
            onClick={() => setEditing('new')}
            className="self-start bg-ochre px-4 py-2 font-semibold text-ink hover:bg-ochre-dark"
          >
            New plan
          </button>
        )}
        {editing === 'new' && (
          <PlanForm
            onSaved={() => {
              setEditing(null)
              reload()
            }}
            onCancel={() => setEditing(null)}
          />
        )}

        <ul className="flex flex-col divide-y divide-line">
          {plans.map((p) => (
            <li key={p.id} className="flex flex-col gap-2 py-3">
              <div className="flex items-center justify-between">
                <div>
                  <span className="font-medium text-ink">{p.name}</span>{' '}
                  <span className="text-sm text-ink-soft">
                    {formatPrice(p.price, 'TOTAL')} · {p.contactLimit} contacts · {p.validityDays} days
                  </span>
                  {!p.active && <span className="ml-2 text-xs text-brick">Inactive</span>}
                </div>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => setEditing(editing === p ? null : p)}
                    className="border border-line px-3 py-1 text-xs font-medium text-steel"
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    onClick={() => toggleActive(p)}
                    className="border border-line px-3 py-1 text-xs font-medium text-brick"
                  >
                    {p.active ? 'Deactivate' : 'Activate'}
                  </button>
                </div>
              </div>
              {editing === p && (
                <PlanForm
                  initial={p}
                  onSaved={() => {
                    setEditing(null)
                    reload()
                  }}
                  onCancel={() => setEditing(null)}
                />
              )}
            </li>
          ))}
        </ul>
      </div>
    </AdminLayout>
  )
}
