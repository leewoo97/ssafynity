import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'
import './PostPages.css'

export default function PostDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()

  const [post, setPost] = useState(null)
  const [comments, setComments] = useState([])
  const [commentText, setCommentText] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      api.get(`/posts/${id}`),
      api.get(`/comments/post/${id}`),
    ])
      .then(([postRes, commentRes]) => {
        setPost(postRes.data.data)
        setComments(commentRes.data.data)
      })
      .finally(() => setLoading(false))
  }, [id])

  const handleDelete = async () => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return
    await api.delete(`/posts/${id}`)
    navigate('/posts')
  }

  const handleComment = async (e) => {
    e.preventDefault()
    if (!commentText.trim()) return
    const res = await api.post(`/comments/post/${id}`, { content: commentText })
    setComments([...comments, res.data.data])
    setCommentText('')
  }

  const handleDeleteComment = async (commentId) => {
    await api.delete(`/comments/${commentId}`)
    setComments(comments.filter(c => c.id !== commentId))
  }

  if (loading) return <div className="loading">로딩 중...</div>
  if (!post) return <div className="loading">게시글을 찾을 수 없습니다.</div>

  const isAuthor = member?.id === post.authorId
  const isAdmin = member?.role === 'ADMIN'

  return (
    <div className="post-detail">
      <div className="card">
        <div className="post-detail-header">
          <div>
            <span className="post-category">{post.category}</span>
            <h2 className="post-detail-title">{post.title}</h2>
          </div>
          {(isAuthor || isAdmin) && (
            <div className="post-actions">
              <Link to={`/posts/${id}/edit`} className="btn btn-outline">수정</Link>
              <button className="btn btn-danger" onClick={handleDelete}>삭제</button>
            </div>
          )}
        </div>

        <div className="post-detail-meta">
          <span>{post.authorNickname}</span>
          <span>{post.campus}</span>
          <span>{dayjs(post.createdAt).format('YYYY.MM.DD HH:mm')}</span>
          <span>👁 {post.viewCount}</span>
        </div>

        <div className="post-content">{post.content}</div>
      </div>

      {/* 댓글 영역 */}
      <div className="card" style={{ marginTop: 16 }}>
        <h3>댓글 {comments.length}개</h3>
        <div className="comment-list">
          {comments.map(c => (
            <div key={c.id} className="comment-item">
              <div className="comment-header">
                <strong>{c.authorNickname}</strong>
                <span>{dayjs(c.createdAt).format('MM.DD HH:mm')}</span>
                {(member?.id === c.authorId || isAdmin) && (
                  <button
                    className="btn btn-outline"
                    style={{ padding: '2px 8px', fontSize: 12 }}
                    onClick={() => handleDeleteComment(c.id)}
                  >삭제</button>
                )}
              </div>
              <p>{c.content}</p>
            </div>
          ))}
        </div>

        <form className="comment-form" onSubmit={handleComment}>
          <textarea
            className="form-control"
            rows={3}
            placeholder="댓글을 입력하세요..."
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">등록</button>
        </form>
      </div>

      <div style={{ marginTop: 16 }}>
        <Link to="/posts" className="btn btn-outline">목록으로</Link>
      </div>
    </div>
  )
}
