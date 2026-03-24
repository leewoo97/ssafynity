import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

export default function ProfilePage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [profile, setProfile] = useState(null)
  const [friendStatus, setFriendStatus] = useState(null) // null | 'PENDING' | 'ACCEPTED'

  useEffect(() => {
    api.get(`/members/${id}`).then(r => setProfile(r.data.data))
    api.get(`/friends/status/${id}`).then(r => setFriendStatus(r.data.data)).catch(() => setFriendStatus(null))
  }, [id])

  const handleSendFriend = async () => {
    await api.post(`/friends/send/${id}`)
    setFriendStatus('PENDING')
  }
  const handleUnfriend = async () => {
    await api.delete(`/friends/${id}`)
    setFriendStatus(null)
  }

  if (!profile) return <div className="loading">로딩 중...</div>
  const isSelf = member?.id === parseInt(id)

  return (
    <div style={{ maxWidth:600, margin:'0 auto' }}>
      <div className="card" style={{ padding:32 }}>
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start' }}>
          <div>
            <h2 style={{ margin:'0 0 4px' }}>{profile.nickname}</h2>
            <div style={{ fontSize:14, color:'var(--color-text-muted)', marginBottom:12 }}>
              {profile.campus} | {profile.cohort}기 {profile.classCode}반
            </div>
          </div>
          {!isSelf && (
            <div style={{ display:'flex', gap:8 }}>
              {friendStatus === 'ACCEPTED' && (
                <button onClick={handleUnfriend} className="btn btn-secondary">친구 해제</button>
              )}
              {friendStatus === 'PENDING' && (
                <button disabled className="btn btn-secondary">요청 전송됨</button>
              )}
              {!friendStatus && (
                <button onClick={handleSendFriend} className="btn btn-primary">친구 추가</button>
              )}
              <button onClick={async () => {
                const r = await api.post(`/dm/users/${id}`)
                navigate(`/dm/${r.data.data.id}`)
              }} className="btn btn-secondary">DM 보내기</button>
            </div>
          )}
        </div>

        {profile.bio && (
          <div style={{ background:'var(--color-surface-secondary)', borderRadius:8, padding:'14px 16px', marginBottom:16, lineHeight:1.7 }}>
            {profile.bio}
          </div>
        )}

        {isSelf && (
          <button onClick={() => navigate('/mypage')} className="btn btn-secondary">프로필 편집</button>
        )}
      </div>
    </div>
  )
}
