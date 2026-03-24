import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function DmListPage() {
  const { member } = useAuthStore()
  const [rooms, setRooms] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/dm/rooms').then(r => setRooms(r.data.data || [])).finally(() => setLoading(false))
  }, [])

  return (
    <div className="section-sm"><div className="container">
    <div style={{ maxWidth: 640, margin: '0 auto' }}>
      <div className="section-head"><h2>다이렉트 메시지</h2></div>

      {loading ? (
        <div className="empty"><div className="empty-icon">⏳</div></div>
      ) : rooms.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">✉️</div>
          <div className="empty-title">대화 내역이 없습니다</div>
          <div className="empty-sub">멘토 목록에서 DM을 시작해보세요</div>
        </div>
      ) : (
        <div className="card" style={{ padding: 0 }}>
          <div className="room-list">
            {rooms.map(room => {
              const other = room.members?.find(m => m.id !== member?.id)
              return (
                <Link key={room.id} to={`/dm/${room.id}`} className="room-item" style={{ textDecoration: 'none' }}>
                  <div className="av av-md">{other?.nickname?.charAt(0)?.toUpperCase()}</div>
                  <div className="room-info">
                    <div className="room-name">{other?.nickname}</div>
                    <div style={{ fontSize: '.78rem', color: 'var(--t4)' }}>{room.lastMessage || '메시지 없음'}</div>
                  </div>
                  {room.lastMessageAt && (
                    <span style={{ fontSize: '.75rem', color: 'var(--t4)', marginLeft: 'auto' }}>
                      {dayjs(room.lastMessageAt).format('MM.DD')}
                    </span>
                  )}
                </Link>
              )
            })}
          </div>
        </div>
      )}
    </div>
    </div></div>
  )
}
