import { useEffect, useState } from 'react'
import './App.css'

async function readResponse(response) {
  const data = await response.json().catch(() => null)
  if (!response.ok) throw new Error(data?.message || `Request failed (${response.status})`)
  return data
}

export default function App() {
  const [offices, setOffices] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [deletingId, setDeletingId] = useState(null)

  useEffect(() => {
    const controller = new AbortController()
    fetch('/api/offices/all', { signal: controller.signal })
      .then(readResponse)
      .then(data => {
        if (!Array.isArray(data)) throw new Error('Invalid office list from server')
        setOffices(data)
      })
      .catch(err => { if (!controller.signal.aborted) setError(err.message) })
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [])

  async function deleteOffice(office) {
    if (deletingId !== null || !window.confirm(`Delete office ${office.officeNumber}?`)) return
    setDeletingId(office.id)
    setError('')
    setMessage('')
    try {
      const response = await fetch(`/api/offices/delete/${encodeURIComponent(office.id)}`, { method: 'DELETE' })
      const data = await readResponse(response)
      setOffices(current => current.filter(item => item.id !== office.id))
      setMessage(data?.message || 'Office deleted successfully')
    } catch (err) {
      setError(err.message)
    } finally {
      setDeletingId(null)
    }
  }

  const query = search.trim().toLowerCase()
  const filtered = offices.filter(office =>
    [office.name, office.officeNumber].some(value => String(value ?? '').toLowerCase().includes(query)))

  return (
    <>
      <header><span className="brand-icon" aria-hidden="true">+</span> Clinic Management</header>
      <main>
        <p className="eyebrow">CLINIC ADMINISTRATION</p>
        <h1>Offices</h1>
        <p className="subtitle">Find and manage your clinic's offices.</p>
        {error && <div className="notice error" role="alert">{error}</div>}
        {message && <div className="notice success" role="status">{message}</div>}
        <section aria-label="Office directory">
          <div className="toolbar">
            <div><h2>All offices</h2><p>{loading ? 'Loading...' : `${filtered.length} of ${offices.length} offices`}</p></div>
            <div className="search"><label htmlFor="search">Search offices</label><input id="search" type="search" placeholder="Search by name or office number" value={search} onChange={event => setSearch(event.target.value)} /></div>
          </div>
          <div className="table-scroll">
            <table aria-busy={loading}>
              <thead><tr><th scope="col">Office number</th><th scope="col">Office name</th><th scope="col">Action</th></tr></thead>
              <tbody>
                {loading ? <tr><td colSpan="3" className="empty">Loading offices...</td></tr> : filtered.map(office => (
                  <tr key={office.id}>
                    <td className="number">{office.officeNumber}</td><td>{office.name || 'Unnamed office'}</td>
                    <td><div className="actions"><button type="button" className="update" title="Update is not available yet">Update</button><button type="button" className="delete" disabled={deletingId !== null} onClick={() => deleteOffice(office)} aria-label={`Delete office ${office.officeNumber}`}>{deletingId === office.id ? 'Deleting...' : 'Delete'}</button></div></td>
                  </tr>
                ))}
                {!loading && filtered.length === 0 && <tr><td colSpan="3" className="empty">{error ? 'Unable to load offices. Please refresh to try again.' : query ? 'No offices match your search.' : 'No offices yet.'}</td></tr>}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </>
  )
}
