import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import './PostPages.css'

const CATEGORIES = ['잡담', '질문', '정보공유', '취업정보', '스터디']
const CAMPUS_LIST = ['서울', '부산', '대전', '광주', '구미', '강원']

export default function PostFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const isEdit = Boolean(id)

  const [form, setForm] = useState({
    title: '',
    content: '',
    category: '잡담',
    campus: member?.campus || '서울',
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

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  return (
    <div className="post-form card">
      <h2>{isEdit ? '게시글 수정' : '새 게시글'}</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-row" style={{ marginBottom: 16 }}>
          <div className="form-group">
            <label>카테고리</label>
            <select className="form-control" value={form.category} onChange={set('category')}>
              {CATEGORIES.map(c => <option key={c}>{c}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>캠퍼스</label>
            <select className="form-control" value={form.campus} onChange={set('campus')}>
              {CAMPUS_LIST.map(c => <option key={c}>{c}</option>)}
            </select>
          </div>
        </div>
        <div className="form-group">
          <label>제목</label>
          <input className="form-control" value={form.title} onChange={set('title')} required />
        </div>
        <div className="form-group">
          <label>내용</label>
          <textarea
            className="form-control"
            rows={12}
            value={form.content}
            onChange={set('content')}
            required
          />
        </div>
        {error && <p className="error-msg">{error}</p>}
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? '저장 중...' : isEdit ? '수정하기' : '등록하기'}
          </button>
          <button type="button" className="btn btn-outline" onClick={() => navigate(-1)}>
            취소
          </button>
        </div>
      </form>
    </div>
  )
}
