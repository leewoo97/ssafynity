import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function ProfilePage() {
  const { id } = useParams()
  const { member } = useAuthStore()
  const [profile, setProfile] = useState(null)
  const [posts, setPosts] = useState([])
  const [friendStatus, setFriendStatus] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const uid = id || member?.id
    if (!uid) return
    Promise.all([
      api.get(`/members/${uid}`),
      api.get(`/posts?authorId=${uid}&size=10`),
    ]).then(([pr, postr]) => {
      setProfile(pr.data.data)
      setPosts(postr.data.data?.content || [])
    }).finally(() => setLoading(false))

    if (id && member && String(id) !== String(member.id)) {
      api.get(`/friends/status/${id}`).then(r => setFriendStatus(r.data.data)).catch(() => {})
    }
  }, [id, member])

  if (loading) return <div className="section-sm"><div className="container"><div className="empty"><div className="empty-icon">⏳</div></div></div></div>
  if (!profile) return <div className="section-sm"><div className="container"><div className="empty"><div className="empty-title">사용자를 찾을 수 없습니다</div></div></div></div>

  const isMe = member?.id === profile.id || (!id && member)

  return (
    <div className="section-sm">
      <div className="container">
        <div className="two-col">
          <div>
            <div className="card" style={{ textAlign: 'center', padding: '40px 28px' }}>
              <div className="av av-2xl" style={{ margin: '0 auto 16px' }}>
                {profile.nickname?.charAt(0)?.toUpperCase()}
              </div>

              <div style={{ fontSize: '1.4rem', fontWeight: 700, color: 'var(--t1)' }}>{profile.nickname}</div>
              <div style={{ fontSize: '.88rem', color: 'var(--t4)', marginTop: 4 }}>{profile.username}</div>

              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8, flexWrap: 'wrap', marginTop: 12 }}>
                {profile.campus && <span className="pill pill-blue">{profile.campus}</span>}
                {profile.cohort && <span className="pill pill-gray">{profile.cohort}기</span>}
              </div>

              {profile.bio && (
                <div style={{ fontSize: '.88rem', color: 'var(--t3)', marginTop: 12, lineHeight: 1.5 }}>{profile.bio}</div>
              )}

              <div style={{ marginTop: 20, display: 'flex', justifyContent: 'center', gap: 32 }}>
                <div>
                  <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--t1)' }}>{posts.length}</div>
                  <div style={{ fontSize: '.78rem', color: 'var(--t4)' }}>게시글</div>
                </div>
              </div>

              {member && !isMe && (
                <div style={{ marginTop: 20 }}>
                  {(!friendStatus || friendStatus === 'REJECTED') && (
                    <button className="btn btn-blue btn-md" style={{ width: '100%' }}
                      onClick={() => api.post(`/friends/request/${profile.id}`).then(() => setFriendStatus('PENDING'))}>
                      👤 친구 추가
                    </button>
                  )}
                  {friendStatus === 'PENDING' && (
                    <div style={{ padding: '10px 0', fontSize: '.88rem', color: 'var(--t4)' }}>⏳ 친구 요청 대기 중</div>
                  )}
                  {friendStatus === 'ACCEPTED' && (
                    <button className="btn btn-ghost btn-md" style={{ width: '100%' }}
                      onClick={() => api.delete(`/friends/${profile.id}`).then(() => setFriendStatus(null))}>
                      💔 친구 끊기
                    </button>
                  )}
                </div>
              )}

              {isMe && (
                <div style={{ marginTop: 20 }}>
                  <Link to="/profile/edit" className="btn btn-ghost btn-md" style={{ width: '100%' }}>✏️ 프로필 수정</Link>
                </div>
              )}
            </div>
          </div>

          <div>
            <div className="sidebar-block">
              <div className="sidebar-block-head">최근 게시글</div>
              <div className="sidebar-block-body">
                {posts.length === 0 ? (
                  <div style={{ padding: '16px 18px', color: 'var(--t4)', fontSize: '.85rem' }}>게시글이 없습니다.</div>
                ) : posts.map(post => (
                  <div key={post.id} className="sidebar-row">
                    <Link to={`/posts/${post.id}`} className="sidebar-row-title">{post.title}</Link>
                    <span className="sidebar-row-meta">{dayjs(post.createdAt).format('MM.DD')}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
