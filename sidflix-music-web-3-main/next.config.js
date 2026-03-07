/** @type {import('next').NextConfig} */
const nextConfig = {
  // reactStrictMode: true,
  swcMinify: true,
  images: {
    unoptimized:true,
    // remotePatterns: [
    //   {
    //     // protocol: "https",
    //     hostname: "**",
    //   },
    // ],
    // domains: ['i.scdn.co','i.ibb.co','liveradio.de','dpashj04akcoe.cloudfront.net'],
  },



  eslint: {
    ignoreDuringBuilds: true,
  },
  // basePath:'/spotify',
  typescript: {
    // !! WARN !!
    // Dangerously allow production builds to successfully complete even if
    // your project has type errors.
    // !! WARN !!
    ignoreBuildErrors: true,
  },
  experimental: {
    esmExternals: true,
  }
}

module.exports = nextConfig