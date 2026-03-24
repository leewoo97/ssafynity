import { useState, useEffect } from 'react'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function MentorListPage() {
  const { member } = useAuthStore()
  const [mentors, setMentors] = useState([])
  const [myMentor, setMyMentor] = useState(null)
  const [mentorForm, setMentorForm] = useState({ skills:'', introduction:'', maxMentees:3 })
  const [showForm, setShowForm] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      api.get('/mentors').then(r => setMentors(r.data.data || [])),
      api.get('/mentors/me').then(r => setMyMentor(r.data.data)).catch(() => setMyMentor(null))
    ]).finally(() => setLoading(false))
  }, [])

  const handleRegister = async e => {
    e.preventDefault()
    const isEdit = !!myMentor
    try {
      if (isEdit) {
        const r = await api.put('/mentors/me', mentorForm)
        setMyMentor(r.data.data)
      } else {
        const r = await api.post('/mentors/register', mentorForm)
        setMyMentor(r.data.data)
      }
      setShowForm(false)
      api.get('/mentors').then(r => setMentors(r.data.data || []))
    } catch (err) {
      alert(err.response?.data?.message || '저장 실패')
    }
  }

  const handleApply = async (mentorId) => {
    try {
      await api.post(`/mentoring/apply/${mentorId}`)
      alert('멘토링 신청이 완료되었습니다.')
    } catch (err) {
      alert(err.response?.data?.message || '신청 실패')
    }
  }

  const handleWithdraw = async () => {
    if (!confirm('멘토 등록을 취소하시겠습니까?')) return
    await api.delete('/mentors/me')
    setMyMentor(null)
    api.get('/mentors').then(r => setMentors(r.data.data || []))
  }

  if (loading) return <div className="loading">로딩 중...</div>

  return (
    <div style={{ maxWidth:800, margin:'0 auto' }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 }}>
        <h2>멘토링</h2>
        <div style={{ display:'flex', gap:8 }}>
          {myMentor ? (
            <>
              <button onClick={() => { setMentorForm({ skills:myMentor.skills, introduction:myMentor.introduction, maxMentees:myMentor.maxMentees }); setShowForm(true) }}
                className="btn btn-secondary">내 멘토 프로필 수정</button>
              <button onClick={handleWithdraw} className="btn" style={{ background:'#e74c3c', color:'#fff' }}>멘토 취소</button>
            </>
          ) : (
            <button onClick={() => setShowForm(s => !s)} className="btn btn-primary">멘토 등록</button>
          )}
        </div>
      </div>

      {myMentor && (
        <div className="card" style={{ padding:20, marginBottom:20, background:'#f0f4ff', border:'1px solid #c5d5f0' }}>
          <h4 style={{ marginBottom:8 }}>📌 나의 멘토 프로필</h4>
          <div style={{ fontSize:14, marginBottom:4 }}>기술: {myMentor.skills}</div>
          <div style={{ fontSize:14, marginBottom:4 }}>소개: {myMentor.introduction}</div>
          <div style={{ fontSize:13, color:'var(--color-text-muted)' }}>최대 {myMentor.maxMentees}명</div>
        </div>
      )}

      {showForm && (
        <form onSubmit={handleRegister} className="card" style={{ padding:24, marginBottom:20, display:'flex', flexDirection:'column', gap:12 }}>
          <h4>{myMentor ? '멘토 프로필 수정' : '멘토 등록'}</h4>
          <div>
            <label>보유 기술 (쉼표로 구분)</label>
            <input className="form-control" value={mentorForm.skills} onChange={e => setMentorForm(f => ({ ...f, skills:e.target.value }))} placeholder="React, Spring Boot, Python" required />
          </div>
          <div>
            <label>소개</label>
            <textarea className="form-control" value={mentorForm.introduction} onChange={e => setMentorForm(f => ({ ...f, introduction:e.target.value }))} rows={4} required />
          </div>
          <div>
            <label>최대 멘티 수</label>
            <input type="number" className="form-control" value={mentorForm.maxMentees} onChange={e => setMentorForm(f => ({ ...f, maxMentees:parseInt(e.target.value) }))} min={1} max={10} />
          </div>
          <div style={{ display:'flex', gap:8 }}>
            <button type="submit" className="btn btn-primary">저장</button>
            <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>취소</button>
          </div>
        </form>
      )}

      <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill,minmax(280px,1fr))', gap:16 }}>
        {mentors.map(m => (
          <div key={m.id} className="card" style={{ padding:20 }}>
            <div style={{ fontWeight:600, fontSize:16, marginBottom:6 }}>{m.memberNickname}</div>
            <div style={{ fontSize:13, color:'var(--color-text-muted)', marginBottom:8 }}>{m.memberCampus}</div>
            <div style={{ display:'flex', flexWrap:'wrap', gap:4, marginBottom:10 }}>
              {m.skills?.split(',').map(s => (
                <span key={s} style={{ fontSize:11, padding:'2px 6px', background:'#e9ecef', borderRadius:3 }}>{s.trim()}</span>
              ))}
            </div>
            <p style={{ fontSize:13, lineHeight:1.6, marginBottom:12 }}>{m.introduction}</p>
            <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
              <span style={{ fontSize:12, color:'var(--color-text-muted)' }}>잔여 {m.remainingSlots}/{m.maxMentees}명</span>
              {member?.id !== m.memberId && m.remainingSlots > 0 && (
                <button onClick={() => handleApply(m.id)} className="btn btn-primary" style={{ fontSize:12, padding:'4px 12px' }}>신청</button>
              )}
            </div>
          </div>
        ))}
        {mentors.length === 0 && <p style={{ gridColumn:'1/-1', textAlign:'center', color:'var(--color-text-muted)', padding:40 }}>등록된 멘토가 없습니다.</p>}
      </div>
    </div>
  )
}
