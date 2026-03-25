import { useState, useEffect } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function EventListPage() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/events').then(r => setEvents(r.data.data)).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="loading">로딩 중...</div>
  return (
    <div>
      <div className="page-header" style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 }}>
        <h2>이벤트</h2>
        <Link to="/events/new" className="btn btn-primary">이벤트 등록</Link>
      </div>
      <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
        {events.map(ev => (
          <div key={ev.id} className="card" style={{ padding:'14px 20px' }}>
            <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start' }}>
              <div>
                <span style={{ fontSize:11, padding:'2px 8px', borderRadius:4, background:'#e9ecef', marginRight:8 }}>{ev.eventType}</span>
                <Link to={`/events/${ev.id}`} style={{ fontSize:16, fontWeight:500, color:'var(--color-text)' }}>{ev.title}</Link>
              </div>
              <span style={{ fontSize:13, color:'var(--color-text-muted)' }}>
                {ev.currentParticipants}/{ev.maxParticipants}명
              </span>
            </div>
            <div style={{ fontSize:12, color:'var(--color-text-muted)', marginTop:6 }}>
              📅 {dayjs(ev.startDate).format('YYYY.MM.DD')} ~ {dayjs(ev.endDate).format('MM.DD')} | 📍{ev.location} | 주최: {ev.organizerNickname}
            </div>
          </div>
        ))}
        {events.length === 0 && <p style={{ textAlign:'center', padding:40, color:'var(--color-text-muted)' }}>이벤트가 없습니다.</p>}
      </div>
    </div>
  )
}
