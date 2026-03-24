import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

const STATUS_LABEL = { UPCOMING: '예정', ONGOING: '진행 중', COMPLETED: '완료', CANCELLED: '취소' }
const STATUS_PILL = { UPCOMING: 'pill-blue', ONGOING: 'pill-green', COMPLETED: 'pill-gray', CANCELLED: 'pill-red' }

export default function EventListPage() {
  const { member } = useAuthStore()
  const [ongoing, setOngoing] = useState([])
  const [upcoming, setUpcoming] = useState([])
  const [allEvents, setAllEvents] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/events').then(r => {
      const evts = r.data.data || []
      setOngoing(evts.filter(e => e.status === 'ONGOING'))
      setUpcoming(evts.filter(e => e.status === 'UPCOMING'))
      setAllEvents(evts)
    }).finally(() => setLoading(false))
  }, [])

  const EventCard = ({ ev }) => (
    <div className="event-card">
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
        <span className="event-status" style={{ color: ev.status === 'ONGOING' ? 'var(--green)' : 'var(--blue)' }}>
          <span style={{ display: 'inline-block', width: 7, height: 7, borderRadius: '50%', background: ev.status === 'ONGOING' ? 'var(--green)' : 'var(--blue)', marginRight: 4 }}></span>
          {STATUS_LABEL[ev.status]}
        </span>
        <span className="pill pill-gray">{ev.eventType}</span>
      </div>
      <h4><Link to={`/events/${ev.id}`}>{ev.title}</Link></h4>
      {ev.description && (
        <div style={{ marginBottom: 6, fontSize: '.8rem', color: 'var(--t3)', lineHeight: 1.5 }}>
          {ev.description.slice(0, 80)}
        </div>
      )}
      <div className="event-info">
        {ev.location && <span>📍 {ev.location}</span>}
        <span>🗓 {dayjs(ev.startDate).format('MM.DD')} — {dayjs(ev.endDate).format('MM.DD')}</span>
        {ev.maxParticipants > 0 && (
          <span>👥 {ev.currentParticipants}/{ev.maxParticipants}명</span>
        )}
      </div>
      {ev.maxParticipants > 0 && (
        <div className="event-progress" style={{ marginTop: 10 }}>
          <div className="event-progress-bar"
            style={{ width: `${Math.min(100, ev.currentParticipants * 100 / ev.maxParticipants)}%` }} />
        </div>
      )}
      <div style={{ marginTop: 12, fontSize: '.78rem', color: 'var(--t5)' }}>
        by {ev.organizerNickname}
      </div>
    </div>
  )

  return (
    <>
      <div className="page-header">
        <div className="container">
          <div className="page-header-inner">
            <div>
              <div className="label" style={{ marginBottom: 6 }}>Events</div>
              <h1>이벤트</h1>
              <p>해커톤, 세미나, 스터디 모임 정보를 확인하세요</p>
            </div>
            {member && <Link to="/events/new" className="btn btn-blue btn-md">📅 이벤트 등록</Link>}
          </div>
        </div>
      </div>

      <div className="section-sm">
        <div className="container">
          {loading ? (
            <div className="empty"><div className="empty-icon">⏳</div></div>
          ) : (
            <>
              {ongoing.length > 0 && (
                <div style={{ marginBottom: 32 }}>
                  <div className="label" style={{ marginBottom: 14 }}>🟢 진행 중</div>
                  <div className="event-grid">
                    {ongoing.map(ev => <EventCard key={ev.id} ev={ev} />)}
                  </div>
                </div>
              )}

              {upcoming.length > 0 && (
                <div style={{ marginBottom: 32 }}>
                  <div className="label" style={{ marginBottom: 14 }}>⏳ 예정된 이벤트</div>
                  <div className="event-grid">
                    {upcoming.map(ev => <EventCard key={ev.id} ev={ev} />)}
                  </div>
                </div>
              )}

              <div className="label" style={{ marginBottom: 12 }}>📋 전체 이벤트</div>
              {allEvents.length === 0 ? (
                <div className="empty">
                  <div className="empty-icon">📅</div>
                  <div className="empty-title">이벤트가 없습니다.</div>
                  {member && (
                    <div className="empty-sub" style={{ marginTop: 16 }}>
                      <Link to="/events/new" className="btn btn-blue btn-md">이벤트 등록하기</Link>
                    </div>
                  )}
                </div>
              ) : (
                <div className="card card-flush">
                  <div className="table-wrap">
                    <table className="table">
                      <thead>
                        <tr><th>제목</th><th>유형</th><th>위치</th><th>시작일</th><th>상태</th></tr>
                      </thead>
                      <tbody>
                        {allEvents.map(ev => (
                          <tr key={ev.id}>
                            <td><Link to={`/events/${ev.id}`} className="table-link">{ev.title}</Link></td>
                            <td><span className="pill pill-gray">{ev.eventType}</span></td>
                            <td>{ev.location}</td>
                            <td style={{ fontSize: '.78rem', color: 'var(--t5)' }}>{dayjs(ev.startDate).format('MM.DD')}</td>
                            <td>
                              <span className={`pill ${STATUS_PILL[ev.status] || 'pill-gray'}`}>
                                {STATUS_LABEL[ev.status] || ev.status}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </>
  )
}
