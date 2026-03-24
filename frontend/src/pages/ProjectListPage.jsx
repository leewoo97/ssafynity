import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function ProjectListPage() {
  const [projects, setProjects] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)

  const fetchProjects = (p = 0) => {
    setLoading(true)
    api.get('/projects', { params: { page: p, size: 10, keyword: search } })
      .then(r => {
        setProjects(r.data.data.content)
        setTotalPages(r.data.data.totalPages)
        setPage(p)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchProjects(0) }, [search])

  const handleSearch = e => {
    e.preventDefault()
    setSearch(keyword)
  }

  return (
    <div>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 }}>
        <h2>프로젝트</h2>
        <Link to="/projects/new" className="btn btn-primary">프로젝트 등록</Link>
      </div>
      <form onSubmit={handleSearch} style={{ display:'flex', gap:8, marginBottom:16 }}>
        <input className="form-control" placeholder="프로젝트명 / 기술 검색" value={keyword}
          onChange={e => setKeyword(e.target.value)} style={{ maxWidth:320 }} />
        <button type="submit" className="btn btn-secondary">검색</button>
      </form>

      {loading ? <div>로딩 중...</div> : (
        <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill,minmax(280px,1fr))', gap:16 }}>
          {projects.map(p => (
            <Link key={p.id} to={`/projects/${p.id}`} style={{ textDecoration:'none', color:'inherit' }}>
              <div className="card" style={{ padding:20, height:'100%' }}>
                <h3 style={{ margin:'0 0 8px', fontSize:17 }}>{p.title}</h3>
                <p style={{ fontSize:13, color:'var(--color-text-muted)', marginBottom:12 }}>{p.description?.slice(0,80)}...</p>
                <div style={{ display:'flex', flexWrap:'wrap', gap:4, marginBottom:8 }}>
                  {p.techStack?.split(',').map(t => (
                    <span key={t} style={{ fontSize:11, padding:'2px 6px', background:'#e9ecef', borderRadius:3 }}>{t.trim()}</span>
                  ))}
                </div>
                <div style={{ fontSize:12, color:'var(--color-text-muted)', display:'flex', gap:12 }}>
                  <span>❤️ {p.likeCount}</span>
                  {p.githubUrl && <span>GitHub</span>}
                </div>
              </div>
            </Link>
          ))}
          {projects.length === 0 && <p style={{ gridColumn:'1/-1', textAlign:'center', color:'var(--color-text-muted)' }}>프로젝트가 없습니다.</p>}
        </div>
      )}

      {totalPages > 1 && (
        <div className="pagination" style={{ marginTop:24 }}>
          {Array.from({ length: totalPages }, (_, i) => (
            <button key={i} onClick={() => fetchProjects(i)} className={`page-btn${page===i?' active':''}`}>{i+1}</button>
          ))}
        </div>
      )}
    </div>
  )
}
