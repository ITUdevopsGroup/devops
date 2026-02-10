'use client'

import {useRouter,useParams } from 'next/navigation'
import { FormEvent} from 'react'

function route(router:any,path:string) {
  router.push(path)
}

export default function Login() {
  function checkAccess(user: FormDataEntryValue | null, password: FormDataEntryValue | null) {
    return "hej"
}
    async function onSubmit(event: FormEvent<HTMLFormElement>) {
      event.preventDefault()
      const formData = new FormData(event.currentTarget)
      var username = formData.get('username')
      var password = formData.get('password')
      let user = checkAccess(username,password)
      if(username != null) route(router,"/timeline?user=" + user + "&username=" + username)
    }

  const router = useRouter()

 return (
  <div><h1>Sign in</h1>
            <form onSubmit={onSubmit}>
            <dl>
                <dt>Username:</dt>
                <dd><input type="text" name="username" size={30} ></input></dd>
                <dt>Password:</dt>
                <dd><input type="password" name="password" size={30}></input></dd>
            </dl>
            <div className="actions"><input type="submit" value= "Sign In"></input> </div>
        </form>
  </div>)
}


