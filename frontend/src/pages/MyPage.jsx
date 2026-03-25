import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function MyPage() {
  const { member, logout } = useAuthStore()
  const navigate = useNavigate()
  const [myPosts, setMyPosts] = useState([])
  const [myComments, setMyComments] = useState([])
  const [friends, setFriends] = useState([])
  const [pendingRequests, setPendingRequests] = useState([])
  const [tab, setTab] = useState('posts')

  useEffect(() => {
    if (!member) { navigate('/login'); return }
    api.get('/members/me/posts').then(r => setMyPosts(r.data.data || [])).catch(() => {})
    api.get('/members/me/comments').then(r => setMyComments(r.data.data || [])).catch(() => {})
    api.get('/friends').then(r => setFriends(r.data.data || [])).catch(() => {})
    api.get('/friends/requests/received').then(r => setPendingRequests(r.data.data || [])).catch(() => {})
  }, [member])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  if (!member) return null

  return (
    <div className="section-sm">
      <div className="container">
        <div className="profile-hero">
          <div className="av av-2xl">{member.nickname?.charAt(0)?.toUpperCase()}</div>
          <div className="profile-hero-info">
            <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--t1)', letterSpacing: '-.025em' }}>
              {member.nickname}
            </div>
            <div style={{ fontSize: '.88rem', color: 'var(--t4)', marginTop: 4 }}>{member.username}</div>
            {member.email && <div style={{ fontSize: '.82rem', color: 'var(--t5)', marginTop: 2 }}>{member.email}</div>}
            {member.bio && (
              <div style={{ fontSize: '.88rem', color: 'var(--t3)', marginTop: 6, lineHeight: 1.55 }}>{member.bio}</div>
            )}
          </div>
          <div style={{ marginLeft: 'auto', flexShrink: 0 }}>
            <Link to="/profile/edit" className="btn btn-ghost btn-sm">✏️ 프로필 수정</Link>
          </div>
        </div>

        <div className="profile-stat-row">
          <div className="profile-stat">
            <span className="profile-stat-num">{myPosts.length}</span>
            <span className="profile-stat-lbl">게시글</span>
          </div>
          <div className="profile-stat">
            <span className="profile-stat-num">{myComments.length}</span>
            <span className="profile-stat-lbl">댓글</span>
          </div>
          <div className="profile-stat">
            <span className="profile-stat-num">{friends.length}</span>
            <span className="profile-stat-lbl">친구</span>
          </div>
        </div>

        <div className="tabs" style={{ marginTop: 24 }}>
          <button className={`tab${tab === 'posts' ? ' active' : ''}`} onClick={() => setTab('posts')}>내 게시글</button>
          <button className={`tab${tab === 'comments' ? ' active' : ''}`} onClick={() => setTab('comments')}>내 댓글</button>
          <button className={`tab${tab === 'friends' ? ' active' : ''}`} onClick={() => setTab('friends')}>
            친구
            {pendingRequests.length > 0 && (
              <span style={{ display: 'inline-block', background: 'var(--red)', color: '#fff', borderRadius: 10, padding: '0 6px', fontSize: '.75rem', fontWeight: 700, marginLeft: 4 }}>
                {pendingRequests.length}
              </span>
            )}
          </button>
          <button className={`tab${tab === 'settings' ? ' active' : ''}`} onClick={() => setTab('settings')}>설정</button>
        </div>

        {tab === 'posts' && (
          myPosts.length === 0 ? (
            <div className="empty" style={{ marginTop: 24 }}>
              <div className="empty-icon">📝</div>
              <div className="empty-title">작성한 게시글이 없습니다.</div>
            </div>
          ) : (
            <div className="card card-flush" style={{ marginTop: 16 }}>
              <table className="table">
                <thead><tr><th>제목</th><th>카테고리</th><th>작성일</th><th>조회</th></tr></thead>
                <tbody>
                  {myPosts.map(p => (
                    <tr key={p.id}>
                      <td><Link to={`/posts/${p.id}`} className="table-link">{p.title}</Link></td>
                      <td><span className="pill pill-gray">{p.category}</span></td>
                      <td>{dayjs(p.createdAt).format('YYYY.MM.DD')}</td>
                      <td>{p.viewCount || 0}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        )}

        {tab === 'comments' && (
          myComments.length === 0 ? (
            <div className="empty" style={{ marginTop: 24 }}>
              <div className="empty-icon">💬</div>
              <div className="empty-title">작성한 댓글이 없습니다.</div>
            </div>
          ) : (
            <div className="card card-flush" style={{ marginTop: 16 }}>
              <table className="table">
                <thead><tr><th>댓글 내용</th><th>게시글</th><th>작성일</th></tr></thead>
                <tbody>
                  {myComments.map(c => (
                    <tr key={c.id}>
                      <td>{c.content}</td>
                      <td><Link to={`/posts/${c.postId}`} className="table-link">{c.postTitle}</Link></td>
                      <td>{dayjs(c.createdAt).format('YYYY.MM.DD')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        )}

        {tab === 'friends' && (
          <div style={{ marginTop: 24 }}>
            {pendingRequests.length > 0 && (
              <div style={{ marginBottom: 24 }}>
                <div style={{ fontSize: '.9rem', fontWeight: 700, color: 'var(--t2)', marginBottom: 12 }}>📬 받은 친구 요청</div>
                <div className="card card-flush">
                  {pendingRequests.map(req => (
                    <div key={req.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '14px 18px', borderBottom: '1px solid var(--b1)' }}>
                      <div className="av">{req.requesterNickname?.charAt(0)?.toUpperCase()}</div>
                      <div style={{ flex: 1 }}>
                        <Link to={`/profile/${req.requesterId}`} style={{ fontWeight: 600, color: 'var(--t1)', textDecoration: 'none' }}>{req.requesterNickname}</Link>
                      </div>
                      <div style={{ display: 'flex', gap: 8 }}>
                        <button className="btn btn-blue btn-sm" onClick={() => api.post(`/friends/accept/${req.id}`).then(() => setPendingRequests(p => p.filter(x => x.id !== req.id)))}>수락</button>
                        <button className="btn btn-ghost btn-sm" onClick={() => api.post(`/friends/reject/${req.id}`).then(() => setPendingRequests(p => p.filter(x => x.id !== req.id)))}>거절</button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div>
              <div style={{ fontSize: '.9rem', fontWeight: 700, color: 'var(--t2)', marginBottom: 12 }}>
                👥 친구 목록 <span style={{ fontWeight: 400, color: 'var(--t5)' }}>({friends.length}명)</span>
              </div>
              {friends.length === 0 ? (
                <div className="empty">
                  <div className="empty-icon">👤</div>
                  <div className="empty-title">아직 친구가 없습니다.</div>
                </div>
              ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(180px,1fr))', gap: 12 }}>
                  {friends.map(f => (
                    <div key={f.id} className="card" style={{ textAlign: 'center', padding: '20px 12px' }}>
                      <Link to={`/profile/${f.id}`} style={{ textDecoration: 'none' }}>
                        <div className="av av-lg" style={{ margin: '0 auto 10px' }}>{f.nickname?.charAt(0)?.toUpperCase()}</div>
                        <div style={{ fontWeight: 600, fontSize: '.9rem', color: 'var(--t1)' }}>{f.nickname}</div>
                        {f.campus && <div style={{ fontSize: '.78rem', color: 'var(--t5)', marginTop: 2 }}>{f.campus} {f.cohort && `${f.cohort}기`}</div>}
                      </Link>
                      <button className="btn btn-ghost btn-sm" style={{ width: '100%', marginTop: 10 }}
                        onClick={() => api.post(`/dm/users/${f.id}`).then(r => navigate(`/dm/${r.data.data.id}`))}>
                        💬 DM
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {tab === 'settings' && (
          <div style={{ marginTop: 24 }}>
            <div className="card" style={{ padding: '24px 28px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                <Link to="/profile/edit" className="btn btn-ghost btn-sm" style={{ justifyContent: 'start' }}>✏️ 프로필 수정</Link>
                <button className="btn btn-danger btn-sm" onClick={handleLogout} style={{ justifyContent: 'start' }}>로그아웃</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
