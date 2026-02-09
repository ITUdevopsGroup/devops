'use client'

import {useRouter,useParams } from 'next/navigation'

function route(router:any,path:string) {
  router.push(path)
}

export default function Login() {
  
  const router = useRouter()
  const params = useParams<{ user: string}>()
  console.log("user: " + params.user)


 return (<div>Hello from Login</div>)
}
