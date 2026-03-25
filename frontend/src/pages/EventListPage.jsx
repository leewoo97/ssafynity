import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

function getStatus(ev) {
  const now = dayjs()
  const start = dayjs(ev.startDate)
  const end = dayjs(ev.endDate)
  if (now.isBefore(start)) return 'UPCOMING'
  if (now.isAfter(end)) return 'COMPLETED'
  return 'ONGOING'
}

const STATUS_LABEL = { UPCOMING: '예정', ONGOING: '진행 중', COMPLETED: '완료' }
const STATUS_PILL  = { UPCOMING: 'pill-blue', ONGOING: 'pill-green', COMPLETED: 'pill-gray' }
const STATUS_COLOR = { UPCOMING: 'var(--blue)', ONGOING: 'var(--green)', COMPLETED: 'var(--t5)' }
const STATUS_DOT   = { UPCOMING: 'var(--blue)', ONGOING: 'var(--green)', COMPLETED: 'var(--t5)' }

function EventCard({ ev }) {
  const status = getStatus(ev)
  const pct = ev.maxParticipants > 0
    ? Math.min(100, Math.round(ev.currentParticipants * 100 / ev.maxParticipants))
    : 0

  return (
    <div className="event-card">
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
        <span className="event-status" style={{ color: STATUS_COLOR[status] }}>
          <span style={{ display: 'inline-block', width: 7, height: 7, borderRadius: '50%', background: STATUS_DOT[status] }} />
          {STATUS_LABEL[status]}
        </span>
        <span className="pill pill-gray">{ev.eventType}</span>
      </div>
      <h4><Link to={`/events/${ev.id}`}>{ev.title}</Link></h4>
      <div className="event-info" style={{ marginTop: 8, marginBottom: 6 }}>
        <span>📍 {ev.location}</span>
        <span>🗓 {dayjs(ev.startDate).format('MM.DD')} — {dayjs(ev.endDate).format('MM.DD')}</span>
        {ev.maxParticipants > 0 && <span>👥 {ev.currentParticipants}/{ev.maxParticipants}명</span>}
      </div>
      {ev.maxParticipants > 0 && (
        <div className="event-progress">
          <div className="event-progress-bar" style={{ width: `${pct}%` }} />
        </div>
      )}
      <div style={{ marginTop: 12, fontSize: '.78rem', color: 'var(--t5)' }}>
        by {ev.organizerNickname}
      </div>
    </div>
  )
}

export default function EventListPage() {
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)
  const { member } = useAuthStore()

  useEffect(() => {
    api.get('/events').then(r => setEvents(r.data.data ?? [])).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="loading">로딩 중...</div>

  const ongoing  = events.filter(ev => getStatus(ev) === 'ONGOING')
  const upcoming = events.filter(ev => getStatus(ev) === 'UPCOMING')

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

          {/* 진행 중 */}
          {ongoing.length > 0 && (
            <div style={{ marginBottom: 32 }}>
              <div className="label" style={{ marginBottom: 14 }}>🟢 진행 중</div>
              <div className="event-grid">
                {ongoing.map(ev => <EventCard key={ev.id} ev={ev} />)}
              </div>
            </div>
          )}

          {/* 예정 */}
          {upcoming.length > 0 && (
            <div style={{ marginBottom: 32 }}>
              <div className="label" style={{ marginBottom: 14 }}>⏳ 예정된 이벤트</div>
              <div className="event-grid">
                {upcoming.map(ev => <EventCard key={ev.id} ev={ev} />)}
              </div>
            </div>
          )}

          {/* 전체 테이블 */}
          <div className="label" style={{ marginBottom: 12 }}>📋 전체 이벤트</div>
          {events.length === 0 ? (
            <div className="empty">
              <div className="empty-icon">📅</div>
              <div className="empty-title">이벤트가 없습니다.</div>
              {member && (
                <div style={{ marginTop: 16 }}>
                  <Link to="/events/new" className="btn btn-blue btn-md">이벤트 등록하기</Link>
                </div>
              )}
            </div>
          ) : (
            <div className="card card-flush">
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>제목</th>
                      <th>유형</th>
                      <th>위치</th>
                      <th>시작일</th>
                      <th>상태</th>
                    </tr>
                  </thead>
                  <tbody>
                    {events.map(ev => {
                      const status = getStatus(ev)
                      return (
                        <tr key={ev.id}>
                          <td>
                            <Link to={`/events/${ev.id}`} className="table-link">{ev.title}</Link>
                          </td>
                          <td><span className="pill pill-gray">{ev.eventType}</span></td>
                          <td>{ev.location}</td>
                          <td style={{ fontSize: '.78rem', color: 'var(--t5)' }}>
                            {dayjs(ev.startDate).format('MM.DD')}
                          </td>
                          <td>
                            <span className={`pill ${STATUS_PILL[status]}`}>{STATUS_LABEL[status]}</span>
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          )}

        </div>
      </div>
    </>
  )
}
