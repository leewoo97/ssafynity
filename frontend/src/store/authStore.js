import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export const useAuthStore = create(
  persist(
    (set) => ({
      token: null,
      member: null,

      login: (token, member) => set({ token, member }),
      logout: () => set({ token: null, member: null }),
      updateMember: (member) => set({ member }),
    }),
    {
      name: 'auth-storage',
    }
  )
)
