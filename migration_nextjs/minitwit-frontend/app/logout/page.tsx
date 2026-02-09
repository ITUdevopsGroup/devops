'use client'

import {useRouter,useParams } from 'next/navigation'

function route(router:any,path:string) {
  router.push(path)
}

export default function Logout() {
  
  const router = useRouter()
  const params = useParams<{ user: string}>()
  console.log("user: " + params.user)


 return (<div>Hello from Logout</div>)
}
