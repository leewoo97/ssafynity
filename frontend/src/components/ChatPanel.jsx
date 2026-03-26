/**
 * ChatPanel — 우측 슬라이딩 패널
 * 탭: 채팅방 | 다이렉트 | 멘션
 * 방 목록 → 방 내부 (인라인, 페이지 이동 없음)
 */
import { useState, useEffect, useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

// ── 색상 헬퍼 ─────────────────────────────────────────────────────────
const AV_COLORS = ['#3B82F6','#10B981','#F59E0B','#EF4444','#8B5CF6','#EC4899','#14B8A6','#F97316']
function avColor(name) { let n=0; for(const c of (name||'')) n+=c.charCodeAt(0); return AV_COLORS[n%8] }
function Av({ name, size=36, style:s={} }) {
  return (
    <div style={{
      width:size, height:size, borderRadius:8,
      background:avColor(name), color:'#fff', flexShrink:0,
      display:'flex', alignItems:'center', justifyContent:'center',
      fontWeight:700, fontSize:Math.round(size*.44), ...s,
    }}>
      {name?.charAt(0)?.toUpperCase()}
    </div>
  )
}

function isSameDay(a,b) { return dayjs(a).format('YYYY-MM-DD')===dayjs(b).format('YYYY-MM-DD') }

function getLastRead() { try { return JSON.parse(localStorage.getItem('dm_lastRead')||'{}') } catch { return {} } }

// ── 방 목록 아이템 ────────────────────────────────────────────────────
function RoomItem({ room, myId, lastRead, onClick }) {
  const isGroup = room.type==='GROUP'
  const other = room.otherMember || room.members?.find(m=>m.id!==myId)
  const name = isGroup ? (room.name||'그룹') : (other?.nickname||'?')
  const lr = lastRead[String(room.id)]
  const unread = room.lastMessageAt && (!lr || new Date(room.lastMessageAt).getTime()>lr)

  return (
    <div onClick={onClick} style={{
      display:'flex', alignItems:'center', gap:10, padding:'10px 14px',
      cursor:'pointer', borderBottom:'1px solid rgba(255,255,255,.06)',
      background:'transparent', transition:'background .12s',
    }}
    onMouseEnter={e=>e.currentTarget.style.background='rgba(255,255,255,.07)'}
    onMouseLeave={e=>e.currentTarget.style.background='transparent'}>
      {isGroup
        ? <div style={{width:38,height:38,borderRadius:8,background:'rgba(139,92,246,.35)',display:'flex',alignItems:'center',justifyContent:'center',fontSize:18,flexShrink:0}}>👥</div>
        : <Av name={other?.nickname} size={38} />
      }
      <div style={{flex:1,minWidth:0}}>
        <div style={{display:'flex',justifyContent:'space-between',alignItems:'center'}}>
          <span style={{fontWeight:unread?700:500,fontSize:13,color:'#e8eaf0',whiteSpace:'nowrap',overflow:'hidden',textOverflow:'ellipsis',maxWidth:130}}>{name}</span>
          {room.lastMessageAt && <span style={{fontSize:10,color:'rgba(255,255,255,.38)',flexShrink:0,marginLeft:6}}>{dayjs(room.lastMessageAt).format('HH:mm')}</span>}
        </div>
        <div style={{fontSize:11,color:unread?'rgba(255,255,255,.7)':'rgba(255,255,255,.38)',fontWeight:unread?500:400,whiteSpace:'nowrap',overflow:'hidden',textOverflow:'ellipsis',marginTop:1}}>
          {room.lastMessageContent||'메시지 없음'}
        </div>
      </div>
      {unread && <span style={{width:8,height:8,borderRadius:'50%',background:'#3B82F6',flexShrink:0}} />}
    </div>
  )
}

// ── 인라인 채팅 뷰 ────────────────────────────────────────────────────
function InlineRoom({ room, myId, token, onBack, onMarkRead }) {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const stompRef = useRef(null)
  const bottomRef = useRef(null)
  const inputRef = useRef(null)
  const isGroup = room.type==='GROUP'

  useEffect(() => {
    api.get(`/dm/rooms/${room.id}/messages`).then(r => setMessages(r.data.data||[])).catch(()=>{})
    try {
      const lr = JSON.parse(localStorage.getItem('dm_lastRead')||'{}')
      lr[String(room.id)] = Date.now()
      localStorage.setItem('dm_lastRead', JSON.stringify(lr))
    } catch {}
    onMarkRead(room.id)

    const client = new Client({
      webSocketFactory: ()=>new SockJS('/ws'),
      connectHeaders: { Authorization:`Bearer ${token}` },
      onConnect: () => {
        client.subscribe(`/topic/dm/${room.id}`, msg => {
          setMessages(prev=>[...prev, JSON.parse(msg.body)])
        })
      },
    })
    client.activate()
    stompRef.current = client
    return () => client.deactivate()
  }, [room.id])

  useEffect(() => { bottomRef.current?.scrollIntoView({behavior:'smooth'}) }, [messages])

  const send = () => {
    if(!input.trim() || !stompRef.current?.connected) return
    stompRef.current.publish({
      destination:'/app/dm.send',
      body: JSON.stringify({type:'CHAT', roomId:room.id, content:input.trim()}),
    })
    setInput('')
    inputRef.current?.focus()
  }

  const other = room.otherMember || room.members?.find(m=>m.id!==myId)
  const roomName = isGroup ? (room.name||'그룹') : (other?.nickname||'?')
  const memberCount = isGroup ? (room.members?.length||0) : null

  return (
    <div style={{display:'flex',flexDirection:'column',height:'100%'}}>
      {/* 방 헤더 */}
      <div style={{display:'flex',alignItems:'center',gap:8,padding:'10px 14px',borderBottom:'1px solid rgba(255,255,255,.1)',background:'rgba(0,0,0,.2)',flexShrink:0}}>
        <button onClick={onBack} style={{background:'none',border:'none',color:'rgba(255,255,255,.7)',cursor:'pointer',fontSize:16,padding:'2px 4px',lineHeight:1}}>←</button>
        <span style={{fontWeight:600,fontSize:13,color:'#e8eaf0',flex:1}}>{roomName}</span>
        {memberCount && <span style={{fontSize:11,color:'rgba(255,255,255,.4)'}}>{memberCount}</span>}
      </div>

      {/* 메시지 목록 */}
      <div style={{flex:1,overflowY:'auto',padding:'12px 10px',display:'flex',flexDirection:'column',gap:2}}>
        {messages.map((msg,i) => {
          const isMine = String(msg.senderId)===String(myId) || msg.senderNickname===undefined
          const prev = messages[i-1]
          const showDate = !prev || !isSameDay(msg.sentAt||msg.createdAt, prev.sentAt||prev.createdAt)
          const showSender = isGroup && !isMine && (i===0 || messages[i-1]?.senderId!==msg.senderId)

          if(msg.type==='JOIN'||msg.type==='LEAVE') return (
            <div key={i} style={{textAlign:'center',fontSize:10,color:'rgba(255,255,255,.35)',padding:'4px 0'}}>{msg.content}</div>
          )

          return (
            <div key={i}>
              {showDate && (
                <div style={{textAlign:'center',fontSize:10,color:'rgba(255,255,255,.35)',margin:'10px 0 6px',letterSpacing:'.03em'}}>
                  {dayjs(msg.sentAt||msg.createdAt).format('YYYY년 M월 D일')}
                </div>
              )}
              {showSender && msg.senderNickname && (
                <div style={{fontSize:10,color:'rgba(255,255,255,.5)',marginBottom:2,marginTop:6,paddingLeft:2}}>{msg.senderNickname}</div>
              )}
              <div style={{display:'flex',justifyContent:isMine?'flex-end':'flex-start',alignItems:'flex-end',gap:4}}>
                {!isMine && (
                  <span style={{fontSize:9,color:'rgba(255,255,255,.35)',marginBottom:1,flexShrink:0}}>
                    {dayjs(msg.sentAt||msg.createdAt).format('HH:mm')}
                  </span>
                )}
                <div style={{
                  maxWidth:'72%',padding:'7px 10px',borderRadius:isMine?'12px 2px 12px 12px':'2px 12px 12px 12px',
                  background:isMine?'#FEE500':'rgba(255,255,255,.12)',
                  color:isMine?'#1a1a1a':'#e8eaf0',
                  fontSize:12.5,lineHeight:1.5,wordBreak:'break-word',
                }}>
                  {msg.content}
                </div>
                {isMine && (
                  <span style={{fontSize:9,color:'rgba(255,255,255,.35)',marginBottom:1,flexShrink:0}}>
                    {dayjs(msg.sentAt||msg.createdAt).format('HH:mm')}
                  </span>
                )}
              </div>
            </div>
          )
        })}
        <div ref={bottomRef} />
      </div>

      {/* 입력창 */}
      <div style={{padding:'8px 10px',borderTop:'1px solid rgba(255,255,255,.1)',background:'rgba(0,0,0,.2)',flexShrink:0,display:'flex',gap:6,alignItems:'flex-end'}}>
        <textarea
          ref={inputRef}
          value={input}
          onChange={e=>setInput(e.target.value)}
          onKeyDown={e=>{ if(e.key==='Enter'&&!e.shiftKey){e.preventDefault();send()} }}
          placeholder="메시지 입력"
          rows={1}
          style={{flex:1,resize:'none',background:'rgba(255,255,255,.1)',border:'1px solid rgba(255,255,255,.15)',borderRadius:8,padding:'7px 10px',color:'#e8eaf0',fontSize:12.5,outline:'none',lineHeight:1.4,maxHeight:80,overflowY:'auto'}}
        />
        <button onClick={send} style={{width:32,height:32,borderRadius:'50%',border:'none',cursor:'pointer',background:input.trim()?'#FEE500':'rgba(255,255,255,.15)',color:input.trim()?'#1a1a1a':'rgba(255,255,255,.4)',display:'flex',alignItems:'center',justifyContent:'center',fontSize:14,transition:'background .15s',flexShrink:0}}>
          ▶
        </button>
      </div>
    </div>
  )
}

// ── 메인 ChatPanel ─────────────────────────────────────────────────────
export default function ChatPanel({ open, onClose }) {
  const { member, token } = useAuthStore()
  const [tab, setTab] = useState('dm')       // 'chat' | 'dm' | 'mention'
  const [rooms, setRooms] = useState([])
  const [activeRoom, setActiveRoom] = useState(null)
  const [search, setSearch] = useState('')
  const [lastRead, setLastRead] = useState(getLastRead())
  const stompRef = useRef(null)
  const subsRef = useRef([])

  const loadRooms = useCallback(async () => {
    try { setRooms((await api.get('/dm/rooms')).data.data||[]) } catch {}
  }, [])

  useEffect(() => { if(open && member) loadRooms() }, [open, member])

  // 실시간 방 목록 갱신
  const roomIds = rooms.map(r=>r.id).join(',')
  useEffect(() => {
    if(!rooms.length||!token) return
    const client = new Client({
      webSocketFactory:()=>new SockJS('/ws'),
      connectHeaders:{Authorization:`Bearer ${token}`},
      onConnect:()=>{
        subsRef.current.forEach(s=>{try{s.unsubscribe()}catch{}})
        subsRef.current = rooms.map(room =>
          client.subscribe(`/topic/dm/${room.id}`, msg=>{
            const dto = JSON.parse(msg.body)
            if(dto.type!=='CHAT') return
            setRooms(prev=>{
              const upd = prev.map(r=>r.id===room.id ? {...r,lastMessageContent:dto.content,lastMessageAt:dto.sentAt||new Date().toISOString()} : r)
              const hit = upd.find(r=>r.id===room.id)
              return hit ? [hit,...upd.filter(r=>r.id!==room.id)] : upd
            })
            setLastRead(getLastRead())
          })
        )
      },
    })
    client.activate()
    stompRef.current = client
    return ()=>{ client.deactivate(); subsRef.current=[] }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roomIds, token])

  const markRead = (roomId) => {
    setLastRead(prev=>({...prev,[String(roomId)]:Date.now()}))
  }

  const filtered = rooms.filter(r=>{
    if(!search.trim()) return true
    const isGroup=r.type==='GROUP'
    const name=isGroup?(r.name||''):(r.otherMember?.nickname||'')
    return name.toLowerCase().includes(search.toLowerCase())
  })

  const totalUnread = rooms.filter(r=>{
    if(!r.lastMessageAt) return false
    const lr=lastRead[String(r.id)]
    return !lr||new Date(r.lastMessageAt).getTime()>lr
  }).length

  const PANEL_W = 340

  return (
    <>
      {/* 오버레이 — 패널 바깥 클릭으로 닫기 */}
      {open && <div onClick={onClose} style={{position:'fixed',inset:0,zIndex:1199,background:'transparent'}} />}

      <div style={{
        position:'fixed', top:0, right:0, bottom:0, zIndex:1200,
        width:PANEL_W,
        background:'#1e2130',
        boxShadow:open?'-4px 0 32px rgba(0,0,0,.45)':'none',
        transform:open?'translateX(0)':'translateX(100%)',
        transition:'transform .25s cubic-bezier(.4,0,.2,1)',
        display:'flex', flexDirection:'column',
        userSelect:'none',
      }}>
        {/* 패널 헤더 */}
        <div style={{display:'flex',alignItems:'center',padding:'0 14px',height:52,borderBottom:'1px solid rgba(255,255,255,.1)',flexShrink:0,gap:6}}>
          <button onClick={onClose} style={{background:'none',border:'none',color:'rgba(255,255,255,.6)',cursor:'pointer',fontSize:16,padding:'4px',lineHeight:1,borderRadius:4}}>
            ✕
          </button>
          <span style={{fontWeight:700,fontSize:14,color:'#e8eaf0',flex:1}}>팀 채팅</span>
          {/* 탭 */}
          {['dm','mention'].map(t=>(  // chat 탭은 별도 구현 여지
            <button key={t} onClick={()=>{setTab(t);setActiveRoom(null)}} style={{background:'none',border:'none',padding:'4px 8px',borderRadius:6,cursor:'pointer',fontSize:11,fontWeight:600,color:tab===t?'#fff':'rgba(255,255,255,.45)',background:tab===t?'rgba(255,255,255,.12)':'transparent',transition:'all .15s'}}>
              {t==='dm'?'다이렉트':t==='mention'?'멘션':'채팅방'}
            </button>
          ))}
        </div>

        {activeRoom ? (
          /* 방 내부 */
          <InlineRoom
            room={activeRoom}
            myId={member?.id}
            token={token}
            onBack={()=>setActiveRoom(null)}
            onMarkRead={markRead}
          />
        ) : (
          /* 방 목록 */
          <>
            {/* 검색창 */}
            <div style={{padding:'8px 12px',borderBottom:'1px solid rgba(255,255,255,.06)',flexShrink:0}}>
              <div style={{display:'flex',alignItems:'center',gap:6,background:'rgba(255,255,255,.08)',borderRadius:8,padding:'6px 10px'}}>
                <span style={{color:'rgba(255,255,255,.35)',fontSize:13}}>⌕</span>
                <input value={search} onChange={e=>setSearch(e.target.value)} placeholder="채팅방 검색"
                  style={{flex:1,background:'none',border:'none',outline:'none',color:'#e8eaf0',fontSize:12.5}} />
              </div>
            </div>

            {/* 목록 탭 헤더 */}
            <div style={{display:'flex',borderBottom:'1px solid rgba(255,255,255,.06)',flexShrink:0}}>
              {[['dm','다이렉트'],['mention','멘션']].map(([t,label])=>(
                <button key={t} onClick={()=>{setTab(t);setSearch('')}} style={{
                  flex:1,padding:'9px 0',background:'none',border:'none',
                  borderBottom:`2px solid ${tab===t?'#3B82F6':'transparent'}`,
                  color:tab===t?'#e8eaf0':'rgba(255,255,255,.4)',
                  fontWeight:tab===t?700:400,cursor:'pointer',fontSize:12,transition:'all .15s',
                }}>
                  {label}
                  {t==='dm'&&totalUnread>0&&<span style={{marginLeft:4,background:'#3B82F6',color:'#fff',borderRadius:10,padding:'0 5px',fontSize:10,fontWeight:700}}>{totalUnread}</span>}
                </button>
              ))}
            </div>

            <div style={{flex:1,overflowY:'auto'}}>
              {tab==='mention' ? (
                <div style={{textAlign:'center',padding:'40px 20px',color:'rgba(255,255,255,.3)',fontSize:12}}>멘션 기능 준비 중</div>
              ) : filtered.length===0 ? (
                <div style={{textAlign:'center',padding:'40px 20px',color:'rgba(255,255,255,.3)',fontSize:12}}>
                  {search?'검색 결과 없음':'대화 내역이 없습니다'}
                </div>
              ) : (
                filtered.map(r=>(
                  <RoomItem key={r.id} room={r} myId={member?.id} lastRead={lastRead}
                    onClick={()=>setActiveRoom(r)} />
                ))
              )}
            </div>

            {/* 새 대화 버튼 */}
            {tab==='dm' && (
              <div style={{padding:'10px 12px',borderTop:'1px solid rgba(255,255,255,.06)',flexShrink:0}}>
                <a href="/dm" style={{display:'flex',alignItems:'center',justifyContent:'center',gap:6,padding:'8px',borderRadius:8,background:'rgba(59,130,246,.2)',color:'#93c5fd',textDecoration:'none',fontSize:12,fontWeight:600,transition:'background .15s'}}
                  onMouseEnter={e=>e.currentTarget.style.background='rgba(59,130,246,.3)'}
                  onMouseLeave={e=>e.currentTarget.style.background='rgba(59,130,246,.2)'}>
                  + 새 대화 시작
                </a>
              </div>
            )}
          </>
        )}
      </div>
    </>
  )
}
