import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function VideoDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [video, setVideo] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get(`/videos/${id}`).then(r => setVideo(r.data.data)).finally(() => setLoading(false))
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
    <div className="section-sm"><div className="container">
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <div className="card">
        {video.videoUrl && (
          <div style={{ marginBottom: 20, borderRadius: 'var(--r)', overflow: 'hidden', background: '#000' }}>
            <video controls style={{ width: '100%', maxHeight: 400 }} src={video.videoUrl} />
          </div>
        )}
        <div className="post-header">
          <h2 className="post-title">{video.title}</h2>
          {(isAuthor || isAdmin) && (
            <div className="post-actions">
              <Link to={`/videos/${id}/edit`} className="btn btn-ghost btn-sm">수정</Link>
              <button className="btn btn-danger btn-sm" onClick={handleDelete}>삭제</button>
            </div>
          )}
        </div>
        <div className="post-row-meta" style={{ margin: '12px 0 20px' }}>
          {video.channel && <span>📺 {video.channel}</span>}
          {video.duration && <span>⏱ {video.duration}</span>}
        </div>
        <div className="post-body md-body">{video.description}</div>
      </div>
      <div style={{ marginTop: 16 }}>
        <Link to="/videos" className="btn btn-ghost btn-sm">← 목록으로</Link>
      </div>
    </div>
    </div></div>
  )
}
