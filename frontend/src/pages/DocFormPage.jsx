import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'

const CATEGORIES = ['FRONTEND', 'BACKEND', 'DEVOPS', 'AI', 'MOBILE', 'SECURITY', 'OTHER']

export default function DocFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [form, setForm] = useState({ title:'', content:'', category:'BACKEND', referenceUrl:'' })

  useEffect(() => {
    if (isEdit) {
      api.get(`/docs/${id}`).then(r => {
        const d = r.data.data
        setForm({ title:d.title, content:d.content, category:d.category, referenceUrl:d.referenceUrl||'' })
      })
    }
  }, [id])

  const handle = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async e => {
    e.preventDefault()
    try {
      if (isEdit) {
        await api.put(`/docs/${id}`, form)
        navigate(`/docs/${id}`)
      } else {
        const r = await api.post('/docs', form)
        navigate(`/docs/${r.data.data.id}`)
      }
    } catch (err) {
      alert(err.response?.data?.message || '저장 실패')
    }
  }

  return (
    <div style={{ maxWidth:720, margin:'0 auto' }}>
      <h2>{isEdit ? '문서 수정' : '문서 등록'}</h2>
      <form onSubmit={handleSubmit} className="card" style={{ padding:28, display:'flex', flexDirection:'column', gap:14 }}>
        <div>
          <label>제목</label>
          <input className="form-control" name="title" value={form.title} onChange={handle} required />
        </div>
        <div>
          <label>카테고리</label>
          <select className="form-control" name="category" value={form.category} onChange={handle}>
            {CATEGORIES.map(c => <option key={c}>{c}</option>)}
          </select>
        </div>
        <div>
          <label>내용 (Markdown 지원)</label>
          <textarea className="form-control" name="content" value={form.content} onChange={handle} rows={14}
            style={{ fontFamily:'monospace' }} required />
        </div>
        <div>
          <label>참조 URL</label>
          <input className="form-control" name="referenceUrl" value={form.referenceUrl} onChange={handle} />
        </div>
        <div style={{ display:'flex', gap:8 }}>
          <button type="submit" className="btn btn-primary">저장</button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>취소</button>
        </div>
      </form>
    </div>
  )
}
