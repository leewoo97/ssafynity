import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'

export default function ProjectFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [form, setForm] = useState({ title:'', description:'', techStack:'', githubUrl:'', demoUrl:'' })

  useEffect(() => {
    if (isEdit) {
      api.get(`/projects/${id}`).then(r => {
        const d = r.data.data
        setForm({ title:d.title, description:d.description, techStack:d.techStack||'', githubUrl:d.githubUrl||'', demoUrl:d.demoUrl||'' })
      })
    }
  }, [id])

  const handle = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async e => {
    e.preventDefault()
    try {
      if (isEdit) {
        await api.put(`/projects/${id}`, form)
        navigate(`/projects/${id}`)
      } else {
        const r = await api.post('/projects', form)
        navigate(`/projects/${r.data.data.id}`)
      }
    } catch (err) {
      alert(err.response?.data?.message || '저장 실패')
    }
  }

  return (
    <div style={{ maxWidth:640, margin:'0 auto' }}>
      <h2>{isEdit ? '프로젝트 수정' : '프로젝트 등록'}</h2>
      <form onSubmit={handleSubmit} className="card" style={{ padding:28, display:'flex', flexDirection:'column', gap:14 }}>
        <div>
          <label>제목</label>
          <input className="form-control" name="title" value={form.title} onChange={handle} required />
        </div>
        <div>
          <label>설명</label>
          <textarea className="form-control" name="description" value={form.description} onChange={handle} rows={7} required />
        </div>
        <div>
          <label>기술 스택 (쉼표로 구분)</label>
          <input className="form-control" name="techStack" value={form.techStack} onChange={handle} placeholder="React, Spring Boot, MySQL" />
        </div>
        <div>
          <label>GitHub URL</label>
          <input className="form-control" name="githubUrl" value={form.githubUrl} onChange={handle} />
        </div>
        <div>
          <label>Demo URL</label>
          <input className="form-control" name="demoUrl" value={form.demoUrl} onChange={handle} />
        </div>
        <div style={{ display:'flex', gap:8 }}>
          <button type="submit" className="btn btn-primary">저장</button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>취소</button>
        </div>
      </form>
    </div>
  )
}
