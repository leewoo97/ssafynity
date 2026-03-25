import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

const CATEGORIES = ['강의', '세미나', '코드리뷰', '프로젝트발표', '기타']

export default function VideoListPage() {
  const { member } = useAuthStore()
  const [videos, setVideos] = useState([])
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [searchParams, setSearchParams] = useSearchParams()
  const [kw, setKw] = useState(searchParams.get('keyword') || '')

  const category = searchParams.get('category') || ''
  const keyword = searchParams.get('keyword') || ''
  const page = parseInt(searchParams.get('page') || '0')

  useEffect(() => {
    setLoading(true)
    const params = { page, size: 12 }
    if (category) params.category = category
    if (keyword) params.keyword = keyword
    api.get('/videos', { params })
      .then(r => {
        const data = r.data.data
        if (data && data.content) {
          setVideos(data.content)
          setTotalPages(data.totalPages || 1)
        } else {
          setVideos(Array.isArray(data) ? data : [])
          setTotalPages(1)
        }
      })
      .finally(() => setLoading(false))
  }, [category, keyword, page])

  const setParam = (key, value) => {
    const p = new URLSearchParams(searchParams)
    if (value) p.set(key, value); else p.delete(key)
    p.delete('page')
    setSearchParams(p)
  }

  const handleSearch = (e) => {
    e.preventDefault()
    setParam('keyword', kw)
  }

  return (
    <>
      <div className="page-header">
        <div className="container">
          <div className="page-header-inner">
            <div>
              <div className="label" style={{ marginBottom: 6 }}>Learning</div>
              <h1>영상 자료</h1>
              <p>강의, 세미나, 프로젝트 영상을 공유하세요</p>
            </div>
            {member && <Link to="/videos/new" className="btn btn-blue btn-md">🎬 영상 등록</Link>}
          </div>
        </div>
      </div>

      <div className="section-sm">
        <div className="container">
          <div className="tabs">
            <button onClick={() => setParam('category', '')}
              className={`tab${category === '' ? ' active' : ''}`}>전체</button>
            {CATEGORIES.map(c => (
              <button key={c} onClick={() => setParam('category', c)}
                className={`tab${category === c ? ' active' : ''}`}>
                {c}
              </button>
            ))}
          </div>

          <form className="search-row" style={{ marginBottom: 24 }} onSubmit={handleSearch}>
            <input type="text" className="search-input" placeholder="영상 검색..."
              value={kw} onChange={e => setKw(e.target.value)} />
            <button type="submit" className="btn btn-blue btn-md">검색</button>
          </form>

          {loading ? (
            <div className="empty"><div className="empty-icon">⏳</div></div>
          ) : videos.length === 0 ? (
            <div className="empty">
              <div className="empty-icon">🎬</div>
              <div className="empty-title">등록된 영상이 없습니다.</div>
              {member && (
                <div className="empty-sub" style={{ marginTop: 16 }}>
                  <Link to="/videos/new" className="btn btn-blue btn-md">첫 영상 등록하기</Link>
                </div>
              )}
            </div>
          ) : (
            <div className="video-grid">
              {videos.map(v => (
                <div key={v.id} className="video-card">
                  <Link to={`/videos/${v.id}`}>
                    <div className="video-thumb">
                      <img
                        src={v.youtubeId
                          ? `https://img.youtube.com/vi/${v.youtubeId}/mqdefault.jpg`
                          : (v.thumbnailUrl || 'https://via.placeholder.com/320x180?text=Video')}
                        alt="thumbnail"
                      />
                      <div className="video-overlay">
                        <div className="play-btn">▶</div>
                      </div>
                      {v.duration && <div className="video-duration">{v.duration}</div>}
                    </div>
                  </Link>
                  <div className="video-body">
                    <div style={{ display: 'flex', gap: 6, marginBottom: 4, alignItems: 'center' }}>
                      <span className="pill pill-gray">{v.category}</span>
                    </div>
                    <div className="video-title">
                      <Link to={`/videos/${v.id}`}>{v.title}</Link>
                    </div>
                    <div className="video-meta">
                      <span>{v.authorNickname}</span>
                      <span>·</span>
                      <span>{dayjs(v.createdAt).format('YYYY.MM.DD')}</span>
                      <span>·</span>
                      <span>👁 {v.viewCount || 0}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {totalPages > 1 && (
            <div className="pagination">
              {page > 0 && <span onClick={() => setParam('page', String(page - 1))} style={{ cursor: 'pointer' }}>‹</span>}
              {Array.from({ length: totalPages }, (_, i) => (
                <span key={i} onClick={() => setParam('page', String(i))}
                  className={page === i ? 'active' : ''} style={{ cursor: 'pointer' }}>{i + 1}</span>
              ))}
              {page < totalPages - 1 && <span onClick={() => setParam('page', String(page + 1))} style={{ cursor: 'pointer' }}>›</span>}
            </div>
          )}
        </div>
      </div>
    </>
  )
}
