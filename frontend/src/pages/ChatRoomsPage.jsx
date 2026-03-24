import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function ChatRoomsPage() {
  const [rooms, setRooms] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [newRoom, setNewRoom] = useState({ name:'', description:'' })
  const navigate = useNavigate()

  const fetchRooms = () => api.get('/chat/rooms').then(r => setRooms(r.data.data || []))
  useEffect(() => { fetchRooms() }, [])

  const handleCreate = async e => {
    e.preventDefault()
    const r = await api.post('/chat/rooms', newRoom)
    setShowForm(false)
    setNewRoom({ name:'', description:'' })
    fetchRooms()
    navigate(`/chat/${r.data.data.id}`)
  }

  const handleDelete = async (roomId, e) => {
    e.preventDefault()
    e.stopPropagation()
    if (!confirm('채팅방을 삭제하시겠습니까?')) return
    await api.delete(`/chat/rooms/${roomId}`)
    fetchRooms()
  }

  return (
    <div style={{ maxWidth:700, margin:'0 auto' }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 }}>
        <h2>채팅방</h2>
        <button onClick={() => setShowForm(s => !s)} className="btn btn-primary">+ 방 만들기</button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="card" style={{ padding:20, marginBottom:16, display:'flex', flexDirection:'column', gap:10 }}>
          <input className="form-control" placeholder="방 이름" value={newRoom.name} onChange={e => setNewRoom(f => ({ ...f, name:e.target.value }))} required />
          <input className="form-control" placeholder="설명 (선택)" value={newRoom.description} onChange={e => setNewRoom(f => ({ ...f, description:e.target.value }))} />
          <div style={{ display:'flex', gap:8 }}>
            <button type="submit" className="btn btn-primary">만들기</button>
            <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>취소</button>
          </div>
        </form>
      )}

      <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
        {rooms.map(room => (
          <Link key={room.id} to={`/chat/${room.id}`} style={{ textDecoration:'none', color:'inherit' }}>
            <div className="card" style={{ padding:'14px 20px', display:'flex', justifyContent:'space-between', alignItems:'center' }}>
              <div>
                <div style={{ fontWeight:500 }}>#{room.name}</div>
                {room.description && <div style={{ fontSize:12, color:'var(--color-text-muted)', marginTop:2 }}>{room.description}</div>}
              </div>
              <div style={{ display:'flex', gap:8, alignItems:'center' }}>
                <span style={{ fontSize:12, color:'var(--color-text-muted)' }}>입장</span>
                <button onClick={e => handleDelete(room.id, e)} style={{ background:'none', border:'none', color:'#e74c3c', cursor:'pointer', fontSize:18 }}>×</button>
              </div>
            </div>
          </Link>
        ))}
        {rooms.length === 0 && <p style={{ textAlign:'center', color:'var(--color-text-muted)', padding:32 }}>채팅방이 없습니다.</p>}
      </div>
    </div>
  )
}
