import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function ProjectListPage() {
  const { member } = useAuthStore()
  const [projects, setProjects] = useState([])
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [searchParams, setSearchParams] = useSearchParams()
  const [kw, setKw] = useState(searchParams.get('keyword') || '')

  const keyword = searchParams.get('keyword') || ''
  const sort = searchParams.get('sort') || 'latest'
  const page = parseInt(searchParams.get('page') || '0')

  useEffect(() => {
    setLoading(true)
    const params = { page, size: 12, sort }
    if (keyword) params.keyword = keyword
    api.get('/projects', { params })
      .then(r => {
        const data = r.data.data
        if (data && data.content) {
          setProjects(data.content)
          setTotalPages(data.totalPages || 1)
        } else {
          setProjects(Array.isArray(data) ? data : [])
          setTotalPages(1)
        }
      })
      .finally(() => setLoading(false))
  }, [keyword, sort, page])

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
              <div className="label" style={{ marginBottom: 6 }}>Showcase</div>
              <h1>프로젝트</h1>
              <p>SSAFY 팀 프로젝트를 공유하고 피드백을 받아보세요</p>
            </div>
            {member && <Link to="/projects/new" className="btn btn-blue btn-md">🚀 프로젝트 등록</Link>}
          </div>
        </div>
      </div>

      <div className="section-sm">
        <div className="container">
          <form className="search-row" style={{ marginBottom: 20 }} onSubmit={handleSearch}>
            <input type="text" className="search-input" placeholder="프로젝트 검색..."
              value={kw} onChange={e => setKw(e.target.value)} />
            <select className="search-select" value={sort} onChange={e => setParam('sort', e.target.value)}>
              <option value="latest">최신순</option>
              <option value="popular">인기순</option>
            </select>
            <button type="submit" className="btn btn-blue btn-md">검색</button>
          </form>

          {loading ? (
            <div className="empty"><div className="empty-icon">⏳</div></div>
          ) : projects.length === 0 ? (
            <div className="empty">
              <div className="empty-icon">🚀</div>
              <div className="empty-title">프로젝트가 없습니다.</div>
              {member && (
                <div className="empty-sub" style={{ marginTop: 16 }}>
                  <Link to="/projects/new" className="btn btn-blue btn-md">첫 프로젝트 등록하기</Link>
                </div>
              )}
            </div>
          ) : (
            <div className="project-grid">
              {projects.map(p => (
                <div key={p.id} className="project-card">
                  {p.thumbnailUrl && (
                    <div className="project-thumb">
                      <img src={p.thumbnailUrl} alt="thumbnail" />
                    </div>
                  )}
                  <div className="project-body">
                    <div style={{ display: 'flex', gap: 6, marginBottom: 4, flexWrap: 'wrap' }}>
                      <span className={`pill ${p.status === 'IN_PROGRESS' ? 'pill-blue' : 'pill-gray'}`}>
                        {p.status === 'IN_PROGRESS' ? '진행 중' : '완료'}
                      </span>
                    </div>
                    <div className="project-title">
                      <Link to={`/projects/${p.id}`}>{p.title}</Link>
                    </div>
                    <div className="project-desc">{p.description}</div>
                    {p.techStack && (
                      <div className="project-tags">
                        {p.techStack.split(',').slice(0, 4).map(t => (
                          <span key={t} className="tag">{t.trim()}</span>
                        ))}
                      </div>
                    )}
                    <div className="project-meta">
                      <span>{p.authorNickname}</span>
                      <div style={{ display: 'flex', gap: 8 }}>
                        <span>👥 {p.teamSize || 1}</span>
                        <span>❤ {p.likeCount || 0}</span>
                      </div>
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
