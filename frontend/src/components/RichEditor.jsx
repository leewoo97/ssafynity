import { useMemo, useRef } from 'react'
import ReactQuill from 'react-quill'
import 'react-quill/dist/quill.snow.css'
import api from '../api/axios'

export default function RichEditor({ value, onChange, placeholder = '내용을 입력하세요...', height = 360 }) {
  const quillRef = useRef(null)

  const imageHandler = () => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.click()
    input.onchange = async () => {
      const file = input.files[0]
      if (!file) return
      const formData = new FormData()
      formData.append('file', file)
      try {
        const res = await api.post('/upload/image', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        const url = res.data.data?.url
        if (url && quillRef.current) {
          const editor = quillRef.current.getEditor()
          const range = editor.getSelection(true)
          editor.insertEmbed(range.index, 'image', url)
          editor.setSelection(range.index + 1)
        }
      } catch {
        alert('이미지 업로드에 실패했습니다.')
      }
    }
  }

  const modules = useMemo(() => ({
    toolbar: {
      container: [
        [{ header: [1, 2, 3, false] }],
        ['bold', 'italic', 'underline', 'strike'],
        [{ color: [] }, { background: [] }],
        [{ list: 'ordered' }, { list: 'bullet' }],
        [{ align: [] }],
        ['link', 'image'],
        ['blockquote', 'code-block'],
        ['clean'],
      ],
      handlers: { image: imageHandler },
    },
  }), [])

  const formats = [
    'header', 'bold', 'italic', 'underline', 'strike',
    'color', 'background', 'list', 'bullet', 'align',
    'link', 'image', 'blockquote', 'code-block',
  ]

  return (
    <div style={{ marginBottom: 8 }}>
      <ReactQuill
        ref={quillRef}
        theme="snow"
        value={value}
        onChange={onChange}
        modules={modules}
        formats={formats}
        placeholder={placeholder}
        style={{ height, marginBottom: 42 }}
      />
    </div>
  )
}
