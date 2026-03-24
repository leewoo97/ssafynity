import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'

const TABS = ['프로필', '비밀번호', '내 게시글', '즐겨찾기', '친구']

const CAMPUSES = ['서울', '대전', '광주', '구미', '부울경']

export default function MyPage() {
  const { member, updateMember, logout } = useAuthStore()
  const navigate = useNavigate()
  const [tab, setTab] = useState('프로필')

  // Profile
  const [profileForm, setProfileForm] = useState({ nickname:'', campus:'', cohort:'', classCode:'', bio:'' })
  const [profileMsg, setProfileMsg] = useState('')

  // Password
  const [pwForm, setPwForm] = useState({ currentPassword:'', newPassword:'', confirmPassword:'' })
  const [pwMsg, setPwMsg] = useState('')

  // My posts
  const [myPosts, setMyPosts] = useState([])

  // Bookmarks
  const [bookmarks, setBookmarks] = useState([])

  // Friends
  const [friends, setFriends] = useState([])
  const [requests, setRequests] = useState([])

  useEffect(() => {
    if (member) {
      setProfileForm({
        nickname: member.nickname || '',
        campus: member.campus || '',
        cohort: member.cohort || '',
        classCode: member.classCode || '',
        bio: member.bio || ''
      })
    }
  }, [member])

  useEffect(() => {
    if (tab === '내 게시글') {
      api.get('/posts', { params: { page:0, size:20, authorId: member?.id } }).then(r => setMyPosts(r.data.data?.content || []))
    }
    if (tab === '즐겨찾기') {
      api.get('/bookmarks').then(r => setBookmarks(r.data.data || []))
    }
    if (tab === '친구') {
      api.get('/friends').then(r => setFriends(r.data.data || []))
      api.get('/friends/requests').then(r => setRequests(r.data.data || []))
    }
  }, [tab])

  const handleProfile = async e => {
    e.preventDefault()
    try {
      const r = await api.put('/members/me', profileForm)
      updateMember(r.data.data)
      setProfileMsg('저장되었습니다.')
    } catch (err) {
      setProfileMsg(err.response?.data?.message || '저장 실패')
    }
  }

  const handlePw = async e => {
    e.preventDefault()
    if (pwForm.newPassword !== pwForm.confirmPassword) { setPwMsg('새 비밀번호가 일치하지 않습니다.'); return }
    try {
      await api.put('/members/me/password', { currentPassword: pwForm.currentPassword, newPassword: pwForm.newPassword })
      setPwMsg('비밀번호가 변경되었습니다.')
      setPwForm({ currentPassword:'', newPassword:'', confirmPassword:'' })
    } catch (err) {
      setPwMsg(err.response?.data?.message || '변경 실패')
    }
  }

  const handleWithdraw = async () => {
    if (!confirm('정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return
    await api.delete('/members/me')
    logout()
    navigate('/login')
  }

  const handleAccept = async (friendId) => {
    await api.post(`/friends/${friendId}/accept`)
    api.get('/friends/requests').then(r => setRequests(r.data.data || []))
    api.get('/friends').then(r => setFriends(r.data.data || []))
  }
  const handleReject = async (friendId) => {
    await api.post(`/friends/${friendId}/reject`)
    api.get('/friends/requests').then(r => setRequests(r.data.data || []))
  }
  const handleUnfriend = async (friendId) => {
    await api.delete(`/friends/${friendId}`)
    api.get('/friends').then(r => setFriends(r.data.data || []))
  }

  const profileHandle = e => setProfileForm(f => ({ ...f, [e.target.name]: e.target.value }))
  const pwHandle = e => setPwForm(f => ({ ...f, [e.target.name]: e.target.value }))

  return (
    <div style={{ maxWidth:720, margin:'0 auto' }}>
      <h2>마이페이지</h2>
      <div style={{ display:'flex', gap:8, marginBottom:24, borderBottom:'1px solid var(--color-border)' }}>
        {TABS.map(t => (
          <button key={t} onClick={() => setTab(t)}
            style={{ padding:'8px 16px', border:'none', background:'none', cursor:'pointer', borderBottom: tab===t ? '2px solid var(--color-primary)' : '2px solid transparent', color: tab===t ? 'var(--color-primary)' : 'inherit', fontWeight: tab===t ? 600 : 400 }}>
            {t}
          </button>
        ))}
      </div>

      {tab === '프로필' && (
        <form onSubmit={handleProfile} className="card" style={{ padding:28, display:'flex', flexDirection:'column', gap:14 }}>
          <div><label>닉네임</label><input className="form-control" name="nickname" value={profileForm.nickname} onChange={profileHandle} required /></div>
          <div>
            <label>캠퍼스</label>
            <select className="form-control" name="campus" value={profileForm.campus} onChange={profileHandle}>
              {CAMPUSES.map(c => <option key={c}>{c}</option>)}
            </select>
          </div>
          <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:12 }}>
            <div><label>기수</label><input type="number" className="form-control" name="cohort" value={profileForm.cohort} onChange={profileHandle} /></div>
            <div><label>반</label><input type="number" className="form-control" name="classCode" value={profileForm.classCode} onChange={profileHandle} /></div>
          </div>
          <div><label>자기소개</label><textarea className="form-control" name="bio" value={profileForm.bio} onChange={profileHandle} rows={3} /></div>
          {profileMsg && <div style={{ color: profileMsg.includes('저장') ? 'green' : '#e74c3c', fontSize:14 }}>{profileMsg}</div>}
          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
            <button type="submit" className="btn btn-primary">저장</button>
            <button type="button" onClick={handleWithdraw} style={{ background:'none', border:'none', color:'#e74c3c', cursor:'pointer', fontSize:13 }}>회원 탈퇴</button>
          </div>
        </form>
      )}

      {tab === '비밀번호' && (
        <form onSubmit={handlePw} className="card" style={{ padding:28, display:'flex', flexDirection:'column', gap:14 }}>
          <div><label>현재 비밀번호</label><input type="password" className="form-control" name="currentPassword" value={pwForm.currentPassword} onChange={pwHandle} required /></div>
          <div><label>새 비밀번호</label><input type="password" className="form-control" name="newPassword" value={pwForm.newPassword} onChange={pwHandle} required /></div>
          <div><label>새 비밀번호 확인</label><input type="password" className="form-control" name="confirmPassword" value={pwForm.confirmPassword} onChange={pwHandle} required /></div>
          {pwMsg && <div style={{ color: pwMsg.includes('변경') ? 'green' : '#e74c3c', fontSize:14 }}>{pwMsg}</div>}
          <button type="submit" className="btn btn-primary">변경</button>
        </form>
      )}

      {tab === '내 게시글' && (
        <div>
          {myPosts.map(p => (
            <div key={p.id} className="card" style={{ padding:'12px 16px', marginBottom:8 }}>
              <Link to={`/posts/${p.id}`} style={{ color:'var(--color-text)', fontWeight:500 }}>{p.title}</Link>
              <span style={{ float:'right', fontSize:12, color:'var(--color-text-muted)' }}>{p.createdAt?.slice(0,10)}</span>
            </div>
          ))}
          {myPosts.length === 0 && <p style={{ textAlign:'center', color:'var(--color-text-muted)', padding:32 }}>게시글이 없습니다.</p>}
        </div>
      )}

      {tab === '즐겨찾기' && (
        <div>
          {bookmarks.map(b => (
            <div key={b.id} className="card" style={{ padding:'12px 16px', marginBottom:8 }}>
              <Link to={`/posts/${b.postId}`} style={{ color:'var(--color-text)', fontWeight:500 }}>{b.postTitle}</Link>
            </div>
          ))}
          {bookmarks.length === 0 && <p style={{ textAlign:'center', color:'var(--color-text-muted)', padding:32 }}>즐겨찾기가 없습니다.</p>}
        </div>
      )}

      {tab === '친구' && (
        <div>
          {requests.length > 0 && (
            <div style={{ marginBottom:20 }}>
              <h4>받은 친구 요청</h4>
              {requests.map(r => (
                <div key={r.id} className="card" style={{ padding:'10px 16px', marginBottom:6, display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                  <Link to={`/members/${r.senderId}`} style={{ color:'var(--color-text)' }}>{r.senderNickname}</Link>
                  <div style={{ display:'flex', gap:8 }}>
                    <button onClick={() => handleAccept(r.senderId)} className="btn btn-primary" style={{ fontSize:12, padding:'4px 10px' }}>수락</button>
                    <button onClick={() => handleReject(r.senderId)} className="btn btn-secondary" style={{ fontSize:12, padding:'4px 10px' }}>거절</button>
                  </div>
                </div>
              ))}
            </div>
          )}
          <h4>친구 목록</h4>
          {friends.map(f => (
            <div key={f.id} className="card" style={{ padding:'10px 16px', marginBottom:6, display:'flex', justifyContent:'space-between', alignItems:'center' }}>
              <Link to={`/members/${f.id}`} style={{ color:'var(--color-text)' }}>{f.nickname} <span style={{ fontSize:12, color:'var(--color-text-muted)' }}>({f.campus} {f.cohort}기)</span></Link>
              <button onClick={() => handleUnfriend(f.id)} className="btn btn-secondary" style={{ fontSize:12, padding:'4px 10px' }}>친구 해제</button>
            </div>
          ))}
          {friends.length === 0 && <p style={{ textAlign:'center', color:'var(--color-text-muted)', padding:24 }}>친구가 없습니다.</p>}
        </div>
      )}
    </div>
  )
}
