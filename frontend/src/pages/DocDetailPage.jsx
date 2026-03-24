import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import ReactMarkdown from 'react-markdown'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function DocDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [doc, setDoc] = useState(null)

  useEffect(() => { api.get(`/docs/${id}`).then(r => setDoc(r.data.data)) }, [id])

  const handlePin = async () => {
    await api.patch(`/docs/${id}/pin`)
    api.get(`/docs/${id}`).then(r => setDoc(r.data.data))
  }

  const handleDelete = async () => {
    if (!confirm('삭제하시겠습니까?')) return
    await api.delete(`/docs/${id}`)
    navigate('/docs')
  }

  if (!doc) return <div className="loading">로딩 중...</div>

  const isAdmin = member?.role === 'ADMIN'
  const isAuthor = member?.id === doc.authorId

  return (
    <div style={{ maxWidth:800, margin:'0 auto' }}>
      <div className="card" style={{ padding:32 }}>
        <div style={{ marginBottom:4 }}>
          <span style={{ fontSize:12, background:'#e9ecef', padding:'2px 8px', borderRadius:4 }}>{doc.category}</span>
          {doc.pinned && <span style={{ marginLeft:8, fontSize:12, background:'#fff3cd', padding:'2px 8px', borderRadius:4 }}>📌 고정</span>}
        </div>
        <h2 style={{ margin:'10px 0 4px' }}>{doc.title}</h2>
        <div style={{ fontSize:13, color:'var(--color-text-muted)', marginBottom:20 }}>작성자: {doc.authorNickname}</div>

        <div style={{ borderTop:'1px solid var(--color-border)', paddingTop:20, lineHeight:1.85 }}>
          <ReactMarkdown>{doc.content}</ReactMarkdown>
        </div>

        {doc.referenceUrl && (
          <div style={{ marginTop:20, fontSize:13 }}>
            🔗 <a href={doc.referenceUrl} target="_blank" rel="noopener noreferrer">{doc.referenceUrl}</a>
          </div>
        )}

        <div style={{ display:'flex', gap:8, marginTop:24 }}>
          {isAdmin && (
            <button onClick={handlePin} className="btn btn-secondary">
              {doc.pinned ? '고정 해제' : '고정'}
            </button>
          )}
          {isAuthor && (
            <>
              <Link to={`/docs/${id}/edit`} className="btn btn-secondary">수정</Link>
              <button onClick={handleDelete} className="btn" style={{ background:'#e74c3c', color:'#fff' }}>삭제</button>
            </>
          )}
          <button onClick={() => navigate(-1)} className="btn btn-secondary">뒤로</button>
        </div>
      </div>
    </div>
  )
}
