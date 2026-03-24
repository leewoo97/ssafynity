import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'

const CAMPUS_LIST = ['서울', '부산', '대전', '광주', '구미', '강원']
const CLASS_CODES = Array.from({ length: 12 }, (_, i) => i + 1)

export default function RegisterPage() {
  const [form, setForm] = useState({
    username: '', password: '', nickname: '', email: '',
    campus: '', cohort: '', classCode: '', realName: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post('/auth/register', {
        ...form,
        cohort: Number(form.cohort),
        classCode: Number(form.classCode),
      })
      navigate('/login')
    } catch (err) {
      setError(err.response?.data?.error?.message || '회원가입에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  return (
    <div className="auth-wrap">
      <div className="auth-card" style={{ maxWidth: 560 }}>
        <div className="auth-logo">S</div>
        <div className="auth-tagline">SSAFYnity</div>
        <h2 className="auth-title">회원가입</h2>

        {error && (
          <div style={{ marginBottom: 16, padding: '12px 14px', background: 'rgba(255,59,48,.08)', borderRadius: 'var(--r-sm)', color: 'var(--red)', fontSize: '.88rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="register-section-label">기본 정보</div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">아이디 <span style={{ color: 'var(--red)' }}>*</span></label>
              <input className="form-input" value={form.username} onChange={set('username')} placeholder="영문/숫자 4자 이상" required />
            </div>
            <div className="form-group">
              <label className="form-label">닉네임 <span style={{ color: 'var(--red)' }}>*</span></label>
              <input className="form-input" value={form.nickname} onChange={set('nickname')} placeholder="커뮤니티에서 사용할 이름" required />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">비밀번호 <span style={{ color: 'var(--red)' }}>*</span></label>
            <input className="form-input" type="password" value={form.password} onChange={set('password')} placeholder="8자 이상" required />
          </div>
          <div className="form-group">
            <label className="form-label">이메일 <span style={{ fontWeight: 400, color: 'var(--t5)' }}>(선택)</span></label>
            <input className="form-input" type="email" value={form.email} onChange={set('email')} placeholder="example@ssafy.com" />
          </div>

          <div className="register-section-label" style={{ marginTop: 20 }}>SSAFY 정보</div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">캠퍼스 <span style={{ fontWeight: 400, color: 'var(--t5)' }}>(선택)</span></label>
              <select className="form-select" value={form.campus} onChange={set('campus')}>
                <option value="">선택 안함</option>
                {CAMPUS_LIST.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">기수 <span style={{ fontWeight: 400, color: 'var(--t5)' }}>(선택)</span></label>
              <select className="form-select" value={form.cohort} onChange={set('cohort')}>
                <option value="">선택 안함</option>
                {Array.from({ length: 16 }, (_, i) => i + 1).map(n => <option key={n} value={n}>{n}기</option>)}
              </select>
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">반 <span style={{ fontWeight: 400, color: 'var(--t5)' }}>(선택)</span></label>
            <select className="form-select" value={form.classCode} onChange={set('classCode')}>
              <option value="">선택 안함</option>
              {CLASS_CODES.map(c => <option key={c} value={c}>{c}반</option>)}
            </select>
            <div style={{ fontSize: '.8rem', color: 'var(--t5)', marginTop: 4 }}>같은 반 동기의 실명을 볼 수 있습니다.</div>
          </div>

          <div className="register-section-label" style={{ marginTop: 20 }}>실명 설정</div>
          <div className="form-group">
            <label className="form-label">실명 <span style={{ fontWeight: 400, color: 'var(--t5)' }}>(선택)</span></label>
            <input className="form-input" value={form.realName || ''} onChange={set('realName')} placeholder="홍길동" />
            <div style={{ fontSize: '.8rem', color: 'var(--t5)', marginTop: 4 }}>같은 반 동기와 친구에게만 공개됩니다.</div>
          </div>

          <button className="btn btn-blue btn-lg" type="submit" disabled={loading} style={{ width: '100%', marginTop: 16 }}>
            {loading ? '가입 중...' : '가입하기'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 20, fontSize: '.88rem', color: 'var(--t4)' }}>
          이미 계정이 있으신가요? <Link to="/login" style={{ color: 'var(--blue)', fontWeight: 600 }}>로그인</Link>
        </p>
      </div>
    </div>
  )
}
