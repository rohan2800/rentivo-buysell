import { Link } from 'react-router-dom'
import { formatPrice, formatRelative } from '../lib/format'
import type { ListingSummary } from '../lib/types'

export function ListingCard({ listing }: { listing: ListingSummary }) {
  return (
    <Link
      to={`/listings/${listing.id}`}
      className="group flex flex-col overflow-hidden border border-transparent hover:border-line"
    >
      <div className="aspect-[4/3] overflow-hidden rounded-md bg-paper-raised">
        {listing.thumbnailUrl ? (
          <img
            src={listing.thumbnailUrl}
            alt={listing.title}
            className="h-full w-full object-cover transition group-hover:scale-[1.03]"
            loading="lazy"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-ink-soft">
            No photo
          </div>
        )}
      </div>
      <div className="mt-2 flex flex-1 flex-col gap-0.5">
        <span className="font-display text-base font-semibold text-ochre-dark">
          {formatPrice(listing.price, listing.priceUnit)}
        </span>
        <span className="line-clamp-1 text-sm font-medium text-ink">{listing.title}</span>
        <span className="text-sm text-ink-soft">
          {listing.locality}, {listing.city}
        </span>
        <span className="mt-1 text-xs text-ink-soft">{formatRelative(listing.createdAt)}</span>
      </div>
    </Link>
  )
}
