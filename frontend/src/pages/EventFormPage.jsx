import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'

const EVENT_TYPES = ['SEMINAR', 'HACKATHON', 'STUDY', 'NETWORKING', 'WORKSHOP', 'OTHER']

export default function EventFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [form, setForm] = useState({
    title: '', description: '', location: '', eventType: 'SEMINAR',
    startDate: '', endDate: '', maxParticipants: 30
  })

  useEffect(() => {
    if (isEdit) {
      api.get(`/events/${id}`).then(r => {
        const d = r.data.data
        setForm({
          title: d.title, description: d.description, location: d.location,
          eventType: d.eventType,
          startDate: d.startDate?.slice(0,16),
          endDate: d.endDate?.slice(0,16),
          maxParticipants: d.maxParticipants
        })
      })
    }
  }, [id])

  const handle = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async e => {
    e.preventDefault()
    try {
      if (isEdit) {
        await api.put(`/events/${id}`, form)
        navigate(`/events/${id}`)
      } else {
        const r = await api.post('/events', form)
        navigate(`/events/${r.data.data.id}`)
      }
    } catch (err) {
      alert(err.response?.data?.message || '저장 실패')
    }
  }

  return (
    <div style={{ maxWidth:640, margin:'0 auto' }}>
      <h2>{isEdit ? '이벤트 수정' : '이벤트 등록'}</h2>
      <form onSubmit={handleSubmit} className="card" style={{ padding:28, display:'flex', flexDirection:'column', gap:14 }}>
        <div>
          <label>제목</label>
          <input className="form-control" name="title" value={form.title} onChange={handle} required />
        </div>
        <div>
          <label>유형</label>
          <select className="form-control" name="eventType" value={form.eventType} onChange={handle}>
            {EVENT_TYPES.map(t => <option key={t}>{t}</option>)}
          </select>
        </div>
        <div>
          <label>내용</label>
          <textarea className="form-control" name="description" value={form.description} onChange={handle} rows={6} required />
        </div>
        <div>
          <label>장소</label>
          <input className="form-control" name="location" value={form.location} onChange={handle} />
        </div>
        <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:12 }}>
          <div>
            <label>시작일시</label>
            <input type="datetime-local" className="form-control" name="startDate" value={form.startDate} onChange={handle} required />
          </div>
          <div>
            <label>종료일시</label>
            <input type="datetime-local" className="form-control" name="endDate" value={form.endDate} onChange={handle} required />
          </div>
        </div>
        <div>
          <label>최대 참가 인원</label>
          <input type="number" className="form-control" name="maxParticipants" value={form.maxParticipants} onChange={handle} min={1} />
        </div>
        <div style={{ display:'flex', gap:8 }}>
          <button type="submit" className="btn btn-primary">저장</button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>취소</button>
        </div>
      </form>
    </div>
  )
}
