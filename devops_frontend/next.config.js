const os = require("os");

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: false,
  env: {
    host: "http://ec2-16-171-31-204.eu-north-1.compute.amazonaws.com",
    port: "5001",
    portClient: "3000",
  },
};

module.exports = nextConfig;
