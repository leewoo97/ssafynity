import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function EventDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [ev, setEv] = useState(null)
  const [loading, setLoading] = useState(true)

  const fetchEv = () => api.get(`/events/${id}`).then(r => setEv(r.data.data)).finally(() => setLoading(false))
  useEffect(() => { fetchEv() }, [id])

  const handleJoin = async () => {
    try {
      await api.post(`/events/${id}/join`)
      fetchEv()
    } catch (e) {
      alert(e.response?.data?.message || '참가 실패')
    }
  }

  const handleDelete = async () => {
    if (!confirm('삭제하시겠습니까?')) return
    await api.delete(`/events/${id}`)
    navigate('/events')
  }

  if (loading) return <div className="loading">로딩 중...</div>
  if (!ev) return <div>이벤트를 찾을 수 없습니다.</div>

  const isAuthor = member?.id === ev.organizerId
  const full = ev.currentParticipants >= ev.maxParticipants

  return (
    <div style={{ maxWidth:720, margin:'0 auto' }}>
      <div className="card" style={{ padding:28 }}>
        <div style={{ marginBottom:16 }}>
          <span style={{ fontSize:12, padding:'2px 8px', background:'#e9ecef', borderRadius:4 }}>{ev.eventType}</span>
          <h2 style={{ margin:'10px 0 4px' }}>{ev.title}</h2>
          <div style={{ fontSize:13, color:'var(--color-text-muted)' }}>주최: {ev.organizerNickname}</div>
        </div>
        <div style={{ lineHeight:1.9, whiteSpace:'pre-wrap', borderTop:'1px solid var(--color-border)', paddingTop:16, marginBottom:16 }}>
          {ev.description}
        </div>
        <div style={{ display:'flex', gap:24, fontSize:13, marginBottom:16 }}>
          <span>📅 시작: {dayjs(ev.startDate).format('YYYY.MM.DD HH:mm')}</span>
          <span>📅 종료: {dayjs(ev.endDate).format('YYYY.MM.DD HH:mm')}</span>
        </div>
        <div style={{ fontSize:13, marginBottom:20 }}>
          📍 {ev.location} &nbsp; 👥 {ev.currentParticipants}/{ev.maxParticipants}명
        </div>
        <div style={{ display:'flex', gap:8 }}>
          {!isAuthor && (
            <button onClick={handleJoin} disabled={full} className="btn btn-primary">
              {full ? '마감' : '참가하기'}
            </button>
          )}
          {isAuthor && (
            <>
              <Link to={`/events/${id}/edit`} className="btn btn-secondary">수정</Link>
              <button onClick={handleDelete} className="btn" style={{ background:'#e74c3c', color:'#fff' }}>삭제</button>
            </>
          )}
          <button onClick={() => navigate(-1)} className="btn btn-secondary">뒤로</button>
        </div>
      </div>
    </div>
  )
}
