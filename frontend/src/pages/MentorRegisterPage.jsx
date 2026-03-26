import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

export default function MentorRegisterPage() {
  const navigate = useNavigate()
  const { member, token } = useAuthStore()

  const [isEdit, setIsEdit] = useState(false)
  const [existingProfile, setExistingProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [done, setDone] = useState(false)

  const [title, setTitle] = useState('')
  const [career, setCareer] = useState('')
  const [introduction, setIntroduction] = useState('')
  const [maxMentees, setMaxMentees] = useState(3)
  const [tags, setTags] = useState([])
  const [tagInput, setTagInput] = useState('')

  useEffect(() => {
    if (!member) { navigate('/login'); return }
    fetch('/api/mentors/me', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(r => r.json())
      .then(data => {
        const existing = data.data
        if (existing) {
          setIsEdit(true)
          setExistingProfile(existing)
          setTitle(existing.title || '')
          setCareer(existing.career || '')
          setIntroduction(existing.mentorBio || '')
          setMaxMentees(existing.maxMentees || 3)
          setTags(
            existing.specialties
              ? existing.specialties.split(',').map(s => s.trim()).filter(Boolean)
              : []
          )
        }
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [])

  const addTag = (val) => {
    const v = val.trim().replace(/,$/, '')
    if (v && !tags.includes(v) && tags.length < 10) setTags(prev => [...prev, v])
    setTagInput('')
  }

  const handleTagKey = (e) => {
    if (e.key === 'Enter' || e.key === ',') { e.preventDefault(); addTag(tagInput) }
    if (e.key === 'Backspace' && tagInput === '' && tags.length > 0)
      setTags(prev => prev.slice(0, -1))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (!title.trim()) return setError('멘토링 제목을 입력해주세요.')
    if (!career.trim()) return setError('경력 및 이력을 입력해주세요.')
    if (tags.length === 0) return setError('전문 분야를 최소 1개 이상 입력해주세요.')
    if (!introduction.trim()) return setError('멘토 소개를 입력해주세요.')
    if (introduction.trim().length < 30) return setError('멘토 소개를 30자 이상 작성해주세요.')

    setSaving(true)
    const payload = {
      title: title.trim(),
      career: career.trim(),
      specialty: tags.join(', '),
      introduction: introduction.trim(),
      maxMentees: Number(maxMentees),
    }

    try {
      const res = await fetch(isEdit ? '/api/mentors/me' : '/api/mentors', {
        method: isEdit ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(payload),
      })
      const data = await res.json()
      if (!res.ok) {
        setError(data.message || '저장에 실패했습니다.')
      } else {
        navigate('/mentors')
      }
    } catch {
      setError('네트워크 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }

  const handleToggle = async () => {
    try {
      const res = await fetch('/api/mentors/me/toggle', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` },
      })
      if (res.ok) {
        setExistingProfile(prev => ({ ...prev, active: !prev.active }))
      }
    } catch { /* silent */ }
  }

  if (loading) {
    return (
      <div className="section-sm"><div className="container">
        <div className="loading">불러오는 중...</div>
      </div></div>
    )
  }

  return (
    <div className="section-sm"><div className="container">
    <div style={{ maxWidth: 720, margin: '0 auto' }}>
      {/* 헤더 */}
      <div style={{ marginBottom: 24 }}>
        <button className="btn btn-ghost btn-sm" onClick={() => navigate('/mentors')}>
          ← 멘토 목록
        </button>
      </div>

      <div className="card" style={{ padding: '36px 40px' }}>
        <div style={{ marginBottom: 28 }}>
          <h1 style={{ fontSize: 22, fontWeight: 700, marginBottom: 6 }}>
            {isEdit ? '🎓 멘토 프로필 수정' : '🎓 멘토 등록하기'}
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>
            {isEdit
              ? '멘토링 정보를 최신 상태로 유지해주세요.'
              : 'SSAFY 후배들의 멘토가 되어 경험을 나눠보세요!'}
          </p>
        </div>

        {/* 활성/비활성 토글 (수정 모드) */}
        {isEdit && existingProfile && (
          <div style={{
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
            background: existingProfile.active ? '#f0fdf4' : '#f9fafb',
            border: `1px solid ${existingProfile.active ? '#86efac' : '#e5e7eb'}`,
            borderRadius: 10, padding: '12px 16px', marginBottom: 28,
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <span style={{ fontSize: 18 }}>{existingProfile.active ? '✅' : '⏸️'}</span>
              <div>
                <div style={{ fontWeight: 600, fontSize: 14 }}>
                  현재 상태: {existingProfile.active ? '활성 (멘토링 수락 중)' : '비활성 (신청 받지 않음)'}
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                  세션 {existingProfile.sessionCount}회 · 멘티 {existingProfile.currentMentees}/{existingProfile.maxMentees}명
                </div>
              </div>
            </div>
            <button
              type="button"
              className={`btn btn-sm ${existingProfile.active ? 'btn-outline' : 'btn-blue'}`}
              onClick={handleToggle}
            >
              {existingProfile.active ? '비활성화' : '활성화'}
            </button>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* 멘토링 제목 */}
          <div className="form-group" style={{ marginBottom: 20 }}>
            <label className="form-label">
              멘토링 제목 <span style={{ color: 'var(--color-error)' }}>*</span>
            </label>
            <input
              className="form-input"
              type="text"
              placeholder="예: 백엔드 취업 멘토링 · 알고리즘 코치"
              value={title}
              onChange={e => setTitle(e.target.value)}
              maxLength={80}
            />
            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4, textAlign: 'right' }}>
              {title.length}/80
            </div>
          </div>

          {/* 경력 및 이력 */}
          <div className="form-group" style={{ marginBottom: 20 }}>
            <label className="form-label">
              경력 및 이력 <span style={{ color: 'var(--color-error)' }}>*</span>
            </label>
            <textarea
              className="form-textarea"
              rows={3}
              placeholder="예: 삼성 SDS 3년 · 현재 카카오 재직 중 · SSAFY 5기 우수 수료"
              value={career}
              onChange={e => setCareer(e.target.value)}
              style={{ resize: 'vertical' }}
            />
          </div>

          {/* 전문 분야 */}
          <div className="form-group" style={{ marginBottom: 20 }}>
            <label className="form-label">
              전문 분야 <span style={{ color: 'var(--color-error)' }}>*</span>
              <span style={{ fontWeight: 400, color: 'var(--text-muted)', marginLeft: 6, fontSize: 12 }}>
                Enter 또는 쉼표로 추가 (최대 10개)
              </span>
            </label>
            <div className="tag-chip-input">
              {tags.map(t => (
                <span key={t} className="tag-chip">
                  <span className="tag-chip-text-input">{t}</span>
                  <button type="button" onClick={() => setTags(tags.filter(x => x !== t))}>×</button>
                </span>
              ))}
              {tags.length < 10 && (
                <input
                  type="text"
                  placeholder={tags.length === 0 ? 'Java, Spring, 알고리즘...' : ''}
                  value={tagInput}
                  onChange={e => setTagInput(e.target.value)}
                  onKeyDown={handleTagKey}
                  onBlur={() => tagInput.trim() && addTag(tagInput)}
                  style={{
                    border: 'none', outline: 'none', flex: 1, minWidth: 120,
                    fontSize: 14, background: 'transparent',
                  }}
                />
              )}
            </div>
          </div>

          {/* 멘토 소개 */}
          <div className="form-group" style={{ marginBottom: 20 }}>
            <label className="form-label">
              멘토 소개 <span style={{ color: 'var(--color-error)' }}>*</span>
              <span style={{ fontWeight: 400, color: 'var(--text-muted)', marginLeft: 6, fontSize: 12 }}>
                최소 30자
              </span>
            </label>
            <textarea
              className="form-textarea"
              rows={5}
              placeholder="멘티에게 어떤 도움을 줄 수 있는지, 어떤 방식으로 멘토링을 진행하는지 소개해주세요."
              value={introduction}
              onChange={e => setIntroduction(e.target.value)}
              style={{ resize: 'vertical' }}
            />
            <div style={{ fontSize: 12, color: introduction.length < 30 ? 'var(--color-error)' : 'var(--text-muted)', marginTop: 4, textAlign: 'right' }}>
              {introduction.length}자 {introduction.length < 30 ? `(${30 - introduction.length}자 더 필요)` : ''}
            </div>
          </div>

          {/* 최대 멘티 수 */}
          <div className="form-group" style={{ marginBottom: 28 }}>
            <label className="form-label">
              최대 멘티 수
              <span style={{ fontWeight: 400, color: 'var(--text-muted)', marginLeft: 6, fontSize: 12 }}>
                동시에 진행할 수 있는 멘티의 최대 인원
              </span>
            </label>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              {[1, 2, 3, 5, 10].map(n => (
                <button
                  key={n}
                  type="button"
                  onClick={() => setMaxMentees(n)}
                  style={{
                    width: 44, height: 44, borderRadius: 8, border: 'none', cursor: 'pointer',
                    background: maxMentees === n ? 'var(--color-primary)' : '#f3f4f6',
                    color: maxMentees === n ? '#fff' : 'var(--text-main)',
                    fontWeight: 600, fontSize: 15,
                    transition: 'all .15s',
                  }}
                >
                  {n}
                </button>
              ))}
              <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>명</span>
            </div>
          </div>

          {/* 에러 */}
          {error && (
            <div style={{
              background: '#fef2f2', border: '1px solid #fecaca',
              borderRadius: 8, padding: '10px 14px', color: '#dc2626',
              fontSize: 14, marginBottom: 20,
            }}>
              ⚠️ {error}
            </div>
          )}

          {/* 안내 박스 */}
          {!isEdit && (
            <div style={{
              background: '#eff6ff', border: '1px solid #bfdbfe',
              borderRadius: 10, padding: '14px 16px', marginBottom: 24, fontSize: 13,
              color: '#1d4ed8',
            }}>
              <div style={{ fontWeight: 600, marginBottom: 6 }}>📌 멘토 등록 전 안내</div>
              <ul style={{ margin: 0, paddingLeft: 18, lineHeight: 1.8 }}>
                <li>멘토링 신청을 받으면 이메일로 알림이 발송됩니다.</li>
                <li>멘토는 신청을 수락하거나 거절할 수 있으며, 수락 시 1:1 채팅방이 개설됩니다.</li>
                <li>등록 후 <strong>내 멘토링 현황</strong>에서 신청 내역을 관리할 수 있습니다.</li>
              </ul>
            </div>
          )}

          {/* 제출 버튼 */}
          <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
            <button
              type="button"
              className="btn btn-outline"
              onClick={() => navigate('/mentors')}
              disabled={saving}
            >
              취소
            </button>
            <button
              type="submit"
              className="btn btn-blue"
              disabled={saving}
              style={{ minWidth: 120 }}
            >
              {saving ? '저장 중...' : isEdit ? '✅ 수정 완료' : '🎓 멘토 등록'}
            </button>
          </div>
        </form>
      </div>
    </div>
    </div></div>
  )
}
