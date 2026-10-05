import { reportReachable, reportUnreachable } from './connectivity'
import type {
  AppNotification,
  AuthResponse,
  Category,
  ContactResponse,
  CreateListingRequest,
  FavoriteStatus,
  ImageResponse,
  ListingFilter,
  OwnedListing,
  PageResponse,
  PublicListing,
  SendOtpResponse,
  SubscriptionPlan,
  SubscriptionStatus,
} from './types'

const BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://localhost:8081'

/** Thrown for any non-2xx response. Carries the backend's RFC 7807 detail and field errors. */
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string> | null

  constructor(status: number, message: string, fieldErrors: Record<string, string> | null) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

let authToken: string | null = null

/** Called once by AuthProvider on mount/login/logout so every request carries the right token. */
export function setAuthToken(token: string | null) {
  authToken = token
}

async function request<T>(
  path: string,
  options: { method?: string; body?: unknown; isForm?: boolean } = {},
): Promise<T> {
  const headers: Record<string, string> = {}
  if (authToken) {
    headers.Authorization = `Bearer ${authToken}`
  }
  let body: BodyInit | undefined
  if (options.body !== undefined) {
    if (options.isForm) {
      body = options.body as FormData
    } else {
      headers['Content-Type'] = 'application/json'
      body = JSON.stringify(options.body)
    }
  }

  let res: Response
  try {
    res = await fetch(`${BASE_URL}${path}`, { method: options.method ?? 'GET', headers, body })
  } catch {
    reportUnreachable()
    throw new ApiError(0, "Can't reach the server. Check your connection and try again.", null)
  }
  reportReachable()

  if (res.status === 204) {
    return undefined as T
  }

  const text = await res.text()
  const data = text ? JSON.parse(text) : null

  if (!res.ok) {
    const message = data?.detail ?? data?.title ?? `Request failed (${res.status})`
    throw new ApiError(res.status, message, data?.errors ?? null)
  }
  return data as T
}

function query(params: Record<string, string | number | boolean | undefined>): string {
  const q = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== '') {
      q.set(key, String(value))
    }
  }
  const s = q.toString()
  return s ? `?${s}` : ''
}

export const api = {
  auth: {
    sendOtp: (phone: string) =>
      request<SendOtpResponse>('/api/auth/send-otp', { method: 'POST', body: { phone } }),
    verifyOtp: (phone: string, code: string, name?: string) =>
      request<AuthResponse>('/api/auth/verify-otp', { method: 'POST', body: { phone, code, name } }),
  },

  categories: {
    list: () => request<Category[]>('/api/categories'),
  },

  listings: {
    browse: (filter: ListingFilter) =>
      request<PageResponse<import('./types').ListingSummary>>(
        `/api/listings/approved${query({ ...filter })}`,
      ),
    get: (id: number) => request<PublicListing>(`/api/listings/${id}`),
    mine: () => request<OwnedListing[]>('/api/listings/mine'),
    create: (body: CreateListingRequest) =>
      request<OwnedListing>('/api/listings', { method: 'POST', body }),
    update: (id: number, body: CreateListingRequest) =>
      request<OwnedListing>(`/api/listings/${id}`, { method: 'PUT', body }),
    remove: (id: number) => request<void>(`/api/listings/${id}`, { method: 'DELETE' }),
    renew: (id: number) => request<OwnedListing>(`/api/listings/${id}/renew`, { method: 'POST' }),
    addImage: (id: number, file: File) => {
      const form = new FormData()
      form.set('file', file)
      return request<ImageResponse>(`/api/listings/${id}/images`, {
        method: 'POST',
        body: form,
        isForm: true,
      })
    },
    removeImage: (id: number, imageId: number) =>
      request<void>(`/api/listings/${id}/images/${imageId}`, { method: 'DELETE' }),
    reorderImages: (id: number, imageIds: number[]) =>
      request<void>(`/api/listings/${id}/images/order`, { method: 'PUT', body: { imageIds } }),
  },

  subscriptions: {
    plans: () => request<SubscriptionPlan[]>('/api/subscriptions/plans'),
    status: () => request<SubscriptionStatus>('/api/subscriptions/status'),
    activateDev: (planId: number) =>
      request<unknown>(`/api/subscriptions/activate-dev${query({ planId })}`, { method: 'POST' }),
    unlockContact: (listingId: number) =>
      request<ContactResponse>(`/api/subscriptions/contact/${listingId}`, { method: 'POST' }),
  },

  favorites: {
    mine: (page = 0, size = 20) =>
      request<PageResponse<import('./types').ListingSummary>>(
        `/api/favorites${query({ page, size })}`,
      ),
    status: (listingId: number) => request<FavoriteStatus>(`/api/favorites/${listingId}`),
    add: (listingId: number) => request<void>(`/api/favorites/${listingId}`, { method: 'POST' }),
    remove: (listingId: number) => request<void>(`/api/favorites/${listingId}`, { method: 'DELETE' }),
  },

  notifications: {
    mine: (page = 0, size = 20) =>
      request<PageResponse<AppNotification>>(`/api/notifications${query({ page, size })}`),
    unreadCount: () => request<{ count: number }>('/api/notifications/unread-count'),
    markRead: (id: number) =>
      request<AppNotification>(`/api/notifications/${id}/read`, { method: 'PATCH' }),
    markAllRead: () => request<void>('/api/notifications/read-all', { method: 'POST' }),
  },
}
