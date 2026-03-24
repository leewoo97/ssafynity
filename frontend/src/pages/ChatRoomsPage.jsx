import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'

export default function ChatRoomsPage() {
  const [rooms, setRooms] = useState([])
  const [loading, setLoading] = useState(true)
  const [newRoomName, setNewRoomName] = useState('')
  const [creating, setCreating] = useState(false)

  useEffect(() => {
    api.get('/chat/rooms').then(r => setRooms(r.data.data || [])).finally(() => setLoading(false))
  }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    if (!newRoomName.trim()) return
    setCreating(true)
    try {
      const r = await api.post('/chat/rooms', { name: newRoomName })
      setRooms([...rooms, r.data.data])
      setNewRoomName('')
    } finally { setCreating(false) }
  }

  return (
    <div className="section-sm"><div className="container">
    <div>
      <div className="section-head">
        <h2>오픈 채팅</h2>
      </div>
      <form onSubmit={handleCreate} style={{ display: 'flex', gap: 8, marginBottom: 24 }}>
        <input className="form-input" value={newRoomName} onChange={e => setNewRoomName(e.target.value)}
          placeholder="채팅방 이름 입력..." style={{ flex: 1 }} />
        <button type="submit" className="btn btn-blue btn-md" disabled={creating}>방 만들기</button>
      </form>

      {loading ? (
        <div className="empty"><div className="empty-icon">⏳</div></div>
      ) : rooms.length === 0 ? (
        <div className="empty"><div className="empty-icon">💬</div><div className="empty-title">채팅방이 없습니다</div></div>
      ) : (
        <div className="card" style={{ padding: 0 }}>
          <div className="room-list">
            {rooms.map(room => (
              <Link key={room.id} to={`/chat/${room.id}`} className="room-item" style={{ textDecoration: 'none' }}>
                <div className="room-icon">#</div>
                <div className="room-info">
                  <div className="room-name">{room.name}</div>
                  <div style={{ fontSize: '.78rem', color: 'var(--t4)' }}>{room.memberCount || 0}명 참여 중</div>
                </div>
                <span className="pill pill-blue" style={{ marginLeft: 'auto' }}>입장</span>
              </Link>
            ))}
          </div>
        </div>
      )}
    </div>
    </div></div>
  )
}
