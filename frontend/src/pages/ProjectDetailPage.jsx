import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function ProjectDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [project, setProject] = useState(null)

  useEffect(() => {
    api.get(`/projects/${id}`).then(r => setProject(r.data.data))
  }, [id])

  const handleLike = async () => {
    await api.post(`/projects/${id}/like`)
    api.get(`/projects/${id}`).then(r => setProject(r.data.data))
  }

  const handleDelete = async () => {
    if (!confirm('삭제하시겠습니까?')) return
    await api.delete(`/projects/${id}`)
    navigate('/projects')
  }

  if (!project) return <div className="loading">로딩 중...</div>
  const isAuthor = member?.id === project.authorId

  return (
    <div style={{ maxWidth:760, margin:'0 auto' }}>
      <div className="card" style={{ padding:32 }}>
        <h2 style={{ marginBottom:8 }}>{project.title}</h2>
        <div style={{ fontSize:13, color:'var(--color-text-muted)', marginBottom:16 }}>
          {project.authorNickname}
        </div>
        <div style={{ display:'flex', flexWrap:'wrap', gap:6, marginBottom:16 }}>
          {project.techStack?.split(',').map(t => (
            <span key={t} style={{ fontSize:12, padding:'3px 8px', background:'#e9ecef', borderRadius:4 }}>{t.trim()}</span>
          ))}
        </div>
        <div style={{ display:'flex', gap:16, marginBottom:20 }}>
          {project.githubUrl && <a href={project.githubUrl} target="_blank" rel="noopener noreferrer" className="btn btn-secondary" style={{ fontSize:13 }}>GitHub</a>}
          {project.demoUrl && <a href={project.demoUrl} target="_blank" rel="noopener noreferrer" className="btn btn-secondary" style={{ fontSize:13 }}>Demo</a>}
        </div>
        <div style={{ lineHeight:1.9, whiteSpace:'pre-wrap', borderTop:'1px solid var(--color-border)', paddingTop:16, marginBottom:20 }}>
          {project.description}
        </div>
        <div style={{ display:'flex', gap:8, alignItems:'center' }}>
          <button onClick={handleLike} className="btn btn-secondary">❤️ {project.likeCount}</button>
          {isAuthor && (
            <>
              <Link to={`/projects/${id}/edit`} className="btn btn-secondary">수정</Link>
              <button onClick={handleDelete} className="btn" style={{ background:'#e74c3c', color:'#fff' }}>삭제</button>
            </>
          )}
          <button onClick={() => navigate(-1)} className="btn btn-secondary">뒤로</button>
        </div>
      </div>
    </div>
  )
}
