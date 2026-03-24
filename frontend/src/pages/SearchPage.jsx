import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api/axios'

const TABS = ['posts', 'projects', 'docs', 'members']
const TAB_LABELS = { posts:'게시글', projects:'프로젝트', docs:'문서', members:'멤버' }

export default function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [query, setQuery] = useState(searchParams.get('q') || '')
  const [results, setResults] = useState({})
  const [activeTab, setActiveTab] = useState('posts')
  const [loading, setLoading] = useState(false)

  const doSearch = async (q) => {
    if (!q.trim()) return
    setLoading(true)
    try {
      const r = await api.get('/search', { params: { q } })
      setResults(r.data.data || {})
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    const q = searchParams.get('q')
    if (q) { setQuery(q); doSearch(q) }
  }, [])

  const handleSubmit = e => {
    e.preventDefault()
    setSearchParams({ q: query })
    doSearch(query)
  }

  const list = results[activeTab] || []

  return (
    <div>
      <h2>검색</h2>
      <form onSubmit={handleSubmit} style={{ display:'flex', gap:8, marginBottom:24, maxWidth:480 }}>
        <input className="form-control" value={query} onChange={e => setQuery(e.target.value)} placeholder="검색어 입력..." />
        <button type="submit" className="btn btn-primary">검색</button>
      </form>

      {Object.keys(results).length > 0 && (
        <>
          <div style={{ display:'flex', gap:8, marginBottom:16, borderBottom:'1px solid var(--color-border)' }}>
            {TABS.map(t => (
              <button key={t} onClick={() => setActiveTab(t)}
                style={{ padding:'8px 16px', border:'none', background:'none', cursor:'pointer',
                  borderBottom: activeTab===t ? '2px solid var(--color-primary)' : '2px solid transparent',
                  color: activeTab===t ? 'var(--color-primary)' : 'inherit', fontWeight: activeTab===t ? 600 : 400 }}>
                {TAB_LABELS[t]} ({(results[t]||[]).length})
              </button>
            ))}
          </div>

          {loading ? <div>검색 중...</div> : (
            <div>
              {activeTab === 'posts' && list.map(p => (
                <div key={p.id} className="card" style={{ padding:'12px 16px', marginBottom:8 }}>
                  <Link to={`/posts/${p.id}`} style={{ fontWeight:500, color:'var(--color-text)' }}>{p.title}</Link>
                  <span style={{ float:'right', fontSize:12, color:'var(--color-text-muted)' }}>{p.authorNickname}</span>
                </div>
              ))}

              {activeTab === 'projects' && list.map(p => (
                <div key={p.id} className="card" style={{ padding:'12px 16px', marginBottom:8 }}>
                  <Link to={`/projects/${p.id}`} style={{ fontWeight:500, color:'var(--color-text)' }}>{p.title}</Link>
                  <span style={{ float:'right', fontSize:12, color:'var(--color-text-muted)' }}>❤️ {p.likeCount}</span>
                </div>
              ))}

              {activeTab === 'docs' && list.map(d => (
                <div key={d.id} className="card" style={{ padding:'12px 16px', marginBottom:8 }}>
                  <span style={{ fontSize:11, background:'#e9ecef', padding:'2px 6px', borderRadius:3, marginRight:8 }}>{d.category}</span>
                  <Link to={`/docs/${d.id}`} style={{ fontWeight:500, color:'var(--color-text)' }}>{d.title}</Link>
                </div>
              ))}

              {activeTab === 'members' && list.map(m => (
                <div key={m.id} className="card" style={{ padding:'12px 16px', marginBottom:8, display:'flex', justifyContent:'space-between' }}>
                  <Link to={`/members/${m.id}`} style={{ fontWeight:500, color:'var(--color-text)' }}>{m.nickname}</Link>
                  <span style={{ fontSize:12, color:'var(--color-text-muted)' }}>{m.campus} {m.cohort}기</span>
                </div>
              ))}

              {list.length === 0 && <p style={{ textAlign:'center', color:'var(--color-text-muted)', padding:32 }}>결과가 없습니다.</p>}
            </div>
          )}
        </>
      )}
    </div>
  )
}
