import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function DmRoomPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member, token } = useAuthStore()
  const [messages, setMessages] = useState([])
  const [room, setRoom] = useState(null)
  const [input, setInput] = useState('')
  const [connected, setConnected] = useState(false)
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(true)
  const clientRef = useRef(null)
  const bottomRef = useRef(null)

  // Load room info and history
  useEffect(() => {
    api.get(`/dm/rooms`).then(r => {
      const found = (r.data.data || []).find(rm => String(rm.id) === String(id))
      setRoom(found)
    })
    api.get(`/dm/rooms/${id}/messages`, { params: { page:0, size:30 } }).then(r => {
      const data = r.data.data
      const content = [...(data.content || [])].reverse()
      setMessages(content)
      setHasMore(!data.last)
      setPage(0)
    })
  }, [id])

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)
        client.subscribe(`/user/queue/dm`, msg => {
          const body = JSON.parse(msg.body)
          if (String(body.roomId) === String(id)) {
            setMessages(prev => [...prev, body])
            setTimeout(() => bottomRef.current?.scrollIntoView({ behavior:'smooth' }), 50)
          }
        })
      },
      onDisconnect: () => setConnected(false),
    })
    client.activate()
    clientRef.current = client
    return () => client.deactivate()
  }, [id, token])

  useEffect(() => {
    bottomRef.current?.scrollIntoView()
  }, [messages])

  const loadMore = async () => {
    const nextPage = page + 1
    const r = await api.get(`/dm/rooms/${id}/messages`, { params: { page: nextPage, size:30 } })
    const data = r.data.data
    const older = [...(data.content || [])].reverse()
    setMessages(prev => [...older, ...prev])
    setHasMore(!data.last)
    setPage(nextPage)
  }

  const sendMessage = e => {
    e.preventDefault()
    if (!input.trim() || !connected) return
    clientRef.current.publish({
      destination: '/app/dm.send',
      body: JSON.stringify({ roomId: parseInt(id), content: input.trim() })
    })
    setInput('')
  }

  const handleLeave = async () => {
    if (!confirm('나가시겠습니까?')) return
    await api.post(`/dm/rooms/${id}/leave`)
    navigate('/dm')
  }

  const otherName = room?.type === 'GROUP' ? room?.name : room?.otherMember?.nickname

  return (
    <div style={{ display:'flex', flexDirection:'column', height:'calc(100vh - 120px)', maxWidth:800, margin:'0 auto' }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:12 }}>
        <h3 style={{ margin:0 }}>
          {room?.type === 'GROUP' ? `👥 ${otherName}` : `💬 ${otherName || '...'}`}
        </h3>
        <div style={{ display:'flex', gap:8, alignItems:'center' }}>
          <span style={{ fontSize:12, color: connected ? 'green' : 'orange' }}>{connected ? '연결됨' : '연결 중...'}</span>
          <button onClick={handleLeave} className="btn btn-secondary" style={{ fontSize:12 }}>나가기</button>
          <button onClick={() => navigate('/dm')} className="btn btn-secondary" style={{ fontSize:12 }}>목록</button>
        </div>
      </div>

      <div className="card" style={{ flex:1, overflowY:'auto', padding:16, display:'flex', flexDirection:'column', gap:10 }}>
        {hasMore && (
          <button onClick={loadMore} className="btn btn-secondary" style={{ alignSelf:'center', fontSize:12, marginBottom:8 }}>이전 메시지 보기</button>
        )}
        {messages.map((msg, i) => {
          const isMe = msg.senderId === member?.id
          return (
            <div key={i} style={{ display:'flex', justifyContent: isMe ? 'flex-end' : 'flex-start', gap:8 }}>
              {!isMe && <span style={{ fontSize:13, fontWeight:600, alignSelf:'flex-end' }}>{msg.senderNickname}</span>}
              <div style={{
                maxWidth:'60%', background: isMe ? 'var(--color-primary)' : '#f1f3f5',
                color: isMe ? '#fff' : 'inherit',
                padding:'8px 14px', borderRadius: isMe ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
                fontSize:14, lineHeight:1.5
              }}>
                {msg.content}
                <div style={{ fontSize:10, textAlign:'right', marginTop:2, opacity:0.7 }}>
                  {dayjs(msg.sentAt || msg.createdAt).format('HH:mm')}
                </div>
              </div>
            </div>
          )
        })}
        <div ref={bottomRef} />
      </div>

      <form onSubmit={sendMessage} style={{ display:'flex', gap:8, marginTop:8 }}>
        <input className="form-control" value={input} onChange={e => setInput(e.target.value)}
          placeholder={connected ? '메시지 입력...' : '연결 중...'} disabled={!connected} style={{ flex:1 }} />
        <button type="submit" className="btn btn-primary" disabled={!connected || !input.trim()}>전송</button>
      </form>
    </div>
  )
}
