'use client'

import {useRouter,useParams } from 'next/navigation'
import { FormEvent} from 'react'
import { useEffect } from "react";
import { useState } from 'react';

function route(router:any,path:string) {
  router.push(path)
}

// var host = process.env.host
var host: "http://localhost"
var port = process.env.port

export default function Login() {
  const [usernameRemote, setUserNameRemote] = useState(undefined);
  const [userIdRemote, setUserIdRemote] = useState(undefined);
  const [userName, setUserName] = useState(undefined);
  const [pwd, setPwd] = useState(undefined);
  const [pwdOk, setPwdOk] = useState(false);
  const [shoudldFetch, setShouldFetch] = useState(false);
  const [dataAPI, setDataAPI] = useState<message>();
  const [errorText, setErrorText] = useState("");

  interface message {
  username: any;
  userId: any;
  pwOK: any;
}

 useEffect(() => {
      setUserNameRemote(dataAPI?.username)
      setUserIdRemote(dataAPI?.userId)
      setPwdOk(dataAPI?.pwOK)
      if(usernameRemote != "" && usernameRemote != null && userName == usernameRemote && pwdOk) {
        setErrorText("")
        route(router,"/timeline?user=" + userIdRemote + "&username=" + usernameRemote)
      } 
      else {
        dataAPI?.username != "" ? setErrorText("Wrong user name or password") : setErrorText("")
      }

       },[dataAPI]);

  useEffect(() => {
    
      fetchReq(userName,pwd)
       },[shoudldFetch]);
  

    async function fetchReq(user:any, password:any) {
      
      let api = await fetch(host +":" + port + "/spec_user?user=" + user + "&password=" + password)
      let apijson = await api.json()
      setDataAPI(apijson.userData)
      setShouldFetch(false)

      
      if(usernameRemote != "" && usernameRemote != null && user == usernameRemote && pwdOk) route(router,"/timeline?user=" + userIdRemote + "&username=" + usernameRemote)

    }
  
    async function onSubmit(event: FormEvent<HTMLFormElement>) {
      setErrorText("")
      event.preventDefault()
      const formData = new FormData(event.currentTarget)
      let username:any = formData.get('username')?.toString()
      let pwd:any = formData.get('password')?.toString()
      setUserName(username)
      setPwd(pwd)
      setShouldFetch(true)
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
        <div>{errorText }</div>
  </div>)
}


