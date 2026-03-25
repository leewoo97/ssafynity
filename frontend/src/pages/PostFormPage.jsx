import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import RichEditor from '../components/RichEditor'

const CATEGORIES = ['일반', '질문', '정보', '공지', '잡담']
const CAMPUS_LIST = ['서울', '부산', '대전', '광주', '구미', '강원']

export default function PostFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const isEdit = Boolean(id)

  const [form, setForm] = useState({
    title: '', content: '', category: '일반', campus: member?.campus || '서울'
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (isEdit) {
      api.get(`/posts/${id}`).then(r => {
        const { title, content, category, campus } = r.data.data
        setForm({ title, content, category, campus })
      })
    }
  }, [id])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (isEdit) {
        await api.put(`/posts/${id}`, form)
        navigate(`/posts/${id}`)
      } else {
        const res = await api.post('/posts', form)
        navigate(`/posts/${res.data.data.id}`)
      }
    } catch (err) {
      setError(err.response?.data?.error?.message || '저장에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const set = field => e => setForm({ ...form, [field]: e.target.value })

  return (
    <div className="section-sm"><div className="container">
    <div style={{ maxWidth: 720, margin: '0 auto' }}>
      <div className="section-head">
        <h2>{isEdit ? '게시글 수정' : '새 게시글'}</h2>
      </div>
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">카테고리</label>
              <select className="form-select" value={form.category} onChange={set('category')}>
                {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">캠퍼스</label>
              <select className="form-select" value={form.campus} onChange={set('campus')}>
                {CAMPUS_LIST.map(c => <option key={c}>{c}</option>)}
              </select>
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">제목</label>
            <input className="form-input" value={form.title} onChange={set('title')} required placeholder="제목을 입력하세요" />
          </div>
          <div className="form-group">
            <label className="form-label">내용</label>
            <RichEditor value={form.content} onChange={val => setForm({ ...form, content: val })} placeholder="내용을 입력하세요..." />
          </div>
          {error && <p className="alert alert-error" style={{ marginBottom: 12 }}>{error}</p>}
          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <button type="button" className="btn btn-ghost btn-md" onClick={() => navigate(-1)}>취소</button>
            <button type="submit" className="btn btn-blue btn-md" disabled={loading}>
              {loading ? '저장 중...' : isEdit ? '수정하기' : '등록하기'}
            </button>
          </div>
        </form>
      </div>
    </div>
    </div></div>
  )
}
