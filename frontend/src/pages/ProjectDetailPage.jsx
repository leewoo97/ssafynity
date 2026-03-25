import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function ProjectDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [project, setProject] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get(`/projects/${id}`).then(r => setProject(r.data.data)).finally(() => setLoading(false))
  }, [id])

  const handleDelete = async () => {
    if (!window.confirm('삭제하시겠습니까?')) return
    await api.delete(`/projects/${id}`)
    navigate('/projects')
  }

  if (loading) return <div className="empty"><div className="empty-icon">⏳</div></div>
  if (!project) return <div className="empty"><div className="empty-title">프로젝트를 찾을 수 없습니다</div></div>

  const isAuthor = member?.id === project.authorId
  const isAdmin = member?.role === 'ADMIN'

  return (
    <div className="section-sm"><div className="container">
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <div className="card">
        {project.thumbnailUrl && (
          <img src={project.thumbnailUrl} alt={project.title}
            style={{ width: '100%', height: 240, objectFit: 'cover', borderRadius: 'var(--r)', marginBottom: 24 }} />
        )}
        <div className="post-header">
          <h2 className="post-title">{project.title}</h2>
          {(isAuthor || isAdmin) && (
            <div className="post-actions">
              <Link to={`/projects/${id}/edit`} className="btn btn-ghost btn-sm">수정</Link>
              <button className="btn btn-danger btn-sm" onClick={handleDelete}>삭제</button>
            </div>
          )}
        </div>
        {project.techStack && (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, margin: '12px 0 20px' }}>
            {project.techStack.split(',').map(t => (
              <span key={t} className="pill pill-blue">{t.trim()}</span>
            ))}
          </div>
        )}
        <div className="post-row-meta" style={{ marginBottom: 20 }}>
          {project.authorNickname && <span>👤 {project.authorNickname}</span>}
          {project.githubUrl && <a href={project.githubUrl} target="_blank" rel="noreferrer" className="btn btn-dark btn-xs">GitHub →</a>}
          {project.demoUrl && <a href={project.demoUrl} target="_blank" rel="noreferrer" className="btn btn-tinted btn-xs">Demo →</a>}
        </div>
        <div className="post-body md-body" dangerouslySetInnerHTML={{ __html: project.description }} />
      </div>
      <div style={{ marginTop: 16 }}>
        <Link to="/projects" className="btn btn-ghost btn-sm">← 목록으로</Link>
      </div>
    </div>
    </div></div>
  )
}
