import { useState, useEffect } from 'react'
import api from '../api/axios'
import dayjs from 'dayjs'

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)

  const fetchNotifs = () =>
    api.get('/notifications').then(r => setNotifications(r.data.data || [])).finally(() => setLoading(false))

  useEffect(() => { fetchNotifs() }, [])

  const handleMarkAll = async () => {
    await api.post('/notifications/read-all')
    fetchNotifs()
  }

  const handleDelete = async (notifId) => {
    await api.delete(`/notifications/${notifId}`)
    setNotifications(prev => prev.filter(n => n.id !== notifId))
  }

  if (loading) return <div className="loading">로딩 중...</div>

  return (
    <div style={{ maxWidth:700, margin:'0 auto' }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 }}>
        <h2>알림</h2>
        {notifications.some(n => !n.read) && (
          <button onClick={handleMarkAll} className="btn btn-secondary">모두 읽음</button>
        )}
      </div>

      <div style={{ display:'flex', flexDirection:'column', gap:6 }}>
        {notifications.map(n => (
          <div key={n.id} className="card" style={{ padding:'12px 18px', display:'flex', justifyContent:'space-between', alignItems:'center', background: n.read ? undefined : '#f0f4ff' }}>
            <div>
              {!n.read && <span style={{ width:8, height:8, background:'var(--color-primary)', borderRadius:'50%', display:'inline-block', marginRight:8 }} />}
              <span style={{ fontSize:14 }}>{n.message}</span>
              <div style={{ fontSize:11, color:'var(--color-text-muted)', marginTop:2 }}>
                {dayjs(n.createdAt).format('YYYY.MM.DD HH:mm')}
              </div>
            </div>
            <button onClick={() => handleDelete(n.id)} style={{ background:'none', border:'none', color:'var(--color-text-muted)', cursor:'pointer', fontSize:18, lineHeight:1 }}>×</button>
          </div>
        ))}
        {notifications.length === 0 && <p style={{ textAlign:'center', color:'var(--color-text-muted)', padding:40 }}>알림이 없습니다.</p>}
      </div>
    </div>
  )
}
