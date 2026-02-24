"use client";

import { useRouter, useParams } from "next/navigation";

function route(router, path) {
  router.push(path);
}

export default function Home() {
  const router = useRouter();
  const params = useParams();
  console.log("user: " + params.user);

  return route(router, "/timeline");
}
