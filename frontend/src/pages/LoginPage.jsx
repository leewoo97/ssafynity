import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function LoginPage() {
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const login = useAuthStore((state) => state.login)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/auth/login', form)
      const { accessToken, member } = res.data.data
      login(accessToken, member)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.error?.message || '로그인에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="auth-logo">S</div>
        <div className="auth-tagline">SSAFYnity</div>
        <h2 className="auth-title">로그인</h2>

        {error && (
          <div style={{ marginBottom: 16, padding: '12px 14px', background: 'rgba(255,59,48,.08)', borderRadius: 'var(--r-sm)', color: 'var(--red)', fontSize: '.88rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">아이디</label>
            <input className="form-input" type="text" value={form.username}
              onChange={e => setForm({ ...form, username: e.target.value })}
              placeholder="아이디 입력" required />
          </div>
          <div className="form-group">
            <label className="form-label">비밀번호</label>
            <input className="form-input" type="password" value={form.password}
              onChange={e => setForm({ ...form, password: e.target.value })}
              placeholder="비밀번호 입력" required />
          </div>
          <button className="btn btn-blue btn-lg" type="submit" disabled={loading} style={{ width: '100%', marginTop: 8 }}>
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 20, fontSize: '.88rem', color: 'var(--t4)' }}>
          계정이 없으신가요? <Link to="/register" style={{ color: 'var(--blue)', fontWeight: 600 }}>회원가입</Link>
        </p>
      </div>
    </div>
  )
}
