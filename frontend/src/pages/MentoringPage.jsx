import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

const STATUS = {
  PENDING:  { label: '검토중',  color: 'var(--orange)', bg: 'rgba(255,149,0,.09)',   icon: '🕐' },
  ACCEPTED: { label: '수락됨',  color: 'var(--green)',  bg: 'rgba(52,199,89,.09)',   icon: '✅' },
  REJECTED: { label: '거절됨',  color: 'var(--t4)',     bg: 'var(--surface-2)',       icon: '❌' },
}

function StatusBadge({ status }) {
  const s = STATUS[status] || STATUS.PENDING
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 4,
      padding: '3px 10px', borderRadius: 980,
      fontSize: '.76rem', fontWeight: 600,
      color: s.color, background: s.bg,
    }}>
      {s.icon} {s.label}
    </span>
  )
}

function MessageBlock({ label, text, accent }) {
  const [expanded, setExpanded] = useState(false)
  if (!text) return null
  const isLong = text.length > 180
  return (
    <div style={{
      background: accent ? 'var(--blue-xl)' : 'var(--surface-2)',
      borderRadius: 'var(--r-sm)', padding: '14px 18px',
      borderLeft: `3px solid ${accent ? 'var(--blue)' : 'var(--b2)'}`,
      marginTop: 12,
    }}>
      <div style={{ fontSize: '.7rem', fontWeight: 700, color: accent ? 'var(--blue)' : 'var(--t4)', letterSpacing: '.06em', textTransform: 'uppercase', marginBottom: 7 }}>
        {label}
      </div>
      <p style={{ fontSize: '.88rem', color: 'var(--t2)', lineHeight: 1.75, margin: 0, whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
        {isLong && !expanded ? text.slice(0, 180) + '…' : text}
      </p>
      {isLong && (
        <button
          style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--blue)', fontSize: '.78rem', fontWeight: 600, padding: '4px 0 0', fontFamily: 'var(--font)' }}
          onClick={() => setExpanded(v => !v)}
        >{expanded ? '접기 ▲' : '더 보기 ▼'}</button>
      )}
    </div>
  )
}

// ── 보낸 신청 카드 (멘티 뷰) ───────────────────────────────────────────────
function SentCard({ req }) {
  const navigate = useNavigate()

  return (
    <div style={{
      background: 'var(--surface)', borderRadius: 'var(--r)',
      border: '1px solid var(--b1)', padding: '22px 24px',
      boxShadow: 'var(--s0)', transition: 'box-shadow .2s',
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <div style={{
            width: 44, height: 44, borderRadius: '50%', background: 'var(--blue)',
            color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '1.1rem', fontWeight: 700, flexShrink: 0,
          }}>
            {req.mentorNickname?.charAt(0)?.toUpperCase()}
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '.95rem', color: 'var(--t1)' }}>{req.mentorNickname} 멘토</div>
            {req.mentorTitle && <div style={{ fontSize: '.8rem', color: 'var(--t4)', marginTop: 2 }}>{req.mentorTitle}</div>}
            <div style={{ fontSize: '.74rem', color: 'var(--t5)', marginTop: 3 }}>
              신청일: {dayjs(req.createdAt).format('YYYY.MM.DD HH:mm')}
            </div>
          </div>
        </div>
        <StatusBadge status={req.status} />
      </div>

      <MessageBlock label="📝 내 신청 내용" text={req.message} accent={false} />

      {req.reply && (
        <MessageBlock label="💌 멘토님의 답변" text={req.reply} accent={true} />
      )}

      {req.status === 'ACCEPTED' && req.chatRoomId && (
        <div style={{ marginTop: 14 }}>
          <button
            className="btn btn-blue btn-sm"
            onClick={() => navigate(`/chat/${req.chatRoomId}`)}
          >💬 멘토링 채팅 바로가기</button>
        </div>
      )}
    </div>
  )
}

// ── 받은 신청 카드 (멘토 뷰) ───────────────────────────────────────────────
function ReceivedCard({ req, onUpdate }) {
  const [expanded, setExpanded] = useState(req.status === 'PENDING')
  const [action, setAction] = useState(null) // 'accept' | 'reject'
  const [reply, setReply] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const handleAction = async () => {
    setSubmitting(true)
    setError('')
    try {
      await api.post(`/mentoring/requests/${req.id}/${action}`, { reply: reply.trim() })
      onUpdate(req.id, action === 'accept' ? 'ACCEPTED' : 'REJECTED', reply.trim())
      setAction(null)
      setExpanded(true)
    } catch (e) {
      setError(e.response?.data?.error?.message || e.response?.data?.message || '처리에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  const isPending = req.status === 'PENDING'

  return (
    <div style={{
      background: 'var(--surface)', borderRadius: 'var(--r)',
      border: `1px solid ${isPending ? 'rgba(255,149,0,.3)' : 'var(--b1)'}`,
      padding: '22px 24px', boxShadow: 'var(--s0)',
      transition: 'all .2s',
    }}>
      {/* 카드 헤더 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <div style={{
            width: 44, height: 44, borderRadius: '50%',
            background: `hsl(${(req.menteeNickname?.charCodeAt(0) || 0) * 37 % 360},60%,55%)`,
            color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '1.1rem', fontWeight: 700, flexShrink: 0,
          }}>
            {req.menteeNickname?.charAt(0)?.toUpperCase()}
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '.95rem', color: 'var(--t1)' }}>
              {req.menteeNickname}님의 신청
            </div>
            <div style={{ fontSize: '.74rem', color: 'var(--t5)', marginTop: 3 }}>
              {dayjs(req.createdAt).format('YYYY.MM.DD HH:mm')} 신청
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
          <StatusBadge status={req.status} />
          <button
            style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--t4)', fontSize: '.8rem', fontFamily: 'var(--font)', padding: '3px 6px' }}
            onClick={() => setExpanded(v => !v)}
          >{expanded ? '접기 ▲' : '펼치기 ▼'}</button>
        </div>
      </div>

      {/* 펼친 내용 */}
      {expanded && (
        <>
          <MessageBlock label="📝 신청 내용" text={req.message} accent={false} />

          {/* 이미 처리된 경우: 내가 보낸 답변 */}
          {!isPending && req.reply && (
            <MessageBlock label="💌 내 답변" text={req.reply} accent={true} />
          )}

          {/* PENDING 상태: 수락/거절 액션 */}
          {isPending && (
            <div style={{ marginTop: 16 }}>
              {action === null ? (
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                  <Link
                    to={`/profile/${req.menteeId}`}
                    className="btn btn-ghost btn-sm"
                  >프로필 보기</Link>
                  <button
                    className="btn btn-sm"
                    style={{ background: 'var(--green)', color: '#fff', border: 'none' }}
                    onClick={() => { setAction('accept'); setReply('') }}
                  >✅ 수락하기</button>
                  <button
                    className="btn btn-ghost btn-sm"
                    style={{ color: 'var(--t3)' }}
                    onClick={() => { setAction('reject'); setReply('') }}
                  >❌ 거절하기</button>
                </div>
              ) : (
                <div style={{
                  background: action === 'accept' ? 'rgba(52,199,89,.06)' : 'var(--surface-2)',
                  borderRadius: 'var(--r-sm)', padding: '18px',
                  border: `1px solid ${action === 'accept' ? 'rgba(52,199,89,.2)' : 'var(--b2)'}`,
                }}>
                  <div style={{ fontWeight: 600, fontSize: '.88rem', marginBottom: 10, color: action === 'accept' ? 'var(--green)' : 'var(--t2)' }}>
                    {action === 'accept' ? '✅ 수락 메시지를 작성해 주세요' : '❌ 거절 사유를 작성해 주세요 (선택)'}
                  </div>
                  <textarea
                    className="form-input"
                    value={reply}
                    onChange={e => { setReply(e.target.value); setError('') }}
                    placeholder={
                      action === 'accept'
                        ? '안녕하세요! 멘토링 신청을 수락했습니다. 채팅에서 일정을 조율해봐요! 😊'
                        : '이번에는 함께하지 못해 아쉽습니다. 다음에 좋은 기회가 있기를...'
                    }
                    style={{ minHeight: 100, resize: 'vertical', fontFamily: 'var(--font)', fontSize: '.88rem', lineHeight: 1.65 }}
                    maxLength={800}
                  />
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 10, flexWrap: 'wrap', gap: 8 }}>
                    {error && <span style={{ fontSize: '.78rem', color: 'var(--red)' }}>{error}</span>}
                    <div style={{ display: 'flex', gap: 8, marginLeft: 'auto' }}>
                      <button className="btn btn-ghost btn-sm" onClick={() => setAction(null)} disabled={submitting}>취소</button>
                      <button
                        className="btn btn-sm"
                        style={{
                          background: action === 'accept' ? 'var(--green)' : 'var(--t3)',
                          color: '#fff', border: 'none',
                        }}
                        onClick={handleAction}
                        disabled={submitting}
                      >
                        {submitting ? '처리 중...' : action === 'accept' ? '수락 전송 →' : '거절 전송 →'}
                      </button>
                    </div>
                  </div>
                  <div style={{ fontSize: '.73rem', color: 'var(--t5)', marginTop: 6 }}>
                    {action === 'accept'
                      ? '📧 수락 시 멘티에게 이메일이 발송되고 1:1 채팅방이 자동 생성됩니다.'
                      : '📧 거절 시 멘티에게 이메일이 발송됩니다. 답변은 선택 사항입니다.'}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* 처리됨 + 채팅방 있음 */}
          {req.status === 'ACCEPTED' && req.chatRoomId && (
            <div style={{ marginTop: 12 }}>
              <Link to={`/chat/${req.chatRoomId}`} className="btn btn-ghost btn-sm">
                💬 멘토링 채팅방 가기
              </Link>
            </div>
          )}
        </>
      )}
    </div>
  )
}

// ── 메인 페이지 ────────────────────────────────────────────────────────────
export default function MentoringPage() {
  const { member } = useAuthStore()
  const [tab, setTab] = useState('sent')
  const [sent, setSent] = useState([])
  const [received, setReceived] = useState([])
  const [loading, setLoading] = useState(true)
  const [isMentor, setIsMentor] = useState(false)

  useEffect(() => {
    const fetchAll = async () => {
      setLoading(true)
      try {
        const [sentRes] = await Promise.all([api.get('/mentoring/requests/sent')])
        setSent(sentRes.data.data || [])
      } catch { setSent([]) }

      try {
        const recRes = await api.get('/mentoring/requests/received')
        setReceived(recRes.data.data || [])
        setIsMentor(true)
        // 대기중 신청 있으면 받은 탭으로 기본 이동
        const hasPending = recRes.data.data?.some(r => r.status === 'PENDING')
        if (hasPending) setTab('received')
      } catch {
        setIsMentor(false)
      }

      setLoading(false)
    }
    fetchAll()
  }, [])

  const handleUpdate = (id, newStatus, newReply) => {
    setReceived(prev => prev.map(r =>
      r.id === id ? { ...r, status: newStatus, reply: newReply } : r
    ))
  }

  const pendingCount = received.filter(r => r.status === 'PENDING').length

  // ── 정렬: PENDING 먼저
  const sortedReceived = [...received].sort((a, b) => {
    if (a.status === 'PENDING' && b.status !== 'PENDING') return -1
    if (a.status !== 'PENDING' && b.status === 'PENDING') return 1
    return new Date(b.createdAt) - new Date(a.createdAt)
  })

  if (!member) return <div className="empty"><div className="empty-title">로그인이 필요합니다</div></div>

  return (
    <div className="section-sm">
      <div className="container" style={{ maxWidth: 780 }}>

        {/* ── 페이지 헤더 */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 28, flexWrap: 'wrap', gap: 12 }}>
          <div>
            <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--t1)', marginBottom: 4 }}>멘토링 현황</h1>
            <p style={{ fontSize: '.88rem', color: 'var(--t4)' }}>신청한 멘토링과 받은 신청을 관리하세요</p>
          </div>
          <Link to="/mentors" className="btn btn-blue btn-md">🔍 멘토 찾기</Link>
        </div>

        {/* ── 탭 */}
        <div style={{ display: 'flex', borderBottom: '1px solid var(--b1)', marginBottom: 24 }}>
          <button
            onClick={() => setTab('sent')}
            style={{
              background: 'none', border: 'none', cursor: 'pointer',
              fontFamily: 'var(--font)', fontSize: '.88rem', fontWeight: 600,
              padding: '10px 18px', color: tab === 'sent' ? 'var(--blue)' : 'var(--t4)',
              borderBottom: `2px solid ${tab === 'sent' ? 'var(--blue)' : 'transparent'}`,
              marginBottom: -1, transition: 'all .15s',
            }}
          >내가 신청한 ({sent.length})</button>

          {isMentor && (
            <button
              onClick={() => setTab('received')}
              style={{
                background: 'none', border: 'none', cursor: 'pointer',
                fontFamily: 'var(--font)', fontSize: '.88rem', fontWeight: 600,
                padding: '10px 18px', color: tab === 'received' ? 'var(--blue)' : 'var(--t4)',
                borderBottom: `2px solid ${tab === 'received' ? 'var(--blue)' : 'transparent'}`,
                marginBottom: -1, transition: 'all .15s',
                display: 'flex', alignItems: 'center', gap: 6,
              }}
            >
              받은 신청 ({received.length})
              {pendingCount > 0 && (
                <span style={{
                  background: 'var(--orange)', color: '#fff',
                  borderRadius: 980, padding: '1px 7px', fontSize: '.72rem',
                  fontWeight: 700, minWidth: 18, textAlign: 'center',
                }}>{pendingCount}</span>
              )}
            </button>
          )}
        </div>

        {loading ? (
          <div className="empty"><div className="empty-icon">⏳</div></div>
        ) : (
          <>
            {/* ── 보낸 신청 탭 */}
            {tab === 'sent' && (
              sent.length === 0 ? (
                <div className="empty">
                  <div className="empty-icon">🎓</div>
                  <div className="empty-title">아직 신청한 멘토링이 없습니다</div>
                  <div className="empty-sub">마음에 드는 멘토를 찾아 신청해보세요!</div>
                  <div style={{ marginTop: 20 }}>
                    <Link to="/mentors" className="btn btn-blue btn-md">멘토 찾아보기</Link>
                  </div>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                  {[...sent].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).map(req => (
                    <SentCard key={req.id} req={req} />
                  ))}
                </div>
              )
            )}

            {/* ── 받은 신청 탭 (멘토 전용) */}
            {tab === 'received' && isMentor && (
              received.length === 0 ? (
                <div className="empty">
                  <div className="empty-icon">📬</div>
                  <div className="empty-title">아직 받은 멘토링 신청이 없습니다</div>
                  <div className="empty-sub">멘토 프로필을 활성화하면 멘티들이 신청할 수 있습니다</div>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                  {pendingCount > 0 && (
                    <div style={{
                      background: 'rgba(255,149,0,.08)', border: '1px solid rgba(255,149,0,.2)',
                      borderRadius: 'var(--r-sm)', padding: '12px 18px',
                      fontSize: '.85rem', color: 'var(--t2)',
                      display: 'flex', alignItems: 'center', gap: 8,
                    }}>
                      <span style={{ fontSize: '1.1rem' }}>🔔</span>
                      <span>검토 대기 중인 신청이 <strong style={{ color: 'var(--orange)' }}>{pendingCount}건</strong> 있습니다. 빠른 답변이 멘티에게 큰 도움이 됩니다!</span>
                    </div>
                  )}
                  {sortedReceived.map(req => (
                    <ReceivedCard key={req.id} req={req} onUpdate={handleUpdate} />
                  ))}
                </div>
              )
            )}
          </>
        )}
      </div>
    </div>
  )
}
