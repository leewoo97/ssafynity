import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'

export default function EventFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [form, setForm] = useState({ title: '', description: '', organizer: '', type: '공모전', startDate: '', endDate: '', url: '' })
  const [error, setError] = useState('')

  useEffect(() => {
    if (isEdit) api.get(`/events/${id}`).then(r => setForm(r.data.data))
  }, [id])

  const set = f => e => setForm({ ...form, [f]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (isEdit) { await api.put(`/events/${id}`, form); navigate(`/events/${id}`) }
      else { const r = await api.post('/events', form); navigate(`/events/${r.data.data.id}`) }
    } catch (err) { setError(err.response?.data?.error?.message || '저장 실패') }
  }

  return (
    <div className="section-sm"><div className="container">
    <div style={{ maxWidth: 720, margin: '0 auto' }}>
      <div className="section-head"><h2>{isEdit ? '행사 수정' : '행사 등록'}</h2></div>
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">유형</label>
              <select className="form-select" value={form.type} onChange={set('type')}>
                {['공모전', '해커톤', '세미나', '특강', '기타'].map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">제목</label>
            <input className="form-input" value={form.title} onChange={set('title')} required />
          </div>
          <div className="form-group">
            <label className="form-label">주최기관</label>
            <input className="form-input" value={form.organizer} onChange={set('organizer')} />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">시작일</label>
              <input type="date" className="form-input" value={form.startDate} onChange={set('startDate')} required />
            </div>
            <div className="form-group">
              <label className="form-label">종료일</label>
              <input type="date" className="form-input" value={form.endDate} onChange={set('endDate')} required />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">링크 URL</label>
            <input className="form-input" type="url" value={form.url} onChange={set('url')} placeholder="https://" />
          </div>
          <div className="form-group">
            <label className="form-label">설명</label>
            <textarea className="form-textarea" rows={8} value={form.description} onChange={set('description')} />
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
