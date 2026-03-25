import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function MentorListPage() {
  const { member } = useAuthStore()
  const navigate = useNavigate()
  const [mentors, setMentors] = useState([])
  const [loading, setLoading] = useState(true)
  const [keyword, setKeyword] = useState('')
  const [inputVal, setInputVal] = useState('')

  // 신청 모달
  const [applyTarget, setApplyTarget] = useState(null) // mentor object
  const [applyMsg, setApplyMsg] = useState('')
  const [applyError, setApplyError] = useState('')
  const [applyDone, setApplyDone] = useState(false)
  const [applying, setApplying] = useState(false)

  useEffect(() => {
    setLoading(true)
    const params = keyword ? { keyword } : {}
    api.get('/mentors', { params })
      .then(r => setMentors(r.data.data || []))
      .finally(() => setLoading(false))
  }, [keyword])

  const handleSearch = (e) => {
    e.preventDefault()
    setKeyword(inputVal)
  }

  const openApply = (m) => {
    if (!member) { navigate('/login'); return }
    setApplyTarget(m)
    setApplyMsg('')
    setApplyError('')
    setApplyDone(false)
  }

  const closeApply = () => {
    setApplyTarget(null)
    setApplyMsg('')
    setApplyError('')
    setApplyDone(false)
  }

  const handleApply = async () => {
    if (!applyMsg.trim()) { setApplyError('신청 내용을 입력해 주세요.'); return }
    if (applyMsg.trim().length < 20) { setApplyError('최소 20자 이상 작성해 주세요.'); return }
    setApplying(true)
    setApplyError('')
    try {
      await api.post(`/mentoring/apply/${applyTarget.id}`, { message: applyMsg.trim() })
      setApplyDone(true)
    } catch (e) {
      setApplyError(e.response?.data?.error?.message || e.response?.data?.message || '신청에 실패했습니다.')
    } finally {
      setApplying(false)
    }
  }

  const startDm = async (memberId) => {
    try {
      const r = await api.post(`/dm/users/${memberId}`)
      navigate(`/dm/${r.data.data.id}`)
    } catch { alert('DM 시작 실패') }
  }

  return (
    <>
      <div className="page-header">
        <div className="container">
          <div className="page-header-inner">
            <div>
              <div className="label" style={{ marginBottom: 6 }}>Mentoring</div>
              <h1>멘토 찾기</h1>
              <p>SSAFY 선배 개발자에게 1:1 멘토링을 신청해보세요</p>
            </div>
            {member && (
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
                <Link to="/mentoring/my" className="btn btn-ghost btn-md">📋 내 멘토링 현황</Link>
                <Link to="/mentors/register" className="btn btn-blue btn-md">🎓 멘토 등록하기</Link>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="section-sm">
        <div className="container">
          <form onSubmit={handleSearch} style={{ marginBottom: 32, display: 'flex', gap: 8 }}>
            <input
              type="text"
              className="form-input"
              style={{ flex: 1, maxWidth: 480 }}
              placeholder="분야로 검색 (예: Spring Boot, 알고리즘, 프론트엔드...)"
              value={inputVal}
              onChange={e => setInputVal(e.target.value)}
            />
            <button type="submit" className="btn btn-blue btn-md">검색</button>
            {keyword && (
              <button type="button" className="btn btn-ghost btn-md"
                onClick={() => { setKeyword(''); setInputVal('') }}>초기화</button>
            )}
          </form>

          {loading ? (
            <div className="empty"><div className="empty-icon">⏳</div></div>
          ) : mentors.length === 0 ? (
            <div className="empty">
              <div className="empty-icon">🎓</div>
              <div className="empty-title">{keyword ? '검색 결과가 없습니다.' : '아직 등록된 멘토가 없습니다.'}</div>
              {member && !keyword && (
                <div style={{ marginTop: 16 }}>
                  <Link to="/mentors/register" className="btn btn-blue btn-md">첫 번째 멘토가 되기</Link>
                </div>
              )}
            </div>
          ) : (
            <div className="mentor-grid">
              {mentors.map(m => (
                <div key={m.id} className="mentor-card">
                  <div className="mentor-card-top">
                    <div className="mentor-avatar-lg">{m.memberNickname?.charAt(0)?.toUpperCase()}</div>
                    <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 12, justifyContent: 'center' }}>
                      {m.sessionCount > 0 && (
                        <span className="pill pill-blue">{m.sessionCount} 세션 완료</span>
                      )}
                      <span className="pill pill-green">
                        {m.maxMentees - m.currentMentees}/{m.maxMentees} 자리
                      </span>
                    </div>
                  </div>
                  <div className="mentor-card-body">
                    <div className="mentor-name">{m.memberNickname}</div>
                    {m.title && <div className="mentor-title">{m.title}</div>}
                    {m.career && <div className="mentor-career">{m.career}</div>}
                    {m.specialties && (
                      <div className="mentor-tags">
                        {m.specialties.split(',').slice(0, 5).map(s => (
                          <span key={s} className="tag">{s.trim()}</span>
                        ))}
                      </div>
                    )}
                    {m.mentorBio && (
                      <p className="mentor-bio-preview">
                        {m.mentorBio.length > 80 ? m.mentorBio.slice(0, 80) + '…' : m.mentorBio}
                      </p>
                    )}
                    <div style={{ display: 'flex', gap: 6, marginTop: 'auto', flexDirection: 'column' }}>
                      {member && member.id !== m.memberId && m.currentMentees < m.maxMentees && (
                        <button
                          className="btn btn-blue btn-md"
                          style={{ width: '100%', justifyContent: 'center' }}
                          onClick={() => openApply(m)}
                        >
                          🎓 멘토링 신청
                        </button>
                      )}
                      <Link to={`/profile/${m.memberId}`} className="btn btn-ghost btn-md"
                        style={{ width: '100%', justifyContent: 'center', textAlign: 'center' }}>
                        프로필 보기
                      </Link>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* ── 멘토링 신청 모달 ──────────────────────────────────────────────── */}
      {applyTarget && (
        <div
          style={{
            position: 'fixed', inset: 0, zIndex: 9000,
            background: 'rgba(0,0,0,.5)',
            backdropFilter: 'blur(6px)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            padding: '20px',
          }}
          onClick={e => { if (e.target === e.currentTarget) closeApply() }}
        >
          <div style={{
            background: 'var(--surface)', borderRadius: 'var(--r-xl)',
            width: '100%', maxWidth: 560,
            boxShadow: 'var(--s3)',
            overflow: 'hidden',
          }}>
            {applyDone ? (
              /* 신청 완료 화면 */
              <div style={{ padding: '48px 36px', textAlign: 'center' }}>
                <div style={{ fontSize: '3rem', marginBottom: 16 }}>🎉</div>
                <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--t1)', marginBottom: 10 }}>
                  멘토링 신청이 완료됐습니다!
                </h3>
                <p style={{ fontSize: '.9rem', color: 'var(--t3)', lineHeight: 1.7, marginBottom: 28 }}>
                  <strong style={{ color: 'var(--blue)' }}>{applyTarget.memberNickname}</strong> 멘토님에게 신청서가 전달되었습니다.<br />
                  멘토님이 이메일로 알림을 받고 검토 후 답변드릴 예정입니다.
                </p>
                <div style={{ display: 'flex', gap: 10, justifyContent: 'center' }}>
                  <button className="btn btn-ghost btn-md" onClick={closeApply}>계속 둘러보기</button>
                  <Link to="/mentoring/my" className="btn btn-blue btn-md" onClick={closeApply}>내 신청 현황 보기</Link>
                </div>
              </div>
            ) : (
              /* 신청 작성 화면 */
              <>
                {/* 모달 헤더 */}
                <div style={{
                  padding: '24px 28px 20px',
                  borderBottom: '1px solid var(--b1)',
                  display: 'flex', alignItems: 'center', gap: 16,
                }}>
                  <div style={{
                    width: 48, height: 48, borderRadius: '50%',
                    background: 'var(--blue)', color: '#fff',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: '1.3rem', fontWeight: 700, flexShrink: 0,
                  }}>
                    {applyTarget.memberNickname?.charAt(0)?.toUpperCase()}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 700, fontSize: '1rem', color: 'var(--t1)' }}>
                      멘토링 신청 — {applyTarget.memberNickname} 멘토
                    </div>
                    {applyTarget.title && (
                      <div style={{ fontSize: '.82rem', color: 'var(--t4)', marginTop: 2 }}>
                        {applyTarget.title}
                      </div>
                    )}
                  </div>
                  <button
                    style={{ background: 'none', border: 'none', fontSize: '1.3rem', cursor: 'pointer', color: 'var(--t4)', lineHeight: 1 }}
                    onClick={closeApply}
                  >×</button>
                </div>

                {/* 모달 바디 */}
                <div style={{ padding: '24px 28px' }}>
                  <div style={{
                    background: 'var(--blue-xl)', borderRadius: 'var(--r-sm)',
                    padding: '14px 18px', marginBottom: 20,
                    fontSize: '.83rem', color: 'var(--t2)', lineHeight: 1.7,
                    borderLeft: '3px solid var(--blue)',
                  }}>
                    💡 멘토님에게 전달될 신청서입니다. 고민하는 부분, 배우고 싶은 것, 현재 상황 등을
                    솔직하게 적어주세요. 구체적으로 작성할수록 좋은 멘토링이 이루어집니다.
                  </div>

                  <div className="form-group">
                    <label className="form-label" style={{ fontWeight: 600 }}>
                      신청 내용 <span style={{ color: 'var(--red)' }}>*</span>
                    </label>
                    <textarea
                      className="form-input"
                      placeholder={`안녕하세요, ${applyTarget.memberNickname} 멘토님.\n\n현재 저는 ...에 대해 고민이 있습니다.\n멘토링을 통해 ...을 배우고 싶습니다.\n\n현재 상황: ...\n궁금한 점: ...`}
                      value={applyMsg}
                      onChange={e => { setApplyMsg(e.target.value); setApplyError('') }}
                      style={{ minHeight: 200, resize: 'vertical', fontFamily: 'var(--font)', fontSize: '.9rem', lineHeight: 1.7 }}
                      maxLength={1500}
                    />
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 5 }}>
                      {applyError
                        ? <span style={{ fontSize: '.78rem', color: 'var(--red)' }}>{applyError}</span>
                        : <span style={{ fontSize: '.78rem', color: 'var(--t5)' }}>최소 20자 이상 작성해 주세요</span>
                      }
                      <span style={{ fontSize: '.78rem', color: 'var(--t5)' }}>{applyMsg.length}/1500</span>
                    </div>
                  </div>
                </div>

                {/* 모달 푸터 */}
                <div style={{
                  padding: '16px 28px 24px',
                  display: 'flex', gap: 10, justifyContent: 'flex-end',
                }}>
                  <button className="btn btn-ghost btn-md" onClick={closeApply} disabled={applying}>취소</button>
                  <button className="btn btn-blue btn-md" onClick={handleApply} disabled={applying}>
                    {applying ? '신청 중...' : '🎓 멘토링 신청하기'}
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </>
  )
}
