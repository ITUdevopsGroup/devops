'use client'

import { useRouter,useSearchParams  } from 'next/navigation';


function route(router:any,path:string) {
  router.push(path)
}



export default function Register() {
  
  const router = useRouter()
  const params = useSearchParams()
  const userId =  params.get("user")
  const username =  params.get("username")

  function register(username:string,email:any,password:string,password2:string) {
    let error = false
    let errorText = ""
    let text = ""
    
    if(userId != undefined ||userId != null || userId != "") route(router,"/timeline?user=" + userId + "&username="+ g.username + "&refetch=true")
    error = true
        if(username != undefined ||username != null || username != "") 
            errorText = 'You have to enter a username'
        else if(email != undefined ||email != null || email != "" || email.includes('@')) errorText = 'You have to enter a valid email address'
        else if(password != undefined ||password != null || password != "") 
            errorText = 'You have to enter a password'
        else if(password != password2) 
            errorText = 'The two passwords do not match'
        else if(get_user_id(username) != null)
            errorText = 'The username is already taken'
        else {

            route(router,"/login")
        }
}

function get_user_id(username:string) {
    return ""
}


 return (<div>Hello from reigster</div>)
}
