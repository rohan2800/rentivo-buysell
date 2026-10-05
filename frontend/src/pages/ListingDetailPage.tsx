import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Spinner } from '../components/Spinner'
import { ApiError, api } from '../lib/api'
import { useAuth } from '../lib/auth'
import { formatDate, formatPrice } from '../lib/format'
import { useToast } from '../lib/toast'
import type { ContactResponse, PublicListing } from '../lib/types'

export function ListingDetailPage() {
  const { id } = useParams<{ id: string }>()
  const listingId = Number(id)
  const { isAuthenticated } = useAuth()
  const toast = useToast()

  const [listing, setListing] = useState<PublicListing | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [activeImage, setActiveImage] = useState(0)
  const [contact, setContact] = useState<ContactResponse | null>(null)
  const [unlocking, setUnlocking] = useState(false)
  const [unlockError, setUnlockError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)
  const [savingFavorite, setSavingFavorite] = useState(false)

  useEffect(() => {
    api.listings
      .get(listingId)
      .then(setListing)
      .catch(() => setNotFound(true))
  }, [listingId])

  useEffect(() => {
    if (!isAuthenticated) return
    api.favorites
      .status(listingId)
      .then((res) => setSaved(res.favorited))
      .catch(() => undefined)
  }, [listingId, isAuthenticated])

  async function handleUnlock() {
    setUnlocking(true)
    setUnlockError(null)
    try {
      const res = await api.subscriptions.unlockContact(listingId)
      setContact(res)
    } catch (err) {
      if (err instanceof ApiError && err.status === 402) {
        setUnlockError('You need an active plan to reveal contact details.')
      } else {
        setUnlockError(err instanceof ApiError ? err.message : 'Could not unlock contact')
      }
    } finally {
      setUnlocking(false)
    }
  }

  async function toggleFavorite() {
    setSavingFavorite(true)
    try {
      if (saved) {
        await api.favorites.remove(listingId)
        setSaved(false)
        toast.show('Removed from favorites')
      } else {
        await api.favorites.add(listingId)
        setSaved(true)
        toast.show('Saved to favorites')
      }
    } catch (err) {
      toast.show(err instanceof ApiError ? err.message : 'Could not update favorites', 'error')
    } finally {
      setSavingFavorite(false)
    }
  }

  if (notFound) {
    return (
      <div className="text-center text-ink-soft">
        This listing isn't available — it may have expired or been removed.
      </div>
    )
  }
  if (!listing) return <Spinner />

  const image = listing.images[activeImage]

  return (
    <div className="grid gap-10 lg:grid-cols-3">
      <div className="lg:col-span-2">
        <div className="aspect-[4/3] overflow-hidden rounded-md bg-paper-raised">
          {image ? (
            <img src={image.url} alt={listing.title} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full items-center justify-center text-ink-soft">No photos</div>
          )}
        </div>
        {listing.images.length > 1 && (
          <div className="mt-2 flex gap-2">
            {listing.images.map((img, i) => (
              <button
                key={img.id}
                type="button"
                onClick={() => setActiveImage(i)}
                className={`h-16 w-16 overflow-hidden rounded ${i === activeImage ? 'ring-2 ring-ochre-dark' : ''}`}
              >
                <img src={img.url} alt="" className="h-full w-full object-cover" />
              </button>
            ))}
          </div>
        )}

        <div className="mt-6 flex items-start justify-between gap-4">
          <h1 className="font-display text-2xl font-bold text-ink">{listing.title}</h1>
          {isAuthenticated && (
            <button
              type="button"
              onClick={toggleFavorite}
              disabled={savingFavorite}
              className={`flex-shrink-0 border px-3 py-1.5 text-sm font-medium ${
                saved ? 'border-ochre-dark bg-ochre/10 text-ochre-dark' : 'border-line text-steel'
              }`}
            >
              {saved ? 'Saved ♥' : 'Save'}
            </button>
          )}
        </div>
        <p className="text-ink-soft">
          {listing.locality}, {listing.city}, {listing.state}
        </p>

        <p className="mt-4 whitespace-pre-wrap text-ink">{listing.description}</p>

        {listing.fields.length > 0 && (
          <dl className="mt-6 grid grid-cols-2 gap-x-6 gap-y-2 border-t border-line pt-4 sm:grid-cols-3">
            {listing.fields.map((f) => (
              <div key={f.fieldId}>
                <dt className="text-xs text-ink-soft">{f.name}</dt>
                <dd className="text-sm font-medium text-ink">{f.value}</dd>
              </div>
            ))}
          </dl>
        )}
      </div>

      <aside className="flex flex-col gap-4 border border-line p-5">
        <span className="font-display text-2xl font-bold text-ochre-dark">
          {formatPrice(listing.price, listing.priceUnit)}
        </span>
        <span className="text-sm text-ink-soft">
          {listing.listingType === 'RENT' ? 'For rent' : 'For sale'} · Posted{' '}
          {formatDate(listing.createdAt)}
        </span>

        {!contact ? (
          isAuthenticated ? (
            <button
              type="button"
              onClick={handleUnlock}
              disabled={unlocking}
              className="bg-ochre px-4 py-2.5 font-semibold text-ink hover:bg-ochre-dark disabled:opacity-60"
            >
              {unlocking ? 'Unlocking…' : 'Reveal contact number'}
            </button>
          ) : (
            <Link
              to="/login"
              className="bg-ochre px-4 py-2.5 text-center font-semibold text-ink hover:bg-ochre-dark"
            >
              Log in to reveal contact
            </Link>
          )
        ) : (
          <div className="bg-steel-soft px-4 py-3">
            <p className="text-sm text-ink-soft">{contact.ownerName}</p>
            <p className="font-display text-lg font-semibold text-steel">{contact.ownerPhone}</p>
          </div>
        )}
        {unlockError && (
          <p className="text-sm text-brick">
            {unlockError}{' '}
            <Link to="/plans" className="underline">
              View plans
            </Link>
          </p>
        )}
      </aside>
    </div>
  )
}
