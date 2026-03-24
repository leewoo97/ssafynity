import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'

export default function MentorListPage() {
  const [mentors, setMentors] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('')

  useEffect(() => {
    api.get('/mentors').then(r => setMentors(r.data.data || [])).finally(() => setLoading(false))
  }, [])

  const filtered = filter
    ? mentors.filter(m => m.skills?.toLowerCase().includes(filter.toLowerCase()) || m.nickname?.includes(filter))
    : mentors

  const startDm = async (mentorId) => {
    const r = await api.post('/dm/rooms', { targetMemberId: mentorId })
    window.location.href = `/dm/${r.data.data.id}`
  }

  return (
    <div className="section-sm"><div className="container">
    <div>
      <div className="section-head"><h2>멘토 목록</h2></div>

      <div className="search-row" style={{ marginBottom: 24 }}>
        <input className="search-input" placeholder="기술 스택, 이름 검색..."
          value={filter} onChange={e => setFilter(e.target.value)} />
      </div>

      {loading ? (
        <div className="empty"><div className="empty-icon">⏳</div></div>
      ) : filtered.length === 0 ? (
        <div className="empty"><div className="empty-icon">👩‍💻</div><div className="empty-title">멘토가 없습니다</div></div>
      ) : (
        <div className="three-col">
          {filtered.map(m => (
            <div key={m.id} className="card">
              <div style={{ display: 'flex', alignItems: 'center', gap: 14, marginBottom: 14 }}>
                <div className="av av-lg">{m.nickname?.charAt(0)?.toUpperCase()}</div>
                <div>
                  <div style={{ fontWeight: 700 }}>{m.nickname}</div>
                  <div style={{ fontSize: '.8rem', color: 'var(--t4)' }}>{m.campus} {m.cohort}기</div>
                </div>
              </div>
              {m.intro && <p style={{ fontSize: '.84rem', color: 'var(--t3)', marginBottom: 12 }}>{m.intro}</p>}
              {m.skills && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4, marginBottom: 14 }}>
                  {m.skills.split(',').map(s => <span key={s} className="pill pill-blue">{s.trim()}</span>)}
                </div>
              )}
              <div style={{ display: 'flex', gap: 8 }}>
                <Link to={`/profile/${m.id}`} className="btn btn-ghost btn-sm">프로필</Link>
                <button className="btn btn-blue btn-sm" onClick={() => startDm(m.id)}>DM</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
    </div></div>
  )
}
