"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

function route(router, path) {
  router.push(path);
}

const host = process.env.NEXT_PUBLIC_API_HOST;
const port = process.env.NEXT_PUBLIC_API_PORT;

export default function Login() {
  const [userName, setUserName] = useState("");
  const [pwd, setPwd] = useState("");
  const [shoudldFetch, setShouldFetch] = useState(false);
  const [dataAPI, setDataAPI] = useState();
  const [errorText, setErrorText] = useState("");

  const router = useRouter();

  // Only fetch when explicitly requested
  useEffect(() => {
    if (shoudldFetch) {
      fetchReq(userName, pwd);
    }
  }, [shoudldFetch]);

  // Handle API result using dataAPI directly (no stale intermediate state)
  useEffect(() => {
    if (!dataAPI) return;

    const remoteUsername = dataAPI?.username;
    const remoteUserId = dataAPI?.userId;
    const pwOK = dataAPI?.pwOK;

    if (
      remoteUsername != null &&
      remoteUsername !== "" &&
      pwOK === true &&
      userName === remoteUsername
    ) {
      setErrorText("");
      route(
        router,
        "/timeline?user=" + remoteUserId + "&username=" + remoteUsername,
      );
      return;
    }

    // only show error if a request actually came back with a failed auth
    setErrorText("Wrong user name or password");
  }, [dataAPI, userName, router]);

  async function fetchReq(user, password) {
    try {
      let api = await fetch(
        host +
          ":" +
          port +
          "/spec_user?user=" +
          encodeURIComponent(user) +
          "&password=" +
          encodeURIComponent(password),
      );
      let apijson = await api.json();
      setDataAPI(apijson.userData);
    } catch (e) {
      setErrorText("Login request failed");
    } finally {
      setShouldFetch(false);
    }
  }

  async function onSubmit(event) {
    event.preventDefault();
    setErrorText("");

    const formData = new FormData(event.currentTarget);
    const username = formData.get("username")?.toString().trim() || "";
    const password = formData.get("password")?.toString() || "";

    // Frontend validation (prevents empty submits)
    if (!username) {
      setErrorText("Please enter your username");
      return;
    }

    if (!password) {
      setErrorText("Please enter your password");
      return;
    }

    setUserName(username);
    setPwd(password);
    setShouldFetch(true);
  }

  return (
    <div className="w-full justify-center items-center  h-screen flex p-4 sm:p-10 ">
      <div className="lg:w-1/2 w-full bg-white py-6 px-4 lg:px-10 rounded-3xl">
        <div className="justify-center">
          <h1 className="rounded-sm text-4xl font-semibold mt-4 text-[#377c72]">
            MiniTwit
          </h1>

          <div className="mt-2 text-lg">
            <p className="flex">
              <strong className="mr-4 cursor-pointer">
                <a title="" onClick={() => route(router, "/timeline")}>
                  <div className="text-[#377c72] hover:text-[#4dac9e]">
                    Public timeline
                  </div>
                </a>
              </strong>

              <strong className="cursor-pointer">
                <a title="" onClick={() => route(router, "/register")}>
                  <div className="text-[#377c72] hover:text-[#4dac9e]">
                    Sign up
                  </div>
                </a>
              </strong>
            </p>
          </div>

          <div className="justify-center flex">
            <div className="mt-4 w-96">
              <h2 className="text-lg mb-4 mt-4">Sign in</h2>

              {errorText ? (
                <div className="mb-3 rounded-lg bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-700">
                  <strong>{errorText}</strong>
                </div>
              ) : (
                ""
              )}

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
                </dl>

                <input
                  className="cursor-pointer w-full h-10 !rounded-lg !border-0 bg-blue-600 px-4 py-2 text-white font-medium hover:!bg-[#377c72] active:scale-[0.99]"
                  type="submit"
                  value="Sign In"
                />
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
