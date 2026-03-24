import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function DocDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [doc, setDoc] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get(`/docs/${id}`).then(r => setDoc(r.data.data)).finally(() => setLoading(false))
  }, [id])

  const handleDelete = async () => {
    if (!window.confirm('삭제하시겠습니까?')) return
    await api.delete(`/docs/${id}`)
    navigate('/docs')
  }

  if (loading) return <div className="empty"><div className="empty-icon">⏳</div></div>
  if (!doc) return <div className="empty"><div className="empty-title">문서를 찾을 수 없습니다</div></div>

  const isAuthor = member?.id === doc.authorId
  const isAdmin = member?.role === 'ADMIN'

  return (
    <div className="section-sm"><div className="container">
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <div className="card">
        <div className="post-header">
          <div>
            {doc.tags && <div style={{ display: 'flex', gap: 6, marginBottom: 10 }}>
              {doc.tags.split(',').map(t => <span key={t} className="pill pill-gray">{t.trim()}</span>)}
            </div>}
            <h2 className="post-title">{doc.title}</h2>
          </div>
          {(isAuthor || isAdmin) && (
            <div className="post-actions">
              <Link to={`/docs/${id}/edit`} className="btn btn-ghost btn-sm">수정</Link>
              <button className="btn btn-danger btn-sm" onClick={handleDelete}>삭제</button>
            </div>
          )}
        </div>
        <div className="post-row-meta" style={{ margin: '12px 0 20px' }}>
          <span className="av av-sm">{doc.authorNickname?.charAt(0)?.toUpperCase()}</span>
          <span>{doc.authorNickname}</span>
          <span>{dayjs(doc.createdAt).format('YYYY.MM.DD HH:mm')}</span>
        </div>
        <div className="post-body md-body" style={{ whiteSpace: 'pre-wrap' }}>{doc.content}</div>
      </div>
      <div style={{ marginTop: 16 }}>
        <Link to="/docs" className="btn btn-ghost btn-sm">← 목록으로</Link>
      </div>
    </div>
    </div></div>
  )
}
