import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

function getYoutubeEmbedUrl(url) {
  if (!url) return null
  const match = url.match(/(?:v=|youtu\.be\/)([A-Za-z0-9_-]{11})/)
  return match ? `https://www.youtube-nocookie.com/embed/${match[1]}` : null
}

export default function VideoDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [video, setVideo] = useState(null)

  useEffect(() => { api.get(`/videos/${id}`).then(r => setVideo(r.data.data)) }, [id])

  const handlePin = async () => {
    await api.patch(`/videos/${id}/pin`)
    api.get(`/videos/${id}`).then(r => setVideo(r.data.data))
  }

  const handleDelete = async () => {
    if (!confirm('삭제하시겠습니까?')) return
    await api.delete(`/videos/${id}`)
    navigate('/videos')
  }

  if (!video) return <div className="loading">로딩 중...</div>
  const embedUrl = getYoutubeEmbedUrl(video.videoUrl)
  const isAdmin = member?.role === 'ADMIN'
  const isAuthor = member?.id === video.authorId

  return (
    <div style={{ maxWidth:820, margin:'0 auto' }}>
      <div className="card" style={{ padding:0, overflow:'hidden' }}>
        {embedUrl && (
          <div style={{ position:'relative', paddingTop:'56.25%', background:'#000' }}>
            <iframe
              src={embedUrl}
              title={video.title}
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
              style={{ position:'absolute', top:0, left:0, width:'100%', height:'100%', border:'none' }}
            />
          </div>
        )}
        <div style={{ padding:24 }}>
          <div style={{ marginBottom:4 }}>
            <span style={{ fontSize:12, background:'#e9ecef', padding:'2px 8px', borderRadius:4 }}>{video.category}</span>
            {video.pinned && <span style={{ marginLeft:8, fontSize:12, background:'#fff3cd', padding:'2px 8px', borderRadius:4 }}>📌 고정</span>}
          </div>
          <h2 style={{ margin:'10px 0 4px' }}>{video.title}</h2>
          <div style={{ fontSize:13, color:'var(--color-text-muted)', marginBottom:16 }}>업로더: {video.authorNickname}</div>
          <div style={{ lineHeight:1.85, whiteSpace:'pre-wrap', borderTop:'1px solid var(--color-border)', paddingTop:16 }}>
            {video.description}
          </div>
          {!embedUrl && video.videoUrl && (
            <div style={{ marginTop:12 }}>
              <a href={video.videoUrl} target="_blank" rel="noopener noreferrer" className="btn btn-secondary">영상 링크</a>
            </div>
          )}
          <div style={{ display:'flex', gap:8, marginTop:20 }}>
            {isAdmin && <button onClick={handlePin} className="btn btn-secondary">{video.pinned ? '고정 해제' : '고정'}</button>}
            {isAuthor && (
              <>
                <Link to={`/videos/${id}/edit`} className="btn btn-secondary">수정</Link>
                <button onClick={handleDelete} className="btn" style={{ background:'#e74c3c', color:'#fff' }}>삭제</button>
              </>
            )}
            <button onClick={() => navigate(-1)} className="btn btn-secondary">뒤로</button>
          </div>
        </div>
      </div>
    </div>
  )
}
