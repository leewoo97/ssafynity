import { Outlet, Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { useEffect, useState } from 'react'
import api from '../api/axios'
import './Layout.css'

export default function Layout() {
  const { member, logout } = useAuthStore()
  const navigate = useNavigate()
  const [unread, setUnread] = useState(0)

  useEffect(() => {
    api.get('/notifications/unread-count')
      .then(r => setUnread(r.data.data?.count || 0))
      .catch(() => {})
  }, [])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="layout">
      <header className="header">
        <Link to="/" className="logo">SSAFY NITY</Link>
        <nav className="header-nav">
          <Link to="/posts">게시판</Link>
          <Link to="/events">이벤트</Link>
          <Link to="/projects">프로젝트</Link>
          <Link to="/docs">기술문서</Link>
          <Link to="/videos">영상</Link>
          <Link to="/mentors">멘토링</Link>
          <Link to="/chat">오픈채팅</Link>
          <Link to="/dm">DM</Link>
        </nav>
        <div className="header-right">
          <Link to="/search" title="검색">🔍</Link>
          <Link to="/notifications" title="알림">
            🔔{unread > 0 && <span className="badge">{unread}</span>}
          </Link>
          <Link to="/mypage">{member?.nickname}</Link>
          {member?.role === 'ADMIN' && <Link to="/admin">관리자</Link>}
          <button className="btn btn-outline" onClick={handleLogout}>로그아웃</button>
        </div>
      </header>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}
