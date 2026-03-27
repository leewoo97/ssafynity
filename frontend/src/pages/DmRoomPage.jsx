import { useState, useEffect, useRef } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

function isSameDay(a, b) {
  return dayjs(a).format('YYYY-MM-DD') === dayjs(b).format('YYYY-MM-DD')
}

function DateDivider({ date }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10, margin: '16px 0', userSelect: 'none' }}>
      <div style={{ flex: 1, height: 1, background: 'var(--b1)' }} />
      <span style={{ fontSize: 12, color: 'var(--t4)', whiteSpace: 'nowrap' }}>
        {dayjs(date).format('YYYY년 M월 D일')}
      </span>
      <div style={{ flex: 1, height: 1, background: 'var(--b1)' }} />
    </div>
  )
}

export default function DmRoomPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member, token } = useAuthStore()
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [room, setRoom] = useState(null)
  const stompRef = useRef(null)
  const bottomRef = useRef(null)
  const inputRef = useRef(null)

  useEffect(() => {
    // 방 진입 시 읽음 처리 (unread badge 리셋)
    try {
      const lr = JSON.parse(localStorage.getItem('dm_lastRead') || '{}')
      lr[String(id)] = Date.now()
      localStorage.setItem('dm_lastRead', JSON.stringify(lr))
    } catch {}

    // 방 정보 + 메시지 로드
    api.get('/dm/rooms').then(r => {
      const rm = (r.data.data || []).find(rm => String(rm.id) === String(id))
      if (rm) setRoom(rm)
    }).catch(() => {})
    // 메시지 로드 완료 후 읽음 처리 (순서 보장 - read 전에 unreadCount 렌더)
    api.get(`/dm/rooms/${id}/messages`).then(r => {
      setMessages(r.data.data || [])
      return api.post(`/dm/rooms/${id}/read`)
    }).catch(() => {})

    // WebSocket
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => {
        client.subscribe(`/topic/dm/${id}`, frame => {
          const msg = JSON.parse(frame.body)
          // READ 이벤트: 해당 readAt 이전 메시지의 unreadCount 차감
          if (msg.type === 'READ') {
            const readTime = new Date(msg.readAt).getTime()
            setMessages(prev => prev.map(m => {
              const msgTime = new Date(m.createdAt || m.timestamp).getTime()
              // 메시지 발신자와 읽은 사람이 같으면 감소 안 함 (발신자는 unreadCount 계산에서 제외)
              if (msgTime <= readTime && m.unreadCount > 0 && String(m.senderId) !== String(msg.readerId)) {
                return { ...m, unreadCount: m.unreadCount - 1 }
              }
              return m
            }))
            return
          }
          // 실시간 CHAT 메시지: 내가 보낸 메시지은 unreadCount = 멤버 - 1 (나 제외한 모두)
          let finalMsg = msg
          if (msg.type === 'CHAT' && String(msg.senderId) === String(member?.id)) {
            const memberCount = room?.members?.length || 2
            finalMsg = { ...msg, unreadCount: memberCount - 1 }
          }
          setMessages(prev => [...prev, finalMsg])
        })
        client.publish({
          destination: '/app/dm.join',
          body: JSON.stringify({ type: 'JOIN', roomId: Number(id) }),
        })
      },
    })
    client.activate()
    stompRef.current = client
    return () => client.deactivate()
  }, [id])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const sendMessage = (e) => {
    e.preventDefault()
    if (!input.trim() || !stompRef.current?.connected) return
    stompRef.current.publish({
      destination: '/app/dm.send',
      body: JSON.stringify({ type: 'CHAT', roomId: Number(id), content: input.trim() }),
    })
    setInput('')
    inputRef.current?.focus()
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(e) }
  }

  const isGroup = room?.type === 'GROUP'
  const other = room?.otherMember || room?.members?.find(m => m.id !== member?.id)
  const roomName = isGroup
    ? (room?.name || room?.members?.map(m => m.nickname).join(', ') || '그룹')
    : (other?.nickname || 'DM')
  const memberCount = room?.members?.length || 0

  // 날짜 구분선 포함 메시지 렌더링
  const rendered = []
  messages.forEach((msg, i) => {
    const prev = messages[i - 1]
    if (!prev || !isSameDay(prev.createdAt || prev.timestamp, msg.createdAt || msg.timestamp)) {
      rendered.push({ type: 'date', date: msg.createdAt || msg.timestamp, key: `date-${i}` })
    }
    rendered.push({ type: 'msg', msg, key: msg.id || `msg-${i}` })
  })

  return (
    <div style={{ maxWidth: 720, margin: '0 auto', display: 'flex', flexDirection: 'column', height: 'calc(100vh - 72px)' }}>
      {/* 헤더 */}
      <div style={{
        display: 'flex', alignItems: 'center', gap: 12, padding: '14px 16px',
        borderBottom: '1px solid var(--b1)', background: '#fff', flexShrink: 0,
      }}>
        <button onClick={() => navigate('/dm')} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 18, color: 'var(--t3)', padding: '4px 8px' }}>←</button>
        <div className="av av-md" style={{ background: isGroup ? '#f3e8ff' : undefined, color: isGroup ? '#9333ea' : undefined, fontSize: isGroup ? 18 : undefined }}>
          {isGroup ? '👥' : other?.nickname?.charAt(0)?.toUpperCase()}
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontWeight: 700, fontSize: 15 }}>{roomName}</div>
          {isGroup && memberCount > 0 && (
            <div style={{ fontSize: 12, color: 'var(--t4)' }}>{memberCount}명</div>
          )}
        </div>
      </div>

      {/* 메시지 영역 */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '12px 16px', background: 'var(--surface-1, #f8f9fa)' }}>
        {rendered.map(item => {
          if (item.type === 'date') return <DateDivider key={item.key} date={item.date} />

          const msg = item.msg
          if (msg.type === 'JOIN' || msg.type === 'LEAVE') {
            return (
              <div key={item.key} style={{ textAlign: 'center', fontSize: 12, color: 'var(--t4)', margin: '8px 0' }}>
                {msg.senderNickname} {msg.type === 'JOIN' ? '님이 입장했습니다' : '님이 나갔습니다'}
              </div>
            )
          }

          const isMe = String(msg.senderId) === String(member?.id)
          const time = dayjs(msg.createdAt || msg.timestamp).format('HH:mm')

          return (
            <div key={item.key} style={{ display: 'flex', justifyContent: isMe ? 'flex-end' : 'flex-start', marginBottom: 4, alignItems: 'flex-end', gap: 6 }}>
              {/* 상대방: 아바타 + 메시지 */}
              {!isMe && (
                <div style={{ display: 'flex', alignItems: 'flex-end', gap: 8 }}>
                  <div>
                    <div className="av av-sm" style={{ marginBottom: 2 }}>
                      {msg.senderNickname?.charAt(0)?.toUpperCase()}
                    </div>
                  </div>
                  <div>
                    {isGroup && <div style={{ fontSize: 11, color: 'var(--t4)', marginBottom: 3, paddingLeft: 2 }}>{msg.senderNickname}</div>}
                    <div style={{ display: 'flex', alignItems: 'flex-end', gap: 4 }}>
                      <div style={{
                        background: '#fff', color: 'var(--t1)',
                        borderRadius: '0 18px 18px 18px',
                        padding: '9px 14px', fontSize: 14, maxWidth: 280,
                        boxShadow: '0 1px 2px rgba(0,0,0,.08)',
                        lineHeight: 1.5, wordBreak: 'break-word',
                      }}>
                        {msg.content}
                      </div>
                      <span style={{ fontSize: 10, color: 'var(--t5)', flexShrink: 0 }}>{time}</span>
                    </div>
                  </div>
                </div>
              )}

              {/* 내 메시지 */}
              {isMe && (
                <div style={{ display: 'flex', alignItems: 'flex-end', gap: 4 }}>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 2, flexShrink: 0 }}>
                    {msg.unreadCount > 0 && (
                      <span style={{ fontSize: 11, fontWeight: 700, color: '#D97706', lineHeight: 1 }}>{msg.unreadCount}</span>
                    )}
                    <span style={{ fontSize: 10, color: 'var(--t5)' }}>{time}</span>
                  </div>
                  <div style={{
                    background: '#FEE500', color: '#3c1e1e',
                    borderRadius: '18px 0 18px 18px',
                    padding: '9px 14px', fontSize: 14, maxWidth: 280,
                    lineHeight: 1.5, wordBreak: 'break-word',
                  }}>
                    {msg.content}
                  </div>
                </div>
              )}
            </div>
          )
        })}
        <div ref={bottomRef} />
      </div>

      {/* 입력창 */}
      <form onSubmit={sendMessage} style={{
        borderTop: '1px solid var(--b1)', padding: '10px 14px',
        display: 'flex', gap: 8, background: '#fff', flexShrink: 0,
        alignItems: 'flex-end',
      }}>
        <textarea
          ref={inputRef}
          rows={1}
          value={input}
          onChange={e => {
            setInput(e.target.value)
            e.target.style.height = 'auto'
            e.target.style.height = Math.min(e.target.scrollHeight, 120) + 'px'
          }}
          onKeyDown={handleKeyDown}
          placeholder="메시지 입력..."
          style={{
            flex: 1, resize: 'none', border: '1px solid var(--b2)', borderRadius: 22,
            padding: '10px 16px', fontSize: 14, outline: 'none', overflowY: 'auto',
            fontFamily: 'inherit', lineHeight: 1.5,
          }}
        />
        <button type="submit" style={{
          width: 40, height: 40, borderRadius: '50%', border: 'none',
          background: input.trim() ? '#FEE500' : 'var(--surface-2)',
          cursor: input.trim() ? 'pointer' : 'default',
          fontSize: 18, flexShrink: 0, transition: 'background .15s',
        }}>↑</button>
      </form>
    </div>
  )
}
