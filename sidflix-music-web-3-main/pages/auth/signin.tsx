import { useSession, signIn } from 'next-auth/react'
import { useEffect } from 'react'
import { useRouter } from 'next/router'

import Hero from '../../components/Hero'
export default function Signin() {
  const router = useRouter()
  const { status } = useSession()
  useEffect(() => {
    if (status === 'authenticated') {
      router.push('/')
    }
  }, [router, status])
return <Hero/>
}