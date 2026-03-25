import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function VideoDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [video, setVideo] = useState(null)
  const [related, setRelated] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get(`/videos/${id}`)
      .then(r => {
        setVideo(r.data.data)
        // 같은 카테고리 영상 가져오기
        const cat = r.data.data?.category
        if (cat) {
          api.get('/videos', { params: { category: cat, size: 6 } })
            .then(res => {
              const list = res.data.data?.content || []
              setRelated(list.filter(v => String(v.id) !== String(id)).slice(0, 5))
            }).catch(() => {})
        }
      })
      .finally(() => setLoading(false))
  }, [id])

  const handleDelete = async () => {
    if (!window.confirm('삭제하시겠습니까?')) return
    await api.delete(`/videos/${id}`)
    navigate('/videos')
  }

  if (loading) return <div className="empty"><div className="empty-icon">⏳</div></div>
  if (!video) return <div className="empty"><div className="empty-title">영상을 찾을 수 없습니다</div></div>

  const isAdmin = member?.role === 'ADMIN'
  const isAuthor = member?.id === video.authorId

  return (
    <div className="section-sm">
      <div className="container">
        <div className="two-col">
          <div>
            <div className="card">
              {/* YouTube 임베드 */}
              {video.youtubeId && (
                <div className="yt-wrap">
                  <iframe
                    src={`https://www.youtube.com/embed/${video.youtubeId}`}
                    frameBorder="0"
                    allowFullScreen
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    title={video.title}
                  />
                </div>
              )}

              <div className="post-header" style={{ marginTop: 20 }}>
                <div style={{ display: 'flex', gap: 6, alignItems: 'center', marginBottom: 10, flexWrap: 'wrap' }}>
                  <span className="pill pill-gray">{video.category}</span>
                  {video.duration && <span className="pill pill-gray">⏱ {video.duration}</span>}
                  {video.pinned && <span className="pill pill-orange">📌 추천</span>}
                </div>
                <h1 className="post-title">{video.title}</h1>
                <div className="post-attrs">
                  <span className="post-attr">
                    <Link to={`/profile/${video.authorId}`} style={{ fontWeight: 600, color: 'var(--blue)' }}>
                      {video.authorNickname}
                    </Link>
                  </span>
                  <span className="post-attr">🕐 {dayjs(video.createdAt).format('YYYY.MM.DD HH:mm')}</span>
                  <span className="post-attr">👁 {video.viewCount}</span>
                </div>
              </div>

              {video.description && (
                <div className="post-body">{video.description}</div>
              )}

              <div className="post-actions">
                {(isAuthor || isAdmin) && (
                  <>
                    <Link to={`/videos/${id}/edit`} className="btn btn-ghost btn-sm">수정</Link>
                    <button className="btn btn-danger btn-sm" onClick={handleDelete}>삭제</button>
                  </>
                )}
                <Link to="/videos" className="btn btn-ghost btn-sm" style={{ marginLeft: 'auto' }}>목록</Link>
              </div>
            </div>
          </div>

          <aside>
            <div className="sidebar-block">
              <div className="sidebar-block-head">🎬 등록자</div>
              <div className="sidebar-block-body">
                <div style={{ padding: '16px 18px', display: 'flex', alignItems: 'center', gap: 12 }}>
                  <div className="av av-md">{video.authorNickname?.charAt(0)?.toUpperCase()}</div>
                  <div>
                    <div style={{ fontSize: '.9rem', fontWeight: 600, color: 'var(--t1)' }}>
                      {video.authorNickname}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {related.length > 0 && (
              <div className="sidebar-block">
                <div className="sidebar-block-head">관련 영상</div>
                <div className="sidebar-block-body">
                  {related.map(v => (
                    <div key={v.id} className="sidebar-row">
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <Link to={`/videos/${v.id}`} className="sidebar-row-title">{v.title}</Link>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </aside>
        </div>
      </div>
    </div>
  )
}
