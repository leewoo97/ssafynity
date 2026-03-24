import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import dayjs from 'dayjs'

export default function NotificationsPage() {
  const [notifs, setNotifs] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/notifications')
      .then(r => setNotifs(r.data.data || []))
      .finally(() => setLoading(false))
  }, [])

  const deleteNotif = async (id) => {
    await api.delete(`/notifications/${id}`).catch(() => {})
    setNotifs(notifs.filter(n => n.id !== id))
  }

  return (
    <>
      <div className="page-header">
        <div className="container">
          <div className="page-header-inner">
            <div>
              <h1>알림</h1>
              <p>활동 알림을 확인하세요 · 진입 시 자동 읽음 처리됩니다</p>
            </div>
          </div>
        </div>
      </div>

      <div className="section-sm">
        <div className="container-sm">
          {loading ? (
            <div className="empty"><div className="empty-icon">⏳</div></div>
          ) : notifs.length === 0 ? (
            <div className="empty">
              <div className="empty-icon">🔔</div>
              <div className="empty-title">알림이 없습니다.</div>
            </div>
          ) : (
            <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
              <div className="notif-list">
                {notifs.map(n => (
                  <div key={n.id} className={`notif-item${!n.read ? ' unread' : ''}`}>
                    {!n.read ? (
                      <div className="notif-dot" />
                    ) : (
                      <div className="notif-icon">🔔</div>
                    )}
                    <div className="notif-content">
                      <div className="notif-msg">{n.message}</div>
                      <div className="notif-time">{dayjs(n.createdAt).format('YYYY.MM.DD HH:mm')}</div>
                    </div>
                    <div style={{ display: 'flex', gap: 4, flexShrink: 0 }}>
                      {n.link && <Link to={n.link} className="btn btn-ghost btn-xs">보기</Link>}
                      <button className="btn btn-ghost btn-xs" style={{ color: 'var(--t5)' }}
                        onClick={() => deleteNotif(n.id)}>삭제</button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  )
}
