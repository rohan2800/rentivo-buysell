import { useEffect, useState } from 'react'
import { AdminLayout } from '../../components/AdminLayout'
import { Spinner } from '../../components/Spinner'
import { api } from '../../lib/api'
import type { AdminDashboard } from '../../lib/types'

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="border border-line p-4">
      <p className="text-xs text-ink-soft">{label}</p>
      <p className="font-display text-2xl font-bold text-ink">{value}</p>
    </div>
  )
}

export function AdminDashboardPage() {
  const [data, setData] = useState<AdminDashboard | null>(null)

  useEffect(() => {
    api.admin.dashboard().then(setData).catch(() => undefined)
  }, [])

  return (
    <AdminLayout>
      {!data ? (
        <Spinner />
      ) : (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
          <Stat label="Total users" value={data.totalUsers} />
          <Stat label="Active users" value={data.activeUsers} />
          <Stat label="Total listings" value={data.totalListings} />
          <Stat label="Pending review" value={data.pendingListings} />
          <Stat label="Live listings" value={data.approvedListings} />
          <Stat label="Rejected" value={data.rejectedListings} />
          <Stat label="Plans" value={data.subscriptionPlans} />
          <Stat label="Active subscriptions" value={data.activeSubscriptions} />
          <Stat label="Successful payments" value={data.successfulPayments} />
          <Stat label="Revenue" value={`₹${data.revenue}`} />
        </div>
      )}
    </AdminLayout>
  )
}
