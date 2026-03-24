import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'

const CATEGORIES = ['FRONTEND', 'BACKEND', 'DEVOPS', 'AI', 'MOBILE', 'SECURITY', 'OTHER']

export default function VideoFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [form, setForm] = useState({ title:'', description:'', videoUrl:'', category:'BACKEND' })

  useEffect(() => {
    if (isEdit) {
      api.get(`/videos/${id}`).then(r => {
        const d = r.data.data
        setForm({ title:d.title, description:d.description||'', videoUrl:d.videoUrl||'', category:d.category })
      })
    }
  }, [id])

  const handle = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async e => {
    e.preventDefault()
    try {
      if (isEdit) {
        await api.put(`/videos/${id}`, form)
        navigate(`/videos/${id}`)
      } else {
        const r = await api.post('/videos', form)
        navigate(`/videos/${r.data.data.id}`)
      }
    } catch (err) {
      alert(err.response?.data?.message || '저장 실패')
    }
  }

  return (
    <div style={{ maxWidth:640, margin:'0 auto' }}>
      <h2>{isEdit ? '영상 수정' : '영상 등록'}</h2>
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
          <label>영상 URL (YouTube 지원)</label>
          <input className="form-control" name="videoUrl" value={form.videoUrl} onChange={handle}
            placeholder="https://www.youtube.com/watch?v=..." required />
        </div>
        <div>
          <label>설명</label>
          <textarea className="form-control" name="description" value={form.description} onChange={handle} rows={5} />
        </div>
        <div style={{ display:'flex', gap:8 }}>
          <button type="submit" className="btn btn-primary">저장</button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>취소</button>
        </div>
      </form>
    </div>
  )
}
