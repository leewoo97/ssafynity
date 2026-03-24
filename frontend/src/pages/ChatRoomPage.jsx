import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function ChatRoomPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member, token } = useAuthStore()
  const [messages, setMessages] = useState([])
  const [room, setRoom] = useState(null)
  const [input, setInput] = useState('')
  const [connected, setConnected] = useState(false)
  const clientRef = useRef(null)
  const bottomRef = useRef(null)

  useEffect(() => {
    api.get(`/chat/rooms/${id}`).then(r => setRoom(r.data.data))
  }, [id])

  useEffect(() => {
    // Connect STOMP
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)
        client.subscribe(`/topic/chat/${id}`, msg => {
          const body = JSON.parse(msg.body)
          setMessages(prev => [...prev, body])
          setTimeout(() => bottomRef.current?.scrollIntoView({ behavior:'smooth' }), 50)
        })
      },
      onDisconnect: () => setConnected(false),
    })
    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
    }
  }, [id, token])

  useEffect(() => {
    bottomRef.current?.scrollIntoView()
  }, [messages])

  const sendMessage = e => {
    e.preventDefault()
    if (!input.trim() || !connected) return
    clientRef.current.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ roomId: parseInt(id), content: input.trim() })
    })
    setInput('')
  }

  return (
    <div style={{ display:'flex', flexDirection:'column', height:'calc(100vh - 120px)', maxWidth:800, margin:'0 auto' }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:12 }}>
        <h3 style={{ margin:0 }}>#{room?.name || '...'}</h3>
        <div style={{ display:'flex', gap:8, alignItems:'center' }}>
          <span style={{ fontSize:12, color: connected ? 'green' : 'orange' }}>{connected ? '연결됨' : '연결 중...'}</span>
          <button onClick={() => navigate('/chat')} className="btn btn-secondary" style={{ fontSize:12 }}>목록</button>
        </div>
      </div>

      <div className="card" style={{ flex:1, overflowY:'auto', padding:16, display:'flex', flexDirection:'column', gap:10 }}>
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
