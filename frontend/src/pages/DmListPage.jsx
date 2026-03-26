import { useState, useEffect, useRef, useCallback } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

// ── localStorage 마지막 읽음 시각 ────────────────────────────────────
function getLastRead() {
  try { return JSON.parse(localStorage.getItem('dm_lastRead') || '{}') } catch { return {} }
}

// ── 아바타 색상 ───────────────────────────────────────────────────────
const AV_COLORS = ['#3B82F6','#10B981','#F59E0B','#EF4444','#8B5CF6','#EC4899','#14B8A6','#F97316']
function avColor(name) { let n=0; for(const c of (name||'')) n+=c.charCodeAt(0); return AV_COLORS[n%8] }

function Av({ name, size=40, style:s={} }) {
  return (
    <div style={{
      width:size, height:size, borderRadius:Math.round(size*.28),
      background:avColor(name), color:'#fff', flexShrink:0,
      display:'flex', alignItems:'center', justifyContent:'center',
      fontWeight:700, fontSize:Math.round(size*.42), ...s,
    }}>
      {name?.charAt(0)?.toUpperCase()}
    </div>
  )
}

function OverlapAvatars({ members }) {
  const show = members.slice(0,2)
  if(!show.length) return null
  if(show.length===1) return <Av name={show[0].nickname} size={84} />
  return (
    <div style={{position:'relative', width:100, height:80}}>
      <Av name={show[0].nickname} size={64} style={{position:'absolute',top:0,left:0,boxShadow:'0 0 0 2.5px #fff'}} />
      <Av name={show[1].nickname} size={64} style={{position:'absolute',bottom:0,right:0,boxShadow:'0 0 0 2.5px #fff'}} />
    </div>
  )
}

// ── 공통 멤버 목록 (검색 + 전체 기본 표시) ───────────────────────────
function MemberList({ selected, onToggle, singleSelect=false }) {
  const [query, setQuery] = useState('')
  const [all, setAll] = useState([])
  const [allLoaded, setAllLoaded] = useState(false)

  useEffect(() => {
    api.get('/members/search').then(r => {
      setAll(r.data.data || [])
      setAllLoaded(true)
    }).catch(() => setAllLoaded(true))
  }, [])

  const filtered = query.trim()
    ? all.filter(m => m.nickname.toLowerCase().includes(query.toLowerCase()))
    : all

  return (
    <>
      <div style={{padding:'10px 14px', background:'#f4f5f7'}}>
        <input className="form-input" placeholder="이름 검색" value={query}
          onChange={e => setQuery(e.target.value)} style={{background:'#fff'}} />
      </div>
      <div style={{flex:1, overflowY:'auto'}}>
        {!allLoaded && <div style={{textAlign:'center',padding:32,color:'var(--t4)',fontSize:14}}>불러오는 중...</div>}
        {allLoaded && filtered.length===0 && (
          <div style={{textAlign:'center',padding:'52px 20px',color:'var(--t4)',fontSize:14}}>
            {query.trim() ? '검색 결과가 없습니다' : '멤버가 없습니다'}
          </div>
        )}
        {filtered.map(m => {
          const isSel = !!selected.find(x => x.id===m.id)
          return (
            <div key={m.id} onClick={() => onToggle(m)}
              style={{display:'flex',alignItems:'center',gap:14,padding:'11px 20px',borderBottom:'1px solid #f0f1f3',cursor:'pointer',background:isSel?'#f0f6ff':'#fff'}}>
              <Av name={m.nickname} size={48} />
              <div style={{flex:1}}>
                <div style={{fontWeight:600,fontSize:15}}>{m.nickname}</div>
                {m.campus && <div style={{fontSize:12,color:'var(--t4)'}}>{m.campus} {m.cohort}기</div>}
              </div>
              {singleSelect
                ? isSel && <span style={{color:'var(--blue)',fontSize:18,fontWeight:700}}>✓</span>
                : (
                  <div style={{width:26,height:26,borderRadius:'50%',
                    border:`2px solid ${isSel?'var(--blue)':'#ccc'}`,
                    background:isSel?'var(--blue)':'#fff',
                    display:'flex',alignItems:'center',justifyContent:'center',
                    flexShrink:0,transition:'all .15s'}}>
                    {isSel && <span style={{color:'#fff',fontSize:14,fontWeight:700,lineHeight:1}}>✓</span>}
                  </div>
                )
              }
            </div>
          )
        })}
      </div>
    </>
  )
}

// ── 1:1 대화 선택 화면 ────────────────────────────────────────────────
function DmStep({ onBack, onDone }) {
  const [selected, setSelected] = useState([])
  const [busy, setBusy] = useState(false)

  const handleDone = async () => {
    if(busy || !selected.length) return
    setBusy(true)
    try { onDone((await api.post(`/dm/users/${selected[0].id}`)).data.data.id) }
    catch { setBusy(false) }
  }

  return (
    <div style={{position:'fixed',inset:0,background:'#fff',zIndex:1000,display:'flex',flexDirection:'column'}}>
      <div style={{display:'flex',alignItems:'center',justifyContent:'space-between',padding:'14px 20px',borderBottom:'1px solid var(--b1)'}}>
        <button onClick={onBack} style={{background:'none',border:'none',fontSize:24,cursor:'pointer',color:'var(--t2)',lineHeight:1}}>←</button>
        <span style={{fontWeight:700,fontSize:17}}>대화 상대 선택</span>
        <button onClick={handleDone} disabled={!selected.length||busy}
          style={{background:'none',border:'none',cursor:selected.length&&!busy?'pointer':'default',fontWeight:700,fontSize:15,
          color:selected.length&&!busy?'var(--blue)':'var(--t4)'}}>
          확인
        </button>
      </div>
      <MemberList selected={selected} singleSelect
        onToggle={m => setSelected(prev => prev[0]?.id===m.id ? [] : [m])} />
    </div>
  )
}

// ── 그룹 Step 1 ───────────────────────────────────────────────────────
function GroupStep1({ onBack, onNext }) {
  const [selected, setSelected] = useState([])
  const toggle = m => setSelected(prev =>
    prev.find(x=>x.id===m.id) ? prev.filter(x=>x.id!==m.id) : [...prev,m]
  )

  return (
    <div style={{position:'fixed',inset:0,background:'#fff',zIndex:1000,display:'flex',flexDirection:'column'}}>
      <div style={{display:'flex',alignItems:'center',justifyContent:'space-between',padding:'14px 20px',borderBottom:'1px solid var(--b1)'}}>
        <button onClick={onBack} style={{background:'none',border:'none',fontSize:24,cursor:'pointer',color:'var(--t2)',lineHeight:1}}>←</button>
        <span style={{fontWeight:700,fontSize:17}}>대화상대 초대</span>
        <button onClick={() => selected.length>=1 && onNext(selected)}
          style={{background:'none',border:'none',cursor:selected.length>=1?'pointer':'default',fontWeight:700,fontSize:15,
          color:selected.length>=1?'var(--blue)':'var(--t4)'}}>
          확인{selected.length>0?` (${selected.length})`:''}
        </button>
      </div>
      {selected.length>0 && (
        <div style={{display:'flex',gap:10,padding:'10px 16px',overflowX:'auto',background:'#fafafa',borderBottom:'1px solid var(--b1)'}}>
          {selected.map(m => (
            <div key={m.id} style={{display:'flex',flexDirection:'column',alignItems:'center',gap:4,flexShrink:0}}>
              <div style={{position:'relative'}}>
                <Av name={m.nickname} size={46} />
                <button onClick={()=>toggle(m)} style={{position:'absolute',top:-4,right:-4,width:18,height:18,borderRadius:'50%',background:'#444',color:'#fff',border:'none',cursor:'pointer',fontSize:10,display:'flex',alignItems:'center',justifyContent:'center'}}>✕</button>
              </div>
              <span style={{fontSize:10,color:'var(--t3)',maxWidth:52,textAlign:'center',overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{m.nickname}</span>
            </div>
          ))}
        </div>
      )}
      <MemberList selected={selected} onToggle={toggle} />
    </div>
  )
}

// ── 그룹 Step 2 ───────────────────────────────────────────────────────
function GroupStep2({ selected, onBack, onDone }) {
  const defaultName = selected.map(m=>m.nickname).join(', ')
  const [groupName, setGroupName] = useState(defaultName)
  const [busy, setBusy] = useState(false)
  const maxLen = 50

  const handleCreate = async () => {
    if(busy) return; setBusy(true)
    try { onDone((await api.post('/dm/group',{name:groupName.trim()||defaultName,memberIds:selected.map(m=>m.id)})).data.data.id) }
    catch { setBusy(false) }
  }

  return (
    <div style={{position:'fixed',inset:0,background:'#fff',zIndex:1001,display:'flex',flexDirection:'column'}}>
      <div style={{display:'flex',alignItems:'center',justifyContent:'space-between',padding:'14px 20px',borderBottom:'1px solid var(--b1)'}}>
        <button onClick={onBack} style={{background:'none',border:'none',fontSize:24,cursor:'pointer',color:'var(--t2)',lineHeight:1}}>←</button>
        <span style={{fontWeight:700,fontSize:17}}>그룹채팅방 정보 설정</span>
        <button onClick={handleCreate} disabled={busy}
          style={{background:'none',border:'none',cursor:busy?'default':'pointer',fontWeight:700,fontSize:15,color:'var(--blue)'}}>확인</button>
      </div>
      <div style={{display:'flex',justifyContent:'center',padding:'48px 20px 28px'}}>
        <OverlapAvatars members={selected} />
      </div>
      <div style={{padding:'0 28px'}}>
        <div style={{position:'relative',borderBottom:'2px solid var(--blue)',paddingBottom:2}}>
          <input value={groupName} onChange={e=>setGroupName(e.target.value.slice(0,maxLen))} autoFocus
            style={{width:'100%',border:'none',outline:'none',padding:'8px 48px 8px 2px',fontSize:16,color:'var(--t1)',background:'transparent'}} />
          <span style={{position:'absolute',right:2,bottom:10,fontSize:12,color:'var(--t4)'}}>{groupName.length}/{maxLen}</span>
        </div>
        <p style={{fontSize:13,color:'var(--t4)',marginTop:24,lineHeight:1.7}}>채팅시작 전, 내가 설정한 그룹채팅방의 이름은 다른 모든 대화상대에게도 동일하게 보입니다.</p>
      </div>
    </div>
  )
}

// ── FAB 메뉴 아이템 ───────────────────────────────────────────────────
function FabOption({ label, icon, onClick }) {
  return (
    <div onClick={onClick}
      style={{display:'flex',alignItems:'center',gap:10,cursor:'pointer',
        background:'#fff',borderRadius:28,boxShadow:'0 2px 16px rgba(0,0,0,.14)',
        padding:'9px 18px 9px 12px',userSelect:'none'}}>
      <span style={{fontSize:18}}>{icon}</span>
      <span style={{fontWeight:600,fontSize:13,color:'var(--t1)',whiteSpace:'nowrap'}}>{label}</span>
    </div>
  )
}

// ── 메인 페이지 ───────────────────────────────────────────────────────
export default function DmListPage() {
  const { member, token } = useAuthStore()
  const navigate = useNavigate()
  const [rooms, setRooms] = useState([])
  const [loading, setLoading] = useState(true)
  const [mode, setMode] = useState(null)       // null | 'dm' | 'group-step1' | 'group-step2'
  const [groupSelected, setGroupSelected] = useState([])
  const [fabOpen, setFabOpen] = useState(false)
  const [lastRead, setLastReadState] = useState(getLastRead())
  const stompRef = useRef(null)
  const subsRef = useRef([])

  // 방 목록 로드
  const loadRooms = useCallback(async () => {
    try { setRooms((await api.get('/dm/rooms')).data.data || []) }
    finally { setLoading(false) }
  }, [])
  useEffect(() => { loadRooms() }, [loadRooms])

  // WebSocket: 방 목록의 모든 방 구독 → 실시간 목록 갱신
  const roomIds = rooms.map(r => r.id).join(',')
  useEffect(() => {
    if(!rooms.length || !token) return
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => {
        subsRef.current.forEach(s => { try { s.unsubscribe() } catch {} })
        subsRef.current = rooms.map(room =>
          client.subscribe(`/topic/dm/${room.id}`, msg => {
            const dto = JSON.parse(msg.body)
            if(dto.type !== 'CHAT') return
            setRooms(prev => {
              const next = prev.map(r => r.id===room.id
                ? { ...r, lastMessageContent: dto.content, lastMessageAt: dto.sentAt || new Date().toISOString() }
                : r
              )
              const hit = next.find(r => r.id===room.id)
              return hit ? [hit, ...next.filter(r => r.id!==room.id)] : next
            })
            setLastReadState(getLastRead()) // badge 재계산
          })
        )
      },
    })
    client.activate()
    stompRef.current = client
    return () => { client.deactivate(); subsRef.current = [] }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roomIds, token])

  const handleDone = (roomId) => { setMode(null); setFabOpen(false); navigate(`/dm/${roomId}`) }

  const hasUnread = (room) => {
    if(!room.lastMessageAt) return false
    const lr = lastRead[String(room.id)]
    return !lr || new Date(room.lastMessageAt).getTime() > lr
  }

  const dmRooms = rooms.filter(r => r.type==='DM')
  const groupRooms = rooms.filter(r => r.type==='GROUP')

  function RoomRow({ room }) {
    const isGroup = room.type==='GROUP'
    const other = room.otherMember || room.members?.find(m => m.id!==member?.id)
    const displayName = isGroup
      ? (room.name || room.members?.map(m=>m.nickname).join(', ') || '그룹')
      : (other?.nickname || '알 수 없음')
    const memberCount = room.members?.length || 0
    const unread = hasUnread(room)

    return (
      <Link to={`/dm/${room.id}`} className="room-item" style={{textDecoration:'none'}}>
        <div style={{position:'relative',flexShrink:0}}>
          {isGroup
            ? <div style={{width:48,height:48,borderRadius:14,background:'#f3e8ff',display:'flex',alignItems:'center',justifyContent:'center',fontSize:22}}>👥</div>
            : <Av name={other?.nickname} size={48} style={{borderRadius:14}} />
          }
          {unread && (
            <span style={{position:'absolute',top:-3,right:-3,width:11,height:11,borderRadius:'50%',background:'var(--blue)',border:'2px solid #fff'}} />
          )}
        </div>
        <div className="room-info">
          <div style={{display:'flex',alignItems:'center',gap:5}}>
            <span className="room-name" style={{fontWeight:unread?700:600}}>{displayName}</span>
            {isGroup && memberCount>0 && <span style={{fontSize:11,color:'var(--t4)'}}>{memberCount}</span>}
          </div>
          <div style={{fontSize:'.78rem',color:unread?'var(--t2)':'var(--t4)',fontWeight:unread?500:400,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>
            {room.lastMessageContent || '메시지 없음'}
          </div>
        </div>
        <div style={{display:'flex',flexDirection:'column',alignItems:'flex-end',gap:4,flexShrink:0,marginLeft:'auto'}}>
          {room.lastMessageAt && (
            <span style={{fontSize:'.72rem',color:'var(--t4)'}}>{dayjs(room.lastMessageAt).format('MM.DD')}</span>
          )}
          {unread && (
            <span style={{background:'var(--blue)',color:'#fff',borderRadius:10,padding:'1px 7px',fontSize:11,fontWeight:700,minWidth:18,textAlign:'center'}}>N</span>
          )}
        </div>
      </Link>
    )
  }

  return (
    <>
      {/* 전체화면 오버레이 */}
      {mode==='dm' && <DmStep onBack={()=>setMode(null)} onDone={handleDone} />}
      {mode==='group-step1' && (
        <GroupStep1 onBack={()=>setMode(null)} onNext={sel=>{setGroupSelected(sel);setMode('group-step2')}} />
      )}
      {mode==='group-step2' && (
        <GroupStep2 selected={groupSelected} onBack={()=>setMode('group-step1')} onDone={handleDone} />
      )}

      {/* FAB 배경 클릭으로 닫기 */}
      {fabOpen && <div style={{position:'fixed',inset:0,zIndex:899}} onClick={()=>setFabOpen(false)} />}

      <div className="section-sm"><div className="container">
      <div style={{maxWidth:640,margin:'0 auto'}}>
        <h2 style={{margin:'0 0 20px',fontSize:20,fontWeight:700}}>메시지</h2>

        {loading ? (
          <div className="loading">불러오는 중...</div>
        ) : rooms.length===0 ? (
          <div className="empty">
            <div className="empty-icon">💬</div>
            <div className="empty-title">대화 내역이 없습니다</div>
            <p style={{color:'var(--t4)',fontSize:14,marginTop:8}}>우측 하단 + 버튼으로 대화를 시작하세요</p>
          </div>
        ) : (
          <div className="card" style={{padding:0}}>
            {dmRooms.length>0 && (
              <>
                <div style={{padding:'10px 16px 4px',fontSize:11,fontWeight:600,color:'var(--t4)',letterSpacing:'.05em'}}>1:1 대화</div>
                <div className="room-list">{dmRooms.map(r=><RoomRow key={r.id} room={r}/>)}</div>
              </>
            )}
            {groupRooms.length>0 && (
              <>
                <div style={{padding:'10px 16px 4px',fontSize:11,fontWeight:600,color:'var(--t4)',letterSpacing:'.05em',borderTop:dmRooms.length>0?'1px solid var(--b1)':'none'}}>그룹 대화</div>
                <div className="room-list">{groupRooms.map(r=><RoomRow key={r.id} room={r}/>)}</div>
              </>
            )}
          </div>
        )}
      </div>
      </div></div>

      {/* 플로팅 액션 버튼 */}
      <div style={{position:'fixed',bottom:28,right:28,zIndex:900,display:'flex',flexDirection:'column',alignItems:'flex-end',gap:10}}>
        {fabOpen && (
          <>
            <FabOption label="그룹 대화" icon="👥" onClick={()=>{setFabOpen(false);setMode('group-step1')}} />
            <FabOption label="1:1 대화" icon="💬" onClick={()=>{setFabOpen(false);setMode('dm')}} />
          </>
        )}
        <button onClick={()=>setFabOpen(p=>!p)}
          style={{width:56,height:56,borderRadius:'50%',background:'var(--blue)',color:'#fff',border:'none',
            cursor:'pointer',fontSize:28,boxShadow:'0 4px 20px rgba(0,113,227,.45)',
            display:'flex',alignItems:'center',justifyContent:'center',
            transform:fabOpen?'rotate(45deg)':'rotate(0deg)',transition:'transform .22s ease'}}>
          +
        </button>
      </div>
    </>
  )
}
