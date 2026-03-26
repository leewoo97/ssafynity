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
    <div style={{width:size,height:size,borderRadius:8,background:avColor(name),color:'#fff',flexShrink:0,
      display:'flex',alignItems:'center',justifyContent:'center',fontWeight:700,fontSize:Math.round(size*.44),...s}}>
      {name?.charAt(0)?.toUpperCase()}
    </div>
  )
}
function isSameDay(a,b) { return dayjs(a).format('YYYY-MM-DD')===dayjs(b).format('YYYY-MM-DD') }
function getLastRead() { try { return JSON.parse(localStorage.getItem('dm_lastRead')||'{}') } catch { return {} } }

const PANEL_BG = '#1e2130'
const BORDER = 'rgba(255,255,255,.08)'
const TEXT1 = '#e8eaf0'
const TEXT2 = 'rgba(255,255,255,.55)'
const TEXT3 = 'rgba(255,255,255,.3)'

// ── 공통 패널 헤더 ────────────────────────────────────────────────────
function PanelHeader({ onBack, title, actionLabel, actionColor='#3B82F6', onAction, actionDisabled }) {
  return (
    <div style={{display:'flex',alignItems:'center',gap:6,padding:'0 14px',height:50,borderBottom:`1px solid ${BORDER}`,flexShrink:0}}>
      <button onClick={onBack} style={{background:'none',border:'none',color:TEXT2,cursor:'pointer',fontSize:18,padding:'4px 6px',lineHeight:1}}>←</button>
      <span style={{flex:1,fontWeight:700,fontSize:14,color:TEXT1}}>{title}</span>
      {actionLabel && (
        <button onClick={onAction} disabled={actionDisabled}
          style={{background:'none',border:'none',cursor:actionDisabled?'default':'pointer',fontWeight:700,fontSize:13,
          color:actionDisabled?TEXT3:actionColor}}>
          {actionLabel}
        </button>
      )}
    </div>
  )
}

// ── 멤버 목록 (검색 + 전체 표시) ─────────────────────────────────────
function MemberList({ selected, onToggle, singleSelect }) {
  const [q, setQ] = useState('')
  const [all, setAll] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/members/search').then(r=>{setAll(r.data.data||[])}).finally(()=>setLoading(false))
  }, [])

  const list = q.trim() ? all.filter(m=>m.nickname.toLowerCase().includes(q.toLowerCase())) : all

  return (
    <>
      <div style={{padding:'8px 12px',background:'rgba(0,0,0,.15)',borderBottom:`1px solid ${BORDER}`}}>
        <div style={{display:'flex',alignItems:'center',gap:6,background:'rgba(255,255,255,.08)',borderRadius:8,padding:'6px 10px'}}>
          <span style={{color:TEXT3,fontSize:13}}>⌕</span>
          <input value={q} onChange={e=>setQ(e.target.value)} placeholder="이름 검색"
            autoFocus style={{flex:1,background:'none',border:'none',outline:'none',color:TEXT1,fontSize:12.5}} />
        </div>
      </div>
      <div style={{flex:1,overflowY:'auto'}}>
        {loading
          ? <div style={{textAlign:'center',padding:32,color:TEXT3,fontSize:12}}>불러오는 중...</div>
          : list.length===0
            ? <div style={{textAlign:'center',padding:32,color:TEXT3,fontSize:12}}>{q?'검색 결과 없음':'멤버가 없습니다'}</div>
            : list.map(m=>{
              const isSel = !!selected.find(x=>x.id===m.id)
              return (
                <div key={m.id} onClick={()=>onToggle(m)}
                  style={{display:'flex',alignItems:'center',gap:10,padding:'9px 14px',
                    borderBottom:`1px solid ${BORDER}`,cursor:'pointer',
                    background:isSel?'rgba(59,130,246,.12)':'transparent'}}
                  onMouseEnter={e=>{ if(!isSel) e.currentTarget.style.background='rgba(255,255,255,.05)' }}
                  onMouseLeave={e=>{ if(!isSel) e.currentTarget.style.background='transparent' }}>
                  <Av name={m.nickname} size={36} />
                  <div style={{flex:1,minWidth:0}}>
                    <div style={{fontWeight:600,fontSize:13,color:TEXT1}}>{m.nickname}</div>
                    {m.campus&&<div style={{fontSize:11,color:TEXT3}}>{m.campus} {m.cohort}기</div>}
                  </div>
                  {singleSelect
                    ? isSel&&<span style={{color:'#3B82F6',fontSize:16}}>✓</span>
                    : <div style={{width:22,height:22,borderRadius:'50%',
                        border:`2px solid ${isSel?'#3B82F6':'rgba(255,255,255,.25)'}`,
                        background:isSel?'#3B82F6':'transparent',
                        display:'flex',alignItems:'center',justifyContent:'center',transition:'all .15s'}}>
                        {isSel&&<span style={{color:'#fff',fontSize:12,fontWeight:700}}>✓</span>}
                      </div>
                  }
                </div>
              )
            })
        }
      </div>
    </>
  )
}

// ── 방 목록 아이템 ────────────────────────────────────────────────────
function RoomItem({ room, myId, lastRead, onClick }) {
  const isGroup = room.type==='GROUP'
  const other = room.otherMember||room.members?.find(m=>m.id!==myId)
  const name = isGroup?(room.name||'그룹'):(other?.nickname||'?')
  const lr = lastRead[String(room.id)]
  const unread = room.lastMessageAt&&(!lr||new Date(room.lastMessageAt).getTime()>lr)

  return (
    <div onClick={onClick} style={{display:'flex',alignItems:'center',gap:10,padding:'10px 14px',
      cursor:'pointer',borderBottom:`1px solid ${BORDER}`,transition:'background .12s'}}
      onMouseEnter={e=>e.currentTarget.style.background='rgba(255,255,255,.07)'}
      onMouseLeave={e=>e.currentTarget.style.background='transparent'}>
      <div style={{position:'relative',flexShrink:0}}>
        {isGroup
          ? <div style={{width:38,height:38,borderRadius:10,background:'rgba(139,92,246,.3)',display:'flex',alignItems:'center',justifyContent:'center',fontSize:18}}>👥</div>
          : <Av name={other?.nickname} size={38} />
        }
        {unread&&<span style={{position:'absolute',top:-2,right:-2,width:10,height:10,borderRadius:'50%',background:'#3B82F6',border:`2px solid ${PANEL_BG}`}} />}
      </div>
      <div style={{flex:1,minWidth:0}}>
        <div style={{display:'flex',justifyContent:'space-between',alignItems:'center'}}>
          <span style={{fontWeight:unread?700:500,fontSize:13,color:TEXT1,whiteSpace:'nowrap',overflow:'hidden',textOverflow:'ellipsis',maxWidth:150}}>{name}</span>
          {room.lastMessageAt&&<span style={{fontSize:10,color:TEXT3,flexShrink:0,marginLeft:4}}>{dayjs(room.lastMessageAt).format('HH:mm')}</span>}
        </div>
        <div style={{fontSize:11,color:unread?TEXT2:TEXT3,fontWeight:unread?500:400,whiteSpace:'nowrap',overflow:'hidden',textOverflow:'ellipsis',marginTop:1}}>
          {room.lastMessageContent||'메시지 없음'}
        </div>
      </div>
    </div>
  )
}

// ── 인라인 채팅 뷰 ────────────────────────────────────────────────────
function InlineRoom({ room, myId, token, onBack, onMarkRead }) {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const stompRef = useRef(null)
  const bottomRef = useRef(null)
  const textareaRef = useRef(null)
  const isGroup = room.type==='GROUP'

  useEffect(() => {
    api.get(`/dm/rooms/${room.id}/messages`).then(r=>setMessages(r.data.data||[])).catch(()=>{})
    try {
      const lr = JSON.parse(localStorage.getItem('dm_lastRead')||'{}')
      lr[String(room.id)] = Date.now()
      localStorage.setItem('dm_lastRead',JSON.stringify(lr))
    } catch {}
    onMarkRead(room.id)

    const client = new Client({
      webSocketFactory:()=>new SockJS('/ws'),
      connectHeaders:{Authorization:`Bearer ${token}`},
      onConnect:()=>{
        client.subscribe(`/topic/dm/${room.id}`,msg=>{
          setMessages(prev=>[...prev,JSON.parse(msg.body)])
        })
      },
    })
    client.activate()
    stompRef.current=client
    return ()=>client.deactivate()
  }, [room.id])

  useEffect(()=>{ bottomRef.current?.scrollIntoView({behavior:'smooth'}) },[messages])

  const send = () => {
    if(!input.trim()||!stompRef.current?.connected) return
    stompRef.current.publish({destination:'/app/dm.send',
      body:JSON.stringify({type:'CHAT',roomId:room.id,content:input.trim()})})
    setInput('')
    textareaRef.current?.focus()
  }

  const other = room.otherMember||room.members?.find(m=>m.id!==myId)
  const roomName = isGroup?(room.name||'그룹'):(other?.nickname||'?')

  return (
    <div style={{display:'flex',flexDirection:'column',height:'100%'}}>
      <PanelHeader onBack={onBack} title={roomName}
        actionLabel={isGroup?`${room.members?.length||0}명`:undefined} actionColor={TEXT3} />
      <div style={{flex:1,overflowY:'auto',padding:'10px 10px',display:'flex',flexDirection:'column',gap:2,background:'rgba(0,0,0,.15)'}}>
        {messages.map((msg,i)=>{
          const ts = msg.createdAt||msg.timestamp
          const prev = messages[i-1]
          const prevTs = prev&&(prev.createdAt||prev.timestamp)
          const showDate = !prev||!isSameDay(ts,prevTs)
          const isMine = String(msg.senderId)===String(myId)
          if(msg.type==='JOIN'||msg.type==='LEAVE') return (
            <div key={i} style={{textAlign:'center',fontSize:10,color:TEXT3,padding:'4px 0'}}>{msg.content}</div>
          )
          return (
            <div key={i}>
              {showDate&&<div style={{textAlign:'center',fontSize:10,color:TEXT3,margin:'10px 0 6px'}}>{dayjs(ts).format('YYYY년 M월 D일')}</div>}
              {isGroup&&!isMine&&(i===0||messages[i-1]?.senderId!==msg.senderId)&&msg.senderNickname&&(
                <div style={{fontSize:10,color:TEXT3,marginBottom:2,marginTop:6,paddingLeft:2}}>{msg.senderNickname}</div>
              )}
              <div style={{display:'flex',justifyContent:isMine?'flex-end':'flex-start',alignItems:'flex-end',gap:4}}>
                {!isMine&&<span style={{fontSize:9,color:TEXT3,marginBottom:1}}>{dayjs(ts).format('HH:mm')}</span>}
                <div style={{maxWidth:'72%',padding:'7px 10px',wordBreak:'break-word',fontSize:12.5,lineHeight:1.5,
                  borderRadius:isMine?'12px 2px 12px 12px':'2px 12px 12px 12px',
                  background:isMine?'#FEE500':'rgba(255,255,255,.12)',color:isMine?'#1a1a1a':TEXT1}}>
                  {msg.content}
                </div>
                {isMine&&<span style={{fontSize:9,color:TEXT3,marginBottom:1}}>{dayjs(ts).format('HH:mm')}</span>}
              </div>
            </div>
          )
        })}
        <div ref={bottomRef}/>
      </div>
      <div style={{padding:'8px 10px',borderTop:`1px solid ${BORDER}`,background:'rgba(0,0,0,.2)',flexShrink:0,display:'flex',gap:6,alignItems:'flex-end'}}>
        <textarea ref={textareaRef} value={input} onChange={e=>setInput(e.target.value)}
          onKeyDown={e=>{ if(e.key==='Enter'&&!e.shiftKey){e.preventDefault();send()} }}
          placeholder="메시지 입력" rows={1}
          style={{flex:1,resize:'none',background:'rgba(255,255,255,.1)',border:`1px solid ${BORDER}`,borderRadius:8,
            padding:'7px 10px',color:TEXT1,fontSize:12.5,outline:'none',lineHeight:1.4,maxHeight:80,overflowY:'auto'}}/>
        <button onClick={send} style={{width:32,height:32,borderRadius:'50%',border:'none',cursor:'pointer',flexShrink:0,
          background:input.trim()?'#FEE500':'rgba(255,255,255,.12)',color:input.trim()?'#1a1a1a':TEXT3,
          display:'flex',alignItems:'center',justifyContent:'center',fontSize:14,transition:'background .15s'}}>
          ▶
        </button>
      </div>
    </div>
  )
}

// ── 1:1 대화 시작 화면 ────────────────────────────────────────────────
function StartDm({ onBack, onDone }) {
  const [sel, setSel] = useState([])
  const [busy, setBusy] = useState(false)
  const handleDone = async () => {
    if(!sel.length||busy) return
    setBusy(true)
    try { onDone((await api.post(`/dm/users/${sel[0].id}`)).data.data) }
    catch { setBusy(false) }
  }
  return (
    <div style={{display:'flex',flexDirection:'column',height:'100%'}}>
      <PanelHeader onBack={onBack} title="대화 상대 선택"
        actionLabel={busy?'...':(sel.length?'확인':'확인')} actionColor={sel.length?'#3B82F6':TEXT3}
        actionDisabled={!sel.length||busy} onAction={handleDone} />
      <MemberList selected={sel} singleSelect onToggle={m=>setSel(p=>p[0]?.id===m.id?[]:[m])} />
    </div>
  )
}

// ── 그룹 대화 Step1 ────────────────────────────────────────────────────
function StartGroup1({ onBack, onNext }) {
  const [sel, setSel] = useState([])
  const toggle = m => setSel(p=>p.find(x=>x.id===m.id)?p.filter(x=>x.id!==m.id):[...p,m])
  return (
    <div style={{display:'flex',flexDirection:'column',height:'100%'}}>
      <PanelHeader onBack={onBack} title="대화상대 초대"
        actionLabel={sel.length>0?`확인 (${sel.length})`:'확인'} actionColor={sel.length?'#3B82F6':TEXT3}
        actionDisabled={!sel.length} onAction={()=>onNext(sel)} />
      {sel.length>0&&(
        <div style={{display:'flex',gap:8,padding:'8px 12px',overflowX:'auto',borderBottom:`1px solid ${BORDER}`,background:'rgba(0,0,0,.1)',flexShrink:0}}>
          {sel.map(m=>(
            <div key={m.id} style={{display:'flex',flexDirection:'column',alignItems:'center',gap:2,flexShrink:0}}>
              <div style={{position:'relative'}}>
                <Av name={m.nickname} size={36}/>
                <button onClick={()=>toggle(m)} style={{position:'absolute',top:-4,right:-4,width:16,height:16,
                  borderRadius:'50%',background:'#555',color:'#fff',border:'none',cursor:'pointer',fontSize:9,
                  display:'flex',alignItems:'center',justifyContent:'center'}}>✕</button>
              </div>
              <span style={{fontSize:9,color:TEXT3,maxWidth:44,textAlign:'center',overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{m.nickname}</span>
            </div>
          ))}
        </div>
      )}
      <MemberList selected={sel} onToggle={toggle}/>
    </div>
  )
}

// ── 그룹 대화 Step2 ────────────────────────────────────────────────────
function StartGroup2({ selected, onBack, onDone }) {
  const defaultName = selected.map(m=>m.nickname).join(', ')
  const [name, setName] = useState(defaultName)
  const [busy, setBusy] = useState(false)
  const maxLen = 50
  const handleCreate = async () => {
    if(busy) return; setBusy(true)
    try { onDone((await api.post('/dm/group',{name:name.trim()||defaultName,memberIds:selected.map(m=>m.id)})).data.data) }
    catch { setBusy(false) }
  }
  return (
    <div style={{display:'flex',flexDirection:'column',height:'100%'}}>
      <PanelHeader onBack={onBack} title="그룹채팅방 정보" actionLabel={busy?'...':"확인"} onAction={handleCreate} actionDisabled={busy}/>
      {/* 미리보기 아바타 */}
      <div style={{display:'flex',justifyContent:'center',padding:'36px 0 24px',gap:-8,position:'relative'}}>
        <div style={{position:'relative',width:80,height:64}}>
          {selected.slice(0,2).map((m,i)=>(
            <div key={m.id} style={{position:'absolute',top:i===0?0:'auto',bottom:i===1?0:'auto',
              left:i===0?0:'auto',right:i===1?0:'auto'}}>
              <Av name={m.nickname} size={50} style={{boxShadow:`0 0 0 2px ${PANEL_BG}`}}/>
            </div>
          ))}
        </div>
      </div>
      {/* 이름 입력 */}
      <div style={{padding:'0 20px'}}>
        <div style={{position:'relative',borderBottom:'2px solid #3B82F6',paddingBottom:2}}>
          <input value={name} onChange={e=>setName(e.target.value.slice(0,maxLen))} autoFocus
            style={{width:'100%',border:'none',outline:'none',padding:'8px 46px 8px 2px',fontSize:14,
              color:TEXT1,background:'transparent'}}/>
          <span style={{position:'absolute',right:2,bottom:10,fontSize:11,color:TEXT3}}>{name.length}/{maxLen}</span>
        </div>
        <p style={{fontSize:11,color:TEXT3,marginTop:16,lineHeight:1.6}}>그룹채팅방 이름은 모든 참여자에게 동일하게 보입니다.</p>
      </div>
    </div>
  )
}

// ── 메인 ChatPanel ─────────────────────────────────────────────────────
export default function ChatPanel({ open, onClose }) {
  const { member, token } = useAuthStore()
  const [tab, setTab] = useState('dm')
  const [rooms, setRooms] = useState([])
  const [activeRoom, setActiveRoom] = useState(null)
  const [search, setSearch] = useState('')
  const [lastRead, setLastRead] = useState(getLastRead())
  // 새 대화 시작 흐름: null | 'pick' | 'dm' | 'group1' | 'group2'
  const [startMode, setStartMode] = useState(null)
  const [group1Sel, setGroup1Sel] = useState([])
  const stompRef = useRef(null)
  const subsRef = useRef([])

  const loadRooms = useCallback(async () => {
    try { setRooms((await api.get('/dm/rooms')).data.data||[]) } catch {}
  }, [])
  useEffect(() => { if(open&&member) loadRooms() }, [open,member])

  // 실시간 방 목록 갱신
  const roomIds = rooms.map(r=>r.id).join(',')
  useEffect(()=>{
    if(!rooms.length||!token) return
    const client = new Client({
      webSocketFactory:()=>new SockJS('/ws'),
      connectHeaders:{Authorization:`Bearer ${token}`},
      onConnect:()=>{
        subsRef.current.forEach(s=>{try{s.unsubscribe()}catch{}})
        subsRef.current = rooms.map(room=>
          client.subscribe(`/topic/dm/${room.id}`,msg=>{
            const dto=JSON.parse(msg.body)
            if(dto.type!=='CHAT') return
            setRooms(prev=>{
              const upd=prev.map(r=>r.id===room.id?{...r,lastMessageContent:dto.content,lastMessageAt:dto.timestamp||new Date().toISOString()}:r)
              const hit=upd.find(r=>r.id===room.id)
              return hit?[hit,...upd.filter(r=>r.id!==room.id)]:upd
            })
            setLastRead(getLastRead())
          })
        )
      },
    })
    client.activate()
    stompRef.current=client
    return ()=>{client.deactivate();subsRef.current=[]}
  // eslint-disable-next-line react-hooks/exhaustive-deps
  },[roomIds,token])

  const markRead = id => setLastRead(prev=>({...prev,[String(id)]:Date.now()}))

  const handleNewRoomDone = (room) => {
    setStartMode(null); setGroup1Sel([])
    setRooms(prev=> prev.find(r=>r.id===room.id)?prev:[room,...prev] )
    // 새로 만든 방도 전체 목록 새로고침
    loadRooms()
    setActiveRoom(room)
  }

  const totalUnread = rooms.filter(r=>{
    if(!r.lastMessageAt) return false
    const lr=lastRead[String(r.id)]
    return !lr||new Date(r.lastMessageAt).getTime()>lr
  }).length

  const filtered = rooms.filter(r=>{
    if(!search.trim()) return true
    const n = r.type==='GROUP'?(r.name||''):(r.otherMember?.nickname||'')
    return n.toLowerCase().includes(search.toLowerCase())
  })

  const PANEL_W = 340

  // 현재 패널 내 표시할 내용
  const renderContent = () => {
    // 인라인 채팅
    if(activeRoom) return (
      <InlineRoom room={activeRoom} myId={member?.id} token={token}
        onBack={()=>setActiveRoom(null)} onMarkRead={markRead}/>
    )
    // 새 대화 시작 흐름
    if(startMode==='pick') return (
      <div style={{display:'flex',flexDirection:'column',height:'100%'}}>
        <PanelHeader onBack={()=>setStartMode(null)} title="새 대화 시작"/>
        <div style={{padding:20,display:'flex',flexDirection:'column',gap:10}}>
          {[['dm','💬','1:1 대화','일대일로 메시지를 보냅니다'],
            ['group1','👥','그룹 대화','여러 명과 함께 채팅합니다']].map(([mode,icon,label,desc])=>(
            <div key={mode} onClick={()=>setStartMode(mode)}
              style={{display:'flex',alignItems:'center',gap:14,padding:'14px 16px',borderRadius:12,
                cursor:'pointer',background:'rgba(255,255,255,.07)',border:`1px solid ${BORDER}`}}
              onMouseEnter={e=>e.currentTarget.style.background='rgba(255,255,255,.12)'}
              onMouseLeave={e=>e.currentTarget.style.background='rgba(255,255,255,.07)'}>
              <span style={{fontSize:26}}>{icon}</span>
              <div>
                <div style={{fontWeight:600,fontSize:13,color:TEXT1}}>{label}</div>
                <div style={{fontSize:11,color:TEXT3,marginTop:2}}>{desc}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    )
    if(startMode==='dm') return <StartDm onBack={()=>setStartMode('pick')} onDone={handleNewRoomDone}/>
    if(startMode==='group1') return <StartGroup1 onBack={()=>setStartMode('pick')} onNext={sel=>{ setGroup1Sel(sel); setStartMode('group2') }}/>
    if(startMode==='group2') return <StartGroup2 selected={group1Sel} onBack={()=>setStartMode('group1')} onDone={handleNewRoomDone}/>

    // 방 목록 (기본)
    return (
      <>
        <div style={{padding:'8px 12px',borderBottom:`1px solid ${BORDER}`,flexShrink:0}}>
          <div style={{display:'flex',alignItems:'center',gap:6,background:'rgba(255,255,255,.08)',borderRadius:8,padding:'6px 10px'}}>
            <span style={{color:TEXT3,fontSize:13}}>⌕</span>
            <input value={search} onChange={e=>setSearch(e.target.value)} placeholder="채팅방 검색"
              style={{flex:1,background:'none',border:'none',outline:'none',color:TEXT1,fontSize:12.5}}/>
          </div>
        </div>
        <div style={{display:'flex',borderBottom:`1px solid ${BORDER}`,flexShrink:0}}>
          {[['dm','다이렉트'],['mention','멘션']].map(([t,label])=>(
            <button key={t} onClick={()=>{setTab(t);setSearch('')}} style={{
              flex:1,padding:'9px 0',background:'none',border:'none',
              borderBottom:`2px solid ${tab===t?'#3B82F6':'transparent'}`,
              color:tab===t?TEXT1:TEXT3,fontWeight:tab===t?700:400,
              cursor:'pointer',fontSize:12,transition:'all .15s'}}>
              {label}
              {t==='dm'&&totalUnread>0&&<span style={{marginLeft:4,background:'#3B82F6',color:'#fff',borderRadius:10,padding:'0 5px',fontSize:10,fontWeight:700}}>{totalUnread}</span>}
            </button>
          ))}
        </div>
        <div style={{flex:1,overflowY:'auto'}}>
          {tab==='mention'
            ? <div style={{textAlign:'center',padding:'40px 20px',color:TEXT3,fontSize:12}}>멘션 기능 준비 중</div>
            : filtered.length===0
              ? <div style={{textAlign:'center',padding:'40px 20px',color:TEXT3,fontSize:12}}>
                  {search?'검색 결과 없음':'대화 내역이 없습니다'}
                </div>
              : filtered.map(r=>(
                <RoomItem key={r.id} room={r} myId={member?.id} lastRead={lastRead}
                  onClick={()=>setActiveRoom(r)}/>
              ))
          }
        </div>
        {tab==='dm'&&(
          <div style={{padding:'10px 12px',borderTop:`1px solid ${BORDER}`,flexShrink:0}}>
            <button onClick={()=>setStartMode('pick')}
              style={{width:'100%',display:'flex',alignItems:'center',justifyContent:'center',gap:6,
                padding:'8px',borderRadius:8,background:'rgba(59,130,246,.18)',color:'#93c5fd',
                border:'none',cursor:'pointer',fontSize:12,fontWeight:600,transition:'background .15s'}}
              onMouseEnter={e=>e.currentTarget.style.background='rgba(59,130,246,.28)'}
              onMouseLeave={e=>e.currentTarget.style.background='rgba(59,130,246,.18)'}>
              + 새 대화 시작
            </button>
          </div>
        )}
      </>
    )
  }

  return (
    <>
      {open&&<div onClick={onClose} style={{position:'fixed',inset:0,zIndex:1199}}/>}
      <div style={{
        position:'fixed',top:0,right:0,bottom:0,zIndex:1200,
        width:PANEL_W,background:PANEL_BG,
        boxShadow:open?'-4px 0 32px rgba(0,0,0,.5)':'none',
        transform:open?'translateX(0)':'translateX(100%)',
        transition:'transform .25s cubic-bezier(.4,0,.2,1)',
        display:'flex',flexDirection:'column',
        color:TEXT1,
      }}>
        {/* 패널 헤더 */}
        <div style={{display:'flex',alignItems:'center',padding:'0 14px',height:52,
          borderBottom:`1px solid ${BORDER}`,flexShrink:0,gap:8}}>
          <button onClick={onClose} style={{background:'none',border:'none',color:TEXT2,cursor:'pointer',fontSize:16,padding:'4px',lineHeight:1}}>✕</button>
          <span style={{fontWeight:700,fontSize:14,color:TEXT1,flex:1}}>팀 채팅</span>
        </div>
        {/* 내용 */}
        <div style={{flex:1,display:'flex',flexDirection:'column',overflow:'hidden'}}>
          {renderContent()}
        </div>
      </div>
    </>
  )
}
