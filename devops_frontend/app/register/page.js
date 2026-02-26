"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState, useEffect } from "react";

function route(router, path) {
  router.push(path);
}

const host = process.env.NEXT_PUBLIC_API_HOST;
const port = process.env.NEXT_PUBLIC_API_PORT;

var userForm = null;
var emailForm = null;
var passwordForm = null;
var password2Form = null;

export default function Register() {
  const [errorText, setErrorText] = useState("");
  const [error, setError] = useState(false);
  const [shoudldFetch, setShouldFetch] = useState(false);
  const [dataAPI, setDataAPI] = useState();

  async function onSubmit(event) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    userForm = formData.get("username");
    emailForm = formData.get("email");
    passwordForm = formData.get("password");
    password2Form = formData.get("password2");
    registerUser(userForm, emailForm, passwordForm, password2Form);
  }

  const router = useRouter();
  const params = useSearchParams();
  const userId = params.get("user");
  const usernameSession = params.get("username");
  let g = { user: userId, username: usernameSession };
  let loggedIn = g.user !== null && g.user !== "" && g.user !== "null";
  const [registerText, setRegisterText] = useState("");

  useEffect(() => {
    if (dataAPI?.result) {
      alert("Great! You can now sign in.");
    } else if (dataAPI?.error) {
      setErrorText(dataAPI?.status);
      setError(true);
    }
  }, [dataAPI]);

  useEffect(() => {
    if (shoudldFetch) {
      registerReqest(userForm, emailForm, passwordForm);
      setShouldFetch(false);
    }
  }, [shoudldFetch]);

  async function registerReqest(username, email, password) {
    let api = await fetch(
      host +
        ":" +
        port +
        "/register?user=" +
        username +
        "&email=" +
        email +
        "&password=" +
        password,
    );
    let apijson = await api.json();
    setDataAPI(apijson.userData);
    setShouldFetch(false);
  }

  function registerUser(username, email, password, password2) {
    if (userId !== null && userId !== "" && userId !== "null") {
      route(
        router,
        "/timeline?user=" +
          userId +
          "&username=" +
          usernameSession +
          "&refetch=true",
      );
      return;
    }

    setErrorText("");
    setError(false);

    if (username == undefined || username == null || username == "") {
      setErrorText("You have to enter a username");
      setError(true);
    } else if (
      email == undefined ||
      email == null ||
      email == "" ||
      !email.includes("@")
    ) {
      setErrorText("You have to enter a valid email address");
      setError(true);
    } else if (password == undefined || password == null || password == "") {
      setErrorText("You have to enter a password");
      setError(true);
    } else if (password != password2) {
      setErrorText("The two passwords do not match");
      setError(true);
    }

    if (!error) setShouldFetch(true);
  }

  return (
    <div className="w-full justify-center items-center  h-screen flex p-4 sm:p-10 ">
      <div className="lg:w-1/2 w-full bg-white py-6 px-4 lg:px-10 rounded-3xl">
        <div className="justify-center  ">
          <h1 className=" rounded-sm text-4xl font-semibold  mt-4 text-[#377c72]">
            MiniTwit
          </h1>
          <div className="mt-2 text-lg  ">
            {loggedIn ? (
              <p className="flex ">
                <strong className="mr-4 cursor-pointer">
                  <a
                    title=""
                    onClick={() =>
                      route(
                        router,
                        "/timeline?user=" + g.user + "&username=" + g.username,
                      )
                    }
                  >
                    <div className="text-[#377c72] hover:text-[#4dac9e]">
                      My timeline
                    </div>
                  </a>
                </strong>{" "}
                <br />
                <strong className="mr-4 cursor-pointer">
                  <a title="" onClick={() => route(router, "/timeline")}>
                    <div className="text-[#377c72] hover:text-[#4dac9e]">
                      Public timeline
                    </div>{" "}
                  </a>
                </strong>
                <br />
                <strong className="cursor-pointer">
                  <a title="" onClick={() => route(router, "/timeline")}>
                    <div className="text-[#377c72] hover:text-[#4dac9e]">
                      Sign out
                    </div>
                  </a>
                </strong>
              </p>
            ) : (
              <p className="flex ">
                <strong className="mr-4 cursor-pointer">
                  <a title="" onClick={() => route(router, "/timeline")}>
                    <div className="text-[#377c72] hover:text-[#4dac9e]">
                      Public timeline
                    </div>{" "}
                  </a>
                </strong>
                <br />
                <strong className="cursor-pointer">
                  <a title="" onClick={() => route(router, "/login")}>
                    <div className="text-[#377c72] hover:text-[#4dac9e]">
                      Sign in
                    </div>
                  </a>
                </strong>{" "}
                <br />
              </p>
            )}
          </div>
          <div className=" justify-center flex">
            <div className="mt-4  w-96">
              <div>{registerText}</div>
              {registerText == "" ? (
                <h2 className="text-lg mb-4 mt-4">
                  Please fill out form to sign up
                </h2>
              ) : (
                <div></div>
              )}

              {error ? (
                <div className="error">
                  <strong>{errorText}</strong>
                </div>
              ) : (
                ""
              )}

              {registerText == "" ? (
                <form onSubmit={onSubmit} className="space-y-4">
                  <dl className="space-y-3">
                    <div>
                      <dt className="mb-1 text-xs font-medium">Username:</dt>
                      <dd>
                        <input
                          className="w-full h-10 rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:border-[#0e5851] focus:ring-2 focus:ring-blue-200"
                          type="text"
                          name="username"
                          size={30}
                        />
                      </dd>
                    </div>

                    <div>
                      <dt className="mb-1 text-xs font-medium">E-Mail:</dt>
                      <dd>
                        <input
                          className="w-full h-10 rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:border-[#0e5851] focus:ring-2 focus:ring-blue-200"
                          type="text"
                          name="email"
                          size={30}
                        />
                      </dd>
                    </div>

                    <div>
                      <dt className="mb-1 text-xs font-medium">Password:</dt>
                      <dd>
                        <input
                          className="w-full h-10 rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:border-[#0e5851] focus:ring-2 focus:ring-blue-200"
                          type="password"
                          name="password"
                          size={30}
                        />
                      </dd>
                    </div>

                    <div>
                      <dt className="mb-1 text-xs font-medium">
                        Password <small>(repeat)</small>:
                      </dt>
                      <dd>
                        <input
                          className="w-full h-10 rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:border-[#0e5851] focus:ring-2 focus:ring-blue-200"
                          type="password"
                          name="password2"
                          size={30}
                        />
                      </dd>
                    </div>
                  </dl>

                  <input
                    className="cursor-pointer w-full h-10 !rounded-lg bg-blue-600 px-4 py-2 text-white font-medium hover:!bg-[#377c72] active:scale-[0.99]"
                    type="submit"
                    value="Sign Up"
                  />
                </form>
              ) : (
                <div></div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
