'use client'

import {useRouter,useParams } from 'next/navigation'

function route(router:any,path:string) {
  router.push(path)
}

export default function Home() {
  
  const router = useRouter()
  const params = useParams<{ user: string}>()
  console.log("user: " + params.user)


 return route(router,"/timeline")
}
