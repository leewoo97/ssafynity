import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function DmListPage() {
  const { member } = useAuthStore()
  const navigate = useNavigate()
  const [rooms, setRooms] = useState([])
  const [search, setSearch] = useState('')
  const [searchResults, setSearchResults] = useState([])
  const [showSearch, setShowSearch] = useState(false)
  const [groupForm, setGroupForm] = useState({ name:'', memberIds:'' })
  const [showGroup, setShowGroup] = useState(false)

  const fetchRooms = () => api.get('/dm/rooms').then(r => setRooms(r.data.data || []))
  useEffect(() => { fetchRooms() }, [])

  const handleSearch = async e => {
    e.preventDefault()
    if (!search.trim()) return
    const r = await api.get('/search', { params: { q: search } })
    setSearchResults(r.data.data?.members || [])
  }

  const startDm = async (targetId) => {
    const r = await api.post(`/dm/users/${targetId}`)
    navigate(`/dm/${r.data.data.id}`)
  }

  const createGroup = async e => {
    e.preventDefault()
    const ids = groupForm.memberIds.split(',').map(s => parseInt(s.trim())).filter(Boolean)
    const r = await api.post('/dm/group', { name: groupForm.name, memberIds: ids })
    setShowGroup(false)
    navigate(`/dm/${r.data.data.id}`)
  }

  return (
    <div style={{ maxWidth:700, margin:'0 auto' }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 }}>
        <h2>DM</h2>
        <div style={{ display:'flex', gap:8 }}>
          <button onClick={() => setShowSearch(s => !s)} className="btn btn-secondary">1:1 DM</button>
          <button onClick={() => setShowGroup(s => !s)} className="btn btn-primary">그룹 DM</button>
        </div>
      </div>

      {showSearch && (
        <div className="card" style={{ padding:16, marginBottom:16 }}>
          <form onSubmit={handleSearch} style={{ display:'flex', gap:8, marginBottom:12 }}>
            <input className="form-control" placeholder="닉네임 검색" value={search} onChange={e => setSearch(e.target.value)} />
            <button type="submit" className="btn btn-secondary">검색</button>
          </form>
          {searchResults.map(u => (
            <div key={u.id} style={{ display:'flex', justifyContent:'space-between', alignItems:'center', padding:'6px 0', borderBottom:'1px solid var(--color-border)' }}>
              <span>{u.nickname} <span style={{ fontSize:12, color:'var(--color-text-muted)' }}>({u.campus})</span></span>
              <button onClick={() => startDm(u.id)} className="btn btn-primary" style={{ fontSize:12, padding:'4px 10px' }}>DM</button>
            </div>
          ))}
        </div>
      )}

      {showGroup && (
        <form onSubmit={createGroup} className="card" style={{ padding:16, marginBottom:16, display:'flex', flexDirection:'column', gap:10 }}>
          <input className="form-control" placeholder="그룹 이름" value={groupForm.name} onChange={e => setGroupForm(f => ({ ...f, name:e.target.value }))} required />
          <input className="form-control" placeholder="멤버 ID (쉼표로 구분, 예: 2,3,4)" value={groupForm.memberIds} onChange={e => setGroupForm(f => ({ ...f, memberIds:e.target.value }))} required />
          <div style={{ display:'flex', gap:8 }}>
            <button type="submit" className="btn btn-primary">만들기</button>
            <button type="button" className="btn btn-secondary" onClick={() => setShowGroup(false)}>취소</button>
          </div>
        </form>
      )}

      <div style={{ display:'flex', flexDirection:'column', gap:6 }}>
        {rooms.map(room => (
          <Link key={room.id} to={`/dm/${room.id}`} style={{ textDecoration:'none', color:'inherit' }}>
            <div className="card" style={{ padding:'12px 18px', display:'flex', justifyContent:'space-between', alignItems:'center' }}>
              <div>
                <div style={{ fontWeight:500 }}>
                  {room.type === 'GROUP' ? `👥 ${room.name}` : `💬 ${room.otherMember?.nickname || '(알 수 없음)'}`}
                </div>
                {room.lastMessageContent && (
                  <div style={{ fontSize:12, color:'var(--color-text-muted)', marginTop:2 }}>{room.lastMessageContent}</div>
                )}
              </div>
              {room.lastMessageAt && (
                <span style={{ fontSize:11, color:'var(--color-text-muted)' }}>{dayjs(room.lastMessageAt).format('MM.DD HH:mm')}</span>
              )}
            </div>
          </Link>
        ))}
        {rooms.length === 0 && <p style={{ textAlign:'center', color:'var(--color-text-muted)', padding:40 }}>대화가 없습니다.</p>}
      </div>
    </div>
  )
}
