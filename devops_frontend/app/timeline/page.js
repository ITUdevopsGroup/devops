import { Suspense } from "react";
import TimelineClient from "./TimelineClient";

export default function Page() {
  return (
    <Suspense fallback={null}>
      <TimelineClient />
    </Suspense>
  );
}
