import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

const CAT_LABEL = { FREE: '자유', QUESTION: 'Q&A', INFO: '정보', REVIEW: '후기', RECRUIT: '모집' }

export default function PostDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [post, setPost] = useState(null)
  const [comments, setComments] = useState([])
  const [commentText, setCommentText] = useState('')
  const [liked, setLiked] = useState(false)
  const [likeCount, setLikeCount] = useState(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const requests = [api.get(`/posts/${id}`), api.get(`/comments/post/${id}`)]
    if (member) requests.push(api.get(`/posts/${id}/liked`))
    Promise.all(requests).then(([pr, cr, lr]) => {
      setPost(pr.data.data)
      setComments(cr.data.data)
      setLikeCount(pr.data.data.likeCount)
      if (lr) {
        setLiked(lr.data.data.liked)
        setLikeCount(lr.data.data.likeCount)
      }
    }).finally(() => setLoading(false))
  }, [id, member])

  const handleDelete = async () => {
    if (!window.confirm('삭제하시겠습니까?')) return
    await api.delete(`/posts/${id}`)
    navigate('/posts')
  }

  const handleLike = async () => {
    const res = await api.post(`/posts/${id}/like`)
    setLiked(res.data.data.liked)
    setLikeCount(res.data.data.likeCount)
  }

  const handleComment = async (e) => {
    e.preventDefault()
    if (!commentText.trim()) return
    const res = await api.post(`/comments/post/${id}`, { content: commentText })
    setComments([...comments, res.data.data])
    setCommentText('')
  }

  const handleDeleteComment = async (cid) => {
    await api.delete(`/comments/${cid}`)
    setComments(comments.filter(c => c.id !== cid))
  }

  if (loading) return <div className="empty"><div className="empty-icon">⏳</div></div>
  if (!post) return <div className="empty"><div className="empty-title">게시글을 찾을 수 없습니다</div></div>

  const isAuthor = member?.id === post.authorId
  const isAdmin = member?.role === 'ADMIN'

  return (
    <div className="section-sm">
      <div className="container">
        <div className="two-col">
          <div>
            <div className="card">
              <div className="post-header">
                <div style={{ display: 'flex', gap: 6, marginBottom: 12, flexWrap: 'wrap' }}>
                  <span className="pill pill-gray">{CAT_LABEL[post.category] || post.category}</span>
                </div>
                <h1 className="post-title">{post.title}</h1>
                <div className="post-attrs">
                  <span className="post-attr">
                    <Link to={`/profile/${post.authorId}`} style={{ fontWeight: 600, color: 'var(--blue)' }}>
                      {post.authorNickname}
                    </Link>
                  </span>
                  <span className="post-attr">🕐 {dayjs(post.createdAt).format('YYYY.MM.DD HH:mm')}</span>
                  <span className="post-attr">👁 {post.viewCount}</span>
                  <span className="post-attr">❤ {likeCount}</span>
                </div>
              </div>

              <div className="post-body" dangerouslySetInnerHTML={{ __html: post.content }} />

              <div className="post-actions">
                {member && (
                  <button className={`like-btn${liked ? ' liked' : ''}`} onClick={handleLike}>
                    <span>❤</span>
                    <span>{liked ? '좋아요 취소' : '좋아요'}</span>
                    <span>{likeCount}</span>
                  </button>
                )}
                {member && (
                  <Link to={`/report?targetType=POST&targetId=${post.id}`} className="btn btn-ghost btn-sm">신고</Link>
                )}
                {(isAuthor || isAdmin) && (
                  <>
                    <Link to={`/posts/${id}/edit`} className="btn btn-ghost btn-sm">수정</Link>
                    <button className="btn btn-danger btn-sm" onClick={handleDelete}>삭제</button>
                  </>
                )}
                <Link to="/posts" className="btn btn-ghost btn-sm" style={{ marginLeft: 'auto' }}>목록</Link>
              </div>
            </div>

            <div className="card" style={{ marginTop: 16 }}>
              <div style={{ fontSize: '.95rem', fontWeight: 600, color: 'var(--t2)', marginBottom: 16 }}>
                💬 댓글 {comments.length}
              </div>

              {member ? (
                <div className="comment-compose">
                  <div className="av av-sm">{member.nickname?.charAt(0)?.toUpperCase()}</div>
                  <form onSubmit={handleComment} style={{ flex: 1, display: 'flex', gap: 8 }}>
                    <input className="form-input" type="text" placeholder="댓글을 입력하세요..."
                      value={commentText} onChange={e => setCommentText(e.target.value)}
                      style={{ flex: 1 }} />
                    <button type="submit" className="btn btn-blue btn-sm">등록</button>
                  </form>
                </div>
              ) : (
                <div style={{ padding: 14, background: 'var(--surface-2)', borderRadius: 'var(--r-sm)', textAlign: 'center', fontSize: '.88rem', color: 'var(--t4)' }}>
                  댓글을 달려면 <Link to="/login" style={{ color: 'var(--blue)' }}>로그인</Link>하세요
                </div>
              )}

              {comments.length > 0 ? (
                <div className="comment-list">
                  {comments.map(c => (
                    <div key={c.id} className="comment-item">
                      <div className="av av-sm">{c.authorNickname?.charAt(0)?.toUpperCase()}</div>
                      <div className="comment-body">
                        <div className="comment-header">
                          <Link to={`/profile/${c.authorId}`} style={{ fontWeight: 600, fontSize: '.88rem', color: 'var(--t1)' }}>
                            {c.authorNickname}
                          </Link>
                          <span style={{ fontSize: '.78rem', color: 'var(--t5)' }}>
                            {dayjs(c.createdAt).format('MM.DD HH:mm')}
                          </span>
                        </div>
                        <div className="comment-text">{c.content}</div>
                        {(member?.id === c.authorId || isAdmin) && (
                          <button className="btn btn-ghost btn-xs" style={{ color: 'var(--t5)' }}
                            onClick={() => handleDeleteComment(c.id)}>삭제</button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ padding: 24, textAlign: 'center', fontSize: '.88rem', color: 'var(--t4)' }}>
                  첫 댓글을 남겨보세요! 💬
                </div>
              )}
            </div>
          </div>

          <aside>
            <div className="sidebar-block">
              <div className="sidebar-block-head">✍️ 작성자</div>
              <div className="sidebar-block-body">
                <div style={{ padding: '16px 18px', display: 'flex', alignItems: 'center', gap: 12 }}>
                  <div className="av av-md">{post.authorNickname?.charAt(0)?.toUpperCase()}</div>
                  <div>
                    <div style={{ fontSize: '.9rem', fontWeight: 600, color: 'var(--t1)' }}>{post.authorNickname}</div>
                    {post.authorBio && (
                      <div style={{ fontSize: '.78rem', color: 'var(--t4)' }}>{post.authorBio}</div>
                    )}
                  </div>
                </div>
                <div style={{ padding: '0 18px 16px' }}>
                  <Link to={`/profile/${post.authorId}`} className="btn btn-ghost btn-sm" style={{ width: '100%', textAlign: 'center' }}>
                    프로필 보기
                  </Link>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  )
}
