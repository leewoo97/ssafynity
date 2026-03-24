import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'

export default function VideoFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [form, setForm] = useState({ title: '', description: '', videoUrl: '', thumbnailUrl: '', channel: '', duration: '' })
  const [error, setError] = useState('')

  useEffect(() => {
    if (isEdit) api.get(`/videos/${id}`).then(r => setForm(r.data.data))
  }, [id])

  const set = f => e => setForm({ ...form, [f]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (isEdit) { await api.put(`/videos/${id}`, form); navigate(`/videos/${id}`) }
      else { const r = await api.post('/videos', form); navigate(`/videos/${r.data.data.id}`) }
    } catch (err) { setError(err.response?.data?.error?.message || '저장 실패') }
  }

  return (
    <div className="section-sm"><div className="container">
    <div style={{ maxWidth: 720, margin: '0 auto' }}>
      <div className="section-head"><h2>{isEdit ? '영상 수정' : '영상 등록'}</h2></div>
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">제목</label>
            <input className="form-input" value={form.title} onChange={set('title')} required />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">채널명</label>
              <input className="form-input" value={form.channel} onChange={set('channel')} />
            </div>
            <div className="form-group">
              <label className="form-label">재생 시간</label>
              <input className="form-input" value={form.duration} onChange={set('duration')} placeholder="예: 1:24:30" />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">영상 URL</label>
            <input className="form-input" type="url" value={form.videoUrl} onChange={set('videoUrl')} required placeholder="https://" />
          </div>
          <div className="form-group">
            <label className="form-label">썸네일 URL</label>
            <input className="form-input" type="url" value={form.thumbnailUrl} onChange={set('thumbnailUrl')} placeholder="https://" />
          </div>
          <div className="form-group">
            <label className="form-label">설명</label>
            <textarea className="form-textarea" rows={6} value={form.description} onChange={set('description')} />
          </div>
          {error && <p className="alert alert-error">{error}</p>}
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <button type="button" className="btn btn-ghost btn-md" onClick={() => navigate(-1)}>취소</button>
            <button type="submit" className="btn btn-blue btn-md">{isEdit ? '수정' : '등록'}</button>
          </div>
        </form>
      </div>
    </div>
    </div></div>
  )
}
