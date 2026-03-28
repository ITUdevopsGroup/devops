const os = require("os");

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: false,
  env: {
    host: "http://167.172.111.97",
    port: "5001",
    portClient: "3000",
  },
};

module.exports = nextConfig;
