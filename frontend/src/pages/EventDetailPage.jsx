import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function EventDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [event, setEvent] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get(`/events/${id}`).then(r => setEvent(r.data.data)).finally(() => setLoading(false))
  }, [id])

  const handleDelete = async () => {
    if (!window.confirm('삭제하시겠습니까?')) return
    await api.delete(`/events/${id}`)
    navigate('/events')
  }

  if (loading) return <div className="empty"><div className="empty-icon">⏳</div></div>
  if (!event) return <div className="empty"><div className="empty-title">이벤트를 찾을 수 없습니다</div></div>

  const isAdmin = member?.role === 'ADMIN'
  const isAuthor = member?.id === event.organizerId

  return (
    <div className="section-sm"><div className="container">
    <div style={{ maxWidth: 760, margin: '0 auto' }}>
      <div className="card">
        <div className="post-header">
          <div>
            <span className="pill pill-blue">{event.eventType || '행사'}</span>
            <h2 className="post-title" style={{ marginTop: 10 }}>{event.title}</h2>
          </div>
          {(isAuthor || isAdmin) && (
            <div className="post-actions">
              <Link to={`/events/${id}/edit`} className="btn btn-ghost btn-sm">수정</Link>
              <button className="btn btn-danger btn-sm" onClick={handleDelete}>삭제</button>
            </div>
          )}
        </div>
        <div className="post-row-meta" style={{ marginTop: 12, marginBottom: 20 }}>
          {event.organizerNickname && <span>👤 {event.organizerNickname}</span>}
          {event.location && <span>📍 {event.location}</span>}
          <span>📅 {dayjs(event.startDate).format('YYYY.MM.DD')} ~ {dayjs(event.endDate).format('YYYY.MM.DD')}</span>
          {event.maxParticipants > 0 && <span>👥 {event.currentParticipants}/{event.maxParticipants}명</span>}
        </div>
        <div className="post-body md-body" dangerouslySetInnerHTML={{ __html: event.description }} />
      </div>
      <div style={{ marginTop: 16 }}>
        <Link to="/events" className="btn btn-ghost btn-sm">← 목록으로</Link>
      </div>
    </div>
    </div></div>
  )
}
