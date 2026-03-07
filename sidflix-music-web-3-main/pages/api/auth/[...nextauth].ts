import NextAuth from "next-auth"
import Auth0Provider from 'next-auth/providers/auth0'
export default NextAuth({
  // Configure one or more authentication providers
  providers: [
    Auth0Provider({
      clientId: process.env.AUTH0_CLIENT_ID ,
      clientSecret: process.env.AUTH0_CLIENT_SECRET ,
      issuer: process.env.AUTH0_ISSUER,
    }),

    // ...add more providers here
  ],
  session: {
    strategy: 'jwt',
  },
  jwt: {
  },
  callbacks: {
    async jwt({ token, account, user }) {
      // Initial sign in
      if (account) {
        return {
          accessToken: account.access_token,
          accessTokenExpires: Date.now() + (account.expires_at ?? 0) * 1000,
          refreshToken: account.refresh_token,
          user,
        }
      }

      return token
    },
    async session({ session, token }) {
      session.user = token.user as
        | {
          name?: string | null | undefined
          email?: string | null | undefined
          image?: string | null | undefined
        }
        | undefined
      session.accessToken = token.accessToken
      session.error = token.error
      return session
    }
  }

})