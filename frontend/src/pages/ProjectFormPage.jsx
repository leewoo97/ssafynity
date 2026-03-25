import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'
import RichEditor from '../components/RichEditor'

export default function ProjectFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [form, setForm] = useState({ title: '', description: '', techStack: '', githubUrl: '', demoUrl: '', thumbnailUrl: '' })
  const [error, setError] = useState('')

  useEffect(() => {
    if (isEdit) api.get(`/projects/${id}`).then(r => setForm(r.data.data))
  }, [id])

  const set = f => e => setForm({ ...form, [f]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (isEdit) { await api.put(`/projects/${id}`, form); navigate(`/projects/${id}`) }
      else { const r = await api.post('/projects', form); navigate(`/projects/${r.data.data.id}`) }
    } catch (err) { setError(err.response?.data?.error?.message || '저장 실패') }
  }

  return (
    <div className="section-sm"><div className="container">
    <div style={{ maxWidth: 720, margin: '0 auto' }}>
      <div className="section-head"><h2>{isEdit ? '프로젝트 수정' : '프로젝트 등록'}</h2></div>
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">프로젝트명</label>
            <input className="form-input" value={form.title} onChange={set('title')} required />
          </div>
          <div className="form-group">
            <label className="form-label">기술 스택 (쉼표 구분)</label>
            <input className="form-input" value={form.techStack} onChange={set('techStack')} placeholder="React, Spring Boot, MySQL..." />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">GitHub URL</label>
              <input className="form-input" type="url" value={form.githubUrl} onChange={set('githubUrl')} placeholder="https://github.com/..." />
            </div>
            <div className="form-group">
              <label className="form-label">Demo URL</label>
              <input className="form-input" type="url" value={form.demoUrl} onChange={set('demoUrl')} placeholder="https://" />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">썸네일 이미지 URL</label>
            <input className="form-input" type="url" value={form.thumbnailUrl} onChange={set('thumbnailUrl')} placeholder="https://" />
          </div>
          <div className="form-group">
            <label className="form-label">프로젝트 설명</label>
            <RichEditor value={form.description} onChange={val => setForm({ ...form, description: val })} placeholder="프로젝트에 대해 소개해주세요..." />
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
