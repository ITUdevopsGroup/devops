'use client'

import { useRouter,useSearchParams  } from 'next/navigation';
import { FormEvent,useState} from 'react'


function route(router:any,path:string) {
  router.push(path)
}

var host = process.env.host
var port = process.env.port



export default function Register() {

    const [errorText, setErrorText] = useState("");
  const [error, setError] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const formData = new FormData(event.currentTarget)
    var user = formData.get('username')
    var email = formData.get('email')
    var password = formData.get('password')
    var password2 = formData.get('password2')
    registerUser(user,email,password,password2)
  }

  const router = useRouter()
  const params = useSearchParams()
  const userId =  params.get("user")
  const usernameSession =  params.get("username")
  let g = {user:userId,username:usernameSession}
  let loggedIn = g.user !="null" ? true : false
  const [registerText, setRegisterText] = useState("");

  async function registerReqest(username:string,email:any,password:string){
    let api = await fetch(host +":" + port + "/register?user=" + username + "&email=" + email + "&password=" + password)
  }

  function registerUser(username:any,email:any,password:any,password2:any) {
    console.log("userdid " + userId + " username "+ username)
    if(userId != "null") route(router,"/timeline?user=" + userId + "&username="+ usernameSession + "&refetch=true")
    setError(true)
        if(username == undefined ||username == null || username == "") {
            setErrorText('You have to enter a username')
            setError(true)
        }
        else if(email == undefined ||email == null || email == "" || !email.includes('@')) {
            setErrorText('You have to enter a valid email address')
            setError(true)
        }
        else if(password == undefined ||password == null || password == "") {
            setErrorText('You have to enter a password')
            setError(true)
        }
            
        else if(password != password2) {
            setErrorText('The two passwords do not match')
            setError(true)
        }
            
        else if(get_user_id(username) != null) {
            setErrorText('The username is already taken')
            setError(true)
        }
        else {
            setErrorText("")
            registerReqest(username,email,password)
            alert("Great! You can now sign in.")
        }
}

function get_user_id(username:string) {
    return null
}

 return (
    <div>
        
      <h1>MiniTwit</h1> 
      <div className="navigation">
        {loggedIn ? (
          <p> 
            <strong><a title="" onClick={() => route(router,"/timeline?user=" + g.user + "&username="+g.username)}>my timeline</a></strong> <br />
            <strong><a title="" onClick={() => route(router,"/timeline")}>public timeline</a></strong><br />
            <strong><a title="" onClick={() => route(router,"/timeline")}>sign out</a></strong>
          </p>
        ) : (
          <p>
            <strong><a title="" onClick={() => route(router,"/timeline")}>public timeline</a></strong><br />
            <strong><a title="" onClick={() => route(router,"/login")}>sign in</a></strong> <br />
          </p>
        )}
      </div>
      <div>{registerText}</div>
        {registerText == "" ? (<h2>Please fill out form to sign up</h2>) : <div></div>}
        
        {error ? (
            <div className="error">
                <strong>
                    {errorText}
                </strong> 
            </div>) : ""
        }
        {registerText == "" ? (
            <form onSubmit={onSubmit}>
            <dl>
                <dt>Username:</dt>
                <dd><input type="text" name="username" size={30} ></input></dd>
                <dt>E-Mail:</dt>
                <dd><input type="text" name="email" size={30} ></input></dd>
                <dt>Password:</dt>
                <dd><input type="password" name="password" size={30}></input></dd>
                <dt>Password <small>(repeat)</small>:</dt>
                <dd><input type="password" name="password2" size={30}></input></dd>
            </dl>
            <div className="actions"><input type="submit" value= {registerText == "" ? "Sign Up" : ""}></input> </div>
        </form>
        ) : <div></div>
        }

    </div>);
}
