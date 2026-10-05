import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../lib/api'
import { useAuth } from '../lib/auth'

export function LoginPage() {
  const { sendOtp, verifyOtp } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: Location })?.from?.pathname ?? '/'

  const [step, setStep] = useState<'phone' | 'otp'>('phone')
  const [phone, setPhone] = useState('')
  const [name, setName] = useState('')
  const [code, setCode] = useState('')
  const [devOtp, setDevOtp] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function handleSendOtp(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setBusy(true)
    try {
      const dev = await sendOtp(phone)
      setDevOtp(dev)
      setStep('otp')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not send OTP')
    } finally {
      setBusy(false)
    }
  }

  async function handleVerify(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setBusy(true)
    try {
      await verifyOtp(phone, code, name || undefined)
      navigate(from, { replace: true })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Invalid code')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="mx-auto flex max-w-sm flex-col gap-6">
      <h1 className="font-display text-2xl font-bold text-ink">Log in</h1>

      {step === 'phone' && (
        <form onSubmit={handleSendOtp} className="flex flex-col gap-4">
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Mobile number
            <input
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="98765 43210"
              required
              className="border border-line bg-white px-3 py-2 text-base"
            />
          </label>
          {error && <p className="text-sm text-brick">{error}</p>}
          <button
            type="submit"
            disabled={busy}
            className="bg-ochre px-4 py-2.5 font-semibold text-ink hover:bg-ochre-dark disabled:opacity-60"
          >
            {busy ? 'Sending…' : 'Send OTP'}
          </button>
        </form>
      )}

      {step === 'otp' && (
        <form onSubmit={handleVerify} className="flex flex-col gap-4">
          <p className="text-sm text-ink-soft">
            Enter the code sent to <span className="font-medium text-ink">{phone}</span>.
          </p>
          {devOtp && (
            <p className="bg-steel-soft px-3 py-2 text-sm text-steel">
              Dev mode — your code is <span className="font-semibold">{devOtp}</span>
            </p>
          )}
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            OTP code
            <input
              value={code}
              onChange={(e) => setCode(e.target.value)}
              placeholder="123456"
              required
              className="border border-line bg-white px-3 py-2 text-base"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Your name (first time only)
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Optional"
              className="border border-line bg-white px-3 py-2 text-base"
            />
          </label>
          {error && <p className="text-sm text-brick">{error}</p>}
          <button
            type="submit"
            disabled={busy}
            className="bg-ochre px-4 py-2.5 font-semibold text-ink hover:bg-ochre-dark disabled:opacity-60"
          >
            {busy ? 'Verifying…' : 'Verify & continue'}
          </button>
          <button
            type="button"
            onClick={() => setStep('phone')}
            className="text-sm font-medium text-steel"
          >
            Use a different number
          </button>
        </form>
      )}
    </div>
  )
}
