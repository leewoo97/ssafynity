import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

const CATEGORIES = ['', 'Frontend', 'Backend', 'DevOps', 'Algorithm', 'Database', 'ETC']
const CAT_LABEL = { '': '전체' }

export default function DocListPage() {
  const { member } = useAuthStore()
  const [docs, setDocs] = useState([])
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
    api.get('/docs', { params })
      .then(r => {
        const data = r.data.data
        if (data && data.content) {
          setDocs(data.content)
          setTotalPages(data.totalPages || 1)
        } else {
          setDocs(Array.isArray(data) ? data : [])
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
              <div className="label" style={{ marginBottom: 6 }}>Knowledge Base</div>
              <h1>기술 문서</h1>
              <p>튜토리얼, 아키텍처, 기술 노트를 공유하세요</p>
            </div>
            {member && <Link to="/docs/new" className="btn btn-blue btn-md">📄 문서 작성</Link>}
          </div>
        </div>
      </div>

      <div className="section-sm">
        <div className="container">
          <div className="tabs">
            {CATEGORIES.map(c => (
              <button key={c} onClick={() => setParam('category', c)}
                className={`tab${category === c ? ' active' : ''}`}>
                {CAT_LABEL[c] || c || '전체'}
              </button>
            ))}
          </div>

          <form className="search-row" style={{ marginBottom: 24 }} onSubmit={handleSearch}>
            <input type="text" name="keyword" className="search-input"
              placeholder="문서 검색..." value={kw} onChange={e => setKw(e.target.value)} />
            <button type="submit" className="btn btn-blue btn-md">검색</button>
          </form>

          {loading ? (
            <div className="empty"><div className="empty-icon">⏳</div></div>
          ) : docs.length === 0 ? (
            <div className="empty">
              <div className="empty-icon">📄</div>
              <div className="empty-title">문서가 없습니다.</div>
              {member && (
                <div className="empty-sub" style={{ marginTop: 16 }}>
                  <Link to="/docs/new" className="btn btn-blue btn-md">첫 문서 작성하기</Link>
                </div>
              )}
            </div>
          ) : (
            <div className="doc-grid">
              {docs.map(doc => (
                <div key={doc.id} className="doc-card">
                  <div style={{ display: 'flex', gap: 6, alignItems: 'center', marginBottom: 8, flexWrap: 'wrap' }}>
                    <span className="pill pill-gray">{doc.category}</span>
                  </div>
                  <div className="doc-card-title">
                    <Link to={`/docs/${doc.id}`}>{doc.title}</Link>
                  </div>
                  {doc.tags && (
                    <div className="doc-card-tags">
                      {doc.tags.split(',').slice(0, 4).map(t => (
                        <span key={t} className="tag">{t.trim()}</span>
                      ))}
                    </div>
                  )}
                  <div className="doc-card-meta">
                    <span>{doc.authorNickname}</span>
                    <span>·</span>
                    <span>{dayjs(doc.createdAt).format('yyyy.MM.DD')}</span>
                    <span>·</span>
                    <span>👁 {doc.viewCount || 0}</span>
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
