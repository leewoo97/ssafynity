import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'

export default function DocFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [form, setForm] = useState({ title: '', content: '', tags: '' })
  const [error, setError] = useState('')

  useEffect(() => {
    if (isEdit) api.get(`/docs/${id}`).then(r => setForm(r.data.data))
  }, [id])

  const set = f => e => setForm({ ...form, [f]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (isEdit) { await api.put(`/docs/${id}`, form); navigate(`/docs/${id}`) }
      else { const r = await api.post('/docs', form); navigate(`/docs/${r.data.data.id}`) }
    } catch (err) { setError(err.response?.data?.error?.message || '저장 실패') }
  }

  return (
    <div className="section-sm"><div className="container">
    <div style={{ maxWidth: 720, margin: '0 auto' }}>
      <div className="section-head"><h2>{isEdit ? '문서 수정' : '문서 작성'}</h2></div>
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">제목</label>
            <input className="form-input" value={form.title} onChange={set('title')} required />
          </div>
          <div className="form-group">
            <label className="form-label">태그 (쉼표 구분)</label>
            <input className="form-input" value={form.tags} onChange={set('tags')} placeholder="Java, Spring, 알고리즘..." />
          </div>
          <div className="form-group">
            <label className="form-label">내용</label>
            <textarea className="form-textarea" rows={18} value={form.content} onChange={set('content')} required placeholder="마크다운 형식으로 작성하세요..." />
          </div>
          {error && <p className="alert alert-error">{error}</p>}
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <button type="button" className="btn btn-ghost btn-md" onClick={() => navigate(-1)}>취소</button>
            <button type="submit" className="btn btn-blue btn-md">{isEdit ? '수정' : '저장'}</button>
          </div>
        </form>
      </div>
    </div>
    </div></div>
  )
}
