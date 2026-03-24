import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'

// 인증 페이지
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'

// 메인 레이아웃
import Layout from './components/Layout'

// 게시판
import PostListPage from './pages/PostListPage'
import PostDetailPage from './pages/PostDetailPage'
import PostFormPage from './pages/PostFormPage'

// 이벤트
import EventListPage from './pages/EventListPage'
import EventDetailPage from './pages/EventDetailPage'
import EventFormPage from './pages/EventFormPage'

// 프로젝트
import ProjectListPage from './pages/ProjectListPage'
import ProjectDetailPage from './pages/ProjectDetailPage'
import ProjectFormPage from './pages/ProjectFormPage'

// 기술 문서
import DocListPage from './pages/DocListPage'
import DocDetailPage from './pages/DocDetailPage'
import DocFormPage from './pages/DocFormPage'

// 기술 영상
import VideoListPage from './pages/VideoListPage'
import VideoDetailPage from './pages/VideoDetailPage'
import VideoFormPage from './pages/VideoFormPage'

// 회원
import MyPage from './pages/MyPage'
import ProfilePage from './pages/ProfilePage'

// 채팅/메시지
import ChatRoomsPage from './pages/ChatRoomsPage'
import ChatRoomPage from './pages/ChatRoomPage'
import DmListPage from './pages/DmListPage'
import DmRoomPage from './pages/DmRoomPage'

// 기타
import SearchPage from './pages/SearchPage'
import NotificationsPage from './pages/NotificationsPage'
import MentorListPage from './pages/MentorListPage'
import AdminPage from './pages/AdminPage'

// 보호된 라우트
function PrivateRoute({ children }) {
  const token = useAuthStore((state) => state.token)
  return token ? children : <Navigate to="/login" replace />
}

function AdminRoute({ children }) {
  const member = useAuthStore((state) => state.member)
  if (!member) return <Navigate to="/login" replace />
  if (member.role !== 'ADMIN') return <Navigate to="/" replace />
  return children
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 인증 불필요 */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* 인증 필요 - 레이아웃 포함 */}
        <Route path="/" element={<PrivateRoute><Layout /></PrivateRoute>}>
          <Route index element={<Navigate to="/posts" replace />} />

          {/* 게시판 */}
          <Route path="posts" element={<PostListPage />} />
          <Route path="posts/new" element={<PostFormPage />} />
          <Route path="posts/:id" element={<PostDetailPage />} />
          <Route path="posts/:id/edit" element={<PostFormPage />} />

          {/* 이벤트 */}
          <Route path="events" element={<EventListPage />} />
          <Route path="events/new" element={<EventFormPage />} />
          <Route path="events/:id" element={<EventDetailPage />} />
          <Route path="events/:id/edit" element={<EventFormPage />} />

          {/* 프로젝트 */}
          <Route path="projects" element={<ProjectListPage />} />
          <Route path="projects/new" element={<ProjectFormPage />} />
          <Route path="projects/:id" element={<ProjectDetailPage />} />
          <Route path="projects/:id/edit" element={<ProjectFormPage />} />

          {/* 기술 문서 */}
          <Route path="docs" element={<DocListPage />} />
          <Route path="docs/new" element={<DocFormPage />} />
          <Route path="docs/:id" element={<DocDetailPage />} />
          <Route path="docs/:id/edit" element={<DocFormPage />} />

          {/* 기술 영상 */}
          <Route path="videos" element={<VideoListPage />} />
          <Route path="videos/new" element={<VideoFormPage />} />
          <Route path="videos/:id" element={<VideoDetailPage />} />
          <Route path="videos/:id/edit" element={<VideoFormPage />} />

          {/* 채팅 */}
          <Route path="chat" element={<ChatRoomsPage />} />
          <Route path="chat/:id" element={<ChatRoomPage />} />

          {/* DM */}
          <Route path="dm" element={<DmListPage />} />
          <Route path="dm/:id" element={<DmRoomPage />} />

          {/* 회원 */}
          <Route path="mypage" element={<MyPage />} />
          <Route path="members/:id" element={<ProfilePage />} />
          <Route path="profile/:id" element={<ProfilePage />} />

          {/* 기타 */}
          <Route path="search" element={<SearchPage />} />
          <Route path="notifications" element={<NotificationsPage />} />
          <Route path="mentors" element={<MentorListPage />} />

          {/* 관리자 */}
          <Route path="admin" element={<AdminRoute><AdminPage /></AdminRoute>} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
