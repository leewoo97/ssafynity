import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api/axios'

export default function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [results, setResults] = useState(null)
  const [loading, setLoading] = useState(false)
  const [kw, setKw] = useState(searchParams.get('q') || '')

  const doSearch = async (q) => {
    if (!q.trim()) return
    setLoading(true)
    try {
      const r = await api.get('/search', { params: { q } })
      setResults(r.data.data)
    } finally { setLoading(false) }
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    setSearchParams({ q: kw })
    doSearch(kw)
  }

  return (
    <div className="section-sm"><div className="container">
    <div>
      <div className="section-head"><h2>통합 검색</h2></div>

      <form onSubmit={handleSubmit} className="search-row" style={{ marginBottom: 32 }}>
        <input className="search-input" value={kw} onChange={e => setKw(e.target.value)}
          placeholder="검색어를 입력하세요..." style={{ flex: 1 }} />
        <button type="submit" className="btn btn-blue btn-md">검색</button>
      </form>

      {loading && <div className="empty"><div className="empty-icon">⏳</div></div>}

      {results && (
        <div>
          {/* 게시글 */}
          {results.posts?.length > 0 && (
            <div style={{ marginBottom: 32 }}>
              <div className="section-head" style={{ marginBottom: 12 }}>
                <h3>게시글 ({results.posts.length})</h3>
              </div>
              <div className="card" style={{ padding: 0 }}>
                {results.posts.map(p => (
                  <div key={p.id} className="post-row" style={{ padding: '14px 20px' }}>
                    <div className="post-row-main">
                      <Link to={`/posts/${p.id}`} className="post-row-title">{p.title}</Link>
                      <div className="post-row-meta">
                        <span className="pill pill-gray">{p.category}</span>
                        <span>{p.authorNickname}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 문서 */}
          {results.docs?.length > 0 && (
            <div style={{ marginBottom: 32 }}>
              <div className="section-head" style={{ marginBottom: 12 }}>
                <h3>문서 ({results.docs.length})</h3>
              </div>
              <div className="card" style={{ padding: 0 }}>
                {results.docs.map(d => (
                  <div key={d.id} className="post-row" style={{ padding: '14px 20px' }}>
                    <div className="post-row-main">
                      <Link to={`/docs/${d.id}`} className="post-row-title">{d.title}</Link>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {results.posts?.length === 0 && results.docs?.length === 0 && (
            <div className="empty">
              <div className="empty-icon">🔍</div>
              <div className="empty-title">검색 결과가 없습니다</div>
            </div>
          )}
        </div>
      )}
    </div>
    </div></div>
  )
}
