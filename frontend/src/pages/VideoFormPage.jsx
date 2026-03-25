import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'

const CATEGORIES = ['강의', '세미나', '코드리뷰', '프로젝트발표', '기타']

export default function VideoFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [form, setForm] = useState({ title: '', description: '', youtubeUrl: '', duration: '', category: '강의', tags: '' })
  const [error, setError] = useState('')

  useEffect(() => {
    if (isEdit) {
      api.get(`/videos/${id}`).then(r => {
        const v = r.data.data
        // youtubeId → youtubeUrl 로 복원 (편집 시)
        setForm({
          title: v.title || '',
          description: v.description || '',
          youtubeUrl: v.youtubeId ? `https://www.youtube.com/watch?v=${v.youtubeId}` : '',
          duration: v.duration || '',
          category: v.category || '강의',
          tags: v.tags || '',
        })
      })
    }
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
              <label className="form-label">카테고리</label>
              <select className="form-select" value={form.category} onChange={set('category')}>
                {CATEGORIES.map(c => <option key={c}>{c}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">재생 시간</label>
              <input className="form-input" value={form.duration} onChange={set('duration')} placeholder="예: 1:24:30" />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">YouTube URL</label>
            <input className="form-input" value={form.youtubeUrl} onChange={set('youtubeUrl')} required
              placeholder="https://www.youtube.com/watch?v=..." />
          </div>
          <div className="form-group">
            <label className="form-label">태그 (쉼표 구분)</label>
            <input className="form-input" value={form.tags} onChange={set('tags')} placeholder="Spring Boot, JPA, 백엔드" />
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
