import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { useEffect, useState } from 'react'
import api from '../api/axios'
import ChatPanel from './ChatPanel'

export default function Layout() {
  const { member, logout } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()
  const [unread, setUnread] = useState(0)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [chatOpen, setChatOpen] = useState(false)
  const [search, setSearch] = useState('')

  useEffect(() => {
    api.get('/notifications/unread-count')
      .then(r => setUnread(r.data.data?.count || 0))
      .catch(() => {})
  }, [location])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const handleSearch = e => {
    e.preventDefault()
    if (search.trim()) {
      navigate(`/search?q=${encodeURIComponent(search.trim())}`)
      setSearch('')
    }
  }

  const firstChar = member?.nickname?.charAt(0)?.toUpperCase() || 'U'

  return (
    <>
      <div className="reading-bar" />

      <nav className="nav">
        <div className="nav-inner">
          <Link to="/" className="nav-logo">SSAFY<span>nity</span></Link>

          <div className="nav-links">
            <Link to="/posts">커뮤니티</Link>
            <Link to="/projects">프로젝트</Link>
            <Link to="/docs">문서</Link>
            <Link to="/videos">영상</Link>
            <Link to="/events">이벤트</Link>
            <Link to="/chat">채팅</Link>
            <Link to="/mentors">멘토링</Link>
          </div>

          <div className="nav-actions">
            <form onSubmit={handleSearch} className="nav-search-form">
              <button type="submit" className="nav-search-btn">⌕</button>
              <input
                type="text"
                placeholder="검색"
                autoComplete="off"
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </form>

            <Link to="/notifications" className="nav-icon-btn" title="알림" style={{ position: 'relative' }}>
              🔔
              {unread > 0 && (
                <span style={{
                  position: 'absolute', top: 0, right: 0,
                  width: 16, height: 16, borderRadius: '50%',
                  background: 'var(--red)', color: '#fff',
                  fontSize: 10, fontWeight: 700,
                  display: 'flex', alignItems: 'center', justifyContent: 'center'
                }}>{unread > 9 ? '9+' : unread}</span>
              )}
            </Link>

            <Link to="/mypage" className="nav-user-chip">
              <span className="nav-user-dot">{firstChar}</span>
              <span>{member?.nickname}</span>
            </Link>

            {member?.role === 'ADMIN' && (
              <Link to="/admin" className="nav-btn nav-btn-ghost">관리자</Link>
            )}

            {member && (
              <button onClick={()=>setChatOpen(p=>!p)} className="nav-btn" style={{background:'rgba(255,255,255,.12)',border:'1px solid rgba(255,255,255,.15)',color:'#fff',display:'flex',alignItems:'center',gap:6,padding:'6px 14px',borderRadius:20,cursor:'pointer',fontWeight:600,fontSize:13}}>
                💬 팀 채팅
              </button>
            )}
            <button onClick={handleLogout} className="nav-btn nav-btn-ghost">로그아웃</button>
          </div>

          <button className="nav-hamburger" onClick={() => setDrawerOpen(true)} aria-label="메뉴">
            <span /><span /><span />
          </button>
        </div>
      </nav>

      {/* Mobile Drawer */}
      <div className={`nav-drawer${drawerOpen ? ' open' : ''}`}>
        <div className="nav-drawer-bg" onClick={() => setDrawerOpen(false)} />
        <div className="nav-drawer-panel">
          <button className="nav-drawer-close" onClick={() => setDrawerOpen(false)}>✕</button>
          {[
            ['/posts','💬 커뮤니티'], ['/projects','🚀 프로젝트'], ['/docs','📄 문서'],
            ['/videos','🎬 영상'], ['/events','📅 이벤트'], ['/chat','💬 채팅'],
            ['/dm','✉️ DM'], ['/mentors','🎓 멘토링'],
          ].map(([to, label]) => (
            <Link key={to} to={to} onClick={() => setDrawerOpen(false)}>{label}</Link>
          ))}
          <div className="nav-drawer-sep" />
          <Link to="/mypage" onClick={() => setDrawerOpen(false)}>👤 마이페이지</Link>
          <Link to="/notifications" onClick={() => setDrawerOpen(false)}>🔔 알림</Link>
          <div className="nav-drawer-sep" />
          <Link to="#" onClick={() => { setDrawerOpen(false); handleLogout() }}>로그아웃</Link>
        </div>
      </div>

      <ChatPanel open={chatOpen} onClose={()=>setChatOpen(false)} />

      <main className="page">
        <Outlet />
      </main>

      <footer>
        <div className="footer-inner">
          <div className="footer-bottom">
            <span className="footer-copy">© 2026 SSAFYnity. All rights reserved.</span>
            <span className="footer-tagline">삼성 청년 SW 아카데미 커뮤니티</span>
          </div>
        </div>
      </footer>
    </>
  )
}
