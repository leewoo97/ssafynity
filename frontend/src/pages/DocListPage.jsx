import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

const CATEGORIES = ['', 'FRONTEND', 'BACKEND', 'DEVOPS', 'AI', 'MOBILE', 'SECURITY', 'OTHER']

export default function DocListPage() {
  const { member } = useAuthStore()
  const [docs, setDocs] = useState([])
  const [pinned, setPinned] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [category, setCategory] = useState('')
  const [loading, setLoading] = useState(true)

  const fetchDocs = (p = 0) => {
    setLoading(true)
    api.get('/docs', { params: { page: p, size: 10, category: category || undefined } })
      .then(r => {
        const data = r.data.data
        setDocs(data.content)
        setTotalPages(data.totalPages)
        setPage(p)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    api.get('/docs', { params: { page:0, size:5, pinned:true } }).then(r => setPinned(r.data.data.content || []))
  }, [])

  useEffect(() => { fetchDocs(0) }, [category])

  return (
    <div>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 }}>
        <h2>기술 문서</h2>
        <Link to="/docs/new" className="btn btn-primary">문서 등록</Link>
      </div>

      {pinned.length > 0 && (
        <div style={{ marginBottom:20 }}>
          <h4 style={{ marginBottom:8, color:'var(--color-primary)' }}>📌 고정 문서</h4>
          {pinned.map(d => (
            <div key={d.id} className="card" style={{ padding:'10px 16px', marginBottom:6 }}>
              <span style={{ fontSize:11, background:'#fff3cd', padding:'2px 6px', borderRadius:3, marginRight:8 }}>{d.category}</span>
              <Link to={`/docs/${d.id}`} style={{ fontWeight:500, color:'var(--color-text)' }}>{d.title}</Link>
              <span style={{ float:'right', fontSize:12, color:'var(--color-text-muted)' }}>{d.authorNickname}</span>
            </div>
          ))}
        </div>
      )}

      <div style={{ display:'flex', gap:8, marginBottom:16, flexWrap:'wrap' }}>
        {CATEGORIES.map(c => (
          <button key={c} onClick={() => setCategory(c)} className={`btn btn-secondary${category===c?' active':''}`} style={{ fontSize:12 }}>
            {c || '전체'}
          </button>
        ))}
      </div>

      {loading ? <div>로딩 중...</div> : (
        <>
          <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
            {docs.map(d => (
              <div key={d.id} className="card" style={{ padding:'12px 18px', display:'flex', justifyContent:'space-between' }}>
                <div>
                  <span style={{ fontSize:11, background:'#e9ecef', padding:'2px 6px', borderRadius:3, marginRight:8 }}>{d.category}</span>
                  <Link to={`/docs/${d.id}`} style={{ color:'var(--color-text)', fontWeight:500 }}>{d.title}</Link>
                </div>
                <span style={{ fontSize:12, color:'var(--color-text-muted)' }}>{d.authorNickname}</span>
              </div>
            ))}
            {docs.length === 0 && <p style={{ textAlign:'center', color:'var(--color-text-muted)', padding:32 }}>문서가 없습니다.</p>}
          </div>

          {totalPages > 1 && (
            <div className="pagination" style={{ marginTop:16 }}>
              {Array.from({ length: totalPages }, (_, i) => (
                <button key={i} onClick={() => fetchDocs(i)} className={`page-btn${page===i?' active':''}`}>{i+1}</button>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}
