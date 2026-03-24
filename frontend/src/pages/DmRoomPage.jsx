import { useState, useEffect, useRef } from 'react'
import { useParams, Link } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function DmRoomPage() {
  const { id } = useParams()
  const { member, token } = useAuthStore()
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [other, setOther] = useState(null)
  const stompRef = useRef(null)
  const bottomRef = useRef(null)

  useEffect(() => {
    api.get(`/dm/rooms/${id}/messages`).then(r => setMessages(r.data.data || []))
    api.get(`/dm/rooms/${id}`).then(r => {
      const otherMember = r.data.data?.members?.find(m => m.id !== member?.id)
      setOther(otherMember)
    })

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => {
        client.subscribe(`/user/queue/dm/${id}`, msg => {
          setMessages(prev => [...prev, JSON.parse(msg.body)])
        })
      },
    })
    client.activate()
    stompRef.current = client
    return () => client.deactivate()
  }, [id])

  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }) }, [messages])

  const sendMessage = (e) => {
    e.preventDefault()
    if (!input.trim() || !stompRef.current?.connected) return
    stompRef.current.publish({
      destination: `/app/dm/${id}`,
      body: JSON.stringify({ content: input }),
    })
    setInput('')
  }

  return (
    <div style={{ maxWidth: 640, margin: '0 auto' }}>
      <div className="section-head">
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div className="av av-md">{other?.nickname?.charAt(0)?.toUpperCase()}</div>
          <h2 style={{ margin: 0 }}>{other?.nickname || 'DM'}</h2>
        </div>
        <Link to="/dm" className="btn btn-ghost btn-sm">← 목록</Link>
      </div>

      <div className="card" style={{ padding: 0, display: 'flex', flexDirection: 'column', height: 520 }}>
        <div style={{ flex: 1, overflowY: 'auto', padding: '16px 20px' }}>
          {messages.map((msg, i) => {
            const isMe = msg.senderId === member?.id
            return (
              <div key={i} style={{ display: 'flex', justifyContent: isMe ? 'flex-end' : 'flex-start', marginBottom: 12 }}>
                {!isMe && <div className="av av-sm" style={{ marginRight: 8 }}>{other?.nickname?.charAt(0)?.toUpperCase()}</div>}
                <div style={{
                  background: isMe ? 'var(--blue)' : 'var(--bg)',
                  color: isMe ? '#fff' : 'var(--t1)',
                  borderRadius: isMe ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
                  padding: '10px 14px', fontSize: '.9rem', maxWidth: 280
                }}>
                  {msg.content}
                </div>
              </div>
            )
          })}
          <div ref={bottomRef} />
        </div>

        <form onSubmit={sendMessage} style={{ borderTop: '1px solid var(--b1)', padding: '12px 16px', display: 'flex', gap: 8 }}>
          <input className="form-input" value={input} onChange={e => setInput(e.target.value)}
            placeholder="메시지 입력..." style={{ flex: 1 }} />
          <button type="submit" className="btn btn-blue btn-sm">전송</button>
        </form>
      </div>
    </div>
  )
}
