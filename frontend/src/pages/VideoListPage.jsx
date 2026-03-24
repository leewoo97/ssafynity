import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'

const CATEGORIES = ['', 'FRONTEND', 'BACKEND', 'DEVOPS', 'AI', 'MOBILE', 'SECURITY', 'OTHER']

function getYoutubeThumbnail(url) {
  if (!url) return null
  const match = url.match(/(?:v=|youtu\.be\/)([A-Za-z0-9_-]{11})/)
  return match ? `https://img.youtube.com/vi/${match[1]}/mqdefault.jpg` : null
}

export default function VideoListPage() {
  const [videos, setVideos] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [category, setCategory] = useState('')
  const [loading, setLoading] = useState(true)

  const fetchVideos = (p = 0) => {
    setLoading(true)
    api.get('/videos', { params: { page: p, size: 12, category: category || undefined } })
      .then(r => {
        setVideos(r.data.data.content)
        setTotalPages(r.data.data.totalPages)
        setPage(p)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchVideos(0) }, [category])

  return (
    <div>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 }}>
        <h2>기술 영상</h2>
        <Link to="/videos/new" className="btn btn-primary">영상 등록</Link>
      </div>

      <div style={{ display:'flex', gap:8, marginBottom:16, flexWrap:'wrap' }}>
        {CATEGORIES.map(c => (
          <button key={c} onClick={() => setCategory(c)} className={`btn btn-secondary${category===c?' active':''}`} style={{ fontSize:12 }}>
            {c || '전체'}
          </button>
        ))}
      </div>

      {loading ? <div>로딩 중...</div> : (
        <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill,minmax(240px,1fr))', gap:16 }}>
          {videos.map(v => {
            const thumb = getYoutubeThumbnail(v.videoUrl)
            return (
              <Link key={v.id} to={`/videos/${v.id}`} style={{ textDecoration:'none', color:'inherit' }}>
                <div className="card" style={{ overflow:'hidden' }}>
                  {thumb && <img src={thumb} alt={v.title} style={{ width:'100%', aspectRatio:'16/9', objectFit:'cover' }} />}
                  <div style={{ padding:'12px 14px' }}>
                    <div style={{ fontSize:11, background:'#e9ecef', padding:'1px 6px', borderRadius:3, display:'inline-block', marginBottom:6 }}>{v.category}</div>
                    <div style={{ fontWeight:500, lineHeight:1.4 }}>{v.title}</div>
                    <div style={{ fontSize:12, color:'var(--color-text-muted)', marginTop:4 }}>{v.authorNickname}</div>
                  </div>
                </div>
              </Link>
            )
          })}
          {videos.length === 0 && <p style={{ gridColumn:'1/-1', textAlign:'center', color:'var(--color-text-muted)', padding:32 }}>영상이 없습니다.</p>}
        </div>
      )}

      {totalPages > 1 && (
        <div className="pagination" style={{ marginTop:24 }}>
          {Array.from({ length: totalPages }, (_, i) => (
            <button key={i} onClick={() => fetchVideos(i)} className={`page-btn${page===i?' active':''}`}>{i+1}</button>
          ))}
        </div>
      )}
    </div>
  )
}
