import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import './AuthPages.css'

const CAMPUS_LIST = ['서울', '부산', '대전', '광주', '구미', '강원']
const CLASS_CODES = Array.from({ length: 12 }, (_, i) => i + 1)

export default function RegisterPage() {
  const [form, setForm] = useState({
    username: '', password: '', nickname: '', email: '',
    campus: '서울', cohort: 13, classCode: 1,
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
    <div className="auth-container">
      <div className="auth-card card" style={{ maxWidth: 480 }}>
        <h1 className="auth-title">회원가입</h1>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>아이디 <span className="hint">(영소문자, 숫자, _ / 4~20자)</span></label>
            <input className="form-control" value={form.username} onChange={set('username')} required />
          </div>
          <div className="form-group">
            <label>비밀번호 <span className="hint">(6자 이상)</span></label>
            <input className="form-control" type="password" value={form.password} onChange={set('password')} required />
          </div>
          <div className="form-group">
            <label>닉네임</label>
            <input className="form-control" value={form.nickname} onChange={set('nickname')} required />
          </div>
          <div className="form-group">
            <label>이메일</label>
            <input className="form-control" type="email" value={form.email} onChange={set('email')} required />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>캠퍼스</label>
              <select className="form-control" value={form.campus} onChange={set('campus')}>
                {CAMPUS_LIST.map(c => <option key={c}>{c}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>기수</label>
              <input className="form-control" type="number" min={1} value={form.cohort} onChange={set('cohort')} required />
            </div>
            <div className="form-group">
              <label>반</label>
              <select className="form-control" value={form.classCode} onChange={set('classCode')}>
                {CLASS_CODES.map(c => <option key={c} value={c}>{c}반</option>)}
              </select>
            </div>
          </div>
          {error && <p className="error-msg">{error}</p>}
          <button className="btn btn-primary auth-btn" type="submit" disabled={loading}>
            {loading ? '가입 중...' : '회원가입'}
          </button>
        </form>

        <p className="auth-footer">
          이미 계정이 있으신가요? <Link to="/login">로그인</Link>
        </p>
      </div>
    </div>
  )
}
