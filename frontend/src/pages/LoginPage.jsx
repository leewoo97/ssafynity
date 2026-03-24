import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import './AuthPages.css'

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
    <div className="auth-container">
      <div className="auth-card card">
        <h1 className="auth-title">SSAFY NITY</h1>
        <p className="auth-subtitle">캠퍼스 커뮤니티 허브</p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>아이디</label>
            <input
              className="form-control"
              type="text"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              placeholder="아이디를 입력하세요"
              required
            />
          </div>
          <div className="form-group">
            <label>비밀번호</label>
            <input
              className="form-control"
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="비밀번호를 입력하세요"
              required
            />
          </div>
          {error && <p className="error-msg">{error}</p>}
          <button className="btn btn-primary auth-btn" type="submit" disabled={loading}>
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <p className="auth-footer">
          계정이 없으신가요? <Link to="/register">회원가입</Link>
        </p>
      </div>
    </div>
  )
}
